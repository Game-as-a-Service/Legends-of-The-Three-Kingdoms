package com.waterball.LegendsOfTheThreeKingdoms.domain.handcard;

public enum PlayType {

    SKIP("skip"),
    ACTIVE("active"),
    INACTIVE("inactive");

    private final String playType;

    PlayType(String playType) {
        this.playType = playType;
    }
}
