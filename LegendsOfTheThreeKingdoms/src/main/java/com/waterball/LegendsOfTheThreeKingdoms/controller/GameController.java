package com.waterball.LegendsOfTheThreeKingdoms.controller;


import com.waterball.LegendsOfTheThreeKingdoms.controller.dto.FinishRoundRequest;
import com.waterball.LegendsOfTheThreeKingdoms.controller.dto.GameRequest;
import com.waterball.LegendsOfTheThreeKingdoms.controller.dto.GameResponse;
import com.waterball.LegendsOfTheThreeKingdoms.controller.dto.PlayCardRequest;
import com.waterball.LegendsOfTheThreeKingdoms.presenter.CreateGamePresenter;
import com.waterball.LegendsOfTheThreeKingdoms.presenter.MonarchGeneralCardPresenter;
import com.waterball.LegendsOfTheThreeKingdoms.presenter.GetGeneralCardPresenter;
import com.waterball.LegendsOfTheThreeKingdoms.service.GameService;
import com.waterball.LegendsOfTheThreeKingdoms.service.dto.GameDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class GameController {

    private final GameService gameService;

    @Autowired
    private WebSocketBroadCast webSocketBroadCast;

    @Autowired
    private SimpMessagingTemplate template;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/api/games")
    public ResponseEntity<GameResponse> createGame(@RequestBody GameRequest gameRequest) {
        CreateGamePresenter createGamePresenter = new CreateGamePresenter();
        GetGeneralCardPresenter getGeneralCardPresenter = new GetGeneralCardPresenter();
        GameDto game = gameService.startGame(GameRequest.convertToGameDto(gameRequest), createGamePresenter, getGeneralCardPresenter);
        webSocketBroadCast.pushCreateGameEvent(createGamePresenter);
        webSocketBroadCast.pushMonarchGeneralCardsEvent(getGeneralCardPresenter);
        return ResponseEntity.ok(new GameResponse(game));
    }

    @GetMapping("/api/games/{gameId}")
    public ResponseEntity<GameResponse> getGame(@PathVariable String gameId) {
        return ResponseEntity.ok(new GameResponse(gameService.getGame(gameId)));
    }

    @PostMapping("/api/games/{gameId}/{playerId}/general/{generalId}")
    public ResponseEntity<MonarchGeneralCardPresenter.MonarchGeneralCardViewModel> chooseGeneralByMonatch(@PathVariable String gameId, @PathVariable String playerId, @PathVariable String generalId) {
        MonarchGeneralCardPresenter generalCardPresenter = new MonarchGeneralCardPresenter();
        gameService.monarchChooseGeneral(gameId, playerId, generalId, generalCardPresenter);
        webSocketBroadCast.pushMonarchChooseGeneralsCardEvent(generalCardPresenter);
        return ResponseEntity.ok(generalCardPresenter.present());
    }

    @PostMapping("/api/games/{gameId}/player:playCard")
    public ResponseEntity<GameResponse> playerPlayCard(@PathVariable String gameId, @RequestBody PlayCardRequest playRequest) {
        GameDto gameDto = gameService.playCard(gameId, playRequest.getPlayerId(), playRequest.getCardId(), playRequest.getTargetPlayerId(), playRequest.getPlayType());
        return ResponseEntity.ok(new GameResponse(gameDto));
    }

    @PostMapping("/api/games/{gameId}/player:finishAction")
    public ResponseEntity<GameResponse> finishAction(@PathVariable String gameId, @RequestBody FinishRoundRequest finishRoundRequest) {
        GameDto gameDto = gameService.finishAction(gameId, finishRoundRequest.getPlayerId());
        return ResponseEntity.ok(new GameResponse(gameDto));
    }

    @PostMapping("/api/games/{gameId}/player:discardCards")
    public ResponseEntity<GameResponse> discardCards(@PathVariable String gameId, @RequestBody List<String> cardIds) {
        GameDto gameDto = gameService.discardCard(gameId, cardIds);
        return ResponseEntity.ok(new GameResponse(gameDto));
    }
}