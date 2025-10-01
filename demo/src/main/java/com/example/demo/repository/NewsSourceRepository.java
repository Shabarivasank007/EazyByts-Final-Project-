package com.example.demo.repository;

import com.example.demo.model.NewsSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsSourceRepository extends JpaRepository<NewsSource, Long> {
    List<NewsSource> findByIsActive(boolean isActive);
    
    /**
     * Find all news sources, regardless of active status
     * @return List of all news sources
     */
    List<NewsSource> findAll();
    
    /**
     * Find all active news sources ordered by priority level in descending order
     * @return List of active news sources
     */
    List<NewsSource> findByIsActiveTrueOrderByPriorityLevelDesc();
}
