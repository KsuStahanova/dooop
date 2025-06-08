package com.example.project3.controller;

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class RoleRegistrationController {

    private final UserService userService;

    // ====== BOSS REGISTRATION ======

    @GetMapping("/register/boss")
    public String showBossRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register-boss";
    }

    @PostMapping("/register/boss")
    public String registerBoss(@Valid @ModelAttribute("user") User user,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        log.info("Boss registration attempt for email: {}", user.getEmail());

        if (result.hasErrors()) {
            log.warn("Boss registration validation errors: {}", result.getAllErrors());
            return "register-boss";
        }

        if (userService.emailExists(user.getEmail())) {
            result.rejectValue("email", "error.user", "This email is already in use");
            return "register-boss";
        }

        if (userService.usernameExists(user.getUsername())) {
            result.rejectValue("username", "error.user", "This username is already taken");
            return "register-boss";
        }

        try {
            User savedUser = userService.registerBoss(user);
            log.info("Boss registered successfully: {}", savedUser.getEmail());

            redirectAttributes.addFlashAttribute("success",
                    "Boss account created successfully! You can now sign in with executive privileges.");
            return "redirect:/login?registered";

        } catch (Exception e) {
            log.error("Error during boss registration: {}", e.getMessage(), e);
            model.addAttribute("error", "An error occurred during registration. Please try again.");
            return "register-boss";
        }
    }

    // ====== ADMIN REGISTRATION ======

    @GetMapping("/register/admin")
    public String showAdminRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register-admin";
    }

    @PostMapping("/register/admin")
    public String registerAdmin(@Valid @ModelAttribute("user") User user,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        log.info("Admin registration attempt for email: {}", user.getEmail());

        if (result.hasErrors()) {
            log.warn("Admin registration validation errors: {}", result.getAllErrors());
            return "register-admin";
        }

        if (userService.emailExists(user.getEmail())) {
            result.rejectValue("email", "error.user", "This email is already in use");
            return "register-admin";
        }

        if (userService.usernameExists(user.getUsername())) {
            result.rejectValue("username", "error.user", "This username is already taken");
            return "register-admin";
        }

        try {
            User savedUser = userService.registerAdmin(user);
            log.info("Admin registered successfully: {}", savedUser.getEmail());

            redirectAttributes.addFlashAttribute("success",
                    "Administrator account created successfully! You now have full system access.");
            return "redirect:/login?registered";

        } catch (Exception e) {
            log.error("Error during admin registration: {}", e.getMessage(), e);
            model.addAttribute("error", "An error occurred during registration. Please try again.");
            return "register-admin";
        }
    }
}