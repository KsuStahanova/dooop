package com.example.project3.controller;

import com.example.project3.model.User;
import com.example.project3.service.PresenceService;
import com.example.project3.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceApiController {

    private final PresenceService presenceService;
    private final UserService userService;

    @GetMapping("/calendar-data")
    public ResponseEntity<Map<String, Object>> getCalendarData(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "60") int days) {

        try {
            User user = userService.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(days);

            // Получаем даты присутствия
            List<LocalDate> presenceDates = presenceService.getUserPresenceDates(user, startDate, endDate);

            // Вычисляем пропущенные рабочие дни
            List<LocalDate> missedWorkdays = calculateMissedWorkdays(startDate, endDate, presenceDates);

            Map<String, Object> response = new HashMap<>();
            response.put("presenceDates", presenceDates.stream()
                    .map(date -> date.format(DateTimeFormatter.ISO_LOCAL_DATE))
                    .toList());
            response.put("missedWorkdays", missedWorkdays.stream()
                    .map(date -> date.format(DateTimeFormatter.ISO_LOCAL_DATE))
                    .toList());
            response.put("startDate", startDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
            response.put("endDate", endDate.format(DateTimeFormatter.ISO_LOCAL_DATE));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting calendar data: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error loading calendar data"));
        }
    }

    @PostMapping("/manual-exit")
    public ResponseEntity<Map<String, Object>> recordManualExit(
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            User user = userService.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (presenceService.hasActiveSession(user)) {
                presenceService.recordUserExit(user);
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Exit time recorded successfully"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "No active session found"
                ));
            }

        } catch (Exception e) {
            log.error("Error recording manual exit: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Error recording exit time"
            ));
        }
    }

    private List<LocalDate> calculateMissedWorkdays(LocalDate startDate, LocalDate endDate, List<LocalDate> presenceDates) {
        return startDate.datesUntil(endDate.plusDays(1))
                .filter(date -> isWorkday(date))
                .filter(date -> !presenceDates.contains(date))
                .filter(date -> date.isBefore(LocalDate.now()))
                .toList();
    }

    private boolean isWorkday(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }
}
