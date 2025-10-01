package com.example.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "categories", uniqueConstraints = {
        @UniqueConstraint(columnNames = "name"),
        @UniqueConstraint(columnNames = "slug")
})
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must be less than 100 characters")
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @NotBlank(message = "Category slug is required")
    @Size(max = 100, message = "Category slug must be less than 100 characters")
    @Column(name = "slug", nullable = false, unique = true, length = 100)
    private String slug;

    @Size(max = 255, message = "Description must be less than 255 characters")
    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "color", length = 7)
    private String color = "#2563eb"; // Default blue color

    @Column(name = "icon_class", length = 50)
    private String iconClass = "fas fa-newspaper"; // Default icon

    @Column(name = "display_order", columnDefinition = "INT DEFAULT 0")
    private Integer displayOrder = 0;

    @Column(name = "is_active", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean isActive = true;

    @Column(name = "is_featured", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isFeatured = false;

    @Column(name = "news_count", columnDefinition = "BIGINT DEFAULT 0")
    private Long newsCount = 0L;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("publishedAt DESC")
    private List<News> newsList = new ArrayList<>();

    // Constructors
    public Category() {}

    public Category(String name, String slug) {
        this.name = name;
        this.slug = slug;
    }

    public Category(String name, String slug, String description) {
        this.name = name;
        this.slug = slug;
        this.description = description;
    }

    public Category(String name, String slug, String description, String color, String iconClass) {
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.color = color;
        this.iconClass = iconClass;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) {
        this.name = name;
        if (this.slug == null || this.slug.isEmpty()) {
            this.slug = generateSlug(name);
        }
    }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getIconClass() { return iconClass; }
    public void setIconClass(String iconClass) { this.iconClass = iconClass; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Boolean getIsFeatured() { return isFeatured; }
    public void setIsFeatured(Boolean isFeatured) { this.isFeatured = isFeatured; }

    public Long getNewsCount() { return newsCount; }
    public void setNewsCount(Long newsCount) { this.newsCount = newsCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<News> getNewsList() { return newsList; }
    public void setNewsList(List<News> newsList) { this.newsList = newsList; }

    // Utility methods
    public void addNews(News news) {
        this.newsList.add(news);
        news.setCategory(this);
        this.newsCount++;
    }

    public void removeNews(News news) {
        this.newsList.remove(news);
        news.setCategory(null);
        if (this.newsCount > 0) {
            this.newsCount--;
        }
    }

    public List<News> getLatestNews(int limit) {
        return newsList.stream()
                .filter(news -> news.getIsActive())
                .limit(limit)
                .toList();
    }

    public List<News> getTrendingNews(int limit) {
        return newsList.stream()
                .filter(news -> news.getIsActive() && news.getIsTrending())
                .limit(limit)
                .toList();
    }

    public List<News> getFeaturedNews(int limit) {
        return newsList.stream()
                .filter(news -> news.getIsActive() && news.getIsFeatured())
                .limit(limit)
                .toList();
    }

    public long getActiveNewsCount() {
        return newsList.stream()
                .filter(news -> news.getIsActive())
                .count();
    }

    private String generateSlug(String name) {
        if (name == null) return "";
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
    }

    // Static factory methods for common categories
    public static Category createTechnologyCategory() {
        return new Category("Technology", "technology",
                "Latest tech news, gadgets, and innovations",
                "#3b82f6", "fas fa-microchip");
    }

    public static Category createSportsCategory() {
        return new Category("Sports", "sports",
                "Sports news, scores, and updates",
                "#ef4444", "fas fa-futbol");
    }

    public static Category createPoliticsCategory() {
        return new Category("Politics", "politics",
                "Political news and government updates",
                "#7c3aed", "fas fa-landmark");
    }

    public static Category createBusinessCategory() {
        return new Category("Business", "business",
                "Business news, markets, and economy",
                "#059669", "fas fa-chart-line");
    }

    public static Category createEntertainmentCategory() {
        return new Category("Entertainment", "entertainment",
                "Celebrity news, movies, and shows",
                "#f59e0b", "fas fa-film");
    }

    public static Category createHealthCategory() {
        return new Category("Health", "health",
                "Health news, medical breakthroughs, and wellness",
                "#10b981", "fas fa-heartbeat");
    }

    public static Category createScienceCategory() {
        return new Category("Science", "science",
                "Scientific discoveries and research",
                "#6366f1", "fas fa-atom");
    }

    public static Category createWorldCategory() {
        return new Category("World", "world",
                "International news and global events",
                "#dc2626", "fas fa-globe");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return Objects.equals(id, category.id) && Objects.equals(slug, category.slug);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, slug);
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", slug='" + slug + '\'' +
                ", isActive=" + isActive +
                ", newsCount=" + newsCount +
                '}';
    }
}