package com.konoha.jira.service;

import com.konoha.jira.entity.Mission;
import com.konoha.jira.entity.Ninja;
import com.konoha.jira.enums.MissionStatus;
import com.konoha.jira.enums.Rank;
import com.konoha.jira.repository.MissionRepository;
import com.konoha.jira.repository.NinjaRepository;
import com.konoha.jira.dto.NinjaStatsResponse;
import com.konoha.jira.dto.ProfileResponse;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NinjaService {

    private final NinjaRepository ninjaRepository;
    private final MissionRepository missionRepository;

    public NinjaService(NinjaRepository ninjaRepository, MissionRepository missionRepository) {
        this.ninjaRepository = ninjaRepository;
        this.missionRepository = missionRepository;
    }

    public Ninja getCurrentNinja() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("No authenticated user");
        }
        return ninjaRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("User not found"));
    }

    public ProfileResponse profile() {
        Ninja ninja = getCurrentNinja();
        return ProfileResponse.builder()
                .username(ninja.getUsername())
                .firstName(ninja.getFirstName())
                .lastName(ninja.getLastName())
                .village(ninja.getVillage())
                .rank(ninja.getRank())
                .role(ninja.getRole())
                .experience(ninja.getExperience())
                .build();
    }

    @Transactional
    public void recalculateRank(Ninja ninja) {
        Rank recalculated = Rank.fromExperience(ninja.getExperience());
        if (recalculated != ninja.getRank()) {
            ninja.setRank(recalculated);
            ninjaRepository.save(ninja);
        }
    }

    public NinjaStatsResponse stats() {
        Ninja ninja = getCurrentNinja();
        List<Mission> missions = missionRepository.findByAssignee(ninja.getId());
        long inProgress = missions.stream().filter(m -> m.getStatus() == MissionStatus.IN_PROGRESS).count();
        long completed = missions.stream().filter(m -> m.getStatus() == MissionStatus.COMPLETED).count();
        long failed = missions.stream().filter(m -> m.getStatus() == MissionStatus.FAILED).count();
        long pending = missions.stream().filter(m -> m.getStatus() == MissionStatus.PENDING_REVIEW).count();
        long aborted = missions.stream().filter(m -> m.getStatus() == MissionStatus.ABORTED).count();
        return NinjaStatsResponse.builder()
                .inProgress(inProgress)
                .completed(completed)
                .failed(failed)
                .pending(pending)
                .aborted(aborted)
                .build();
    }
}

