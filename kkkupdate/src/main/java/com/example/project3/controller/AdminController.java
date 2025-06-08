package com.example.project3.controller;

import java.time.LocalDateTime;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.project3.model.PresenceRecord;
import com.example.project3.model.User;
import com.example.project3.service.PresenceService;
import com.example.project3.service.UserService;

@Slf4j
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAnyRole('ADMIN', 'BOSS')")
public class AdminController {

    private final UserService userService;
    private final PresenceService presenceService;

    public AdminController(UserService userService, PresenceService presenceService) {
        this.userService = userService;
        this.presenceService = presenceService;
    }

    @GetMapping
    public String adminDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        try {
            log.info("Admin/Boss dashboard accessed by: {}", userDetails.getUsername());

            String email = userDetails.getUsername();
            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found: " + email));

            log.debug("User found: {}", user.getFullName());

            // Определяем тип пользователя для перенаправления
            if (user.getRole().name().equals("BOSS")) {
                return "redirect:/boss";
            }

            model.addAttribute("admin", user);

            // Добавляем статистику
            List<User> allUsers = userService.getAllUsers();
            List<PresenceRecord> todayRecords = getTodayPresenceRecords();

            model.addAttribute("totalUsers", allUsers.size());
            model.addAttribute("todayCheckIns", todayRecords.size());

            log.info("Admin dashboard loaded successfully for: {}", user.getEmail());
            return "admin/dashboard";

        } catch (Exception e) {
            log.error("Error loading admin dashboard for user {}: {}", userDetails.getUsername(), e.getMessage(), e);
            model.addAttribute("error", "Error loading admin dashboard: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/users")
    public String listUsers(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            log.info("Admin/Boss users page accessed by: {}", userDetails.getUsername());

            String email = userDetails.getUsername();
            User user = userService.findByEmail(email).orElseThrow();

            // Перенаправляем Boss на его собственную страницу
            if (user.getRole().name().equals("BOSS")) {
                return "redirect:/boss/users";
            }

            List<User> users = userService.getAllUsers();
            model.addAttribute("users", users);

            log.debug("Loaded {} users for admin view", users.size());
            return "admin/users";

        } catch (Exception e) {
            log.error("Error loading users list: {}", e.getMessage(), e);
            model.addAttribute("error", "Error loading users: " + e.getMessage());
            return "admin/dashboard";
        }
    }

    @GetMapping("/reports")
    public String showReports(@RequestParam(required = false) String period, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            log.info("Admin/Boss reports page accessed by: {} with period: {}", userDetails.getUsername(), period);

            String email = userDetails.getUsername();
            User user = userService.findByEmail(email).orElseThrow();

            // Перенаправляем Boss на его собственную страницу
            if (user.getRole().name().equals("BOSS")) {
                return "redirect:/boss/reports" + (period != null ? "?period=" + period : "");
            }

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startDate = calculateStartDate(period, now);

            List<PresenceRecord> records = presenceService.getPresenceRecordsBetween(startDate, now);

            model.addAttribute("records", records);
            model.addAttribute("period", period != null ? period : "day");

            log.debug("Loaded {} presence records for period: {}", records.size(), period);
            return "admin/reports";

        } catch (Exception e) {
            log.error("Error loading reports: {}", e.getMessage(), e);
            model.addAttribute("error", "Error loading reports: " + e.getMessage());
            return "admin/dashboard";
        }
    }

    @PostMapping("/reports/update/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Только ADMIN может редактировать
    public String updateRecord(@PathVariable Long id, @RequestParam String entryTime, @RequestParam(required = false) String exitTime, RedirectAttributes redirectAttributes, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            log.info("Updating presence record {} by admin: {}", id, userDetails.getUsername());

            PresenceRecord record = presenceService.getPresenceRecordById(id)
                    .orElseThrow(() -> new RuntimeException("Presence record not found with id: " + id));

            // Валидация и установка времени входа
            if (entryTime != null && !entryTime.trim().isEmpty()) {
                try {
                    LocalDateTime entryDateTime = LocalDateTime.parse(entryTime);
                    record.setEntryTime(entryDateTime);
                    log.debug("Entry time updated for record {}: {}", id, entryDateTime);
                } catch (Exception e) {
                    throw new RuntimeException("Invalid entry time format: " + entryTime);
                }
            } else {
                throw new RuntimeException("Entry time is required");
            }

            // Установка времени выхода (опционально)
            if (exitTime != null && !exitTime.trim().isEmpty()) {
                LocalDateTime exitDateTime = LocalDateTime.parse(exitTime);

                // Проверяем, что время выхода после времени входа
                if (exitDateTime.isBefore(record.getEntryTime())) {
                    throw new RuntimeException("Exit time cannot be before entry time");
                }

                record.setExitTime(exitDateTime);
                log.debug("Exit time updated for record {}: {}", id, exitDateTime);
            } else {
                record.setExitTime(null);
                log.debug("Exit time cleared for record {}", id);
            }

            // Помечаем запись как отредактированную вручную
            record.setManuallyEdited(true);

            presenceService.updatePresenceRecord(record);

            redirectAttributes.addFlashAttribute("success",
                    "Record updated successfully for user: " + record.getUser().getFullName());

            log.info("Presence record {} updated successfully by admin: {}", id, userDetails.getUsername());

        } catch (Exception e) {
            log.error("Error updating presence record {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error updating record: " + e.getMessage());
        }

        return "redirect:/admin/reports";
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