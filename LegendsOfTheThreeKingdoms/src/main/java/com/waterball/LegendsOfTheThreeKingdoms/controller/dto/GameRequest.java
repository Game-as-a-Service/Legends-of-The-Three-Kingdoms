package com.waterball.LegendsOfTheThreeKingdoms.controller.dto;

import com.waterball.LegendsOfTheThreeKingdoms.service.dto.GameDto;
import com.waterball.LegendsOfTheThreeKingdoms.service.dto.PlayerDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameRequest implements Serializable {
    private String gameId;
    private List<PlayerRequest> players;

    public static GameDto convertToGameDto(GameRequest gameRequest) {
        String gameId = gameRequest.getGameId();
        List<PlayerDto> playerDtos = gameRequest.getPlayers()
                .stream()
                .map(PlayerRequest::convertToPlayerDto)
                .collect(Collectors.toList());
        GameDto gameDto = new GameDto();
        gameDto.setGameId(gameId);
        gameDto.setPlayers(playerDtos);
        return gameDto;
    }
}
