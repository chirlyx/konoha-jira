package com.konoha.jira.controller;

import com.konoha.jira.dto.MissionCreateRequest;
import com.konoha.jira.dto.MissionResponse;
import com.konoha.jira.dto.UserAnalyticsItem;
import com.konoha.jira.service.HokageService;
import com.konoha.jira.service.MissionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/hokage")
public class HokageController {

    private final MissionService missionService;
    private final HokageService hokageService;

    public HokageController(MissionService missionService, HokageService hokageService) {
        this.missionService = missionService;
        this.hokageService = hokageService;
    }

    @PostMapping("/missions")
    public ResponseEntity<MissionResponse> createMission(@Valid @RequestBody MissionCreateRequest request) {
        return ResponseEntity.ok(missionService.createMission(request));
    }

    @GetMapping("/missions/pending")
    public ResponseEntity<List<MissionResponse>> pending() {
        return ResponseEntity.ok(missionService.pendingReview());
    }

    @PutMapping("/missions/{id}/approve")
    public ResponseEntity<MissionResponse> approve(@PathVariable Long id) {
        return ResponseEntity.ok(missionService.resolveMission(id, true));
    }

    @PutMapping("/missions/{id}/fail")
    public ResponseEntity<MissionResponse> fail(@PathVariable Long id) {
        return ResponseEntity.ok(missionService.resolveMission(id, false));
    }

    @PutMapping("/missions/{id}/abort")
    public ResponseEntity<MissionResponse> abort(@PathVariable Long id) {
        return ResponseEntity.ok(missionService.abortMission(id));
    }

    @GetMapping("/analytics")
    public ResponseEntity<List<UserAnalyticsItem>> analytics() {
        return ResponseEntity.ok(hokageService.analytics());
    }

    @PutMapping("/ninja/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        hokageService.deactivateNinja(id);
        return ResponseEntity.noContent().build();
    }
}

