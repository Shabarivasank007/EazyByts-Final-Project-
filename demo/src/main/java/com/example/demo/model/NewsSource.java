package com.example.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.net.URL;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "news_sources", uniqueConstraints = {
        @UniqueConstraint(columnNames = "name"),
        @UniqueConstraint(columnNames = "base_url")
})
public class NewsSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Source name is required")
    @Size(max = 100, message = "Source name must be less than 100 characters")
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @NotBlank(message = "Base URL is required")
    // URL validation is handled at the service layer
    @Column(name = "base_url", nullable = false, unique = true, length = 255)
    private String baseUrl;

    @Column(name = "api_url", length = 255)
    private String apiUrl;

    @Column(name = "rss_url", length = 255)
    private String rssUrl;

    @Size(max = 255, message = "Description must be less than 255 characters")
    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "logo_url", length = 255)
    private String logoUrl;

    @Column(name = "country_code", length = 2)
    private String countryCode;

    @Column(name = "language_code", length = 5)
    private String languageCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", length = 20)
    private SourceType sourceType = SourceType.WEB;

    @Column(name = "api_key", length = 100)
    private String apiKey;
    
    @Column(name = "api_parameters", length = 1000)
    private String apiParameters;

    @Column(name = "update_frequency_minutes", columnDefinition = "INT DEFAULT 60")
    private Integer updateFrequencyMinutes = 60;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "is_active", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean isActive = true;

    @Column(name = "priority_level", columnDefinition = "INT DEFAULT 1")
    private Integer priorityLevel = 1;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "source", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<News> newsList = new ArrayList<>();

    public enum SourceType {
        WEB, RSS, API, SOCIAL_MEDIA
    }

    // Constructors
    public NewsSource() {}

    public NewsSource(String name, String baseUrl) {
        this.name = name;
        this.baseUrl = baseUrl;
    }

    public NewsSource(String name, String baseUrl, String description, SourceType sourceType) {
        this.name = name;
        this.baseUrl = baseUrl;
        this.description = description;
        this.sourceType = sourceType;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    
    public String getParameters() {
        return apiParameters;
    }
    
    public void setParameters(String parameters) {
        this.apiParameters = parameters;
    }

    public Integer getUpdateFrequencyMinutes() { return updateFrequencyMinutes; }
    public void setUpdateFrequencyMinutes(Integer updateFrequencyMinutes) { this.updateFrequencyMinutes = updateFrequencyMinutes; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Integer getPriorityLevel() { return priorityLevel; }
    public void setPriorityLevel(Integer priorityLevel) { this.priorityLevel = priorityLevel; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<News> getNewsList() { return newsList; }
    public void setNewsList(List<News> newsList) { this.newsList = newsList; }

    // Utility methods
    public void addNews(News news) {
        this.newsList.add(news);
        news.setSource(this);
    }

    public void removeNews(News news) {
        this.newsList.remove(news);
        news.setSource(null);
    }

    public boolean shouldUpdate() {
        if (lastUpdated == null) return true;
        return lastUpdated.plusMinutes(updateFrequencyMinutes).isBefore(LocalDateTime.now());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NewsSource that = (NewsSource) o;
        return Objects.equals(id, that.id) && Objects.equals(baseUrl, that.baseUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, baseUrl);
    }

    @Override
    public String toString() {
        return "NewsSource{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", sourceType=" + sourceType +
                ", isActive=" + isActive +
                ", lastUpdated=" + lastUpdated +
                '}';
    }

    public String getApiUrl() { 
        return apiUrl; 
    }
    
    public void setApiUrl(String apiUrl) { 
        this.apiUrl = apiUrl; 
    }

    // Base URL getter is already defined above
    
    /**
     * Gets the API endpoint for this news source.
     * If apiUrl is not set, returns the base URL.
     * @return The API endpoint URL
     */
    public String getApiEndpoint() {
        return apiUrl != null && !apiUrl.isEmpty() ? apiUrl : baseUrl;
    }
    
    public void setRssUrl(String rssUrl) { 
        this.rssUrl = rssUrl; 
    }

    public String getRssUrl() { 
        return rssUrl; 
    }
    
    public String getDescription() { 
        return description; 
    }
    
    public void setDescription(String description) { 
        this.description = description; 
    }

    public String getLogoUrl() { 
        return logoUrl; 
    }
    
    public void setLogoUrl(String logoUrl) { 
        this.logoUrl = logoUrl; 
    }

    public String getCountryCode() { 
        return countryCode; 
    }
    
    public void setCountryCode(String countryCode) { 
        this.countryCode = countryCode; 
    }

    public String getLanguageCode() { 
        return languageCode; 
    }
    
    public void setLanguageCode(String languageCode) { 
        this.languageCode = languageCode; 
    }

    public SourceType getSourceType() { 
        return sourceType; 
    }
    
    public void setSourceType(SourceType sourceType) { 
        this.sourceType = sourceType; 
    }
}