package com.example.project3.repository;

import com.example.project3.model.Note;
import com.example.project3.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByUserOrderByUpdatedAtDesc(User user);
    List<Note> findByUser_IdOrderByUpdatedAtDesc(Long userId);
    long countByUser(User user);
}