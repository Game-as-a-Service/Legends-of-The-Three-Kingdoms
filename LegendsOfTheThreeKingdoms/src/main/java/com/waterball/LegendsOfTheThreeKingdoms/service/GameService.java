package com.waterball.LegendsOfTheThreeKingdoms.service;

import com.waterball.LegendsOfTheThreeKingdoms.domain.generalcard.GeneralCard;
import com.waterball.LegendsOfTheThreeKingdoms.domain.generalcard.GeneralCardDeck;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.Player;
import com.waterball.LegendsOfTheThreeKingdoms.domain.rolecard.Role;
import com.waterball.LegendsOfTheThreeKingdoms.presenter.GeneralCardPresenter;
import com.waterball.LegendsOfTheThreeKingdoms.presenter.CreateGamePresenter;
import com.waterball.LegendsOfTheThreeKingdoms.service.dto.GameDto;
import com.waterball.LegendsOfTheThreeKingdoms.service.dto.GeneralCardDto;
import com.waterball.LegendsOfTheThreeKingdoms.service.dto.PlayerDto;
import com.waterball.LegendsOfTheThreeKingdoms.domain.*;
import com.waterball.LegendsOfTheThreeKingdoms.repository.InMemoryGameRepository;
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

    public GameDto startGame(GameDto gameDto, CreateGamePresenter presenter) {
        Game game = createGame(gameDto);
        game.assignRoles();
        repository.save(game);
        GameDto returnGameDto = convertToGameDto(game);
        presenter.renderGame(returnGameDto);
        return returnGameDto;
    }

    private Game createGame(GameDto gameDto) {
        List<PlayerDto> playerDtos = gameDto.getPlayers();
        List<Player> players = convertToPlayers(playerDtos);
        return new Game(gameDto.getGameId(), players);
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

    public void chooseGeneral(String gameId, String playerId, String generalId, GeneralCardPresenter presenter) {
        Game game = repository.findGameById(gameId);
        GameDto gameDto = convertToGameDto(game.choosePlayerGeneral(playerId, generalId));
        PlayerDto chooseGeneralPlayer = convertToPlayerDto(game.getPlayer(playerId));
        repository.save(game);
        presenter.renderGame(gameDto, chooseGeneralPlayer);
    }

    public List<GeneralCardDto> getGenerals(String gameId, String playerId) {
        Game game = repository.findGameById(gameId);
        //牌堆 a b c d ef g h i j
        GeneralCardDeck generalCardDeck = game.getGeneralCardDeck();

        //主公有三張固定的兩張隨機 劉備、曹操、孫權 + ? + ? || 假設主公抽 劉備，其他人的話可以抽剩下的武將牌(包含曹操與孫權)

        Player player = game.getPlayer(playerId);
        int needCardCount = 3;
        if (player.getRoleCard().getRole() == Role.MONARCH) {
            needCardCount = 5;
        }

        return generalCardDeck.drawGeneralCards(needCardCount)
                .stream().map(this::convertCardToGeneralDto).collect(Collectors.toList());
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

    private GeneralCardDto convertCardToGeneralDto(GeneralCard card) {
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
