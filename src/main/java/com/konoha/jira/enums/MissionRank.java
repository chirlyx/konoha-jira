package com.konoha.jira.enums;

public enum MissionRank {
    D(50),
    C(120),
    B(250),
    A(500),
    S(900);

    private final int defaultReward;

    MissionRank(int defaultReward) {
        this.defaultReward = defaultReward;
    }

    public int getDefaultReward() {
        return defaultReward;
    }
}

