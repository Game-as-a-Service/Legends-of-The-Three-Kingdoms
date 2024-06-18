package com.gaas.threeKingdoms.controller.dto;

import com.gaas.threeKingdoms.handcard.EquipmentPlayType;
import com.gaas.threeKingdoms.usecase.ChooseHorseUseCase;
import com.gaas.threeKingdoms.usecase.UseEquipmentUseCase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChooseHorseRequest {
    private String playerId;
    private String cardId;

    public ChooseHorseUseCase.ChooseHorseRequest toChooseHorseRequest() {
        return new ChooseHorseUseCase.ChooseHorseRequest(playerId, cardId);
    }
}
