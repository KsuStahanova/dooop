package com.example.project3.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.project3.model.User;
import com.example.project3.service.UserService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/profile")
public class ProfileController {
    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String showProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String email = userDetails.getUsername();
        User user = userService.findByEmail(email).orElseThrow();

        // Чтобы не отображать текущий пароль в форме
        user.setPassword("");

        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping
    public String updateProfile(@Valid @ModelAttribute("user") User updatedUser,
                                BindingResult result,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "profile";
        }

        String email = userDetails.getUsername();
        User existingUser = userService.findByEmail(email).orElseThrow();

        // Обновление только разрешенных полей (сохраняем email и роль текущие)
        existingUser.setFullName(updatedUser.getFullName());
        existingUser.setPosition(updatedUser.getPosition());
        existingUser.setPhoneNumber(updatedUser.getPhoneNumber());

        // Обновляем пароль только если был предоставлен новый
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            existingUser.setPassword(updatedUser.getPassword());
        }

        userService.updateUser(existingUser);
        redirectAttributes.addFlashAttribute("success", "Профиль успешно обновлен");
        return "redirect:/profile";
    }
}