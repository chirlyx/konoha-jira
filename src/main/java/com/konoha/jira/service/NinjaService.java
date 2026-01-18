package com.konoha.jira.service;

import com.konoha.jira.entity.Mission;
import com.konoha.jira.entity.Ninja;
import com.konoha.jira.enums.MissionStatus;
import com.konoha.jira.enums.Rank;
import com.konoha.jira.repository.MissionRepository;
import com.konoha.jira.repository.NinjaRepository;
import com.konoha.jira.dto.NinjaStatsResponse;
import com.konoha.jira.dto.ProfileResponse;
import org.springframework.transaction.annotation.Transactional;
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

    @Transactional(readOnly = true)
    public Ninja getCurrentNinja() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("No authenticated user");
        }
        return ninjaRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("User not found"));
    }

    @Transactional(readOnly = true)
    public ProfileResponse getCurrentNinjaProfile() {
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

    @Transactional(readOnly = true)
    public NinjaStatsResponse getMissionStats() {
        Ninja ninja = getCurrentNinja();
        List<Mission> missions = missionRepository.findByAssignee(ninja.getId());

        return NinjaStatsResponse.builder()
                .inProgress(countMissionsByStatus(MissionStatus.IN_PROGRESS, missions))
                .completed(countMissionsByStatus(MissionStatus.COMPLETED, missions))
                .failed(countMissionsByStatus(MissionStatus.FAILED, missions))
                .pending(countMissionsByStatus(MissionStatus.PENDING_REVIEW, missions))
                .aborted(countMissionsByStatus(MissionStatus.ABORTED, missions))
                .build();
    }

    private long countMissionsByStatus (MissionStatus status, List<Mission> missions) {
        return missions.stream().filter(m -> m.getStatus() == status).count();
    }
}

