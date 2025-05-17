package com.gaas.threeKingdoms.controller.dto;

import com.gaas.threeKingdoms.usecase.PlayWardCardUseCase;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PlayWardCardRequest {
    private String playerId;
    private String cardId;
    private String playType;

    public PlayWardCardUseCase.PlayWardCardRequest toPlayWardCardRequest() {
        return new PlayWardCardUseCase.PlayWardCardRequest(playerId, cardId, playType);
    }
}
