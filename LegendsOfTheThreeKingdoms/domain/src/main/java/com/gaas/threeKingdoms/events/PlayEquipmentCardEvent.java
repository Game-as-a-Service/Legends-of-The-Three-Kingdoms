package com.gaas.threeKingdoms.events;

import lombok.Getter;

@Getter
public class PlayEquipmentCardEvent extends DomainEvent {

    private final String playerId;
    private final String cardId;
    private final String deprecatedCardId;


    public PlayEquipmentCardEvent(String playerId, String cardId, String deprecatedCardId) {
        super("PlayEquipmentCardEvent", "玩家已使用裝備卡");
        this.playerId = playerId;
        this.cardId = cardId;
        this.deprecatedCardId = deprecatedCardId;
    }

}
