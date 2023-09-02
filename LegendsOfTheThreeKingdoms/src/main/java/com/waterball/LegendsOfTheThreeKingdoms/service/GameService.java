package com.waterball.LegendsOfTheThreeKingdoms.service;

import com.waterball.LegendsOfTheThreeKingdoms.domain.Game;
import com.waterball.LegendsOfTheThreeKingdoms.domain.events.CreateGameEvent;
import com.waterball.LegendsOfTheThreeKingdoms.domain.events.DomainEvent;
import com.waterball.LegendsOfTheThreeKingdoms.domain.events.GetMonarchGeneralCardsEvent;
import com.waterball.LegendsOfTheThreeKingdoms.domain.generalcard.GeneralCard;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.Player;
import com.waterball.LegendsOfTheThreeKingdoms.presenter.CreateGamePresenter;
import com.waterball.LegendsOfTheThreeKingdoms.presenter.GetGeneralCardPresenter;
import com.waterball.LegendsOfTheThreeKingdoms.presenter.MonarchChooseGeneralCardPresenter;
import com.waterball.LegendsOfTheThreeKingdoms.repository.InMemoryGameRepository;
import com.waterball.LegendsOfTheThreeKingdoms.service.dto.GameDto;
import com.waterball.LegendsOfTheThreeKingdoms.service.dto.GeneralCardDto;
import com.waterball.LegendsOfTheThreeKingdoms.service.dto.PlayerDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GameService {

    private final InMemoryGameRepository repository;

    public GameService(InMemoryGameRepository repository) {
        this.repository = repository;
    }

    public GameDto startGame(GameDto gameDto, CreateGamePresenter createGamePresenter, GetGeneralCardPresenter getMonarchGeneralCardPresenter) {

        // 創建遊戲
        List<DomainEvent> createGameEvent = createGame(gameDto);
        // 分配角色
        Game game = repository.findGameById(gameDto.getGameId());
        // 改
        List<DomainEvent> assignRolesEvent = game.assignRoles();
        // 存
        repository.save(game);
        // 推
        GameDto returnGameDto = convertToGameDto(game);
        createGamePresenter.renderGame(game);

        // 主公抽可選擇的武將牌
        GetMonarchGeneralCardsEvent event = game.getMonarchCanChooseGeneralCards();

        // 推播給主公
        getMonarchGeneralCardPresenter.renderGame(event, game.getGameId(), game.getMonarchPlayerId());

        return returnGameDto;
    }


    private List<DomainEvent> createGame(GameDto gameDto) {
        List<PlayerDto> playerDtos = gameDto.getPlayers();
        List<Player> players = convertToPlayers(playerDtos);
        Game game = new Game(gameDto.getGameId(), players);
        repository.save(game);
        return List.of(new CreateGameEvent(game.getGameId(), game.getSeatingChart().getPlayers()));
    }

    public GameDto getGame(String gameId) {
        Game game = repository.findGameById(gameId);
        return convertToGameDto(game);
    }

    public GameDto playCard(String gameId, String playerId, String cardId, String targetPlayerId, String playType) {
        Game game = repository.findGameById(gameId);
        game.playerPlayCard(playerId, cardId, targetPlayerId, playType);
        repository.save(game);
        return convertToGameDto(game);
    }

    public void monarchChooseGeneral(String gameId, String playerId, String generalId, MonarchChooseGeneralCardPresenter presenter) {
        Game game = repository.findGameById(gameId);
        List<DomainEvent> events = game.monarchChoosePlayerGeneral(playerId, generalId);
        repository.save(game);
        presenter.renderEvents(events);
    }

    public GameDto finishAction(String gameId, String playerId) {
        Game game = repository.findGameById(gameId);
        game.setDiscardRoundPhase(playerId);
        return convertToGameDto(game);
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

    private List<Player> convertToPlayers(List<PlayerDto> playerDtos) {
        List<Player> players = new ArrayList<>();
        for (PlayerDto playerDto : playerDtos) {
            players.add(convertToPlayer(playerDto));
        }
        return players;
    }

    private PlayerDto convertToPlayerDto(Player player) {
        PlayerDto playerDto = new PlayerDto();
        playerDto.setId(player.getId());
        playerDto.setRoleCard(player.getRoleCard());
        playerDto.setGeneralCard(player.getGeneralCard());
        playerDto.setHand(player.getHand());
        return playerDto;
    }

    private Player convertToPlayer(PlayerDto playerDto) {
        Player player = new Player();
        player.setId(playerDto.getId());
        player.setRoleCard(playerDto.getRoleCard());
        player.setGeneralCard(playerDto.getGeneralCard());
        player.setHand(playerDto.getHand());
        return player;
    }

    public GeneralCardDto convertCardToGeneralDto(GeneralCard card) {
        GeneralCardDto cardDto = new GeneralCardDto();
        cardDto.setGeneralID(card.getGeneralID());
        cardDto.setGeneralName(card.getGeneralName());
        return cardDto;
    }

    public GameDto discardCard(String gameId, List<String> cardIds) {
        Game game = repository.findGameById(gameId);
        game.playerDiscardCard(cardIds);
        return convertToGameDto(game);
    }

    public interface Presenter<T> {
        T present();

    }


}
