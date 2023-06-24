package com.waterball.LegendsOfTheThreeKingdoms.domain;

public enum Role {
    MONARCH("Monarch"),
    MINISTER("Minister"),
    REBEL("Rebel"),
    TRAITOR("Traitor");

    private final String role;

    Role(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}