package com.example.demo.service;

import com.example.demo.dto.NewsApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class NewsApiService {
    private final WebClient newsApiClient;

    @Autowired
    public NewsApiService(@Qualifier("newsApiWebClient") WebClient newsApiClient) {
        this.newsApiClient = newsApiClient;
    }

    @Cacheable(value = "topNews", key = "'top_' + #country")
    public Mono<NewsApiResponse> getTopNews(String country) {
        return newsApiClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/top-headlines")
                    .queryParam("country", country)
                    .build())
                .retrieve()
                .bodyToMono(NewsApiResponse.class);
    }

    @Cacheable(value = "categoryNews", key = "#category + '_' + #country")
    public Mono<NewsApiResponse> getNewsByCategory(String category, String country) {
        return newsApiClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/top-headlines")
                    .queryParam("category", category)
                    .queryParam("country", country)
                    .build())
                .retrieve()
                .bodyToMono(NewsApiResponse.class);
    }

    @Cacheable(value = "searchNews", key = "#query")
    public Mono<NewsApiResponse> searchNews(String query) {
        return newsApiClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/everything")
                    .queryParam("q", query)
                    .build())
                .retrieve()
                .bodyToMono(NewsApiResponse.class);
    }
}
