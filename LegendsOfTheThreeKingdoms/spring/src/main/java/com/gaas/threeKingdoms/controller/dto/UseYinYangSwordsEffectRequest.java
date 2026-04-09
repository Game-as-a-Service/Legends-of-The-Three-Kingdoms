package com.gaas.threeKingdoms.controller.dto;

import com.gaas.threeKingdoms.usecase.UseYinYangSwordsEffectUseCase;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UseYinYangSwordsEffectRequest {
    private String playerId;
    private String choice;  // "TARGET_DISCARDS" or "ATTACKER_DRAWS"
    private String cardId;  // only required when choice is TARGET_DISCARDS

    public UseYinYangSwordsEffectUseCase.UseYinYangSwordsEffectRequest toUseYinYangSwordsEffectRequest() {
        return new UseYinYangSwordsEffectUseCase.UseYinYangSwordsEffectRequest(playerId, choice, cardId);
    }
}
