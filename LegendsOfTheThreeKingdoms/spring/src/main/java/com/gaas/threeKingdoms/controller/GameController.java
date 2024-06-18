package com.gaas.threeKingdoms.controller;



import com.gaas.threeKingdoms.controller.dto.*;
import com.gaas.threeKingdoms.presenter.*;
import com.gaas.threeKingdoms.usecase.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin
public class GameController {

    private final StartGameUseCase startGameUseCase;
    private final DiscardCardUseCase discardCardUseCase;
    private final FinishActionUseCase finishActionUseCase;
    private final MonarchChooseGeneralUseCase monarchChooseGeneralUseCase;
    private final OthersChoosePlayerGeneralUseCase othersChoosePlayerGeneralUseCase;
    private final PlayCardUseCase playCardUseCase;
    private final FindGameByIdUseCase findGameUseCase;
    private final UseEquipmentUseCase useEquipmentUseCase;
    private final ChooseHorseUseCase chooseHorseUseCase;

    @Autowired
    private WebSocketBroadCast webSocketBroadCast;

    @Autowired
    private SimpMessagingTemplate template;


    @PostMapping("/api/games")
    public ResponseEntity<?> createGame(@RequestBody GameRequest gameRequest) {
        CreateGamePresenter createGamePresenter = new CreateGamePresenter();
        GetGeneralCardPresenter getMonarchGeneralCardPresenter = new GetGeneralCardPresenter();
        startGameUseCase.execute(gameRequest.toUseCaseRequest(), createGamePresenter, getMonarchGeneralCardPresenter);
        webSocketBroadCast.pushCreateGameEventToAllPlayers(createGamePresenter);
        webSocketBroadCast.pushMonarchGetGeneralCardsEvent(getMonarchGeneralCardPresenter);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @GetMapping("/api/games/{gameId}")
    public ResponseEntity<?> findGameById(@RequestParam String playerId, @PathVariable String gameId) {
        FindGamePresenter findGamePresenter = new FindGamePresenter();
        findGameUseCase.findGameById(gameId, playerId, findGamePresenter);
        webSocketBroadCast.pushFindGameEvent(findGamePresenter);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PostMapping("/api/games/{gameId}/player:monarchChooseGeneral")
    public ResponseEntity<?> chooseGeneralByMonarch(@PathVariable String gameId, @RequestBody ChooseGeneralRequest request) {
        MonarchChooseGeneralCardPresenter monarchChooseGeneralCardPresenter = new MonarchChooseGeneralCardPresenter();
        monarchChooseGeneralUseCase.execute(gameId, request.toMonarchChooseGeneralRequest(), monarchChooseGeneralCardPresenter);
        webSocketBroadCast.pushMonarchChooseGeneralsCardEvent(monarchChooseGeneralCardPresenter);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PostMapping("/api/games/{gameId}/player:otherChooseGeneral")
    public ResponseEntity<?> chooseGeneralByOthers(@PathVariable String gameId, @RequestBody ChooseGeneralRequest request) {
        InitialEndPresenter initialEndPresenter = new InitialEndPresenter();
        RoundStartPresenter roundStartPresenter = new RoundStartPresenter();
        othersChoosePlayerGeneralUseCase.execute(gameId, request.toMonarchChooseGeneralRequest(), initialEndPresenter, roundStartPresenter);
        webSocketBroadCast.pushInitialEndEvent(initialEndPresenter);
        webSocketBroadCast.pushPlayerTakeTurnEvent(roundStartPresenter);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PostMapping("/api/games/{gameId}/player:playCard")
    public ResponseEntity<?> playerPlayCard(@PathVariable String gameId, @RequestBody PlayCardRequest playRequest) {
        PlayCardPresenter playCardPresenter = new PlayCardPresenter();
        playCardUseCase.execute(gameId,playRequest.toPlayCardRequest(),playCardPresenter);
        webSocketBroadCast.pushPlayerCardEvent(playCardPresenter);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PostMapping("/api/games/{gameId}/player:finishAction")
    public ResponseEntity<?> finishAction(@PathVariable String gameId, @RequestBody FinishRoundRequest finishRoundRequest) {
        FinishActionPresenter finishActionPresenter = new FinishActionPresenter();
        finishActionUseCase.execute(gameId,finishRoundRequest.getPlayerId(), finishActionPresenter);
        webSocketBroadCast.pushFinishActionEvent(finishActionPresenter);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PostMapping("/api/games/{gameId}/player:discardCards")
    public ResponseEntity<?> discardCards(@PathVariable String gameId, @RequestBody List<String> cardIds) {
        DiscardPresenter discardPresenter = new DiscardPresenter();
        discardCardUseCase.execute(gameId,cardIds,discardPresenter);
        webSocketBroadCast.pushDiscardEvent(discardPresenter);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PostMapping("/api/games/{gameId}/player:useEquipmentEffect")
    public ResponseEntity<?> playerUseEquipmentEffect(@PathVariable String gameId, @RequestBody UseEquipmentRequest useEquipmentRequest) {
        UseEquipmentEffectPresenter equipmentEffectPresenter = new UseEquipmentEffectPresenter();
        useEquipmentUseCase.execute(gameId,useEquipmentRequest.toUseEquipmentRequest(), equipmentEffectPresenter);
        webSocketBroadCast.pushEquipmentEffectEvent(equipmentEffectPresenter);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PostMapping("/api/games/{gameId}/player:chooseHorseCard")
    public ResponseEntity<?> playerChooseHorseCard(@PathVariable String gameId, @RequestBody ChooseHorseRequest chooseHorseRequest) {
        ChooseHorsePresenter chooseHorsePresenter = new ChooseHorsePresenter();
        chooseHorseUseCase.execute(gameId, chooseHorseRequest.toChooseHorseRequest(), chooseHorsePresenter);
        webSocketBroadCast.pushChooseHorseEvent(chooseHorsePresenter);
        return ResponseEntity.ok(HttpStatus.OK);
    }

}
