package com.konoha.jira.service;

import com.konoha.jira.entity.Mission;
import com.konoha.jira.entity.Ninja;
import com.konoha.jira.enums.MissionRank;
import com.konoha.jira.enums.MissionStatus;
import com.konoha.jira.enums.Rank;
import com.konoha.jira.dto.MissionCreateRequest;
import com.konoha.jira.dto.MissionResponse;
import com.konoha.jira.repository.MissionRepository;
import com.konoha.jira.repository.NinjaRepository;
import com.konoha.jira.service.NinjaService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class MissionService {

    private final MissionRepository missionRepository;
    private final NinjaService ninjaService;
    private final NinjaRepository ninjaRepository;

    public MissionService(MissionRepository missionRepository, NinjaService ninjaService, NinjaRepository ninjaRepository) {
        this.missionRepository = missionRepository;
        this.ninjaService = ninjaService;
        this.ninjaRepository = ninjaRepository;
    }

    public List<MissionResponse> availableMissions() {
        Ninja current = ninjaService.getCurrentNinja();
        Set<MissionRank> allowed = allowedRanks(current.getRank());
        return missionRepository.findAvailable(allowed).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<MissionResponse> myMissions() {
        Ninja current = ninjaService.getCurrentNinja();
        return missionRepository.findByAssignee(current.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public MissionResponse assignMission(Long missionId) {
        Ninja current = ninjaService.getCurrentNinja();
        Set<MissionRank> allowed = allowedRanks(current.getRank());

        Mission mission = missionRepository.lockById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("Mission not found"));
        if (mission.getStatus() != MissionStatus.AVAILABLE) {
            throw new IllegalStateException("Mission already taken");
        }
        if (!allowed.contains(mission.getRank())) {
            throw new IllegalArgumentException("Rank too low for this mission");
        }
        mission.setAssignedTo(current);
        mission.setStatus(MissionStatus.IN_PROGRESS);
        missionRepository.save(mission);
        return toResponse(mission);
    }

    @Transactional
    public MissionResponse submitForApproval(Long missionId) {
        Ninja current = ninjaService.getCurrentNinja();
        Mission mission = missionRepository.lockById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("Mission not found"));
        if (mission.getAssignedTo() == null || !mission.getAssignedTo().getId().equals(current.getId())) {
            throw new IllegalArgumentException("Cannot submit someone else's mission");
        }
        if (mission.getStatus() != MissionStatus.IN_PROGRESS) {
            throw new IllegalStateException("Mission not in progress");
        }
        mission.setStatus(MissionStatus.PENDING_REVIEW);
        missionRepository.save(mission);
        return toResponse(mission);
    }

    @Transactional
    public MissionResponse createMission(MissionCreateRequest request) {
        int reward = Optional.ofNullable(request.getRewardExperience())
                .orElse(request.getRank().getDefaultReward());
        Mission mission = Mission.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .rank(request.getRank())
                .rewardExperience(reward)
                .status(MissionStatus.AVAILABLE)
                .build();
        missionRepository.save(mission);
        return toResponse(mission);
    }

    public List<MissionResponse> pendingReview() {
        return missionRepository.findPendingReview()
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public MissionResponse resolveMission(Long missionId, boolean success) {
        Mission mission = missionRepository.lockById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("Mission not found"));
        if (mission.getStatus() != MissionStatus.PENDING_REVIEW) {
            throw new IllegalStateException("Mission not waiting for approval");
        }
        mission.setStatus(success ? MissionStatus.COMPLETED : MissionStatus.FAILED);
        if (success && mission.getAssignedTo() != null) {
            Ninja assignee = ninjaRepository.findById(mission.getAssignedTo().getId())
                    .orElseThrow(() -> new IllegalStateException("Assignee missing"));
            assignee.addExperience(mission.getRewardExperience());
            ninjaRepository.save(assignee);
        }
        missionRepository.save(mission);
        return toResponse(mission);
    }

    @Transactional
    public MissionResponse abortMission(Long missionId) {
        Mission mission = missionRepository.lockById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("Mission not found"));
        mission.setStatus(MissionStatus.ABORTED);
        missionRepository.save(mission);
        return toResponse(mission);
    }

    private MissionResponse toResponse(Mission mission) {
        return MissionResponse.builder()
                .id(mission.getId())
                .title(mission.getTitle())
                .description(mission.getDescription())
                .rank(mission.getRank())
                .status(mission.getStatus())
                .rewardExperience(mission.getRewardExperience())
                .assignedTo(mission.getAssignedTo() != null ? mission.getAssignedTo().getUsername() : null)
                .build();
    }

    private Set<MissionRank> allowedRanks(Rank rank) {
        return switch (rank) {
            case GENIN -> Set.of(MissionRank.D, MissionRank.C);
            case CHUNIN -> Set.of(MissionRank.D, MissionRank.C, MissionRank.B);
            case JONIN, HOKAGE -> Set.of(MissionRank.D, MissionRank.C, MissionRank.B, MissionRank.A, MissionRank.S);
        };
    }
}

