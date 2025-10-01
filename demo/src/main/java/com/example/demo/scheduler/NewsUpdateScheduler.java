package com.example.demo.scheduler;

import com.example.demo.service.NewsService;
import com.example.demo.service.CategoryService;
import com.example.demo.service.NewsAggregatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NewsUpdateScheduler {

    private static final Logger logger = LoggerFactory.getLogger(NewsUpdateScheduler.class);

    private final NewsService newsService;
    private final CategoryService categoryService;
    private final NewsAggregatorService newsAggregatorService;

    @Autowired
    public NewsUpdateScheduler(NewsService newsService,
                               CategoryService categoryService,
                               NewsAggregatorService newsAggregatorService) {
        this.newsService = newsService;
        this.categoryService = categoryService;
        this.newsAggregatorService = newsAggregatorService;
    }

    // Fetch new articles every hour
    @Scheduled(fixedRate = 3600000) // 1 hour
    public void fetchLatestNews() {
        logger.info("Starting scheduled news fetch...");

        try {
            int newArticles = newsAggregatorService.fetchLatestNews();
            logger.info("Successfully fetched {} new articles", newArticles);
        } catch (Exception e) {
            logger.error("Error during scheduled news fetch", e);
        }
    }

    // Update trending news every hour
    @Scheduled(fixedRate = 3600000) // 1 hour
    public void updateTrendingNews() {
        logger.info("Updating trending news...");

        try {
            newsService.updateTrendingNews();
            logger.info("Successfully updated trending news");
        } catch (Exception e) {
            logger.error("Error updating trending news", e);
        }
    }

    // Update category news counts every 2 hours
    @Scheduled(fixedRate = 7200000) // 2 hours
    public void updateCategoryNewsCounts() {
        logger.info("Updating category news counts...");

        try {
            categoryService.updateAllCategoryNewsCounts();
            logger.info("Successfully updated category news counts");
        } catch (Exception e) {
            logger.error("Error updating category news counts", e);
        }
    }

    // Clear old trending status daily at 2 AM
    @Scheduled(cron = "0 0 2 * * *")
    public void clearOldTrendingNews() {
        logger.info("Clearing old trending news status...");

        try {
            newsService.clearOldTrendingStatus(7); // Clear items older than 7 days
            logger.info("Successfully cleared old trending news");
        } catch (Exception e) {
            logger.error("Error clearing old trending news", e);
        }
    }

    // Archive very old news monthly
    @Scheduled(cron = "0 0 3 1 * *") // First day of month at 3 AM
    public void archiveOldNews() {
        logger.info("Starting monthly news archiving...");

        try {
            newsService.archiveOldNews(365); // Archive news older than 1 year
            logger.info("Successfully archived old news");
        } catch (Exception e) {
            logger.error("Error during news archiving", e);
        }
    }

    // Cleanup empty categories weekly
    @Scheduled(cron = "0 0 4 * * SUN") // Sunday at 4 AM
    public void cleanupEmptyCategories() {
        logger.info("Starting weekly category cleanup...");

        try {
            categoryService.cleanupEmptyCategories();
            logger.info("Successfully cleaned up empty categories");
        } catch (Exception e) {
            logger.error("Error during category cleanup", e);
        }
    }
}