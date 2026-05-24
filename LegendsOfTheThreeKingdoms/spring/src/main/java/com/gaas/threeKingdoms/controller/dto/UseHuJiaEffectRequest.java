package com.gaas.threeKingdoms.controller.dto;

import com.gaas.threeKingdoms.usecase.UseHuJiaEffectUseCase;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UseHuJiaEffectRequest {
    private String playerId;
    private String choice;   // "ACCEPT" or "DECLINE"
    private String cardId;   // 必填 when ACCEPT

    public UseHuJiaEffectUseCase.UseHuJiaEffectRequest toUseHuJiaEffectRequest() {
        return new UseHuJiaEffectUseCase.UseHuJiaEffectRequest(playerId, choice, cardId);
    }
}
