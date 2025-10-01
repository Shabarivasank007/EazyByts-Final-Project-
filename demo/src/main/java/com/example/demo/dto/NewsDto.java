
package com.example.demo.dto;

import com.example.demo.model.Category;
import com.example.demo.model.NewsSource;
import com.example.demo.model.User;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

public class NewsDto {
    private Long id;
    private String title;
    private String description;
    private String content;
    private String imageUrl;
    private String sourceUrl;
    private String author;
    private LocalDateTime publishedAt;
    private Long viewCount;
    private Boolean isFeatured;
    private Boolean isTrending;
    private Boolean isActive;
    private Integer readingTime;
    private CategoryDto category;
    private NewsSourceDto source;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public NewsDto() {}

    public NewsDto(com.example.demo.model.News news) {
        this.id = news.getId();
        this.title = news.getTitle();
        this.description = news.getDescription();
        this.content = news.getContent();
        this.imageUrl = news.getImageUrl();
        this.sourceUrl = news.getSourceUrl();
        this.author = news.getAuthor();
        this.publishedAt = news.getPublishedAt();
        this.viewCount = news.getViewCount();
        this.isFeatured = news.getIsFeatured();
        this.isTrending = news.getIsTrending();
        this.isActive = news.getIsActive();
        this.readingTime = news.getReadingTime();
        this.category = news.getCategory() != null ? new CategoryDto(news.getCategory()) : null;
        this.source = news.getSource() != null ? new NewsSourceDto(news.getSource()) : null;
        this.createdAt = news.getCreatedAt();
        this.updatedAt = news.getUpdatedAt();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }

    public Long getViewCount() { return viewCount; }
    public void setViewCount(Long viewCount) { this.viewCount = viewCount; }

    public Boolean getIsFeatured() { return isFeatured; }
    public void setIsFeatured(Boolean isFeatured) { this.isFeatured = isFeatured; }

    public Boolean getIsTrending() { return isTrending; }
    public void setIsTrending(Boolean isTrending) { this.isTrending = isTrending; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Integer getReadingTime() { return readingTime; }
    public void setReadingTime(Integer readingTime) { this.readingTime = readingTime; }

    public CategoryDto getCategory() { return category; }
    public void setCategory(CategoryDto category) { this.category = category; }

    public NewsSourceDto getSource() { return source; }
    public void setSource(NewsSourceDto source) { this.source = source; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}