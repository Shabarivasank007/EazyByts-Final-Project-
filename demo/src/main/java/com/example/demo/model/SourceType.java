package com.example.demo.model;

/**
 * Enum representing different types of news sources.
 */
public enum SourceType {
    /**
     * News source that provides data through an API
     */
    API,
    
    /**
     * News source that provides data through an RSS feed
     */
    RSS,
    
    /**
     * News source that requires web scraping
     */
    WEB
}
