package com.example.demo.config;

import com.example.demo.model.Category;
import com.example.demo.model.News;
import com.example.demo.model.NewsSource;
import com.example.demo.repository.NewsSourceRepository;
import com.example.demo.service.CategoryService;
import com.example.demo.service.NewsService;
import com.example.demo.service.NewsSourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
@Profile("!test")
public class DataLoader {

    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);
    
    private final NewsSourceRepository newsSourceRepository;

    @Autowired
    public DataLoader(NewsSourceRepository newsSourceRepository) {
        this.newsSourceRepository = newsSourceRepository;
    }

    @Bean
    CommandLineRunner initDatabase(NewsService newsService, 
                                 CategoryService categoryService, 
                                 NewsSourceService newsSourceService) {
        return args -> {
            try {
                logger.info("Starting database initialization...");
                
                // Check if we already have categories
                List<Category> existingCategories = categoryService.getAllActiveCategories();
                Category technology = null;
                Category business = null;
                
                // Find or create Technology category
                technology = existingCategories.stream()
                    .filter(c -> "Technology".equalsIgnoreCase(c.getName()))
                    .findFirst()
                    .orElseGet(() -> {
                        logger.info("Creating Technology category...");
                        Category cat = new Category();
                        cat.setName("Technology");
                        cat.setSlug("technology");
                        cat.setDescription("Latest in technology and innovation");
                        return categoryService.saveCategory(cat);
                    });
                logger.info("Using category: {}", technology.getName());
                
                // Find or create Business category
                business = existingCategories.stream()
                    .filter(c -> "Business".equalsIgnoreCase(c.getName()))
                    .findFirst()
                    .orElseGet(() -> {
                        logger.info("Creating Business category...");
                        Category cat = new Category();
                        cat.setName("Business");
                        cat.setSlug("business");
                        cat.setDescription("Business and financial news");
                        return categoryService.saveCategory(cat);
                    });
                logger.info("Using category: {}", business.getName());

                // Find or create news source
                logger.info("Setting up news sources...");
                NewsSource techSource = null;
                
                // Try to find existing source
                List<NewsSource> existingSources = newsSourceRepository.findAll();
                if (existingSources != null && !existingSources.isEmpty()) {
                    techSource = existingSources.stream()
                        .filter(s -> "Tech News".equalsIgnoreCase(s.getName()))
                        .findFirst()
                        .orElse(null);
                }
                
                // If not found, create a new one
                if (techSource == null) {
                    logger.info("Creating Tech News source...");
                    techSource = new NewsSource();
                    techSource.setName("Tech News");
                    techSource.setBaseUrl("https://tech-news.example.com");
                    // Configure as external API source so aggregator can fetch from NewsAPI
                    techSource.setIsActive(true);
                    techSource.setSourceType(NewsSource.SourceType.API);
                    // Use top-headlines endpoint with technology category by default
                    techSource.setApiUrl("/top-headlines");
                    techSource.setParameters("category=technology");
                    techSource = newsSourceRepository.save(techSource);
                    logger.info("Created news source: {}", techSource.getName());
                } else {
                    logger.info("Using existing news source: {}", techSource.getName());
                }

                // Create sample news articles
                if (newsService.getAllActiveNews().isEmpty()) {
                    logger.info("Creating sample news articles...");
                    
                    // Technology news
                    News techNews = new News();
                    techNews.setTitle("Breakthrough in Quantum Computing Achieved");
                    techNews.setDescription("Scientists have made a major breakthrough in quantum computing, achieving new levels of qubit stability.");
                    techNews.setContent("In a groundbreaking development, researchers have successfully maintained quantum coherence for over an hour, marking a significant milestone in quantum computing...");
                    techNews.setCategory(technology);
                    techNews.setSource(techSource);
                    techNews.setAuthor("Dr. Sarah Chen");
                    techNews.setPublishedAt(LocalDateTime.now().minusHours(2));
                    techNews.setImageUrl("/images/placeholder/tech-news.jpg");
                    techNews.setIsFeatured(true);
                    newsService.saveNews(techNews);

                    // Business news
                    News businessNews = new News();
                    businessNews.setTitle("Global Markets See Record Growth");
                    businessNews.setDescription("Stock markets worldwide hit new highs as tech sector leads unprecedented growth.");
                    businessNews.setContent("Global financial markets have reached historic highs today, with technology companies leading the charge...");
                    businessNews.setCategory(business);
                    businessNews.setSource(techSource);
                    businessNews.setAuthor("Michael Thompson");
                    businessNews.setPublishedAt(LocalDateTime.now().minusHours(1));
                    businessNews.setImageUrl("/images/placeholder/business-news.jpg");
                    businessNews.setIsTrending(true);
                    newsService.saveNews(businessNews);

                    logger.info("Sample news articles created successfully.");
                }

                logger.info("Database initialization completed successfully!");
            } catch (Exception e) {
                logger.error("Error initializing database: {}", e.getMessage(), e);
                throw e;
            }
        };
    }
    
    private News createNews(String title, String description, String content, 
                           String imageUrl, Category category, NewsSource source,
                           LocalDateTime publishedAt, boolean isFeatured, boolean isTrending) {
        News news = new News(title, description, content, category);
        news.setImageUrl(imageUrl);
        news.setSource(source);
        news.setPublishedAt(publishedAt);
        news.setIsFeatured(isFeatured);
        news.setIsTrending(isTrending);
        news.setViewCount(0L);
        return news;
    }
}
