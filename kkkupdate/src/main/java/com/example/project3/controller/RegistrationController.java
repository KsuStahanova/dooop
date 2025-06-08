package com.example.project3.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.project3.model.User;
import com.example.project3.service.UserService;

import jakarta.validation.Valid;

@Controller
public class RegistrationController {
    private final UserService userService;

    public RegistrationController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            System.out.println("Validation errors found:");
            result.getAllErrors().forEach(error ->
                    System.out.println("Error: " + error.getDefaultMessage())
            );
            return "register";
        }

        if (userService.emailExists(user.getEmail())) {
            result.rejectValue("email", "error.user", "Этот email уже используется");
            return "register";
        }

        if (userService.usernameExists(user.getUsername())) {
            result.rejectValue("username", "error.user", "Это имя пользователя уже занято");
            return "register";
        }

        try {
            User savedUser = userService.registerUser(user);
            System.out.println("User registered successfully: " + savedUser.getEmail());

            redirectAttributes.addFlashAttribute("success", "Регистрация прошла успешно! Теперь вы можете войти в систему.");
            return "redirect:/login?registered";

        } catch (Exception e) {
            System.err.println("Error during user registration: " + e.getMessage());
            model.addAttribute("error", "Произошла ошибка при регистрации. Попробуйте еще раз.");
            return "register";
        }
    }
}