package com.gaas.threeKingdoms.handcard;

import java.util.Arrays;

public enum EquipmentPlayType {

    ACTIVE("equipmentActive"),
    SKIP("equipmentSkip");

    private final String playType;

    public String getPlayType() {
        return playType;
    }

    EquipmentPlayType(String playType) {
        this.playType = playType;
    }

    public static EquipmentPlayType getPlayType(String playType) {
        return Arrays.stream(EquipmentPlayType.values()).filter(equipmentPlayType -> equipmentPlayType.getPlayType().equals(playType)).findFirst()
                .orElseThrow();
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
