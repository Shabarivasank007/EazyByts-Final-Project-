package com.example.demo.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import java.io.StringReader;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import reactor.core.publisher.Mono;

@Component
public class NewsApiClient {

    private static final Logger logger = LoggerFactory.getLogger(NewsApiClient.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${news.api.base-url:https://newsapi.org/v2}")
    private String baseUrl;

    @Value("${news.api.timeout:30000}")
    private int timeoutMs;

    public NewsApiClient() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules(); // For better date/time handling
        this.webClient = WebClient.builder()
                .defaultHeader(HttpHeaders.USER_AGENT, "NewsReadingPlatform/1.0")
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .filter(WebClientFilter.logRequest())
                .filter(WebClientFilter.logResponse())
                .build();
        
        logger.info("NewsApiClient initialized with base URL: {}", baseUrl);
    }

    /**
     * Fetch articles from News API
     */
    public List<Map<String, Object>> fetchArticles(String apiUrl, String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty() || apiKey.equals("YOUR_API_KEY_HERE")) {
            logger.error("News API key is not configured. Please set 'news.api.key' in application.properties");
            throw new IllegalStateException("News API key is not configured");
        }

        try {
            logger.debug("Fetching articles from: {}", apiUrl);
            
            String response = webClient.get()
                    .uri(apiUrl)
                    .header("X-Api-Key", apiKey)
                    .retrieve()
                    .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> {
                            logger.error("News API request failed with status: {}", 
                                clientResponse.statusCode());
                            return clientResponse.bodyToMono(String.class)
                                .defaultIfEmpty("No error details provided")
                                .flatMap(errorBody -> {
                                    logger.error("News API error response: {}", errorBody);
                                    return Mono.error(new RuntimeException("Failed to fetch news (" + 
                                        clientResponse.statusCode() + "): " + errorBody));
                                });
                        }
                    )
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(timeoutMs))
                    .block();

            if (response == null || response.trim().isEmpty()) {
                logger.warn("Received empty response from News API");
                return new ArrayList<>();
            }

            List<Map<String, Object>> articles = parseNewsApiResponse(response);
            logger.debug("Successfully fetched {} articles", articles.size());
            return articles;
            
        } catch (Exception e) {
            logger.error("Error fetching articles from News API: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch news: " + e.getMessage(), e);
        }
    }

    /**
     * Parse News API JSON response
     */
    private List<Map<String, Object>> parseNewsApiResponse(String jsonResponse) {
        List<Map<String, Object>> articles = new ArrayList<>();

        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode articlesNode = rootNode.get("articles");

            if (articlesNode != null && articlesNode.isArray()) {
                for (JsonNode articleNode : articlesNode) {
                    Map<String, Object> article = new HashMap<>();

                    article.put("title", getTextValue(articleNode, "title"));
                    article.put("description", getTextValue(articleNode, "description"));
                    article.put("content", getTextValue(articleNode, "content"));
                    article.put("url", getTextValue(articleNode, "url"));
                    article.put("urlToImage", getTextValue(articleNode, "urlToImage"));
                    article.put("publishedAt", getTextValue(articleNode, "publishedAt"));
                    article.put("author", getTextValue(articleNode, "author"));

                    // Extract source information
                    JsonNode sourceNode = articleNode.get("source");
                    if (sourceNode != null) {
                        article.put("sourceName", getTextValue(sourceNode, "name"));
                    }

                    articles.add(article);
                }
            }
        } catch (Exception e) {
            logger.error("Error parsing News API response", e);
        }

        return articles;
    }

    /**
     * Fetch articles from RSS feed
     */
    public List<Map<String, Object>> fetchRssArticles(String rssUrl) {
        List<Map<String, Object>> articles = new ArrayList<>();

        try {
            URL feedUrl = new URL(rssUrl);
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(feedUrl));

            for (SyndEntry entry : feed.getEntries()) {
                Map<String, Object> article = new HashMap<>();

                article.put("title", entry.getTitle());
                article.put("description", entry.getDescription() != null ?
                        entry.getDescription().getValue() : "");
                article.put("content", extractContentFromEntry(entry));
                article.put("url", entry.getLink());
                article.put("publishedAt", formatDate(entry.getPublishedDate()));
                article.put("author", entry.getAuthor());

                // Try to extract image from content
                article.put("urlToImage", extractImageFromEntry(entry));

                articles.add(article);
            }
        } catch (Exception e) {
            logger.error("Error fetching RSS articles from: " + rssUrl, e);
        }

        return articles;
    }

    /**
     * Scrape website for news articles
     */
    public List<Map<String, Object>> scrapeWebsite(String websiteUrl) {
        List<Map<String, Object>> articles = new ArrayList<>();

        try {
            Document doc = Jsoup.connect(websiteUrl)
                    .userAgent("NewsReadingPlatform/1.0")
                    .timeout(timeoutMs)
                    .get();

            // Generic article selectors (works for many news sites)
            Elements articleElements = doc.select("article, .article, .news-item, .story");

            if (articleElements.isEmpty()) {
                // Fallback to common patterns
                articleElements = doc.select("h1, h2, h3").parents();
            }

            for (Element element : articleElements) {
                try {
                    Map<String, Object> article = extractArticleFromElement(element, websiteUrl);
                    if (article != null && !article.isEmpty()) {
                        articles.add(article);
                    }
                } catch (Exception e) {
                    logger.debug("Error extracting article from element", e);
                }

                // Limit to prevent memory issues
                if (articles.size() >= 50) break;
            }
        } catch (Exception e) {
            logger.error("Error scraping website: " + websiteUrl, e);
        }

        return articles;
    }

    /**
     * Extract article information from HTML element
     */
    private Map<String, Object> extractArticleFromElement(Element element, String baseUrl) {
        Map<String, Object> article = new HashMap<>();

        // Extract title
        String title = extractTitle(element);
        if (title == null || title.trim().isEmpty()) return null;

        // Extract description
        String description = extractDescription(element);

        // Extract URL
        String url = extractUrl(element, baseUrl);
        if (url == null) return null;

        // Extract image
        String imageUrl = extractImage(element, baseUrl);

        article.put("title", title);
        article.put("description", description);
        article.put("content", description); // Use description as content for scraped articles
        article.put("url", url);
        article.put("urlToImage", imageUrl);
        article.put("publishedAt", LocalDateTime.now().toString());
        article.put("author", "");

        return article;
    }

    private String extractTitle(Element element) {
        // Try multiple selectors for title
        Element titleElement = element.selectFirst("h1, h2, h3, .title, .headline, [data-title]");
        return titleElement != null ? titleElement.text().trim() : null;
    }

    private String extractDescription(Element element) {
        Element descElement = element.selectFirst("p, .description, .excerpt, .summary");
        if (descElement != null) {
            String text = descElement.text().trim();
            return text.length() > 500 ? text.substring(0, 500) + "..." : text;
        }
        return "";
    }

    private String extractUrl(Element element, String baseUrl) {
        Element linkElement = element.selectFirst("a[href]");
        if (linkElement != null) {
            String href = linkElement.attr("href");
            if (href.startsWith("http")) {
                return href;
            } else if (href.startsWith("/")) {
                return baseUrl + href;
            }
        }
        return null;
    }

    private String extractImage(Element element, String baseUrl) {
        Element imgElement = element.selectFirst("img[src]");
        if (imgElement != null) {
            String src = imgElement.attr("src");
            if (src.startsWith("http")) {
                return src;
            } else if (src.startsWith("/")) {
                return baseUrl + src;
            }
        }
        return null;
    }

    /**
     * Extract content from RSS entry
     */
    private String extractContentFromEntry(SyndEntry entry) {
        if (entry.getContents() != null && !entry.getContents().isEmpty()) {
            return entry.getContents().get(0).getValue();
        } else if (entry.getDescription() != null) {
            return entry.getDescription().getValue();
        }
        return "";
    }

    /**
     * Extract image URL from RSS entry
     */
    private String extractImageFromEntry(SyndEntry entry) {
        // Check enclosures for images
        if (entry.getEnclosures() != null) {
            for (Object enclosure : entry.getEnclosures()) {
                if (enclosure.toString().contains("image")) {
                    return enclosure.toString();
                }
            }
        }

        // Try to extract from content
        String content = extractContentFromEntry(entry);
        if (content != null) {
            try {
                Document doc = Jsoup.parse(content);
                Element img = doc.selectFirst("img[src]");
                if (img != null) {
                    return img.attr("src");
                }
            } catch (Exception e) {
                logger.debug("Error extracting image from RSS content", e);
            }
        }

        return null;
    }

    /**
     * Format date for consistent output
     */
    private String formatDate(Date date) {
        if (date == null) return LocalDateTime.now().toString();

        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).toString();
    }

    /**
     * Safely extract text value from JSON node
     */
    private String getTextValue(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode != null && !fieldNode.isNull() ? fieldNode.asText() : null;
    }

    /**
     * Fetch trending topics from News API
     */
    public List<String> fetchTrendingTopics(String apiKey) {
        List<String> topics = new ArrayList<>();

        try {
            String url = baseUrl + "/top-headlines/sources";
            String response = webClient.get()
                    .uri(url)
                    .header("X-Api-Key", apiKey)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(timeoutMs))
                    .block();

            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode sourcesNode = rootNode.get("sources");

            if (sourcesNode != null && sourcesNode.isArray()) {
                for (JsonNode sourceNode : sourcesNode) {
                    String category = getTextValue(sourceNode, "category");
                    if (category != null && !topics.contains(category)) {
                        topics.add(category);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching trending topics", e);
        }

        return topics;
    }

    /**
     * Search articles by keyword
     */
    public List<Map<String, Object>> searchArticles(String query, String apiKey) {
        try {
            String url = baseUrl + "/everything?q=" + query + "&sortBy=publishedAt&pageSize=50";
            String response = webClient.get()
                    .uri(url)
                    .header("X-Api-Key", apiKey)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(timeoutMs))
                    .block();

            return parseNewsApiResponse(response);
        } catch (Exception e) {
            logger.error("Error searching articles", e);
            return new ArrayList<>();
        }
    }

    /**
     * Get articles by category
     */
    public List<Map<String, Object>> getArticlesByCategory(String category, String apiKey) {
        try {
            String url = baseUrl + "/top-headlines?category=" + category + "&pageSize=50";
            String response = webClient.get()
                    .uri(url)
                    .header("X-Api-Key", apiKey)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(timeoutMs))
                    .block();

            return parseNewsApiResponse(response);
        } catch (Exception e) {
            logger.error("Error fetching articles by category: " + category, e);
            return new ArrayList<>();
        }
    }

    /**
     * Validate URL format
     */
    public boolean isValidUrl(String url) {
        try {
            new URL(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Clean HTML content and extract plain text
     */
    public String cleanHtmlContent(String htmlContent) {
        if (htmlContent == null || htmlContent.trim().isEmpty()) {
            return "";
        }

        try {
            Document doc = Jsoup.parse(htmlContent);
            return doc.text();
        } catch (Exception e) {
            logger.debug("Error cleaning HTML content", e);
            return htmlContent;
        }
    }
}