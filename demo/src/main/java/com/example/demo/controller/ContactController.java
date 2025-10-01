package com.example.demo.controller;

import com.example.demo.service.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ContactController {

    private final CategoryService categoryService;

    public ContactController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("pageTitle", "Contact Us - NewsHub");
        model.addAttribute("activePage", "contact");
        model.addAttribute("categories", categoryService.getAllActiveCategories());
        return "contact";
    }
}
