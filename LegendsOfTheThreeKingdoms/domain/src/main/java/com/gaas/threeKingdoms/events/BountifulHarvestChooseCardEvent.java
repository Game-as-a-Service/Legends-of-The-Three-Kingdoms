package com.gaas.threeKingdoms.events;

import com.gaas.threeKingdoms.handcard.PlayCard;
import com.gaas.threeKingdoms.player.Player;
import lombok.Getter;

@Getter
public class BountifulHarvestChooseCardEvent extends DomainEvent {

    private final String playerId;
    private final String cardId;

    public BountifulHarvestChooseCardEvent(Player player, String cardId) {
        super("BountifulHarvestChooseCardEvent", String.format("%s 選擇了 %s", player.getGeneralCard().getGeneralName(), PlayCard.getCardName(cardId)));
        this.playerId = player.getId();
        this.cardId = cardId;
    }
}
