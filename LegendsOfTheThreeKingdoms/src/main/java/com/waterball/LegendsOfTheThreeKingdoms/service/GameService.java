package com.waterball.LegendsOfTheThreeKingdoms.service;

import com.waterball.LegendsOfTheThreeKingdoms.domain.generalcard.GeneralCard;
import com.waterball.LegendsOfTheThreeKingdoms.domain.generalcard.GeneralCardDeck;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.Hand;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.Player;
import com.waterball.LegendsOfTheThreeKingdoms.domain.rolecard.Role;
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

    public GameDto startGame(GameDto gameDto) {
        Game game = createGame();
        game.setGameId(gameDto.getGameId());
        List<PlayerDto> playerDtos = gameDto.getPlayers();
        List<Player> players = new ArrayList<>();
        for (PlayerDto playerDto : playerDtos) {
            Player player = new Player();
            player.setId(playerDto.getId());
            player.setHand(new Hand());
            players.add(player);
        }
        game.setPlayers(players);
        game.assignRoles();
        repository.save(game);
        return convertToGameDto(game);
    }

    private Game createGame() {
        return new Game();
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

    public PlayerDto chooseGeneral(String gameId, String playerId, String generalId) {
        Game game = repository.findGameById(gameId);
        game.choosePlayerGeneral(playerId, generalId);
        repository.save(game);
        return convertToPlayerDto(game.getPlayer(playerId));
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

    private GameDto convertToGameDto(Game game) {
        String gameId = game.getGameId();
        List<PlayerDto> playerDtos = convertToPlayerDtos(game.getPlayers());
        GameDto gameDto = new GameDto();
        gameDto.setGameId(gameId);
        gameDto.setPlayers(playerDtos);
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
}
