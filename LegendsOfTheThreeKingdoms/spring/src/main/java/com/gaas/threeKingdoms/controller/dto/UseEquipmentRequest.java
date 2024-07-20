package com.gaas.threeKingdoms.controller.dto;

import com.gaas.threeKingdoms.handcard.EquipmentPlayType;
import com.gaas.threeKingdoms.usecase.UseEquipmentUseCase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UseEquipmentRequest {
    private String playerId;
    private String targetPlayerId;
    private String cardId;
    private String playType;

    public UseEquipmentRequest(String playerId, String targetPlayerId, String cardId, String playType) {
        this.playerId = playerId;
        this.targetPlayerId = targetPlayerId;
        this.cardId = cardId;
        this.playType = playType;
    }

    public UseEquipmentRequest(String playerId, String cardId, String playType) {
        this.playerId = playerId;
        this.cardId = cardId;
        this.playType = playType;
    }

    public UseEquipmentUseCase.UseEquipmentRequest toUseEquipmentRequest() {
        return new UseEquipmentUseCase.UseEquipmentRequest(playerId, targetPlayerId, cardId, EquipmentPlayType.getPlayType(playType));
    }
}
