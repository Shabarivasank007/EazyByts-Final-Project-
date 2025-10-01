package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class NewsSourceDto {
    private Long id;
    
    @NotBlank(message = "Source name is required")
    @Size(max = 100, message = "Source name must be less than 100 characters")
    private String name;
    
    @NotBlank(message = "Base URL is required")
    private String baseUrl;
    
    private String description;
    private String logoUrl;
    private String countryCode;
    private String languageCode;
    private String sourceType;
    
    public NewsSourceDto() {}
    
    public NewsSourceDto(com.example.demo.model.NewsSource source) {
        if (source != null) {
            this.id = source.getId();
            this.name = source.getName();
            this.baseUrl = source.getBaseUrl();
            this.description = source.getDescription();
            this.logoUrl = source.getLogoUrl();
            this.countryCode = source.getCountryCode();
            this.languageCode = source.getLanguageCode();
            this.sourceType = source.getSourceType() != null ? source.getSourceType().name() : null;
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getBaseUrl() {
        return baseUrl;
    }
    
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
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
    
    public String getSourceType() {
        return sourceType;
    }
    
    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }
}
