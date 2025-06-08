package com.example.project3.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.project3.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username); // This now matches the entity field
}