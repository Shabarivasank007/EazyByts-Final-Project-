package com.example.demo.repository;

import com.example.demo.model.Category;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {

    // Basic finders
    Optional<Category> findByName(String name);
    
    @Query("SELECT c FROM Category c WHERE LOWER(c.name) = LOWER(:name)")
    Optional<Category> findByNameIgnoreCase(@Param("name") String name);
    
    Optional<Category> findBySlug(String slug);
    Optional<Category> findByNameAndIsActiveTrue(String name);
    Optional<Category> findBySlugAndIsActiveTrue(String slug);

    // Active categories
    List<Category> findByIsActiveTrueOrderByDisplayOrderAscNameAsc();
    List<Category> findByIsActiveTrueOrderByNameAsc();
    List<Category> findByIsActiveTrueOrderByNewsCountDesc();

    // Featured categories
    List<Category> findByIsFeaturedTrueAndIsActiveTrueOrderByDisplayOrderAscNameAsc();
    List<Category> findByIsFeaturedTrueAndIsActiveTrueOrderByNameAsc();

    // Count operations
    long countByIsActiveTrue();
    long countByIsFeaturedTrueAndIsActiveTrue();

    // Search functionality
    @Query("SELECT c FROM Category c WHERE c.isActive = true AND " +
            "(LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY c.displayOrder ASC, c.name ASC")
    List<Category> searchByKeyword(@Param("keyword") String keyword);

    // Update operations
    @Modifying
    @Transactional
    @Query("UPDATE Category c SET c.newsCount = " +
            "(SELECT COUNT(n) FROM News n WHERE n.category.id = c.id AND n.isActive = true) " +
            "WHERE c.id = :categoryId")
    void updateNewsCount(@Param("categoryId") Long categoryId);

    @Modifying
    @Transactional
    @Query("UPDATE Category c SET c.newsCount = " +
            "(SELECT COUNT(n) FROM News n WHERE n.category.id = c.id AND n.isActive = true)")
    void updateAllNewsCounts();

    @Modifying
    @Transactional
    @Query("UPDATE Category c SET c.isFeatured = :featured WHERE c.id = :categoryId")
    void updateFeaturedStatus(@Param("categoryId") Long categoryId, @Param("featured") boolean featured);

    @Modifying
    @Transactional
    @Query("UPDATE Category c SET c.displayOrder = :displayOrder WHERE c.id = :categoryId")
    void updateDisplayOrder(@Param("categoryId") Long categoryId, @Param("displayOrder") int displayOrder);

    @Modifying
    @Transactional
    @Query("UPDATE Category c SET c.isActive = false WHERE c.id = :categoryId")
    void deactivateCategory(@Param("categoryId") Long categoryId);

    // Analytics queries
    @Query("SELECT c.name, c.newsCount, " +
            "(SELECT COUNT(n) FROM News n WHERE n.category.id = c.id AND n.isActive = true) as recentCount, " +
            "(SELECT COALESCE(SUM(n.viewCount), 0) FROM News n WHERE n.category.id = c.id AND n.isActive = true) as totalViews " +
            "FROM Category c WHERE c.isActive = true ORDER BY c.newsCount DESC")
    List<Object[]> getCategoryStatistics();

    // Top categories by news count
    @Query("SELECT c FROM Category c WHERE c.isActive = true ORDER BY c.newsCount DESC")
    List<Category> findTopCategoriesByNewsCount(Pageable pageable);

    // Top categories by total view count
    @Query("SELECT c FROM Category c WHERE c.isActive = true ORDER BY " +
            "(SELECT COALESCE(SUM(n.viewCount), 0) FROM News n WHERE n.category.id = c.id AND n.isActive = true) DESC")
    List<Category> findTopCategoriesByViewCount(Pageable pageable);

    // Find empty categories (no active news)
    @Query("SELECT c FROM Category c WHERE c.isActive = true AND c.newsCount = 0")
    List<Category> findEmptyCategories();

    // Find categories with recent activity
    @Query("SELECT DISTINCT c FROM Category c JOIN c.newsList n " +
            "WHERE c.isActive = true AND n.isActive = true " +
            "AND n.publishedAt >= FUNCTION('DATEADD', DAY, -:days, CURRENT_DATE) " +
            "ORDER BY c.displayOrder ASC, c.name ASC")
    List<Category> findCategoriesWithRecentActivity(@Param("days") int days);

    // Categories ordered by creation date
    List<Category> findByIsActiveTrueOrderByCreatedAtDesc();
    List<Category> findByIsActiveTrueOrderByCreatedAtAsc();

    // Categories with specific color or icon
    List<Category> findByColorAndIsActiveTrue(String color);
    List<Category> findByIconClassAndIsActiveTrue(String iconClass);
}