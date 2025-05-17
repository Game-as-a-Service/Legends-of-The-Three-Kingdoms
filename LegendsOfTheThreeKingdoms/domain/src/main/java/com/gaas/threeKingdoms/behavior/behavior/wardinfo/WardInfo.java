package com.gaas.threeKingdoms.behavior.behavior.wardinfo;

public class WardInfo {
    private String playerId;
    private String cardId;
    private String wardCardId;

    public String getPlayerId() {
        return playerId;
    }

    public String getCardId() {
        return cardId;
    }

    public String getWardCardId() {
        return wardCardId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public void setWardCardId(String wardCardId) {
        this.wardCardId = wardCardId;
    }

    @Override
    public String toString() {
        return "WardInfo{" +
                "playerId='" + playerId + '\'' +
                ", cardId='" + cardId + '\'' +
                ", wardCardId='" + wardCardId + '\'' +
                '}';
    }
}

