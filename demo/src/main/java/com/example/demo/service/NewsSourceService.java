package com.example.demo.service;

import com.example.demo.model.NewsSource;
import com.example.demo.repository.NewsSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class NewsSourceService {

    private final NewsSourceRepository newsSourceRepository;

    @Autowired
    public NewsSourceService(NewsSourceRepository newsSourceRepository) {
        this.newsSourceRepository = newsSourceRepository;
    }

    @Cacheable("allNewsSources")
    public List<NewsSource> getAllNewsSources() {
        return newsSourceRepository.findByIsActiveTrueOrderByPriorityLevelDesc();
    }

    @Cacheable(value = "newsSource", key = "#id")
    public NewsSource getNewsSourceById(Long id) {
        return newsSourceRepository.findById(id).orElse(null);
    }

    @Transactional
    @CacheEvict(value = {"allNewsSources", "newsSource"}, allEntries = true)
    public NewsSource save(NewsSource newsSource) {
        return newsSourceRepository.save(newsSource);
    }

    @Transactional
    @CacheEvict(value = {"allNewsSources", "newsSource"}, allEntries = true)
    public void deleteNewsSource(Long id) {
        newsSourceRepository.deleteById(id);
    }

    @Cacheable(value = "activeNewsSources")
    public List<NewsSource> getActiveNewsSources() {
        return newsSourceRepository.findByIsActiveTrueOrderByPriorityLevelDesc();
    }

    @Cacheable(value = "newsSourcesPage")
    public List<NewsSource> getNewsSourcesPage(Pageable pageable) {
        return newsSourceRepository.findByIsActiveTrueOrderByPriorityLevelDesc();
    }
}
