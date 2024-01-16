package org.gaas.domain.events;

import org.gaas.domain.generalcard.GeneralCard;

import java.util.List;

public class MonarchChooseGeneralCardEvent extends DomainEvent {
    private String name = "MonarchChooseGeneralCardEvent";
    private String message ;
    private GeneralCard generalCard;
    private String gameId;
    private List<String> playerIds;

    public MonarchChooseGeneralCardEvent(GeneralCard generalCard,String message, String gameId, List<String> playerIds) {
        this.playerIds = playerIds;
        this.generalCard = generalCard;
        this.message = message;
        this.gameId = gameId;
    }

    public GeneralCard getGeneralCard() {
        return generalCard;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getPlayerIds() {
        return playerIds;
    }

    public String getGameId() {
        return gameId;
    }
}
