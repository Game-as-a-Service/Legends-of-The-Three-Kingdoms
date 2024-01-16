package org.example.controller;


import com.waterball.LegendsOfTheThreeKingdoms.controller.dto.GameResponse;
import com.waterball.LegendsOfTheThreeKingdoms.domain.Game;
import com.waterball.LegendsOfTheThreeKingdoms.domain.gamephase.Initial;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.Player;
import com.waterball.LegendsOfTheThreeKingdoms.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api")
public class HelloWorldController {
    @Autowired
    private GameService gameService;

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

        return new GameResponse(gameService.convertToGameDto(game));
    }
}