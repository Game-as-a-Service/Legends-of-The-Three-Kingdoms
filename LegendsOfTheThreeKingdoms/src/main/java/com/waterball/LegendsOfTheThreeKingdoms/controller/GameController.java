package com.waterball.LegendsOfTheThreeKingdoms.controller;


import com.waterball.LegendsOfTheThreeKingdoms.controller.dto.GameDto;
import com.waterball.LegendsOfTheThreeKingdoms.controller.dto.PlayerDto;
import com.waterball.LegendsOfTheThreeKingdoms.domain.Game;
import com.waterball.LegendsOfTheThreeKingdoms.domain.Player;
import com.waterball.LegendsOfTheThreeKingdoms.repository.InMemoryGameRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

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
            PlayerDto playerDto = new PlayerDto();
            playerDto.setId(player.getId());
            playerDto.setRole(player.getRole());
            playerDtos.add(playerDto);
        }
        return playerDtos;
    }

}