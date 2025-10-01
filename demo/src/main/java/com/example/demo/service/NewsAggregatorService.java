package com.example.demo.service;

import com.example.demo.config.NewsApiConfig;
import com.example.demo.model.News;
import com.example.demo.model.Category;
import com.example.demo.model.NewsSource;
import com.example.demo.model.SourceType;
import com.example.demo.util.NewsApiClient;
import com.example.demo.util.WebClientFilter;
import com.example.demo.repository.NewsSourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class NewsAggregatorService {
    private static final Logger logger = LoggerFactory.getLogger(NewsAggregatorService.class);
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;

    private final NewsService newsService;
    private final CategoryService categoryService;
    private final NewsSourceRepository newsSourceRepository;
    private final NewsApiClient newsApiClient;
    private final NewsApiConfig newsApiConfig;
    private final ExecutorService executorService;
    private final Map<String, Object> apiLocks = new ConcurrentHashMap<>();

    @Value("${news.api.key:}")
    private String apiKey;

    @Value("${news.rss.enabled:true}")
    private boolean rssEnabled;

    @Autowired
    public NewsAggregatorService(NewsService newsService,
                               CategoryService categoryService,
                               NewsSourceRepository newsSourceRepository,
                               NewsApiClient newsApiClient,
                               NewsApiConfig newsApiConfig,
                               @Value("${news.aggregation.threads:5}") int threadPoolSize) {
        this.newsService = newsService;
        this.categoryService = categoryService;
        this.newsSourceRepository = newsSourceRepository;
        this.newsApiClient = newsApiClient;
        this.newsApiConfig = newsApiConfig;
        this.executorService = Executors.newFixedThreadPool(threadPoolSize, r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("news-aggregator-" + t.getId());
            return t;
        });
        
        logger.info("NewsAggregatorService initialized with thread pool size: {}", threadPoolSize);
    }

    @Scheduled(fixedRateString = "${news.aggregation.interval:3600000}")
    @Transactional
    public void scheduleNewsAggregation() {
        logger.info("Scheduled news aggregation started at {}", LocalDateTime.now());
        try {
            int newArticles = fetchLatestNews();
            logger.info("Scheduled news aggregation completed. Found {} new articles.", newArticles);
        } catch (Exception e) {
            logger.error("Error during scheduled news aggregation", e);
        }
    }

    @Transactional
    public int fetchLatestNews() {
        logger.info("Starting news aggregation process...");
        long startTime = System.currentTimeMillis();
        int totalNewArticles = 0;
        
        try {
            List<NewsSource> activeSources = newsSourceRepository.findByIsActiveTrueOrderByPriorityLevelDesc();
            logger.info("Found {} active news sources to check", activeSources.size());
            
            // Process sources in parallel using CompletableFuture
            List<CompletableFuture<Integer>> futures = activeSources.stream()
                .filter(NewsSource::shouldUpdate)
                .map(source -> CompletableFuture.supplyAsync(
                    () -> processNewsSource(source),
                    executorService
                ))
                .collect(Collectors.toList());
            
            // Wait for all futures to complete and sum results
            CompletableFuture<Void> allOf = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
            );
            
            totalNewArticles = allOf.thenApply(v ->
                futures.stream()
                    .map(CompletableFuture::join)
                    .mapToInt(Integer::intValue)
                    .sum()
            ).join();
            
            logger.info("News aggregation completed in {} ms. Total new articles: {}", 
                System.currentTimeMillis() - startTime, totalNewArticles);
                
        } catch (Exception e) {
            logger.error("Error during news aggregation", e);
            throw new RuntimeException("Failed to fetch latest news", e);
        }
        
        return totalNewArticles;
    }
    
    private int processNewsSource(NewsSource source) {
        // Use a lock per source to prevent concurrent processing of the same source
        Object lock = apiLocks.computeIfAbsent(source.getName(), k -> new Object());
        
        synchronized (lock) {
            try {
                logger.debug("Processing news source: {}", source.getName());
                int newArticles = fetchNewsFromSourceWithRetry(source, 0);
                
                if (newArticles > 0) {
                    source.setLastUpdated(LocalDateTime.now());
                    newsSourceRepository.save(source);
                    logger.info("Fetched {} new articles from {}", newArticles, source.getName());
                } else {
                    logger.debug("No new articles from {}", source.getName());
                }
                
                return newArticles;
                
            } catch (Exception e) {
                logger.error("Error processing news source: {}", source.getName(), e);
                return 0;
            } finally {
                apiLocks.remove(source.getName());
            }
        }
    }
    
    private int fetchNewsFromSourceWithRetry(NewsSource source, int attempt) {
        try {
            return fetchNewsFromSource(source);
        } catch (Exception e) {
            if (attempt < MAX_RETRIES) {
                long delay = RETRY_DELAY_MS * (long) Math.pow(2, attempt);
                logger.warn("Attempt {} failed for source {} ({}), retrying in {} ms: {}", 
                    attempt + 1, source.getName(), source.getSourceType(), delay, e.getMessage());
                
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during retry", ie);
                }
                
                return fetchNewsFromSourceWithRetry(source, attempt + 1);
            }
            throw new RuntimeException("Max retries exceeded for source: " + source.getName(), e);
        }
    }

    private int fetchNewsFromSource(NewsSource source) {
        switch (source.getSourceType()) {
            case API:
                return fetchFromApi(source);
            case RSS:
                return fetchFromRss(source);
            case WEB:
                return fetchFromWeb(source);
            default:
                logger.warn("Unknown source type for source: {}", source.getName());
                return 0;
        }
    }

    private int fetchFromApi(NewsSource source) {
        if (newsApiConfig == null) {
            logger.error("NewsApiConfig is not properly initialized");
            return 0;
        }
        
        String apiKey = newsApiConfig.getApiKey();
        if (apiKey == null || apiKey.trim().isEmpty() || apiKey.equals("YOUR_API_KEY_HERE")) {
            logger.warn("News API key is not properly configured. Please check your application.properties");
            return 0;
        }

        try {
            String apiUrl = buildApiUrl(source);
            logger.debug("Fetching news from API: {}", apiUrl.replace(apiKey, "[REDACTED]"));
            
            // Use the NewsApiClient to fetch articles
            List<Map<String, Object>> articles = newsApiClient.fetchArticles(apiUrl, apiKey);
            
            return processArticles(articles, source);
        } catch (Exception e) {
            logger.error("Error fetching from API for source: {}", source.getName(), e);
            throw new RuntimeException("API fetch failed for source: " + source.getName(), e);
        }
    }
    
    private String buildApiUrl(NewsSource source) {
        String baseUrl = newsApiConfig.getBaseUrl();
        String endpoint = source.getApiEndpoint() != null ? 
            source.getApiEndpoint() : "/everything";
            
        Map<String, String> params = new LinkedHashMap<>();
        
        // Add source-specific parameters
        if (source.getParameters() != null) {
            params.putAll(parseQueryParams(source.getParameters()));
        }
        
        // Add default parameters if not overridden
        params.putIfAbsent("pageSize", String.valueOf(newsApiConfig.getPageSize()));
        params.putIfAbsent("language", newsApiConfig.getLanguage());
        
        // For top headlines endpoint, add country if not specified
        if (endpoint.contains("top-headlines")) {
            params.putIfAbsent("country", newsApiConfig.getCountry());
        }
        
        // Build query string
        String queryString = params.entrySet().stream()
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .collect(Collectors.joining("&"));
            
        return baseUrl + endpoint + "?" + queryString;
    }
    
    private Map<String, String> parseQueryParams(String queryString) {
        if (queryString == null || queryString.trim().isEmpty()) {
            return new HashMap<>();
        }
        
        return Arrays.stream(queryString.split("&"))
            .map(param -> param.split("=", 2))
            .filter(pair -> pair.length == 2)
            .collect(Collectors.toMap(
                pair -> pair[0],
                pair -> pair[1],
                (existing, replacement) -> replacement
            ));
    }
    
    private int processArticles(List<Map<String, Object>> articles, NewsSource source) {
        if (articles == null || articles.isEmpty()) {
            return 0;
        }
        
        int newArticles = 0;
        for (Map<String, Object> article : articles) {
            try {
                if (saveArticle(article, source)) {
                    newArticles++;
                }
            } catch (Exception e) {
                logger.error("Error processing article: {}", article.get("url"), e);
            }
        }
        
        return newArticles;
    }
    
    @Transactional
    public boolean saveArticle(Map<String, Object> article, NewsSource source) {
        try {
            String sourceUrl = (String) article.get("url");
            if (sourceUrl == null || newsService.existsByUrl(sourceUrl)) {
                return false;
            }
            
            News news = new News();
            news.setTitle((String) article.get("title"));
            news.setDescription((String) article.get("description"));
            news.setContent((String) article.get("content"));
            news.setSourceUrl(sourceUrl);
            news.setImageUrl((String) article.get("urlToImage"));
            news.setSource(source);
            news.setIsActive(true);
            
            // Set published date
            Object publishedAt = article.get("publishedAt");
            if (publishedAt != null) {
                try {
                    if (publishedAt instanceof String) {
                        news.setPublishedAt(LocalDateTime.parse((String) publishedAt, 
                            DateTimeFormatter.ISO_OFFSET_DATE_TIME));
                    } else if (publishedAt instanceof LocalDateTime) {
                        news.setPublishedAt((LocalDateTime) publishedAt);
                    }
                } catch (Exception e) {
                    logger.warn("Failed to parse published date: {}", publishedAt, e);
                    news.setPublishedAt(LocalDateTime.now());
                }
            } else {
                news.setPublishedAt(LocalDateTime.now());
            }
            
            // Set category - use source's category parameter or default to "General"
            String categoryName = (String) article.get("category");
            if (categoryName == null || categoryName.trim().isEmpty()) {
                // Try to extract category from source parameters
                if (source.getParameters() != null && source.getParameters().contains("category=")) {
                    String params = source.getParameters();
                    int start = params.indexOf("category=") + 9;
                    int end = params.indexOf("&", start);
                    categoryName = end > start ? params.substring(start, end) : params.substring(start);
                } else {
                    categoryName = "General";
                }
            }
            Category category = categoryService.findOrCreateCategory(categoryName);
            news.setCategory(category);
            
            // Save the news article
            newsService.saveNews(news);
            return true;
            
        } catch (Exception e) {
            logger.error("Error saving article: {}", article.get("url"), e);
            return false;
        }
    }
    
    private LocalDateTime parsePublishedDate(Object dateObj) {
        if (dateObj == null) {
            return LocalDateTime.now();
        }
        
        try {
            if (dateObj instanceof String) {
                return LocalDateTime.parse((String) dateObj, 
                    DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            } else if (dateObj instanceof LocalDateTime) {
                return (LocalDateTime) dateObj;
            }
        } catch (Exception e) {
            logger.warn("Failed to parse date: {}", dateObj, e);
        }
        
        return LocalDateTime.now();
    }
    
    private int fetchFromRss(NewsSource source) {
        if (!rssEnabled) {
            return 0;
        }

        try {
            // TODO: Implement RSS feed fetching
            logger.warn("RSS feed fetching not implemented yet for source: {}", source.getName());
            return 0;
        } catch (Exception e) {
            logger.error("Error fetching from RSS source: {}", source.getName(), e);
            return 0;
        }
    }

    private int fetchFromWeb(NewsSource source) {
        // TODO: Implement web scraping
        logger.warn("Web scraping not implemented yet for source: {}", source.getName());
        return 0;
    }
    
    // Cleanup resources
    public void shutdown() {
        if (executorService != null) {
            try {
                executorService.shutdown();
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
