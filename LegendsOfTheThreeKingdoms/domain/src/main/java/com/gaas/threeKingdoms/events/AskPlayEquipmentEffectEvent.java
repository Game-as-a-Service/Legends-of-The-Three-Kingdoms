package com.gaas.threeKingdoms.events;

import com.gaas.threeKingdoms.handcard.equipmentcard.EquipmentCard;
import lombok.Getter;

import java.util.List;


@Getter
public class AskPlayEquipmentEffectEvent extends DomainEvent {
    private final String playerId;
    private final EquipmentCard equipmentCard;
    private final List<String> targetPlayerIds;

    public AskPlayEquipmentEffectEvent(String playerId, EquipmentCard equipmentCard, List<String> targetPlayerIds) {
        super("AskPlayEquipmentEffectEvent", String.format("請問是否要發動裝備卡%s的效果", equipmentCard.getName()));
        this.playerId = playerId;
        this.equipmentCard = equipmentCard;
        this.targetPlayerIds = targetPlayerIds;
    }
}
