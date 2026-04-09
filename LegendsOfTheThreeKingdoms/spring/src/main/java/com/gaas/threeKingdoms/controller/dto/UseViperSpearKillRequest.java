package com.gaas.threeKingdoms.controller.dto;

import com.gaas.threeKingdoms.usecase.UseViperSpearKillUseCase;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class UseViperSpearKillRequest {
    private String playerId;
    private String targetPlayerId;
    private List<String> discardCardIds;

    public UseViperSpearKillUseCase.UseViperSpearKillRequest toUseViperSpearKillRequest() {
        return new UseViperSpearKillUseCase.UseViperSpearKillRequest(playerId, targetPlayerId, discardCardIds);
    }
}
