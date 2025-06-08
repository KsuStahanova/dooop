package com.example.project3.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, LogoutSuccessHandler customLogoutSuccessHandler) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Отключаем CSRF для API
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/home", "/login", "/register", "/register/boss", "/register/admin",
                                "/error", "/test", "/css/**", "/js/**", "/images/**",
                                "/swagger-ui/**", "/api-docs/**").permitAll()
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        .requestMatchers("/api/presence/**").hasRole("ADMIN")
                        .requestMatchers("/api/attendance/**").hasAnyRole("USER", "ADMIN", "BOSS")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/boss/**").hasRole("BOSS")
                        .requestMatchers("/notes/**").hasAnyRole("USER", "ADMIN", "BOSS")
                        .requestMatchers("/dashboard").hasAnyRole("USER", "ADMIN", "BOSS")
                        .requestMatchers("/profile").hasAnyRole("USER", "ADMIN", "BOSS")
                        .requestMatchers("/colleagues").hasAnyRole("USER", "ADMIN", "BOSS")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard")
                        .successHandler((request, response, authentication) -> {
                            // Перенаправление в зависимости от роли
                            String role = authentication.getAuthorities().iterator().next().getAuthority();
                            switch (role) {
                                case "ROLE_ADMIN":
                                    response.sendRedirect("/admin");
                                    break;
                                case "ROLE_BOSS":
                                    response.sendRedirect("/boss");
                                    break;
                                case "ROLE_MANAGER":
                                    response.sendRedirect("/dashboard");
                                    break;
                                default:
                                    response.sendRedirect("/dashboard");
                                    break;
                            }
                        })
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessHandler(customLogoutSuccessHandler)
                        .permitAll()
                )
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("frame-ancestors 'self'")
                        )
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}