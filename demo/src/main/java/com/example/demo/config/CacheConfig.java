package com.example.demo.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(
            new ConcurrentMapCache("news"),
            new ConcurrentMapCache("categories"),
            new ConcurrentMapCache("latestNews"),
            new ConcurrentMapCache("trendingNews"),
            new ConcurrentMapCache("categoryNews"),
            new ConcurrentMapCache("allNews"),
            new ConcurrentMapCache("featuredNews"),
            new ConcurrentMapCache("breakingNews"),
            new ConcurrentMapCache("allCategories"),
            new ConcurrentMapCache("featuredCategories"),
            new ConcurrentMapCache("categoryBySlug"),
            new ConcurrentMapCache("categoryByName"),
            new ConcurrentMapCache("categoriesWithNews"),
            new ConcurrentMapCache("topViewedNews"),
            new ConcurrentMapCache("relatedNews")
        ));
        return cacheManager;
    }
}
