package com.konoha.jira.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NinjaStatsResponse {
    private long inProgress;
    private long completed;
    private long failed;
    private long pending;
    private long aborted;
}

