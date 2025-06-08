package com.example.project3.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.project3.model.User;
import com.example.project3.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User API", description = "Operations related to users")
public class UserApiController {
    private final UserService userService;

    public UserApiController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BOSS')")
    @Operation(summary = "Get all users", security = @SecurityRequirement(name = "bearerAuth"))
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{email}")
    @Operation(summary = "Get user by email", security = @SecurityRequirement(name = "bearerAuth"))
    public User getUserByEmail(@PathVariable String email) {
        return userService.findByEmail(email).orElseThrow();
    }
}