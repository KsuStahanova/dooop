package com.example.project3.controller;

import com.example.project3.model.User;
import com.example.project3.service.PresenceService;
import com.example.project3.service.UserService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/dashboard")
public class DashboardController {
    private final UserService userService;
    private final PresenceService presenceService;

    public DashboardController(UserService userService, PresenceService presenceService) {
        this.userService = userService;
        this.presenceService = presenceService;
    }

    @GetMapping
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        try {
            log.info("Dashboard accessed by user: {}", userDetails.getUsername());

            String email = userDetails.getUsername();
            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found: " + email));

            log.debug("User found: {}", user.toString());

            try {
                presenceService.recordUserEntry(user);
                log.debug("Presence recorded for user: {}", user.getEmail());
            } catch (Exception e) {
                log.warn("Failed to record presence for user {}: {}", user.getEmail(), e.getMessage());
            }

            // Добавляем данные в модель
            model.addAttribute("user", user);

            // Добавляем текущее время
            LocalDateTime now = LocalDateTime.now();
            String formattedTime = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            String formattedDate = now.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

            model.addAttribute("currentTime", formattedTime);
            model.addAttribute("currentDate", formattedDate);

            log.info("Dashboard loaded successfully for user: {}", user.getEmail());
            return "dashboard";

        } catch (Exception e) {
            log.error("Error loading dashboard for user {}: {}", userDetails.getUsername(), e.getMessage(), e);
            model.addAttribute("error", "Произошла ошибка при загрузке дашборда");
            return "error"; // Создадим страницу ошибки
        }
    }
}