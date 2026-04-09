package com.gaas.threeKingdoms.controller.dto;

import com.gaas.threeKingdoms.usecase.UseGreenDragonCrescentBladeEffectUseCase;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UseGreenDragonCrescentBladeEffectRequest {
    private String playerId;
    private String choice;    // "KILL" or "SKIP"
    private String killCardId; // only required when choice is KILL

    public UseGreenDragonCrescentBladeEffectUseCase.UseGreenDragonCrescentBladeEffectRequest toUseGreenDragonCrescentBladeEffectRequest() {
        return new UseGreenDragonCrescentBladeEffectUseCase.UseGreenDragonCrescentBladeEffectRequest(playerId, choice, killCardId);
    }
}
