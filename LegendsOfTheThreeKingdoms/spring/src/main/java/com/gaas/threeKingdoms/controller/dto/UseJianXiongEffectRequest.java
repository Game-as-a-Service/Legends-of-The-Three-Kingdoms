package com.gaas.threeKingdoms.controller.dto;

import com.gaas.threeKingdoms.usecase.UseJianXiongEffectUseCase;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UseJianXiongEffectRequest {
    private String playerId;
    private String choice; // "ACCEPT" or "SKIP"

    public UseJianXiongEffectUseCase.UseJianXiongEffectRequest toUseJianXiongEffectRequest() {
        return new UseJianXiongEffectUseCase.UseJianXiongEffectRequest(playerId, choice);
    }
}
