package com.example.demo.controller;

import com.example.demo.dto.NewsApiResponse;
import com.example.demo.service.NewsApiService;
import com.example.demo.service.NewsAggregatorService;
import com.example.demo.service.NewsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/news")
public class NewsApiController {
    private static final Logger logger = LoggerFactory.getLogger(NewsApiController.class);

    private final NewsApiService newsApiService;
    private final NewsAggregatorService newsAggregatorService;
    private final NewsService newsService;

    @Autowired
    public NewsApiController(NewsApiService newsApiService, NewsAggregatorService newsAggregatorService, NewsService newsService) {
        this.newsApiService = newsApiService;
        this.newsAggregatorService = newsAggregatorService;
        this.newsService = newsService;
    }

    @GetMapping("/live")
    public String getLiveNews(Model model, @RequestParam(defaultValue = "us") String country) {
        try {
            NewsApiResponse topNews = newsApiService.getTopNews(country).block();
            model.addAttribute("topNews", topNews);
            logger.info("Successfully fetched {} live news articles",
                    topNews != null && topNews.getArticles() != null ? topNews.getArticles().size() : 0);
        } catch (Exception e) {
            logger.error("Error fetching live news from API: {}", e.getMessage());
            // Fallback to database news when API fails
            try {
                var fallbackNews = new NewsApiResponse();
                var dbNews = newsService.getLatestNews(20);

                // Convert database news to API format for template compatibility
                var articles = dbNews.stream().map(news -> {
                    var article = new NewsApiResponse.NewsArticle();
                    article.setTitle(news.getTitle());
                    article.setDescription(news.getDescription());
                    article.setUrl(news.getSourceUrl() != null ? news.getSourceUrl() : "/news/" + news.getId());
                    article.setUrlToImage(news.getImageUrl());
                    article.setPublishedAt(news.getPublishedAt().toString());
                    article.setAuthor(news.getAuthor());

                    var source = new NewsApiResponse.Source();
                    source.setName(news.getSource() != null ? news.getSource().getName() : "Local News");
                    article.setSource(source);

                    return article;
                }).toList();

                fallbackNews.setArticles(articles);
                fallbackNews.setStatus("ok");
                fallbackNews.setTotalResults(articles.size());

                model.addAttribute("topNews", fallbackNews);
                model.addAttribute("isFromDatabase", true);
                logger.info("Using {} fallback articles from database", articles.size());
            } catch (Exception dbError) {
                logger.error("Error fetching fallback news from database: {}", dbError.getMessage());
                model.addAttribute("topNews", null);
                model.addAttribute("errorMessage", "Unable to fetch news at the moment. Please try again later.");
            }
        }
        return "news/live-news";
    }

    @GetMapping("/category")
    public String getNewsByCategory(
            Model model,
            @RequestParam String category,
            @RequestParam(defaultValue = "us") String country) {
        try {
            NewsApiResponse categoryNews = newsApiService.getNewsByCategory(category, country).block();
            model.addAttribute("categoryNews", categoryNews);
            model.addAttribute("currentCategory", category);
            logger.info("Successfully fetched {} articles for category: {}",
                    categoryNews != null && categoryNews.getArticles() != null ? categoryNews.getArticles().size() : 0, category);
        } catch (Exception e) {
            logger.error("Error fetching category news for {}: {}", category, e.getMessage());
            // Fallback to database news for this category
            try {
                var fallbackNews = new NewsApiResponse();
                var dbNews = newsService.getLatestNews(20);

                // Filter by category if possible or just show latest
                var articles = dbNews.stream()
                        .filter(news -> news.getCategory() != null &&
                                news.getCategory().getName().toLowerCase().contains(category.toLowerCase()))
                        .limit(20)
                        .map(news -> {
                            var article = new NewsApiResponse.NewsArticle();
                            article.setTitle(news.getTitle());
                            article.setDescription(news.getDescription());
                            article.setUrl(news.getSourceUrl() != null ? news.getSourceUrl() : "/news/" + news.getId());
                            article.setUrlToImage(news.getImageUrl());
                            article.setPublishedAt(news.getPublishedAt().toString());
                            article.setAuthor(news.getAuthor());

                            var source = new NewsApiResponse.Source();
                            source.setName(news.getSource() != null ? news.getSource().getName() : "Local News");
                            article.setSource(source);

                            return article;
                        }).toList();

                fallbackNews.setArticles(articles);
                fallbackNews.setStatus("ok");
                fallbackNews.setTotalResults(articles.size());

                model.addAttribute("categoryNews", fallbackNews);
                model.addAttribute("isFromDatabase", true);
                model.addAttribute("currentCategory", category);
                logger.info("Using {} fallback articles from database for category: {}", articles.size(), category);
            } catch (Exception dbError) {
                logger.error("Error fetching fallback category news: {}", dbError.getMessage());
                model.addAttribute("categoryNews", null);
                model.addAttribute("currentCategory", category);
                model.addAttribute("errorMessage", "Unable to fetch " + category + " news at the moment.");
            }
        }
        return "news/category-news";
    }

    @GetMapping("/search")
    public String searchNews(Model model, @RequestParam String query) {
        model.addAttribute("searchResults", newsApiService.searchNews(query).block());
        model.addAttribute("searchQuery", query);
        return "news/search-results";
    }

    @GetMapping("/refresh")
    public String refreshNow() {
        try {
            newsAggregatorService.fetchLatestNews();
        } catch (Exception ignored) {}
        return "redirect:/";
    }
}