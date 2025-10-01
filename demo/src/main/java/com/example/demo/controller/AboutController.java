package com.example.demo.controller;

import com.example.demo.service.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AboutController {

    private final CategoryService categoryService;

    public AboutController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("pageTitle", "About Us - NewsHub");
        model.addAttribute("activePage", "about");
        model.addAttribute("categories", categoryService.getAllActiveCategories());
        return "about";
    }
}
