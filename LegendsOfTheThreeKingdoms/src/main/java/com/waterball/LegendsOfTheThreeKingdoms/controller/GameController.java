package com.waterball.LegendsOfTheThreeKingdoms.controller;


import com.waterball.LegendsOfTheThreeKingdoms.controller.dto.GameDto;
import com.waterball.LegendsOfTheThreeKingdoms.controller.dto.GeneralCardDto;
import com.waterball.LegendsOfTheThreeKingdoms.controller.dto.PlayerDto;
import com.waterball.LegendsOfTheThreeKingdoms.domain.*;
import com.waterball.LegendsOfTheThreeKingdoms.repository.InMemoryGameRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class GameController {


    private final InMemoryGameRepository repository;

    public GameController(InMemoryGameRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/api/games")
    public ResponseEntity<GameDto> createGame(@RequestBody GameDto gameDto) {
        Game game = createGame();
        game.setGameId(gameDto.getGameId());

        List<PlayerDto> playerDtos = gameDto.getPlayers();
        List<Player> players = new ArrayList<>();
        for (PlayerDto playerDto : playerDtos) {
            Player player = new Player();
            player.setId(playerDto.getId());
            players.add(player);
        }
        game.setPlayers(players);
        game.assignRoles();

        repository.save(game);
        return new ResponseEntity<GameDto>(convertToGameDto(game), HttpStatus.OK);
    }

    private static Game createGame() {
        return new Game();
    }

    @GetMapping("/api/games/{gameId}")
    public ResponseEntity<GameDto> getGame(@PathVariable String gameId) {
        return new ResponseEntity<GameDto>(convertToGameDto(repository.findGameById(gameId)), HttpStatus.OK);
    }

    @GetMapping("/api/games/{gameId}/{playerId}/generals")
    public ResponseEntity<List<GeneralCardDto>> getGenerals(@PathVariable String gameId, @PathVariable String playerId) {
        Game game = repository.findGameById(gameId);
        //牌堆 a b c d ef g h i j
        GeneralCardDeck generalCardDeck = game.getGeneralCardDeck();

        //主公有三張固定的兩張隨機 劉備、曹操、孫權 + ? + ? || 假設主公抽 劉備，其他人的話可以抽剩下的武將牌(包含曹操與孫權)

        Player player = game.getPlayer(playerId);
        int needCardCount = 3;
        if (player.getRoleCard().getRole() == Role.MONARCH) {
            needCardCount = 5;
        }

        List<GeneralCardDto> generalCardDtoList = generalCardDeck.drawGeneralCards(needCardCount)
                .stream().map(this::convertCardToGeneralDto).collect(Collectors.toList());
        return ResponseEntity.ok(generalCardDtoList);
    }


    @PostMapping("/api/games/{gameId}/{playerId}/general/{generalId}")
    public ResponseEntity<PlayerDto> chooseGeneral(@PathVariable String gameId, @PathVariable String playerId, @PathVariable String generalId) {
        Game game = repository.findGameById(gameId);
        game.setPlayerGeneral(playerId, generalId);
        repository.save(game);
        Player player = game.getPlayer(playerId);
        return ResponseEntity.ok(convertToPlayerDto(player));
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
        return playerDto;
    }

    private GeneralCardDto convertCardToGeneralDto(GeneralCard card) {

        GeneralCardDto cardDto = new GeneralCardDto();
        cardDto.setGeneralID(card.getGeneralID());
        cardDto.setGeneralName(card.getGeneralName());
        return cardDto;
    }
}