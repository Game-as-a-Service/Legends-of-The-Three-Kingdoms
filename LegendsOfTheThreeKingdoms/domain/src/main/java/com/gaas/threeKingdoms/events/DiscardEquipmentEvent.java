package com.gaas.threeKingdoms.events;


import lombok.Getter;

import java.util.List;

@Getter
public class DiscardEquipmentEvent extends DomainEvent {
    private final String playerId;
    private final List<String> equipmentCardIds;

    public DiscardEquipmentEvent(String playerId, List<String> equipmentCardIds, String message) {
        super("DiscardEquipment", message);
        this.playerId = playerId;
        this.equipmentCardIds = equipmentCardIds;
    }
}
