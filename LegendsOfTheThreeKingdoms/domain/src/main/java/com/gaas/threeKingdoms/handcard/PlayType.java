package com.gaas.threeKingdoms.handcard;

import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.QilinBowCard;

import javax.print.DocFlavor;
import java.util.Arrays;

public enum PlayType {

    SKIP("skip"),
    ACTIVE("active"),
    INACTIVE("inactive"),
    QilinBow("qilinBow");

    private final String playType;

    public String getPlayType() {
        return playType;
    }

    PlayType(String playType) {
        this.playType = playType;
    }

    public static PlayType getPlayType(String playType) {
       return Arrays.stream(PlayType.values()).filter(playtype -> playtype.getPlayType().equals(playType)).findFirst()
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
