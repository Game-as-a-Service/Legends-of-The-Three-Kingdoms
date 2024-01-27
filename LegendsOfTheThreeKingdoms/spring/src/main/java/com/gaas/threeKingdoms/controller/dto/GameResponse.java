package com.gaas.threeKingdoms.controller.dto;

import com.gaas.threeKingdoms.controller.HelloWorldController.GameDto;
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
public class GameResponse implements Serializable {
    private String gameId;
    private List<PlayerViewModel> players;

    public GameResponse(GameDto game) {
        gameId = game.getGameId();
        players = game.getPlayers()
                .stream()
                .map(PlayerViewModel::new)
                .collect(Collectors.toList());
    }
}
