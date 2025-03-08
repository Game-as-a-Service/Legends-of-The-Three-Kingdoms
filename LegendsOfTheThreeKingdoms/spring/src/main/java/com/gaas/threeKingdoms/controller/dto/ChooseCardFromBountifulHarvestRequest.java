package com.gaas.threeKingdoms.controller.dto;

import com.gaas.threeKingdoms.usecase.ChooseCardFromBountifulHarvestUseCase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChooseCardFromBountifulHarvestRequest {
    private String playerId;
    private String cardId;

    public ChooseCardFromBountifulHarvestUseCase.ChooseCardFromBountifulHarvestRequest toChooseCardFromBountifulHarvestRequest() {
        return new ChooseCardFromBountifulHarvestUseCase.ChooseCardFromBountifulHarvestRequest(playerId, cardId);
    }
}
