package com.gaas.threeKingdoms.controller.dto;

import com.gaas.threeKingdoms.usecase.UseHeavenlyDoubleHalberdKillUseCase;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class UseHeavenlyDoubleHalberdKillRequest {
    private String playerId;
    private String cardId;
    private String primaryTargetPlayerId;
    private List<String> additionalTargetPlayerIds;

    public UseHeavenlyDoubleHalberdKillUseCase.UseHeavenlyDoubleHalberdKillRequest toUseHeavenlyDoubleHalberdKillRequest() {
        return new UseHeavenlyDoubleHalberdKillUseCase.UseHeavenlyDoubleHalberdKillRequest(
                playerId, cardId, primaryTargetPlayerId, additionalTargetPlayerIds);
    }
}
