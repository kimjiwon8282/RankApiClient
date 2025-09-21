package com.example.RankCat.controller.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UserViewController {
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/app-login")
    public String appLogin() {
        return "app-login"; // Thymeleaf 템플릿 이름
    }

    @GetMapping("/signup") //회원가입
    public String signup() {
        return "signup";
    }

    @GetMapping("/home")
    public String home() {
        return "home";
    }

    @GetMapping("/optimize-product-name")
    public String optimizeProductName() {
        return "optimize-product-name";
    }

    @GetMapping("/my-history")
    public String history() {
        return "my-history";
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/home";
    }
}
