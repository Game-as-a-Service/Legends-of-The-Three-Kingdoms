package com.waterball.LegendsOfTheThreeKingdoms.controller.dto;

import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.PlayType;
import com.waterball.LegendsOfTheThreeKingdoms.service.GameService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlayCardRequest {
    private String playerId;
    private String targetPlayerId;
    private String cardId;
    private String playType; //skip

    public GameService.PlayCardRequest toPlayCardRequest() {
        return new GameService.PlayCardRequest(playerId, targetPlayerId, cardId, playType);
    }
}
