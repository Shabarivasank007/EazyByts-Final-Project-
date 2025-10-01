package com.example.demo.service;

import com.example.demo.model.News;
import com.example.demo.model.Category;
import com.example.demo.model.NewsSource;
import com.example.demo.repository.NewsRepository;
import com.example.demo.util.SlugUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class NewsService {

    private final NewsRepository newsRepository;

    @Autowired
    public NewsService(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    // Basic CRUD operations
    @Cacheable(value = "news", key = "#id")
    public News getNewsById(Long id) {
        return newsRepository.findById(id).orElse(null);
    }

    @Cacheable(value = "allNews")
    public List<News> getAllActiveNews() {
        return newsRepository.findByIsActiveTrue();
    }
    
    /**
     * Checks if a news article with the given source URL exists in the database
     * @param sourceUrl The source URL to check
     * @return true if a news article with the source URL exists, false otherwise
     */
    public boolean existsByUrl(String sourceUrl) {
        return newsRepository.existsBySourceUrl(sourceUrl);
    }

    @Cacheable(value = "latestNews", key = "#pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<News> getLatestNews(Pageable pageable) {
        return newsRepository.findByIsActiveTrueOrderByPublishedAtDesc(pageable);
    }

    public List<News> getLatestNews(int count) {
        Pageable pageable = PageRequest.of(0, count, Sort.by(Sort.Direction.DESC, "publishedAt"));
        Page<News> page = newsRepository.findByIsActiveTrueOrderByPublishedAtDesc(pageable);
        return page.getContent();
    }

    @Transactional
    @CacheEvict(value = {"news", "allNews", "latestNews", "trendingNews"}, allEntries = true)
    public News saveNews(News news) {
        if (news.getPublishedAt() == null) {
            news.setPublishedAt(LocalDateTime.now());
        }

        // Generate slug from title
        if (news.getSlug() == null || news.getSlug().isEmpty()) {
            String baseSlug = SlugUtil.toSlug(news.getTitle());
            String finalSlug = baseSlug;
            int attempt = 0;

            // Keep trying until we find a unique slug
            while (newsRepository.existsBySlug(finalSlug)) {
                attempt++;
                finalSlug = SlugUtil.toUniqueSlug(baseSlug, attempt);
            }

            news.setSlug(finalSlug);
        }

        return newsRepository.save(news);
    }

    @Transactional
    @CacheEvict(value = {"news", "allNews", "latestNews", "trendingNews"}, allEntries = true)
    public News updateNews(News news) {
        return newsRepository.save(news);
    }

    @Transactional
    @CacheEvict(value = {"news", "allNews", "latestNews", "trendingNews"}, allEntries = true)
    public void deleteNews(Long id) {
        newsRepository.softDelete(id);
    }

    // Category-based queries
    @Cacheable(value = "categoryNews", key = "#category.id + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<News> getNewsByCategory(Category category, Pageable pageable) {
        return newsRepository.findByCategoryAndIsActiveTrueOrderByPublishedAtDesc(category, pageable);
    }

    @Cacheable(value = "categoryLatestNews", key = "#category.id + '_' + #limit")
    public List<News> getLatestNewsByCategory(Category category, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return newsRepository.findByCategoryAndIsActiveTrueOrderByPublishedAtDesc(category, pageable).getContent();
    }

    // Featured news
    @Cacheable(value = "featuredNews", key = "#limit")
    public List<News> getFeaturedNews(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return newsRepository.findByIsFeaturedTrueAndIsActiveTrueOrderByPublishedAtDesc();
    }

    // Trending news
    @Cacheable(value = "trendingNews", key = "#limit")
    public List<News> getTrendingNews(int limit) {
        LocalDateTime since = LocalDateTime.now().minusWeeks(1); // Last 7 days
        Pageable pageable = PageRequest.of(0, limit);
        return newsRepository.findTrendingNews(since, pageable);
    }

    // Breaking news (last 2 hours)
    @Cacheable(value = "breakingNews", key = "#limit")
    public List<News> getBreakingNews(int limit) {
        LocalDateTime since = LocalDateTime.now().minusHours(2);
        List<News> breakingNews = newsRepository.findBreakingNews(since);
        return breakingNews.stream().limit(limit).toList();
    }

    // Recent news
    public List<News> getRecentNews(int hours, int limit) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        Pageable pageable = PageRequest.of(0, limit);
        return newsRepository.findRecentNews(since, pageable).getContent();
    }

    // Search functionality
    public Page<News> searchNews(String keyword, Long categoryId, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Page.empty(pageable);
        }

        if (categoryId != null) {
            // Create a simple category object for the query
            Category category = new Category();
            category.setId(categoryId);
            return newsRepository.searchByKeywordAndCategory(keyword.trim(), category, pageable);
        } else {
            return newsRepository.searchByKeyword(keyword.trim(), pageable);
        }
    }

    // Related news
    @Cacheable(value = "relatedNews", key = "#news.id + '_' + #limit")
    public List<News> getRelatedNews(News news, int limit) {
        if (news.getCategory() == null) {
            return List.of();
        }

        Pageable pageable = PageRequest.of(0, limit);
        return newsRepository.findRelatedNews(news.getCategory(), news.getId(), pageable);
    }

    // View count operations
    @Transactional
    public void incrementViewCount(Long newsId) {
        newsRepository.incrementViewCount(newsId);
        // Clear cache for this specific news item
        // Note: In a real application, you might want to use @CacheEvict here
    }

    @Transactional
    public void updateTrendingStatus(Long newsId, boolean trending) {
        newsRepository.updateTrendingStatus(newsId, trending);
    }

    @Transactional
    public void updateFeaturedStatus(Long newsId, boolean featured) {
        newsRepository.updateFeaturedStatus(newsId, featured);
    }

    // Popular content
    public List<News> getPopularNews(int days, int limit) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        Pageable pageable = PageRequest.of(0, limit);
        return newsRepository.findPopularNews(since, pageable);
    }

    // Top viewed news
    @Cacheable(value = "topViewedNews", key = "#limit")
    public List<News> getTopViewedNews(int limit) {
        return newsRepository.findTop10ByIsActiveTrueOrderByViewCountDesc()
                .stream()
                .limit(limit)
                .toList();
    }

    public List<News> getTopViewedNewsByCategory(Category category, int limit) {
        return newsRepository.findTop10ByCategoryAndIsActiveTrueOrderByViewCountDesc(category)
                .stream()
                .limit(limit)
                .toList();
    }

    // Author-based queries
    public Page<News> getNewsByAuthor(String author, Pageable pageable) {
        return newsRepository.findByAuthorAndIsActiveTrueOrderByPublishedAtDesc(author, pageable);
    }

    public List<News> getNewsByAuthor(String author, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return newsRepository.findByAuthorAndIsActiveTrueOrderByPublishedAtDesc(author, pageable).getContent();
    }

    // Source-based queries
    public Page<News> getNewsBySource(NewsSource source, Pageable pageable) {
        return newsRepository.findBySourceAndIsActiveTrueOrderByPublishedAtDesc(source, pageable);
    }

    public List<News> getNewsBySource(NewsSource source, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return newsRepository.findBySourceAndIsActiveTrueOrderByPublishedAtDesc(source, pageable).getContent();
    }

    // Statistics
    public long getTotalActiveNewsCount() {
        return newsRepository.countActiveNews();
    }

    public long getNewsCountByCategory(Category category) {
        return newsRepository.countActiveNewsByCategory(category);
    }

    public long getRecentNewsCount(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return newsRepository.countRecentNews(since);
    }

    public Long getTotalViewCount() {
        return newsRepository.getTotalViewCount();
    }

    public Long getViewCountByCategory(Category category) {
        return newsRepository.getTotalViewCountByCategory(category);
    }

    // Content validation
    public boolean isDuplicateContent(String sourceUrl) {
        return newsRepository.existsBySourceUrlAndIsActiveTrue(sourceUrl);
    }

    public boolean isDuplicateContent(String title, NewsSource source) {
        return newsRepository.existsByTitleAndSourceAndIsActiveTrue(title, source);
    }

    public Optional<News> findBySourceUrl(String sourceUrl) {
        return newsRepository.findBySourceUrlAndIsActiveTrue(sourceUrl);
    }

    // Batch operations
    @Transactional
    @CacheEvict(value = {"allNews", "latestNews", "trendingNews"}, allEntries = true)
    public void archiveOldNews(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        newsRepository.archiveOldNews(cutoffDate);
    }

    @Transactional
    public void clearOldTrendingStatus(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        newsRepository.clearOldTrendingStatus(cutoffDate);
    }

    @Transactional
    @CacheEvict(value = {"allNews", "latestNews", "categoryNews"}, allEntries = true)
    public void migrateCategoryNews(Category oldCategory, Category newCategory) {
        newsRepository.migrateCategoryNews(oldCategory, newCategory);
    }

    // Analytics data
    public List<Object[]> getNewsByCategory(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return newsRepository.getNewsByCategory(since);
    }

    public List<Object[]> getNewsByDate(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return newsRepository.getNewsByDate(since);
    }

    public List<Object[]> getNewsByAuthor(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return newsRepository.getNewsByAuthor(since);
    }

    // RSS and Sitemap support
    public List<News> getAllNewsForSitemap() {
        return newsRepository.findAllForSitemap();
    }

    public List<News> getNewsForCategoryRSS(Category category, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return newsRepository.findForCategoryRSS(category, pageable);
    }

    // Helper methods for trending calculation
    @Transactional
    public void updateTrendingNews() {
        // Clear all trending status first
        clearOldTrendingStatus(7);

        // Get popular news from last week and mark as trending
        List<News> popularNews = getPopularNews(7, 20);

        for (News news : popularNews) {
            updateTrendingStatus(news.getId(), true);
        }
    }

    // Calculate reading time based on content length
    public int calculateReadingTime(String content) {
        if (content == null || content.isEmpty()) {
            return 1;
        }

        // Average reading speed is about 200 words per minute
        String[] words = content.split("\\s+");
        int wordCount = words.length;
        int readingTime = Math.max(1, (int) Math.ceil(wordCount / 200.0));

        return readingTime;
    }

    // Content processing
    @Transactional
    public News processAndSaveNews(News news) {
        // Calculate reading time
        if (news.getReadingTime() == null || news.getReadingTime() == 0) {
            news.setReadingTime(calculateReadingTime(news.getContent()));
        }

        // Set published date if not set
        if (news.getPublishedAt() == null) {
            news.setPublishedAt(LocalDateTime.now());
        }

        // Validate content
        if (isDuplicateContent(news.getSourceUrl())) {
            throw new IllegalArgumentException("Duplicate content detected");
        }

        return saveNews(news);
    }
}

