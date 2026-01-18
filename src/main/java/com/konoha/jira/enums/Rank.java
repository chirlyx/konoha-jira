package com.konoha.jira.enums;

public enum Rank {
    GENIN(0),
    CHUNIN(300),
    JONIN(1000),
    HOKAGE(Integer.MAX_VALUE);

    private final int minExperience;

    Rank(int minExperience) {
        this.minExperience = minExperience;
    }

    public int getMinExperience() {
        return minExperience;
    }

    public static Rank fromExperience(long experience) {
        if (experience >= JONIN.minExperience) {
            return JONIN;
        }
        if (experience >= CHUNIN.minExperience) {
            return CHUNIN;
        }
        return GENIN;
    }
}

