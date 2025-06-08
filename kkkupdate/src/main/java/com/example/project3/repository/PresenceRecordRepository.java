package com.example.project3.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.project3.model.PresenceRecord;

@Repository
public interface PresenceRecordRepository extends JpaRepository<PresenceRecord, Long> {
    List<PresenceRecord> findByUser_Id(Long userId);
    List<PresenceRecord> findByEntryTimeBetween(LocalDateTime start, LocalDateTime end);
}