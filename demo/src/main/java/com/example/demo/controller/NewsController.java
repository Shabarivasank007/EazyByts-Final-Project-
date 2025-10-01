package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import com.example.demo.service.NewsService;
import com.example.demo.model.News;

@Controller
@RequestMapping("/api/news")
public class NewsController {
    @Autowired
    private NewsService newsService;

    @GetMapping("/{id}")
    public String getNewsDetail(@PathVariable("id") Long id, Model model) {
        News news = newsService.getNewsById(id);
        if (news == null) {
            return "error/404";
        }
        model.addAttribute("news", news);
        return "news/detail";
    }

    // News related endpoints will be added here
}
