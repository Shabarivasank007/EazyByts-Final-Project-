package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class NewsApiConfig {
    @Value("${newsapi.base-url}")
    private String baseUrl;

    @Value("${newsapi.api-key}")
    private String apiKey;

    @Value("${newsapi.page-size}")
    private int pageSize;

    @Value("${newsapi.language}")
    private String language;

    @Value("${newsapi.country}")
    private String country;

    @Bean(name = "newsApiWebClient")
    public WebClient newsApiClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("X-Api-Key", apiKey)
                .build();
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public int getPageSize() {
        return pageSize;
    }

    public String getLanguage() {
        return language;
    }

    public String getCountry() {
        return country;
    }
}
