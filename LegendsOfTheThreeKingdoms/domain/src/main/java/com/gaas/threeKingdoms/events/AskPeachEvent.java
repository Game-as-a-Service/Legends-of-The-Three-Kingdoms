package com.gaas.threeKingdoms.events;

public class AskPeachEvent extends DomainEvent{

    private String playerId;
    private String dyingPlayerId;
    private String name = "AskPeachEvent";
    private String message = "要求玩家出桃";

    public AskPeachEvent(String playerId, String dyingPlayerId) {
        this.playerId = playerId;
        this.dyingPlayerId = dyingPlayerId;
    }

//    public AskPeachEvent(String playerId) {
//        this.playerId = playerId;
//    }

    public String getPlayerId() {
        return playerId;
    }

    public String getDyingPlayerId() {
        return dyingPlayerId;
    }
}
