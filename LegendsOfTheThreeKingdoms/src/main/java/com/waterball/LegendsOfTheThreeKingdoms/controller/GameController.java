package com.waterball.LegendsOfTheThreeKingdoms.controller;


import com.waterball.LegendsOfTheThreeKingdoms.controller.dto.*;
import com.waterball.LegendsOfTheThreeKingdoms.presenter.*;
import com.waterball.LegendsOfTheThreeKingdoms.service.GameService;
import com.waterball.LegendsOfTheThreeKingdoms.service.dto.GameDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity createGame(@RequestBody GameRequest gameRequest) {
        CreateGamePresenter createGamePresenter = new CreateGamePresenter();
        GetGeneralCardPresenter getMonarchGeneralCardPresenter = new GetGeneralCardPresenter();
        gameService.startGame(gameRequest.toUseCaseRequest(), createGamePresenter, getMonarchGeneralCardPresenter);
        webSocketBroadCast.pushCreateGameEventToAllPlayers(createGamePresenter);
        webSocketBroadCast.pushMonarchGetGeneralCardsEvent(getMonarchGeneralCardPresenter);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @GetMapping("/api/games/{gameId}")
    public ResponseEntity findGameById(@RequestParam String playerId, @PathVariable String gameId){
        FindGamePresenter findGamePresenter = new FindGamePresenter();
        gameService.findGameById(gameId, playerId, findGamePresenter);
        webSocketBroadCast.pushFindGameEvent(findGamePresenter);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PostMapping("/api/games/{gameId}/player:monarchChooseGeneral")
    public ResponseEntity chooseGeneralByMonarch(@PathVariable String gameId, @RequestBody ChooseGeneralRequest request) {
        MonarchChooseGeneralCardPresenter monarchChooseGeneralCardPresenter = new MonarchChooseGeneralCardPresenter();
        gameService.monarchChooseGeneral(gameId, request.toMonarchChooseGeneralRequest(), monarchChooseGeneralCardPresenter);
        webSocketBroadCast.pushMonarchChooseGeneralsCardEvent(monarchChooseGeneralCardPresenter);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PostMapping("/api/games/{gameId}/player:otherChooseGeneral")
    public ResponseEntity chooseGeneralByOthers(@PathVariable String gameId, @RequestBody ChooseGeneralRequest request) {
        InitialEndPresenter initialEndPresenter = new InitialEndPresenter();
        RoundStartPresenter roundStartPresenter = new RoundStartPresenter();
        gameService.othersChoosePlayerGeneral(gameId, request.toMonarchChooseGeneralRequest(), initialEndPresenter, roundStartPresenter);
        webSocketBroadCast.pushInitialEndEvent(initialEndPresenter);
        webSocketBroadCast.pushPlayerTakeTurnEvent(roundStartPresenter);
        return ResponseEntity.ok(HttpStatus.OK);
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