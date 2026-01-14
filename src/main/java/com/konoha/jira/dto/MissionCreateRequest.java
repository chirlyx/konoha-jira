package com.konoha.jira.dto;

import com.konoha.jira.enums.MissionRank;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MissionCreateRequest {
    @NotBlank
    private String title;

    private String description;

    @NotNull
    private MissionRank rank;

    private Integer rewardExperience;
}

