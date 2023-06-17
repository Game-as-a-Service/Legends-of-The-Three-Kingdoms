package com.waterball.LegendsOfTheThreeKingdoms.controller;


import com.waterball.LegendsOfTheThreeKingdoms.controller.dto.GameDto;
import com.waterball.LegendsOfTheThreeKingdoms.controller.dto.GeneralCardDto;
import com.waterball.LegendsOfTheThreeKingdoms.controller.dto.PlayerDto;
import com.waterball.LegendsOfTheThreeKingdoms.domain.Game;
import com.waterball.LegendsOfTheThreeKingdoms.domain.GeneralCard;
import com.waterball.LegendsOfTheThreeKingdoms.domain.GeneralCardDeck;
import com.waterball.LegendsOfTheThreeKingdoms.domain.Player;
import com.waterball.LegendsOfTheThreeKingdoms.repository.InMemoryGameRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class GameController {


    private final InMemoryGameRepository repository;

    public GameController(InMemoryGameRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/api/games")
    public ResponseEntity<GameDto> createGame(@RequestBody GameDto gameDto) {
        Game game = new Game();
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

    @GetMapping("/api/games/{gameId}")
    public ResponseEntity<GameDto> getGame(@PathVariable String gameId) {
        return new ResponseEntity<GameDto>(convertToGameDto(repository.findGameById(gameId)), HttpStatus.OK);
    }

    @GetMapping("/api/games/{gameId}/{playerId}/generals")
    public ResponseEntity<List<GeneralCardDto>> getGenerals(@PathVariable String gameId, @PathVariable String playerId) {
        Game game = repository.findGameById(gameId);
        //牌堆 a b c d ef g h i j
        GeneralCardDeck generalCardDeck = game.getGeneralCardDeck();

        //主公有三張固定的兩張隨幾 a、b、c + ? + ? || 假設主公抽 a 其他人的話可以抽剩下的 b c d e f g h i j
        List<GeneralCardDto> generalCardDtoList = generalCardDeck.drawGeneralCards()
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
            playerDto.setRole(player.getRole());
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