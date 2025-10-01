package com.example.demo.repository;

import com.example.demo.model.News;
import com.example.demo.model.Category;
import com.example.demo.model.NewsSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NewsRepository extends JpaRepository<News, Long>, JpaSpecificationExecutor<News> {

    // Basic finders
    List<News> findByIsActiveTrue();
    
    /**
     * Checks if a news article with the given source URL exists in the database
     * @param sourceUrl The source URL to check
     * @return true if a news article with the source URL exists, false otherwise
     */
    boolean existsBySourceUrl(String sourceUrl);
    Page<News> findByIsActiveTrueOrderByPublishedAtDesc(Pageable pageable);
    Page<News> findByIsActiveTrueOrderByViewCountDesc(Pageable pageable);

    // Category-based queries
    Page<News> findByCategoryAndIsActiveTrueOrderByPublishedAtDesc(Category category, Pageable pageable);
    List<News> findByCategoryAndIsActiveTrueOrderByPublishedAtDesc(Category category);
    List<News> findTop10ByCategoryAndIsActiveTrueOrderByPublishedAtDesc(Category category);

    // Featured and trending news
    List<News> findByIsFeaturedTrueAndIsActiveTrueOrderByPublishedAtDesc();
    List<News> findByIsTrendingTrueAndIsActiveTrueOrderByViewCountDesc();
    List<News> findTop5ByIsFeaturedTrueAndIsActiveTrueOrderByPublishedAtDesc();
    List<News> findTop10ByIsTrendingTrueAndIsActiveTrueOrderByViewCountDesc();

    // Source-based queries
    Page<News> findBySourceAndIsActiveTrueOrderByPublishedAtDesc(NewsSource source, Pageable pageable);
    List<News> findBySourceAndIsActiveTrueOrderByPublishedAtDesc(NewsSource source);

    // Time-based queries
    @Query("SELECT n FROM News n WHERE n.isActive = true AND n.publishedAt >= :since ORDER BY n.publishedAt DESC")
    List<News> findRecentNews(@Param("since") LocalDateTime since);

    @Query("SELECT n FROM News n WHERE n.isActive = true AND n.publishedAt >= :since ORDER BY n.publishedAt DESC")
    Page<News> findRecentNews(@Param("since") LocalDateTime since, Pageable pageable);

    // Breaking news (last 2 hours)
    @Query("SELECT n FROM News n WHERE n.isActive = true AND n.publishedAt >= :since ORDER BY n.publishedAt DESC")
    List<News> findBreakingNews(@Param("since") LocalDateTime since);

    // Trending news with time filter and view count
    @Query("SELECT n FROM News n WHERE n.isActive = true AND n.publishedAt >= :since ORDER BY n.viewCount DESC, n.publishedAt DESC")
    List<News> findTrendingNews(@Param("since") LocalDateTime since, Pageable pageable);

    // Search functionality
    @Query("SELECT n FROM News n WHERE n.isActive = true AND " +
            "(LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(n.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(n.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY n.publishedAt DESC")
    Page<News> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT n FROM News n WHERE n.isActive = true AND n.category = :category AND " +
            "(LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(n.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(n.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY n.publishedAt DESC")
    Page<News> searchByKeywordAndCategory(@Param("keyword") String keyword,
                                          @Param("category") Category category,
                                          Pageable pageable);

    // Related news (same category, excluding current news)
    @Query("SELECT n FROM News n WHERE n.isActive = true AND n.category = :category AND n.id != :excludeId " +
            "ORDER BY n.publishedAt DESC")
    List<News> findRelatedNews(@Param("category") Category category, @Param("excludeId") Long excludeId, Pageable pageable);

    // Statistics queries
    @Query("SELECT COUNT(n) FROM News n WHERE n.isActive = true")
    long countActiveNews();

    @Query("SELECT COUNT(n) FROM News n WHERE n.isActive = true AND n.category = :category")
    long countActiveNewsByCategory(@Param("category") Category category);

    @Query("SELECT COUNT(n) FROM News n WHERE n.isActive = true AND n.publishedAt >= :since")
    long countRecentNews(@Param("since") LocalDateTime since);

    @Query("SELECT SUM(n.viewCount) FROM News n WHERE n.isActive = true")
    Long getTotalViewCount();

    @Query("SELECT SUM(n.viewCount) FROM News n WHERE n.isActive = true AND n.category = :category")
    Long getTotalViewCountByCategory(@Param("category") Category category);

    // Top viewed news
    List<News> findTop10ByIsActiveTrueOrderByViewCountDesc();
    List<News> findTop10ByCategoryAndIsActiveTrueOrderByViewCountDesc(Category category);

    // Author-based queries
    Page<News> findByAuthorAndIsActiveTrueOrderByPublishedAtDesc(String author, Pageable pageable);
    List<News> findByAuthorAndIsActiveTrueOrderByPublishedAtDesc(String author);

    // Check for duplicate content
    Optional<News> findBySourceUrlAndIsActiveTrue(String sourceUrl);
    boolean existsBySourceUrlAndIsActiveTrue(String sourceUrl);
    boolean existsByTitleAndSourceAndIsActiveTrue(String title, NewsSource source);

    // Update operations
    @Modifying
    @Transactional
    @Query("UPDATE News n SET n.viewCount = n.viewCount + 1 WHERE n.id = :id")
    void incrementViewCount(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE News n SET n.isTrending = :trending WHERE n.id = :id")
    void updateTrendingStatus(@Param("id") Long id, @Param("trending") boolean trending);

    @Modifying
    @Transactional
    @Query("UPDATE News n SET n.isFeatured = :featured WHERE n.id = :id")
    void updateFeaturedStatus(@Param("id") Long id, @Param("featured") boolean featured);

    @Modifying
    @Transactional
    @Query("UPDATE News n SET n.isActive = false WHERE n.id = :id")
    void softDelete(@Param("id") Long id);

    // Batch operations
    @Modifying
    @Transactional
    @Query("UPDATE News n SET n.isActive = false WHERE n.publishedAt < :cutoffDate")
    void archiveOldNews(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Modifying
    @Transactional
    @Query("UPDATE News n SET n.isTrending = false WHERE n.publishedAt < :cutoffDate")
    void clearOldTrendingStatus(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Category migration
    @Modifying
    @Transactional
    @Query("UPDATE News n SET n.category = :newCategory WHERE n.category = :oldCategory")
    void migrateCategoryNews(@Param("oldCategory") Category oldCategory, @Param("newCategory") Category newCategory);

    // Custom queries for analytics
    @Query("SELECT n.category.name, COUNT(n) FROM News n WHERE n.isActive = true AND n.publishedAt >= :since " +
            "GROUP BY n.category.name ORDER BY COUNT(n) DESC")
    List<Object[]> getNewsByCategory(@Param("since") LocalDateTime since);

    @Query("SELECT DATE(n.publishedAt), COUNT(n) FROM News n WHERE n.isActive = true AND n.publishedAt >= :since " +
            "GROUP BY DATE(n.publishedAt) ORDER BY DATE(n.publishedAt)")
    List<Object[]> getNewsByDate(@Param("since") LocalDateTime since);

    @Query("SELECT n.author, COUNT(n) FROM News n WHERE n.isActive = true AND n.author IS NOT NULL AND n.publishedAt >= :since " +
            "GROUP BY n.author ORDER BY COUNT(n) DESC")
    List<Object[]> getNewsByAuthor(@Param("since") LocalDateTime since);

    // Popular content queries
    @Query("SELECT n FROM News n WHERE n.isActive = true AND n.publishedAt >= :since " +
            "ORDER BY (n.viewCount * 0.7 + (CASE WHEN n.isFeatured = true THEN 100 ELSE 0 END) * 0.3) DESC")
    List<News> findPopularNews(@Param("since") LocalDateTime since, Pageable pageable);

    // RSS/Sitemap queries
    @Query("SELECT n FROM News n WHERE n.isActive = true ORDER BY n.updatedAt DESC")
    List<News> findAllForSitemap();

    @Query("SELECT n FROM News n WHERE n.isActive = true AND n.category = :category ORDER BY n.publishedAt DESC")
    List<News> findForCategoryRSS(@Param("category") Category category, Pageable pageable);

    // Slug-related queries
    boolean existsBySlug(String slug);
    Optional<News> findBySlug(String slug);
}

