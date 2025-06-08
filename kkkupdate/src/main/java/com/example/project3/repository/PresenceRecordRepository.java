package com.example.project3.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.project3.model.PresenceRecord;
import com.example.project3.model.User;

@Repository
public interface PresenceRecordRepository extends JpaRepository<PresenceRecord, Long> {

    // Существующие методы
    List<PresenceRecord> findByUser_Id(Long userId);
    List<PresenceRecord> findByEntryTimeBetween(LocalDateTime start, LocalDateTime end);

    // Новые методы, которые нужны для PresenceService

    /**
     * Найти все записи присутствия для конкретного пользователя
     */
    List<PresenceRecord> findByUser(User user);

    /**
     * Найти все записи присутствия для пользователя, отсортированные по времени входа
     */
    List<PresenceRecord> findByUserOrderByEntryTimeDesc(User user);

    /**
     * Найти активную сессию пользователя (без времени выхода) за сегодня
     */
    @Query("SELECT p FROM PresenceRecord p WHERE p.user = :user AND p.exitTime IS NULL AND p.entryTime >= :startOfDay AND p.entryTime < :endOfDay")
    Optional<PresenceRecord> findActiveSessionByUser(@Param("user") User user, @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    /**
     * Найти все записи пользователя за определенный период
     */
    @Query("SELECT p FROM PresenceRecord p WHERE p.user = :user AND p.entryTime BETWEEN :start AND :end ORDER BY p.entryTime DESC")
    List<PresenceRecord> findByUserAndEntryTimeBetween(@Param("user") User user, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Найти последнюю запись пользователя
     */
    Optional<PresenceRecord> findTopByUserOrderByEntryTimeDesc(User user);

    /**
     * Проверить, есть ли у пользователя запись за сегодня
     */
    @Query("SELECT COUNT(p) > 0 FROM PresenceRecord p WHERE p.user = :user AND p.entryTime >= :startOfDay AND p.entryTime < :endOfDay")
    boolean existsByUserAndToday(@Param("user") User user, @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    /**
     * Найти все записи с пустым временем выхода (активные сессии)
     */
    List<PresenceRecord> findByExitTimeIsNull();

    /**
     * Найти активные сессии конкретного пользователя
     */
    List<PresenceRecord> findByUserAndExitTimeIsNull(User user);

    /**
     * Подсчитать количество записей пользователя за период
     */
    @Query("SELECT COUNT(p) FROM PresenceRecord p WHERE p.user = :user AND p.entryTime BETWEEN :start AND :end")
    long countByUserAndEntryTimeBetween(@Param("user") User user, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}