package com.example.project3.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.project3.model.PresenceRecord;
import com.example.project3.service.PresenceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/presence")
@Tag(name = "Presence API", description = "Operations related to presence records")
public class PresenceApiController {
    private final PresenceService presenceService;

    public PresenceApiController(PresenceService presenceService) {
        this.presenceService = presenceService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BOSS')")
    @Operation(summary = "Get all presence records", security = @SecurityRequirement(name = "bearerAuth"))
    public List<PresenceRecord> getAllRecords() {
        return presenceService.getAllPresenceRecords();
    }

    @GetMapping("/period")
    @PreAuthorize("hasAnyRole('ADMIN', 'BOSS')")
    @Operation(summary = "Get presence records between dates", security = @SecurityRequirement(name = "bearerAuth"))
    public List<PresenceRecord> getRecordsBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return presenceService.getPresenceRecordsBetween(start, end);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Только ADMIN может редактировать через API
    @Operation(summary = "Update presence record", security = @SecurityRequirement(name = "bearerAuth"))
    public PresenceRecord updateRecord(@PathVariable Long id, @RequestBody PresenceRecord updatedRecord) {
        return presenceService.updatePresenceRecord(updatedRecord);
    }
}