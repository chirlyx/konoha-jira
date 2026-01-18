package com.konoha.jira.dto;

import com.konoha.jira.enums.MissionRank;
import com.konoha.jira.enums.MissionStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MissionResponse {
    private Long id;
    private String title;
    private String description;
    private MissionRank rank;
    private MissionStatus status;
    private int rewardExperience;
    private String assignedTo;
}

