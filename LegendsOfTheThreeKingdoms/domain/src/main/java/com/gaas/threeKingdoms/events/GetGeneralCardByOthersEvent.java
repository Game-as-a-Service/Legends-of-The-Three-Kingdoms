package com.gaas.threeKingdoms.events;

import com.gaas.threeKingdoms.generalcard.GeneralCard;

import java.util.List;

public class GetGeneralCardByOthersEvent extends DomainEvent {
    private String name = "GetGeneralCardByOthersEvent";
    private String message = "請選擇武將";
    private String playerId;
    private List<GeneralCard> generalCardsList;

    public GetGeneralCardByOthersEvent(String playerId,List<GeneralCard> generalCardsList) {
        this.playerId = playerId;
        this.generalCardsList = generalCardsList;
    }

    public List<GeneralCard> getGeneralCardsList() {
        return generalCardsList;
    }

    public String getPlayerId() {
        return playerId;
    }
    public String getMessage(){
        return message;
    }
}
