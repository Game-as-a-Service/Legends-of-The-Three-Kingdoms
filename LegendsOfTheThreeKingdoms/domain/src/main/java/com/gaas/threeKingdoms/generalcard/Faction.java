package com.gaas.threeKingdoms.generalcard;

public enum Faction {
    SHU("蜀"),
    WEI("魏"),
    WU("吳"),
    QUN("群");

    private final String displayName;

    Faction(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
