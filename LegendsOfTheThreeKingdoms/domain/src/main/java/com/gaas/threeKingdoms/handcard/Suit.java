package com.gaas.threeKingdoms.handcard;

import lombok.Getter;

public enum Suit {

    CLUB('C', "梅花"),
    DIAMOND('D', "方塊"),
    HEART('H', "紅心"),
    SPADE('S', "黑桃");

    private final char representation;
    private final String displayName;

    Suit(char representation, String displayName) {
        this.representation = representation;
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return String.valueOf(representation);
    }

    public String getDisplayName() {
        return displayName;
    }

}
