package com.example.project3.controller;

import com.example.project3.model.PresenceRecord;
import com.example.project3.model.User;
import com.example.project3.service.PresenceService;
import com.example.project3.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/boss")
@PreAuthorize("hasRole('BOSS')")
public class BossController {

    private final UserService userService;
    private final PresenceService presenceService;

    public BossController(UserService userService, PresenceService presenceService) {
        this.userService = userService;
        this.presenceService = presenceService;
    }

    @GetMapping
    public String bossDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        try {
            log.info("Boss dashboard accessed by: {}", userDetails.getUsername());

            String email = userDetails.getUsername();
            User boss = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Boss user not found: " + email));

            model.addAttribute("boss", boss);

            // Статистика для руководства
            List<User> allUsers = userService.getAllUsers();
            List<PresenceRecord> todayRecords = getTodayPresenceRecords();

            model.addAttribute("totalUsers", allUsers.size());
            model.addAttribute("todayCheckIns", todayRecords.size());

            log.info("Boss dashboard loaded successfully for: {}", boss.getEmail());
            return "boss/dashboard";

        } catch (Exception e) {
            log.error("Error loading boss dashboard for user {}: {}", userDetails.getUsername(), e.getMessage(), e);
            model.addAttribute("error", "Error loading boss dashboard: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/reports")
    public String showReports(
            @RequestParam(required = false) String period,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            log.info("Boss reports page accessed by: {} with period: {}", userDetails.getUsername(), period);

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startDate = calculateStartDate(period, now);

            List<PresenceRecord> records = presenceService.getPresenceRecordsBetween(startDate, now);

            model.addAttribute("records", records);
            model.addAttribute("period", period != null ? period : "day");

            log.debug("Loaded {} presence records for period: {}", records.size(), period);
            return "boss/reports";

        } catch (Exception e) {
            log.error("Error loading boss reports: {}", e.getMessage(), e);
            model.addAttribute("error", "Error loading reports: " + e.getMessage());
            return "boss/dashboard";
        }
    }

    @GetMapping("/users")
    public String listUsers(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            log.info("Boss users page accessed by: {}", userDetails.getUsername());

            List<User> users = userService.getAllUsers();
            model.addAttribute("users", users);

            log.debug("Loaded {} users for boss view", users.size());
            return "boss/users";

        } catch (Exception e) {
            log.error("Error loading users list for boss: {}", e.getMessage(), e);
            model.addAttribute("error", "Error loading users: " + e.getMessage());
            return "boss/dashboard";
        }
    }

    private LocalDateTime calculateStartDate(String period, LocalDateTime now) {
        if (period == null) {
            period = "day";
        }

        return switch (period) {
            case "week" -> {
                log.debug("Calculating start date for week period");
                yield now.minusWeeks(1);
            }
            case "month" -> {
                log.debug("Calculating start date for month period");
                yield now.minusMonths(1);
            }
            default -> {
                log.debug("Calculating start date for day period (default)");
                yield now.withHour(0).withMinute(0).withSecond(0).withNano(0);
            }
        };
    }

    private List<PresenceRecord> getTodayPresenceRecords() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        return presenceService.getPresenceRecordsBetween(startOfDay, endOfDay);
    }
}