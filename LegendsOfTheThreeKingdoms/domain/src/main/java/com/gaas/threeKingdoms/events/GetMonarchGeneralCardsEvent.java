package com.gaas.threeKingdoms.events;

import com.gaas.threeKingdoms.generalcard.GeneralCard;
import lombok.Getter;

import java.util.List;

@Getter
public class GetMonarchGeneralCardsEvent extends DomainEvent {

    private List<GeneralCard> generalCardsList;

    public GetMonarchGeneralCardsEvent(List<GeneralCard> generalCardsList) {
        super("GetMonarchGeneralCardsEven", "主公可以選擇的武將牌");
        this.generalCardsList = generalCardsList;
    }

}
