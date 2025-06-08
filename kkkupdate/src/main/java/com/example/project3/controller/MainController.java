package com.example.project3.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;


@Controller
public class MainController {
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/")
    public String home(Authentication authentication) {
        // Если пользователь авторизован, перенаправляем на dashboard
        if (authentication != null && authentication.isAuthenticated() &&
                !authentication.getName().equals("anonymousUser")) {
            return "redirect:/dashboard";
        }
        // Если не авторизован, показываем главную страницу
        return "index";
    }

    @GetMapping("/home")
    public String homeAlias(Authentication authentication) {
        return home(authentication);
    }
}