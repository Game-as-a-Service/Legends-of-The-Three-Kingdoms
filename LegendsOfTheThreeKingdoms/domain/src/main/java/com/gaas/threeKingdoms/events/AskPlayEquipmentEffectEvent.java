package com.gaas.threeKingdoms.events;

import com.gaas.threeKingdoms.handcard.equipmentcard.EquipmentCard;
import lombok.Getter;

@Getter
public class AskPlayEquipmentEffectEvent extends DomainEvent {
    private final String playerId;
    private final EquipmentCard equipmentCard;
//    private final String equipmentId;
//    private final String equipmentName;

    //    public AskPlayEquipmentEffectEvent(String playerId, String equipmentId, String equipmentName) {
    public AskPlayEquipmentEffectEvent(String playerId, EquipmentCard equipmentCard) {
        super("AskPlayEquipmentEffectEvent", String.format("請問是否要發動裝備卡%s的效果", equipmentCard.getName()));
        this.playerId = playerId;
        this.equipmentCard = equipmentCard;
    }
}
