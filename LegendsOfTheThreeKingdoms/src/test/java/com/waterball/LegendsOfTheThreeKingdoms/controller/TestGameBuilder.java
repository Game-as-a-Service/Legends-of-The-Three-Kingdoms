package com.waterball.LegendsOfTheThreeKingdoms.controller;

import ch.qos.logback.classic.spi.LoggingEventVO;
import com.waterball.LegendsOfTheThreeKingdoms.controller.dto.GameDto;
import com.waterball.LegendsOfTheThreeKingdoms.controller.dto.PlayerDto;
import com.waterball.LegendsOfTheThreeKingdoms.domain.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class TestGameBuilder {

    GameDto gameDto = new GameDto();
    List<PlayerDto> players = new ArrayList<>();


    public TestGameBuilder() {
        this.gameDto.setGameId("my-id");
        this.gameDto.setPlayers(players);
    }

    public static TestGameBuilder newGame() {
        return new TestGameBuilder();
    }

    public GameDto build() {
        return gameDto;
    }

    public TestGameBuilder players(int requiredPlayers) {
        String[] players = {"player-a", "player-b", "player-c", "player-d"};
        Arrays.stream(players).limit(requiredPlayers).map(id -> {
            PlayerDto p = new PlayerDto();
            p.setId(id);
            return p;
        }).forEach(this.players::add);
        return this;
    }

    public TestGameBuilder withPlayerRoles(String... roles) {
        for (int i = 0; i < roles.length; i++) {
            this.players.get(i).setRole(roles[i]);
        }
        return this;
    }
}
