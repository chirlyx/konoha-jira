package com.konoha.jira.dto;

import com.konoha.jira.enums.Rank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserAnalyticsItem {
    private String username;
    private String village;
    private Rank rank;
    private long experience;
}

