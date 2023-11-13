package com.waterball.LegendsOfTheThreeKingdoms.service;

import com.waterball.LegendsOfTheThreeKingdoms.presenter.DiscardPresenter;
import com.waterball.LegendsOfTheThreeKingdoms.domain.Game;
import com.waterball.LegendsOfTheThreeKingdoms.domain.events.DomainEvent;
import com.waterball.LegendsOfTheThreeKingdoms.domain.events.GetMonarchGeneralCardsEvent;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.Player;
import com.waterball.LegendsOfTheThreeKingdoms.presenter.*;
import com.waterball.LegendsOfTheThreeKingdoms.repository.InMemoryGameRepository;
import com.waterball.LegendsOfTheThreeKingdoms.service.dto.GameDto;
import com.waterball.LegendsOfTheThreeKingdoms.service.dto.PlayerDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GameService {

    private final InMemoryGameRepository repository;

    public GameService(InMemoryGameRepository repository) {
        this.repository = repository;
    }

    public void startGame(CreateGameRequest createGameRequest, CreateGamePresenter createGamePresenter, GetGeneralCardPresenter getMonarchGeneralCardPresenter) {
        // 創建遊戲
        List<Player> players = createGameRequest.getPlayers().stream().map(playerId -> {
            Player player = new Player();
            player.setId(playerId);
            return player;
        }).collect(Collectors.toList());

        // 改
        Game game = new Game(createGameRequest.getGameId(), players);
        game.assignRoles();

        // 存
        repository.save(game);

        // 推
        createGamePresenter.renderGame(game);

        // 主公抽可選擇的武將牌
        GetMonarchGeneralCardsEvent event = game.getMonarchCanChooseGeneralCards();

        // 推播給主公
        getMonarchGeneralCardPresenter.renderGame(event, game.getGameId(), game.getMonarchPlayerId());

    }

    public void monarchChooseGeneral(String gameId, MonarchChooseGeneralRequest request, MonarchChooseGeneralCardPresenter presenter) {
        Game game = repository.findGameById(gameId);
        List<DomainEvent> events = game.monarchChoosePlayerGeneral(request.getPlayerId(), request.getGeneralId());
        repository.save(game);
        presenter.renderEvents(events);
    }

    public void othersChoosePlayerGeneral(String gameId, MonarchChooseGeneralRequest request, InitialEndPresenter initialEndPresenter, RoundStartPresenter roundStartPresenter) {
        Game game = repository.findGameById(gameId);
        List<DomainEvent> events = game.othersChoosePlayerGeneral(request.getPlayerId(), request.getGeneralId());
        repository.save(game);
        initialEndPresenter.renderEvents(events);
        roundStartPresenter.renderEvents(events);
    }

    public void findGameById(String gameId, String playerId, FindGamePresenter presenter) {
        Game game = repository.findGameById(gameId);
        presenter.renderGame(game, playerId);
    }

    public void playCard(String gameId, PlayCardRequest request, PlayCardPresenter presenter) {
        Game game = repository.findGameById(gameId);
        List<DomainEvent> events = game.playerPlayCard(request.playerId, request.cardId, request.targetPlayerId , request.playType);
        repository.save(game);
        presenter.renderEvents(events);
    }


    public void finishAction(String gameId, String playerId, FinishActionPresenter presenter) {
        Game game = repository.findGameById(gameId);
        List<DomainEvent> events = game.finishAction(playerId);
        repository.save(game);
        presenter.renderEvents(events);
    }

    public void discardCard(String gameId, List<String> cardIds, DiscardPresenter presenter) {
        Game game = repository.findGameById(gameId);
        List<DomainEvent> events = game.playerDiscardCard(cardIds);
        repository.save(game);
        presenter.renderEvents(events);
    }

    public GameDto convertToGameDto(Game game) {
        String gameId = game.getGameId();
        List<PlayerDto> playerDtos = convertToPlayerDtos(game.getPlayers());
        GameDto gameDto = new GameDto();
        gameDto.setGameId(gameId);
        gameDto.setPlayers(playerDtos);
        gameDto.setGamePhaseState(game.getGamePhase().getClass().getName());
        return gameDto;
    }

    private List<PlayerDto> convertToPlayerDtos(List<Player> players) {
        List<PlayerDto> playerDtos = new ArrayList<>();
        for (Player player : players) {
            playerDtos.add(convertToPlayerDto(player));
        }
        return playerDtos;
    }


    private PlayerDto convertToPlayerDto(Player player) {
        PlayerDto playerDto = new PlayerDto();
        playerDto.setId(player.getId());
        playerDto.setRoleCard(player.getRoleCard());
        playerDto.setGeneralCard(player.getGeneralCard());
        playerDto.setHand(player.getHand());
        return playerDto;
    }


    public interface Presenter<T> {
        T present();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateGameRequest {
        private String gameId;
        private List<String> players;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonarchChooseGeneralRequest {
        private String playerId;
        private String generalId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OthersChooseGeneralRequest {
        private String playerId;
        private String generalId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlayCardRequest {
        private String playerId;
        private String targetPlayerId;
        private String cardId;
        private String playType;
    }
}
