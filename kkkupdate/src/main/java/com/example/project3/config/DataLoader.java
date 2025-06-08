package com.example.project3.config;

import com.example.project3.model.Role;
import com.example.project3.model.User;
import com.example.project3.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataLoader {

    private final UserService userService;

    @Bean
    public ApplicationRunner initializeData() {
        return args -> {
            createDefaultUsers();
        };
    }

    private void createDefaultUsers() {
        log.info("Checking and creating default users...");

        // Создаем админа
        if (!userService.emailExists("admin@office.com")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setFullName("System Administrator");
            admin.setEmail("admin@office.com");
            admin.setPassword("admin123");
            admin.setPosition("System Administrator");
            admin.setPhoneNumber("+1234567890");
            admin.setRole(Role.ADMIN);

            try {
                userService.registerAdmin(admin);
                log.info("Default admin created: admin@office.com / admin123");
            } catch (Exception e) {
                log.error("Failed to create default admin: {}", e.getMessage());
            }
        }

        // Создаем босса
        if (!userService.emailExists("boss@office.com")) {
            User boss = new User();
            boss.setUsername("boss");
            boss.setFullName("CEO Boss");
            boss.setEmail("boss@office.com");
            boss.setPassword("boss123");
            boss.setPosition("Chief Executive Officer");
            boss.setPhoneNumber("+1234567891");
            boss.setRole(Role.BOSS);

            try {
                userService.registerBoss(boss);
                log.info("Default boss created: boss@office.com / boss123");
            } catch (Exception e) {
                log.error("Failed to create default boss: {}", e.getMessage());
            }
        }

        // Создать обычного пользователя
        if (!userService.emailExists("user@office.com")) {
            User user = new User();
            user.setUsername("user");
            user.setFullName("Regular User");
            user.setEmail("user@office.com");
            user.setPassword("user123");
            user.setPosition("Software Developer");
            user.setPhoneNumber("+1234567893");
            user.setRole(Role.USER);

            try {
                userService.registerUser(user);
                log.info("Default user created: user@office.com / user123");
            } catch (Exception e) {
                log.error("Failed to create default user: {}", e.getMessage());
            }
        }

        log.info("Default users initialization completed.");
    }
}