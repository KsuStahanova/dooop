package com.example.project3.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import com.example.project3.model.User;
import com.example.project3.service.PresenceService;
import com.example.project3.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Component
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    private final PresenceService presenceService;
    private final UserService userService;

    // Используем @Lazy для разрыва циклической зависимости
    @Autowired
    public CustomLogoutSuccessHandler(PresenceService presenceService, @Lazy UserService userService) {
        this.presenceService = presenceService;
        this.userService = userService;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                Authentication authentication) throws IOException {

        if (authentication != null && authentication.getName() != null) {
            String email = authentication.getName();
            log.info("User logout: {}", email);

            try {
                Optional<User> userOpt = userService.findByEmail(email);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();

                    // Записываем время выхода, если есть активная сессия
                    if (presenceService.hasActiveSession(user)) {
                        presenceService.recordUserExit(user);
                        log.info("Exit time recorded for user: {}", email);
                    }
                }
            } catch (Exception e) {
                log.error("Error recording exit time for user {}: {}", email, e.getMessage());
            }
        }

        // Перенаправляем на страницу логина с сообщением
        response.sendRedirect("/login?logout");
    }
}