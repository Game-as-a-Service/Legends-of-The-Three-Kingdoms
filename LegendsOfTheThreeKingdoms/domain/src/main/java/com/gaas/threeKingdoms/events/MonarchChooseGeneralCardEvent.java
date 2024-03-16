package com.gaas.threeKingdoms.events;

import com.gaas.threeKingdoms.generalcard.GeneralCard;

import java.util.List;

public class MonarchChooseGeneralCardEvent extends DomainEvent {
    private GeneralCard generalCard;
    private String gameId;
    private List<String> playerIds;

    public MonarchChooseGeneralCardEvent(GeneralCard generalCard,String message, String gameId, List<String> playerIds) {
        super("MonarchChooseGeneralCardEvent", message);
        this.playerIds = playerIds;
        this.generalCard = generalCard;
        this.gameId = gameId;
    }

    public GeneralCard getGeneralCard() {
        return generalCard;
    }

    public List<String> getPlayerIds() {
        return playerIds;
    }

    public String getGameId() {
        return gameId;
    }
}
