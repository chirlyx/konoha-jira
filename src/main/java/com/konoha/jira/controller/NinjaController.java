package com.konoha.jira.controller;

import com.konoha.jira.dto.MissionResponse;
import com.konoha.jira.dto.NinjaStatsResponse;
import com.konoha.jira.dto.ProfileResponse;
import com.konoha.jira.service.NinjaService;
import com.konoha.jira.service.MissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class NinjaController {

    private final NinjaService ninjaService;
    private final MissionService missionService;

    public NinjaController(NinjaService ninjaService, MissionService missionService) {
        this.ninjaService = ninjaService;
        this.missionService = missionService;
    }

    @GetMapping("/ninja/profile")
    public ResponseEntity<ProfileResponse> profile() {
        return ResponseEntity.ok(ninjaService.getCurrentNinjaProfile());
    }

    @GetMapping("/ninja/missions")
    public ResponseEntity<List<MissionResponse>> myMissions() {
        return ResponseEntity.ok(missionService.myMissions());
    }

    @GetMapping("/missions/available")
    public ResponseEntity<List<MissionResponse>> available() {
        return ResponseEntity.ok(missionService.availableMissions());
    }

    @PutMapping("/missions/{id}/assign")
    public ResponseEntity<MissionResponse> assign(@PathVariable Long id) {
        return ResponseEntity.ok(missionService.assignMission(id));
    }

    @PutMapping("/missions/{id}/complete")
    public ResponseEntity<MissionResponse> complete(@PathVariable Long id) {
        return ResponseEntity.ok(missionService.submitForApproval(id));
    }

    @GetMapping("/ninja/stats")
    public ResponseEntity<NinjaStatsResponse> stats() {
        return ResponseEntity.ok(ninjaService.getMissionStats());
    }
}

