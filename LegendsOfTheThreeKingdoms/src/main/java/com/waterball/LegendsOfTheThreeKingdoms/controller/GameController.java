package com.waterball.LegendsOfTheThreeKingdoms.controller;


import com.waterball.LegendsOfTheThreeKingdoms.controller.dto.*;
import com.waterball.LegendsOfTheThreeKingdoms.presenter.GeneralCardPresenter;
import com.waterball.LegendsOfTheThreeKingdoms.service.GameService;
import com.waterball.LegendsOfTheThreeKingdoms.service.dto.GameDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class GameController {

    private final GameService gameService;

    @Autowired
    private WebSocketBroadCast webSocketService;

    @Autowired
    private SimpMessagingTemplate template;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/api/games")
    public ResponseEntity<GameResponse> createGame(@RequestBody GameRequest gameRequest) {
        GameDto game = gameService.startGame(GameRequest.convertToGameDto(gameRequest));
        return ResponseEntity.ok(new GameResponse(game));
    }

    @GetMapping("/api/games/{gameId}")
    public ResponseEntity<GameResponse> getGame(@PathVariable String gameId) {
        return ResponseEntity.ok(new GameResponse(gameService.getGame(gameId)));
    }

    @GetMapping("/api/games/{gameId}/{playerId}/generals")
    public ResponseEntity<List<GeneralCardResponse>> getGenerals(@PathVariable String gameId, @PathVariable String playerId) {
        List<GeneralCardResponse> generalCardResponses = gameService.getGenerals(gameId, playerId)
                .stream().map(GeneralCardResponse::new).collect(Collectors.toList());
        return ResponseEntity.ok(generalCardResponses);
    }

    @PostMapping("/api/games/{gameId}/{playerId}/general/{generalId}")
    public ResponseEntity<GeneralCardPresenter.GeneralCardViewModel> chooseGeneral(@PathVariable String gameId, @PathVariable String playerId, @PathVariable String generalId) {
        GeneralCardPresenter generalCardPresenter = new GeneralCardPresenter();
        gameService.chooseGeneral(gameId, playerId, generalId, generalCardPresenter);
        webSocketService.pushGeneralsCardEvent(generalCardPresenter);
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
        GameDto gameDto = gameService.discardCard(gameId,cardIds);
        return ResponseEntity.ok(new GameResponse(gameDto));
    }
}