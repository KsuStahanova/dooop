package com.example.project3.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Имя пользователя обязательно")
    @Size(min = 3, max = 50, message = "Имя пользователя должно содержать от 3 до 50 символов")
    @Column(unique = true)
    private String username;

    @NotBlank(message = "Полное имя обязательно")
    @Size(min = 2, max = 100, message = "Полное имя должно содержать от 2 до 100 символов")
    private String fullName;

    @NotBlank(message = "Должность обязательна")
    @Size(min = 2, max = 100, message = "Должность должна содержать от 2 до 100 символов")
    private String position;

    @NotBlank(message = "Номер телефона обязателен")
    @Pattern(regexp = "^\\+?[1-9]\\d{9,14}$", message = "Неверный формат номера телефона (10-15 цифр, может начинаться с +)")
    private String phoneNumber;

    @NotBlank(message = "Email обязателен")
    @Email(message = "Неверный формат email")
    @Size(max = 100, message = "Email не должен превышать 100 символов")
    @Column(unique = true)
    private String email;

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 6, max = 100, message = "Пароль должен содержать от 6 до 100 символов")
    private String password;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Роль пользователя обязательна")
    private Role role;

    // Конструктор по умолчанию
    public User() {
        // Устанавливаем роль по умолчанию
        this.role = Role.USER;
    }

    // Конструктор с параметрами (опционально)
    public User(String username, String fullName, String position, String phoneNumber, String email, String password) {
        this.username = username;
        this.fullName = fullName;
        this.position = position;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.password = password;
        this.role = Role.USER;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                '}';
    }
}