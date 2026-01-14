package com.konoha.jira.dto;

import com.konoha.jira.enums.Rank;
import com.konoha.jira.enums.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileResponse {
    private String username;
    private String firstName;
    private String lastName;
    private String village;
    private Rank rank;
    private Role role;
    private long experience;
}

