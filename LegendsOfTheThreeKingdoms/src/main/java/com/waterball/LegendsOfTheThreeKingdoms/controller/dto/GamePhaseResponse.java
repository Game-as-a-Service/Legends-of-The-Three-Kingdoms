package com.waterball.LegendsOfTheThreeKingdoms.controller.dto;

import com.waterball.LegendsOfTheThreeKingdoms.service.dto.GamePhaseDto;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
public class GamePhaseResponse {
    private GamePhaseDto gamePhaseDto;

    public GamePhaseResponse(GamePhaseDto gamePhaseDto) {
        this.gamePhaseDto = gamePhaseDto;
    }
}
