package com.example.project3.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.project3.model.Role;
import com.example.project3.model.User;
import com.example.project3.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User registerUser(User user) {
        logger.info("Attempting to register user with email: {}", user.getEmail());

        try {
            // Проверяем, что пользователь не null
            if (user == null) {
                throw new IllegalArgumentException("User cannot be null");
            }

            // Логируем входные данные (без пароля)
            logger.debug("User data: username={}, email={}, fullName={}, position={}, phone={}",
                    user.getUsername(), user.getEmail(), user.getFullName(),
                    user.getPosition(), user.getPhoneNumber());

            // Проверяем уникальность email
            if (emailExists(user.getEmail())) {
                logger.warn("Registration failed: email {} already exists", user.getEmail());
                throw new IllegalArgumentException("Email already exists: " + user.getEmail());
            }

            // Проверяем уникальность username
            if (usernameExists(user.getUsername())) {
                logger.warn("Registration failed: username {} already exists", user.getUsername());
                throw new IllegalArgumentException("Username already exists: " + user.getUsername());
            }

            // Кодируем пароль
            String originalPassword = user.getPassword();
            if (originalPassword == null || originalPassword.trim().isEmpty()) {
                throw new IllegalArgumentException("Password cannot be empty");
            }

            user.setPassword(passwordEncoder.encode(originalPassword));
            user.setRole(Role.USER);

            // Сохраняем пользователя
            User savedUser = userRepository.save(user);
            logger.info("User registered successfully with ID: {}", savedUser.getId());

            return savedUser;

        } catch (Exception e) {
            logger.error("Error during user registration: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public User registerAdmin(User admin) {
        logger.info("Attempting to register admin with email: {}", admin.getEmail());

        // Проверяем уникальность email
        if (emailExists(admin.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + admin.getEmail());
        }

        // Проверяем уникальность username
        if (usernameExists(admin.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + admin.getUsername());
        }

        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        admin.setRole(Role.ADMIN);
        User savedAdmin = userRepository.save(admin);
        logger.info("Admin registered successfully with ID: {}", savedAdmin.getId());
        return savedAdmin;
    }

    @Transactional
    public User registerBoss(User boss) {
        logger.info("Attempting to register boss with email: {}", boss.getEmail());

        // Проверяем уникальность email
        if (emailExists(boss.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + boss.getEmail());
        }

        // Проверяем уникальность username
        if (usernameExists(boss.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + boss.getUsername());
        }

        boss.setPassword(passwordEncoder.encode(boss.getPassword()));
        boss.setRole(Role.BOSS);
        User savedBoss = userRepository.save(boss);
        logger.info("Boss registered successfully with ID: {}", savedBoss.getId());
        return savedBoss;
    }

    @Transactional
    public User updateUser(User user) {
        logger.info("Attempting to update user with ID: {}", user.getId());

        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            logger.debug("Password updated for user ID: {}", user.getId());
        }

        User updatedUser = userRepository.save(user);
        logger.info("User updated successfully with ID: {}", updatedUser.getId());
        return updatedUser;
    }

    public List<User> getAllUsers() {
        logger.debug("Fetching all users");
        List<User> users = userRepository.findAll();
        logger.debug("Found {} users", users.size());
        return users;
    }

    public Optional<User> findByEmail(String email) {
        logger.debug("Searching for user by email: {}", email);
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            logger.debug("User found by email: {}", email);
        } else {
            logger.debug("No user found with email: {}", email);
        }
        return user;
    }

    public boolean emailExists(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        boolean exists = userRepository.findByEmail(email.trim().toLowerCase()).isPresent();
        logger.debug("Email {} exists: {}", email, exists);
        return exists;
    }

    public boolean usernameExists(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        boolean exists = userRepository.findByUsername(username.trim()).isPresent();
        logger.debug("Username {} exists: {}", username, exists);
        return exists;
    }

    public Optional<User> findByUsername(String username) {
        logger.debug("Searching for user by username: {}", username);
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            logger.debug("User found by username: {}", username);
        } else {
            logger.debug("No user found with username: {}", username);
        }
        return user;
    }
}