package com.waterball.LegendsOfTheThreeKingdoms.domain.handcard;

public enum PlayType {

    SKIP("skip"),
    ACTIVE("active"),
    INACTIVE("inactive");

    private final String playType;

    public String getPlayType() {
        return playType;
    }

    PlayType(String playType) {
        this.playType = playType;
    }

    public static void checkPlayTypeIsValid(String value) {
        for (PlayType type : PlayType.values()) {
            if (type.getPlayType().equals(value)) {
                return;
            }
        }
        throw new IllegalArgumentException(String.format("Play type [%s] not valid.", value));
    }
}
