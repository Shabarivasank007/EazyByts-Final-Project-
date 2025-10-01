package com.example.demo.controller;

import com.example.demo.model.News;
import com.example.demo.model.Category;
import com.example.demo.service.NewsService;
import com.example.demo.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
public class HomeController {

    private final NewsService newsService;
    private final CategoryService categoryService;

    @Autowired
    public HomeController(NewsService newsService, CategoryService categoryService) {
        this.newsService = newsService;
        this.categoryService = categoryService;
    }

    @GetMapping("/splash")
    public String splash() {
        return "splash";
    }

    @GetMapping("/")
    public String home(Model model,
                     @RequestParam(value = "page", defaultValue = "0") int page,
                     @RequestParam(value = "size", defaultValue = "12") int size) {
        
        // Set page title and active page
        model.addAttribute("pageTitle", "Latest News - Best News Platform");
        model.addAttribute("activePage", "home");

        // Initialize variables
        List<News> trendingNews = new ArrayList<>();
        List<News> featuredNews = new ArrayList<>();
        Page<News> latestNews = Page.empty();
        List<Category> categories = new ArrayList<>();
        List<Category> categoriesWithNews = new ArrayList<>();
        List<News> breakingNews = new ArrayList<>();

        try {
            // Get trending news for slideshow (top 15 latest news)
            trendingNews = newsService.getLatestNews(15);
            if (trendingNews == null) {
                trendingNews = new ArrayList<>();
            }
            model.addAttribute("trendingNews", trendingNews);

            // Get featured news for hero section
            featuredNews = newsService.getFeaturedNews(3);
            if (featuredNews == null) {
                featuredNews = new ArrayList<>();
            }

            // Get latest news with pagination
            Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
            latestNews = newsService.getLatestNews(pageable);
            if (latestNews == null) {
                latestNews = Page.empty();
            }

            // Get all active categories
            categories = categoryService.getAllActiveCategories();
            if (categories == null) {
                categories = new ArrayList<>();
            }

            // Get category-wise news preview (3 articles per category)
            categoriesWithNews = categoryService.getCategoriesWithLatestNews(3);
            if (categoriesWithNews == null) {
                categoriesWithNews = new ArrayList<>();
            }
            
            // Breaking news (last 2 hours)
            breakingNews = newsService.getBreakingNews(5);
            if (breakingNews == null) {
                breakingNews = new ArrayList<>();
            }
            
        } catch (Exception e) {
            // Log the error
            e.printStackTrace();
            // Use empty collections as fallback
            trendingNews = new ArrayList<>();
            featuredNews = new ArrayList<>();
            latestNews = Page.empty();
            categories = new ArrayList<>();
            categoriesWithNews = new ArrayList<>();
            breakingNews = new ArrayList<>();
        }

        // Add all attributes to the model
        model.addAttribute("trendingNews", trendingNews);
        model.addAttribute("featuredNews", featuredNews);
        model.addAttribute("latestNews", latestNews);
        model.addAttribute("categories", categories);
        model.addAttribute("categoriesWithNews", categoriesWithNews);
        model.addAttribute("breakingNews", breakingNews);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", latestNews.getTotalPages());
        model.addAttribute("pageTitle", "Latest News - Best News Platform");

        return "index";
    }

    @GetMapping("/category/{slug}")
    public String categoryNews(@PathVariable String slug,
                               Model model,
                               @RequestParam(value = "page", defaultValue = "0") int page,
                               @RequestParam(value = "size", defaultValue = "12") int size) {

        Category category = categoryService.getCategoryBySlug(slug);
        if (category == null) {
            return "redirect:/";
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        Page<News> categoryNews = newsService.getNewsByCategory(category, pageable);

        List<Category> allCategories = categoryService.getAllActiveCategories();

        model.addAttribute("category", category);
        model.addAttribute("categoryNews", categoryNews);
        model.addAttribute("categories", allCategories);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", categoryNews.getTotalPages());
        model.addAttribute("pageTitle", category.getName() + " News");

        return "news/category";
    }

    @GetMapping("/news/{id}")
    public String newsDetail(@PathVariable Long id, Model model) {
        News news = newsService.getNewsById(id);
        if (news == null || !news.getIsActive()) {
            return "redirect:/";
        }

        // Increment view count
        newsService.incrementViewCount(id);

        // Get related news (same category, excluding current)
        List<News> relatedNews = newsService.getRelatedNews(news, 4);

        // Get all categories for navigation
        List<Category> categories = categoryService.getAllActiveCategories();

        model.addAttribute("news", news);
        model.addAttribute("relatedNews", relatedNews);
        model.addAttribute("categories", categories);
        model.addAttribute("pageTitle", news.getTitle());

        return "news/detail";
    }

    @GetMapping("/search")
    public String search(@RequestParam(value = "q", defaultValue = "") String query,
                         @RequestParam(value = "category", required = false) Long categoryId,
                         @RequestParam(value = "page", defaultValue = "0") int page,
                         @RequestParam(value = "size", defaultValue = "12") int size,
                         Model model) {

        if (query.trim().isEmpty() && categoryId == null) {
            return "redirect:/";
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        Page<News> searchResults = newsService.searchNews(query, categoryId, pageable);

        List<Category> categories = categoryService.getAllActiveCategories();

        model.addAttribute("searchResults", searchResults);
        model.addAttribute("categories", categories);
        model.addAttribute("searchQuery", query);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", searchResults.getTotalPages());
        model.addAttribute("pageTitle", "Search Results for: " + query);

        return "news/search";
    }

}