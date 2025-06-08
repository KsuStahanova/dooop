package com.example.project3.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.project3.model.PresenceRecord;
import com.example.project3.model.User;
import com.example.project3.repository.PresenceRecordRepository;

@Service
public class PresenceService {
    private final PresenceRecordRepository presenceRecordRepository;

    public PresenceService(PresenceRecordRepository presenceRecordRepository) {
        this.presenceRecordRepository = presenceRecordRepository;
    }

    public PresenceRecord recordUserEntry(User user) {
        PresenceRecord record = new PresenceRecord(user);
        return presenceRecordRepository.save(record);
    }

    public List<PresenceRecord> getAllPresenceRecords() {
        return presenceRecordRepository.findAll();
    }

    public List<PresenceRecord> getPresenceRecordsBetween(LocalDateTime start, LocalDateTime end) {
        return presenceRecordRepository.findByEntryTimeBetween(start, end);
    }

    public Optional<PresenceRecord> getPresenceRecordById(Long id) {
        return presenceRecordRepository.findById(id);
    }

    public PresenceRecord updatePresenceRecord(PresenceRecord record) {
        record.setManuallyEdited(true);
        return presenceRecordRepository.save(record);
    }
}