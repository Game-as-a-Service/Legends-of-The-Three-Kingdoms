package org.example.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.gaas.usecase.PlayCardUseCase;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlayCardRequest {
    private String playerId;
    private String targetPlayerId;
    private String cardId;
    private String playType; //skip

    public PlayCardUseCase.PlayCardRequest toPlayCardRequest() {
        return new PlayCardUseCase.PlayCardRequest(playerId, targetPlayerId, cardId, playType);
    }
}
