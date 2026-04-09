package com.gaas.threeKingdoms.controller.dto;

import com.gaas.threeKingdoms.usecase.UseStonePiercingAxeEffectUseCase;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class UseStonePiercingAxeEffectRequest {
    private String playerId;
    private String choice;              // "DISCARD_TWO" or "SKIP"
    private List<String> discardCardIds; // exactly 2 cardIds when choice is DISCARD_TWO

    public UseStonePiercingAxeEffectUseCase.UseStonePiercingAxeEffectRequest toUseStonePiercingAxeEffectRequest() {
        return new UseStonePiercingAxeEffectUseCase.UseStonePiercingAxeEffectRequest(playerId, choice, discardCardIds);
    }
}
