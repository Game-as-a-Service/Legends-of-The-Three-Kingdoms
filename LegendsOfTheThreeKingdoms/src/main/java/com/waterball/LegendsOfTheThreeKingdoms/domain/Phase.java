package com.waterball.LegendsOfTheThreeKingdoms.domain;

public enum Phase {

    Judgement("Judgement"),
    Drawing("Drawing"),
    Action("Action"),
    Discard("Discard");

    private final String phaseName;

    Phase(String c) {
        this.phaseName = c;
    }
}
