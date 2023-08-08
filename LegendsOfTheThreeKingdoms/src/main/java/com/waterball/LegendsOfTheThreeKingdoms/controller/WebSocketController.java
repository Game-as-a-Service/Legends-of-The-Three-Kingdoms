package com.waterball.LegendsOfTheThreeKingdoms.controller;

import com.waterball.LegendsOfTheThreeKingdoms.controller.dto.GameResponse;
import com.waterball.LegendsOfTheThreeKingdoms.controller.dto.PlayerResponse;
import com.waterball.LegendsOfTheThreeKingdoms.domain.Game;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.Player;
import com.waterball.LegendsOfTheThreeKingdoms.presenter.GeneralCardPresenter;
import com.waterball.LegendsOfTheThreeKingdoms.presenter.Presenter;
import com.waterball.LegendsOfTheThreeKingdoms.service.GameService;
import com.waterball.LegendsOfTheThreeKingdoms.service.dto.GameDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class WebSocketController {

    @Autowired
    private GameService gameService;

//    @SendTo("/topic/generalCardEvent")
//    public GameResponse push(Event event) throws Exception {
//
//        Game game = new Game();
//        game.setGameId("Hello,world fromBackend");
//
//        game.setPlayers(List.of(
//                Player.builder().id("player-a").build(),
//                Player.builder().id("player-b").build(),
//                Player.builder().id("player-c").build(),
//                Player.builder().id("player-d").build()));
//
//        return new GameResponse(gameService.convertToGameDto(game));
//    }

    @SendTo("/topic/generalCardEvent")
    public GeneralCardPresenter.GeneralCardViewModel pushGeneralsCardEvent(GameDto game) {
        GeneralCardPresenter generalCardPresenter = new GeneralCardPresenter();
        GeneralCardPresenter.GeneralCardViewModel generalCardViewModel = generalCardPresenter.present();
        return generalCardViewModel;
    }
}
