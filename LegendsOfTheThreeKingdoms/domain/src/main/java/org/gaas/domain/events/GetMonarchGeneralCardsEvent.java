package org.gaas.domain.events;

import org.gaas.domain.generalcard.GeneralCard;

import java.util.List;

public class GetMonarchGeneralCardsEvent extends DomainEvent {

    private String name = "GetMonarchGeneralCardsEvent";
    private String message = "主公可以選擇的武將牌";
    private List<GeneralCard> generalCardsList;

    public GetMonarchGeneralCardsEvent(List<GeneralCard> generalCardsList) {
        this.generalCardsList = generalCardsList;
    }

    public List<GeneralCard> getGeneralCardsList() {
        return generalCardsList;
    }
}
