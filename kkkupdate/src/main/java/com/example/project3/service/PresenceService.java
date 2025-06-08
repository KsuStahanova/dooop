package com.example.project3.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.project3.model.PresenceRecord;
import com.example.project3.model.User;
import com.example.project3.repository.PresenceRecordRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PresenceService {
    private final PresenceRecordRepository presenceRecordRepository;

    public PresenceService(PresenceRecordRepository presenceRecordRepository) {
        this.presenceRecordRepository = presenceRecordRepository;
    }

    @Transactional
    public PresenceRecord recordUserEntry(User user) {
        log.info("Recording entry for user: {}", user.getEmail());

        // Check if user already has an active session today
        if (hasActiveSession(user)) {
            log.warn("User {} already has an active session today", user.getEmail());
            LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);
            Optional<PresenceRecord> activeSession = presenceRecordRepository.findActiveSessionByUser(user, startOfDay, endOfDay);
            return activeSession.orElse(null);
        }

        PresenceRecord record = new PresenceRecord(user);
        PresenceRecord savedRecord = presenceRecordRepository.save(record);
        log.info("Entry recorded for user: {} at {}", user.getEmail(), savedRecord.getEntryTime());
        return savedRecord;
    }

    @Transactional
    public void recordUserExit(User user) {
        log.info("Recording exit for user: {}", user.getEmail());

        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        Optional<PresenceRecord> activeSessionOpt = presenceRecordRepository.findActiveSessionByUser(user, startOfDay, endOfDay);

        if (activeSessionOpt.isPresent()) {
            PresenceRecord activeSession = activeSessionOpt.get();
            activeSession.setExitTime(LocalDateTime.now());
            presenceRecordRepository.save(activeSession);
            log.info("Exit recorded for user: {} at {}", user.getEmail(), activeSession.getExitTime());
        } else {
            log.warn("No active session found for user: {}", user.getEmail());
        }
    }

    public boolean hasActiveSession(User user) {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        Optional<PresenceRecord> activeSession = presenceRecordRepository.findActiveSessionByUser(user, startOfDay, endOfDay);
        boolean hasActive = activeSession.isPresent();
        log.debug("User {} has active session: {}", user.getEmail(), hasActive);
        return hasActive;
    }

    public List<LocalDate> getUserPresenceDates(User user, LocalDate startDate, LocalDate endDate) {
        log.debug("Getting presence dates for user: {} between {} and {}",
                user.getEmail(), startDate, endDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        List<PresenceRecord> records = presenceRecordRepository.findByUserAndEntryTimeBetween(user, startDateTime, endDateTime);

        List<LocalDate> presenceDates = records.stream()
                .map(record -> record.getEntryTime().toLocalDate())
                .distinct()
                .sorted()
                .toList();

        log.debug("Found {} presence dates for user: {}", presenceDates.size(), user.getEmail());
        return presenceDates;
    }

    public List<PresenceRecord> getAllPresenceRecords() {
        log.debug("Fetching all presence records");
        return presenceRecordRepository.findAll();
    }

    public List<PresenceRecord> getPresenceRecordsBetween(LocalDateTime start, LocalDateTime end) {
        log.debug("Fetching presence records between {} and {}", start, end);
        return presenceRecordRepository.findByEntryTimeBetween(start, end);
    }

    public Optional<PresenceRecord> getPresenceRecordById(Long id) {
        log.debug("Fetching presence record by id: {}", id);
        return presenceRecordRepository.findById(id);
    }

    @Transactional
    public PresenceRecord updatePresenceRecord(PresenceRecord record) {
        log.info("Updating presence record with id: {}", record.getId());
        record.setManuallyEdited(true);
        PresenceRecord updatedRecord = presenceRecordRepository.save(record);
        log.info("Presence record updated successfully");
        return updatedRecord;
    }

    public List<PresenceRecord> getUserPresenceRecords(User user) {
        log.debug("Fetching presence records for user: {}", user.getEmail());
        return presenceRecordRepository.findByUser(user);
    }

    /**
     * Получить активные сессии (без времени выхода)
     */
    public List<PresenceRecord> getActiveSessions() {
        log.debug("Fetching all active sessions");
        return presenceRecordRepository.findByExitTimeIsNull();
    }

    /**
     * Получить активные сессии конкретного пользователя
     */
    public List<PresenceRecord> getUserActiveSessions(User user) {
        log.debug("Fetching active sessions for user: {}", user.getEmail());
        return presenceRecordRepository.findByUserAndExitTimeIsNull(user);
    }

    /**
     * Проверить, был ли пользователь сегодня в офисе
     */
    public boolean wasUserPresentToday(User user) {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        boolean wasPresent = presenceRecordRepository.existsByUserAndToday(user, startOfDay, endOfDay);
        log.debug("User {} was present today: {}", user.getEmail(), wasPresent);
        return wasPresent;
    }

    /**
     * Подсчитать количество дней присутствия пользователя за период
     */
    public long countUserPresenceDays(User user, LocalDateTime start, LocalDateTime end) {
        long count = presenceRecordRepository.countByUserAndEntryTimeBetween(user, start, end);
        log.debug("User {} was present {} days between {} and {}", user.getEmail(), count, start, end);
        return count;
    }

    /**
     * Получить границы текущего дня
     */
    private LocalDateTime[] getTodayBounds() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        return new LocalDateTime[]{startOfDay, endOfDay};
    }
}