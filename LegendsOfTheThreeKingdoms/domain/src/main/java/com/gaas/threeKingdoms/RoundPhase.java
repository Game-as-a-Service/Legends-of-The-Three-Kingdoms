package com.gaas.threeKingdoms;

public enum RoundPhase {

    Judgement("Judgement"),
    Drawing("Drawing"),
    Action("Action"),
    Discard("Discard");

    private final String phaseName;

    RoundPhase(String c) {
        this.phaseName = c;
    }
}
