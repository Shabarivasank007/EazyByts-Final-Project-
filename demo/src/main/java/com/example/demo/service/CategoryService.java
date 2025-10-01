package com.example.demo.service;

import com.example.demo.model.Category;
import com.example.demo.model.News;
import com.example.demo.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // Basic CRUD operations
    @Cacheable(value = "category", key = "#id")
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }

    @Cacheable(value = "categoryBySlug", key = "#slug")
    public Category getCategoryBySlug(String slug) {
        return categoryRepository.findBySlugAndIsActiveTrue(slug).orElse(null);
    }

    @Cacheable(value = "categoryByName", key = "#name")
    public Category getCategoryByName(String name) {
        return categoryRepository.findByNameAndIsActiveTrue(name).orElse(null);
    }

    @Cacheable(value = "allCategories")
    public List<Category> getAllActiveCategories() {
        return categoryRepository.findByIsActiveTrueOrderByDisplayOrderAscNameAsc();
    }
    
    /**
     * Finds an existing category by name or creates a new one if it doesn't exist
     * @param name The name of the category to find or create
     * @return The existing or newly created category
     */
    @Transactional
    @CacheEvict(value = {"allCategories", "category", "categoryByName", "categoryBySlug"}, allEntries = true)
    public Category findOrCreateCategory(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be null or empty");
        }
        
        String trimmedName = name.trim();
        // First try to find by exact match
        return categoryRepository.findByName(trimmedName)
            .or(() -> categoryRepository.findByNameIgnoreCase(trimmedName)) // Fallback to case-insensitive search if exact match not found
            .orElseGet(() -> {
                Category newCategory = new Category();
                newCategory.setName(trimmedName);
                newCategory.setSlug(createSlug(trimmedName));
                newCategory.setIsActive(true);
                return categoryRepository.save(newCategory);
            });
    }
    
    /**
     * Creates a URL-friendly slug from a category name
     */
    private String createSlug(String name) {
        if (name == null) return null;
        return name.toLowerCase()
                 .replaceAll("[^a-z0-9]+", "-")
                 .replaceAll("(^-|-$)", "");
    }

    @Cacheable(value = "featuredCategories")
    public List<Category> getFeaturedCategories() {
        return categoryRepository.findByIsFeaturedTrueAndIsActiveTrueOrderByDisplayOrderAscNameAsc();
    }

    @Transactional
    @CacheEvict(value = {"category", "allCategories", "featuredCategories", "categoryBySlug", "categoryByName"}, allEntries = true)
    public Category saveCategory(Category category) {
        // Generate slug if not provided
        if (category.getSlug() == null || category.getSlug().isEmpty()) {
            category.setSlug(generateSlug(category.getName()));
        }

        // Ensure unique slug
        category.setSlug(ensureUniqueSlug(category.getSlug(), category.getId()));

        return categoryRepository.save(category);
    }

    @Transactional
    @CacheEvict(value = {"category", "allCategories", "featuredCategories", "categoryBySlug", "categoryByName"}, allEntries = true)
    public Category updateCategory(Category category) {
        return categoryRepository.save(category);
    }

    @Transactional
    @CacheEvict(value = {"category", "allCategories", "featuredCategories", "categoryBySlug", "categoryByName"}, allEntries = true)
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    @Transactional
    @CacheEvict(value = {"category", "allCategories", "featuredCategories", "categoryBySlug", "categoryByName"}, allEntries = true)
    public void deactivateCategory(Long id) {
        categoryRepository.deactivateCategory(id);
    }

    // Categories with latest news
    @Cacheable(value = "categoriesWithNews", key = "#limit")
    public List<Category> getCategoriesWithLatestNews(int limit) {
        List<Category> categories = getAllActiveCategories();

        for (Category category : categories) {
            // Get latest news for each category
            List<News> latestNews = category.getLatestNews(limit);
            category.setNewsList(latestNews);
        }

        return categories;
    }

    // Category statistics
    public long getTotalActiveCategories() {
        return categoryRepository.countByIsActiveTrue();
    }

    public long getTotalFeaturedCategories() {
        return categoryRepository.countByIsFeaturedTrueAndIsActiveTrue();
    }

    // Update category news count
    @Transactional
    public void updateCategoryNewsCount(Long categoryId) {
        categoryRepository.updateNewsCount(categoryId);
    }

    @Transactional
    public void updateAllCategoryNewsCounts() {
        categoryRepository.updateAllNewsCounts();
    }

    // Featured status management
    @Transactional
    @CacheEvict(value = {"category", "featuredCategories"}, allEntries = true)
    public void updateFeaturedStatus(Long categoryId, boolean featured) {
        categoryRepository.updateFeaturedStatus(categoryId, featured);
    }

    // Display order management
    @Transactional
    @CacheEvict(value = {"allCategories", "featuredCategories"}, allEntries = true)
    public void updateDisplayOrder(Long categoryId, int displayOrder) {
        categoryRepository.updateDisplayOrder(categoryId, displayOrder);
    }

    @Transactional
    @CacheEvict(value = {"allCategories", "featuredCategories"}, allEntries = true)
    public void reorderCategories(List<Long> categoryIds) {
        for (int i = 0; i < categoryIds.size(); i++) {
            updateDisplayOrder(categoryIds.get(i), i + 1);
        }
    }

    // Search functionality
    public List<Category> searchCategories(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllActiveCategories();
        }
        return categoryRepository.searchByKeyword(keyword.trim());
    }

    // Category validation
    public boolean isSlugUnique(String slug, Long excludeId) {
        Optional<Category> existing = categoryRepository.findBySlug(slug);
        return existing.isEmpty() ||
                (excludeId != null && existing.get().getId().equals(excludeId));
    }

    public boolean isNameUnique(String name, Long excludeId) {
        Optional<Category> existing = categoryRepository.findByName(name);
        return existing.isEmpty() ||
                (excludeId != null && existing.get().getId().equals(excludeId));
    }

    // Utility methods
    private String generateSlug(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "";
        }

        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    private String ensureUniqueSlug(String baseSlug, Long excludeId) {
        String slug = baseSlug;
        int counter = 1;

        while (!isSlugUnique(slug, excludeId)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }

        return slug;
    }

    // Initialize default categories
    @Transactional
    public void initializeDefaultCategories() {
        if (categoryRepository.count() == 0) {
            List<Category> defaultCategories = List.of(
                    createCategoryWithOrder("World", "world", "International news and global events",
                            "#dc2626", "fas fa-globe", 1, true),
                    createCategoryWithOrder("Politics", "politics", "Political news and government updates",
                            "#7c3aed", "fas fa-landmark", 2, true),
                    createCategoryWithOrder("Business", "business", "Business news, markets, and economy",
                            "#059669", "fas fa-chart-line", 3, true),
                    createCategoryWithOrder("Technology", "technology", "Latest tech news, gadgets, and innovations",
                            "#3b82f6", "fas fa-microchip", 4, true),
                    createCategoryWithOrder("Sports", "sports", "Sports news, scores, and updates",
                            "#ef4444", "fas fa-futbol", 5, true),
                    createCategoryWithOrder("Entertainment", "entertainment", "Celebrity news, movies, and shows",
                            "#f59e0b", "fas fa-film", 6, true),
                    createCategoryWithOrder("Health", "health", "Health news, medical breakthroughs, and wellness",
                            "#10b981", "fas fa-heartbeat", 7, false),
                    createCategoryWithOrder("Science", "science", "Scientific discoveries and research",
                            "#6366f1", "fas fa-atom", 8, false)
            );

            for (Category category : defaultCategories) {
                categoryRepository.save(category);
            }
        }
    }

    private Category createCategoryWithOrder(String name, String slug, String description,
                                             String color, String iconClass, int displayOrder,
                                             boolean featured) {
        Category category = new Category(name, slug, description, color, iconClass);
        category.setDisplayOrder(displayOrder);
        category.setIsFeatured(featured);
        return category;
    }

    // Category analytics
    public List<Object[]> getCategoryStatistics() {
        return categoryRepository.getCategoryStatistics();
    }

    public List<Category> getTopCategoriesByNewsCount(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return categoryRepository.findTopCategoriesByNewsCount(pageable);
    }

    public List<Category> getTopCategoriesByViewCount(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return categoryRepository.findTopCategoriesByViewCount(pageable);
    }

    // Bulk operations
    @Transactional
    @CacheEvict(value = {"allCategories", "featuredCategories"}, allEntries = true)
    public void bulkUpdateFeaturedStatus(List<Long> categoryIds, boolean featured) {
        for (Long categoryId : categoryIds) {
            updateFeaturedStatus(categoryId, featured);
        }
    }

    @Transactional
    @CacheEvict(value = {"allCategories", "featuredCategories"}, allEntries = true)
    public void bulkDeactivateCategories(List<Long> categoryIds) {
        for (Long categoryId : categoryIds) {
            deactivateCategory(categoryId);
        }
    }

    // Category maintenance
    @Transactional
    public void cleanupEmptyCategories() {
        List<Category> emptyCategories = categoryRepository.findEmptyCategories();
        for (Category category : emptyCategories) {
            if (!category.getIsFeatured()) {
                deactivateCategory(category.getId());
            }
        }
    }

    // Export/Import support
    public List<Category> exportAllCategories() {
        return categoryRepository.findAll(Sort.by("displayOrder", "name"));
    }

    @Transactional
    @CacheEvict(value = {"category", "allCategories", "featuredCategories", "categoryBySlug", "categoryByName"}, allEntries = true)
    public void importCategories(List<Category> categories) {
        for (Category category : categories) {
            // Ensure unique slug
            if (!isSlugUnique(category.getSlug(), null)) {
                category.setSlug(ensureUniqueSlug(category.getSlug(), null));
            }
            categoryRepository.save(category);
        }
    }
}