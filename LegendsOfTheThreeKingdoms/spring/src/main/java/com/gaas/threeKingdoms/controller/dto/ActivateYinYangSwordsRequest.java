package com.gaas.threeKingdoms.controller.dto;

import com.gaas.threeKingdoms.usecase.ActivateYinYangSwordsUseCase;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ActivateYinYangSwordsRequest {
    private String playerId;
    private String choice; // "ACTIVATE" or "SKIP"

    public ActivateYinYangSwordsUseCase.ActivateYinYangSwordsRequest toActivateYinYangSwordsRequest() {
        return new ActivateYinYangSwordsUseCase.ActivateYinYangSwordsRequest(playerId, choice);
    }
}
