package com.example.project3.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.project3.model.Role;
import com.example.project3.model.User;
import com.example.project3.service.UserService;

@Controller
@RequestMapping("/colleagues")
public class ColleaguesController {
    private final UserService userService;

    public ColleaguesController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String showColleagues(Model model, Authentication authentication) {
        List<User> allUsers = userService.getAllUsers();

        // Получаем текущего пользователя
        String currentUserEmail = authentication.getName();
        User currentUser = userService.findByEmail(currentUserEmail).orElse(null);

        List<User> visibleColleagues;

        if (currentUser != null && (currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.BOSS)) {
            // Админы и Boss видят всех пользователей
            visibleColleagues = allUsers;
        } else {
            // Обычные пользователи не видят админов и босса
            visibleColleagues = allUsers.stream()
                    .filter(user -> user.getRole() != Role.ADMIN && user.getRole() != Role.BOSS)
                    .collect(Collectors.toList());
        }

        model.addAttribute("colleagues", visibleColleagues);
        model.addAttribute("isAdmin", currentUser != null &&
                (currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.BOSS));

        return "colleagues";
    }
}