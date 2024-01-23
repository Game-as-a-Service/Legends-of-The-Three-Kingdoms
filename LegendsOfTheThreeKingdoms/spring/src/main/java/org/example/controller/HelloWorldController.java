package org.example.controller;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.controller.dto.GameResponse;
import org.gaas.domain.Game;
import org.gaas.domain.gamephase.Initial;
import org.gaas.domain.player.Player;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api")
public class HelloWorldController {


    @MessageMapping("/hello")
    @SendTo("/websocket/legendsOfTheThreeKingdoms/my-id/player-a")
    public GameResponse sayHello() throws Exception {
        Game game = new Game();
        game.setGameId("Hello World From Backend");

        game.setPlayers(List.of(
                Player.builder().id("player-a").build(),
                Player.builder().id("player-b").build(),
                Player.builder().id("player-c").build(),
                Player.builder().id("player-d").build()));

        game.enterPhase(new Initial(game));

        return new GameResponse(convertToGameDto(game));
    }

    private GameDto convertToGameDto(Game game) {
        String gameId = game.getGameId();
        List<Player> players = game.getPlayers();
        GameDto gameDto = new GameDto();
        gameDto.setGameId(gameId);
        gameDto.setPlayers(players);
        gameDto.setGamePhaseState(game.getGamePhase().getClass().getName());
        return gameDto;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public class GameDto {
        private String gameId;
        private List<Player> players;
        private String gamePhaseState;
    }
}