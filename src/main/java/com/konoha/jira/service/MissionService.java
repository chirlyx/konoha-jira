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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class MissionService {

    private final MissionRepository missionRepository;
    private final NinjaService ninjaService;
    private final NinjaRepository ninjaRepository;

    public MissionService(MissionRepository missionRepository, NinjaService ninjaService, NinjaRepository ninjaRepository) {
        this.missionRepository = missionRepository;
        this.ninjaService = ninjaService;
        this.ninjaRepository = ninjaRepository;
    }

    @Transactional(readOnly = true)
    public List<MissionResponse> getAvailableMissions() {
        Ninja currentNinja = ninjaService.getCurrentNinja();
        Set<MissionRank> allowedMissionRanks = getAllowedMissionRanksByNinjaRank(currentNinja.getRank());
        return missionRepository.findAvailable(allowedMissionRanks).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MissionResponse> getMyMissions() {
        Ninja currentNinja = ninjaService.getCurrentNinja();
        return missionRepository.findMissionsByNinjaId(currentNinja.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    public MissionResponse assignMission(Long missionId) {
        Ninja currentNinja = ninjaService.getCurrentNinja();
        Set<MissionRank> allowedMissionRanks = getAllowedMissionRanksByNinjaRank(currentNinja.getRank());

        Mission mission = missionRepository.lockById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("Mission not found"));
        if (mission.getStatus() != MissionStatus.AVAILABLE) {
            throw new IllegalStateException("Mission already taken");
        }
        if (!allowedMissionRanks.contains(mission.getRank())) {
            throw new IllegalArgumentException("Rank too low for this mission");
        }
        mission.setAssignedTo(currentNinja);
        mission.setStatus(MissionStatus.IN_PROGRESS);
        missionRepository.save(mission);
        return toResponse(mission);
    }

    public MissionResponse submitForApproval(Long missionId) {
        Ninja currentNinja = ninjaService.getCurrentNinja();
        Mission mission = missionRepository.lockById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("Mission not found"));
        if (mission.getAssignedTo() == null || !mission.getAssignedTo().getId().equals(currentNinja.getId())) {
            throw new IllegalArgumentException("Cannot submit someone else's mission");
        }
        if (mission.getStatus() != MissionStatus.IN_PROGRESS) {
            throw new IllegalStateException("Mission not in progress");
        }
        mission.setStatus(MissionStatus.PENDING_REVIEW);
        missionRepository.save(mission);
        return toResponse(mission);
    }

    public MissionResponse createMission(MissionCreateRequest request) {
        int reward = calculateReward(request);

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

    private int calculateReward(MissionCreateRequest request) {
        return Optional.ofNullable(request.getRewardExperience())
                .orElse(request.getRank().getDefaultReward());
    }

    @Transactional(readOnly = true)
    public List<MissionResponse> findPendingMissions() {
        return missionRepository.findPendingMissions()
                .stream().map(this::toResponse).toList();
    }

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
                .assignedTo(getAssignedNinjaUsername(mission))
                .build();
    }

    private String getAssignedNinjaUsername(Mission mission) {
        return mission.getAssignedTo() != null
                ? mission.getAssignedTo().getUsername()
                : null;
    }

    private Set<MissionRank> getAllowedMissionRanksByNinjaRank(Rank rank) {
        return switch (rank) {
            case GENIN -> Set.of(MissionRank.D, MissionRank.C);
            case CHUNIN -> Set.of(MissionRank.D, MissionRank.C, MissionRank.B);
            case JONIN, HOKAGE -> Set.of(MissionRank.D, MissionRank.C, MissionRank.B, MissionRank.A, MissionRank.S);
        };
    }
}

