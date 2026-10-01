package com.dxr.health.model;

public enum TriagePriority {
    CRITICAL(1),
    HIGH(2),
    ROUTINE(3);

    private final int rank;

    TriagePriority(int rank) {
        this.rank = rank;
    }

    public int getRank() {
        return rank;
    }
}
