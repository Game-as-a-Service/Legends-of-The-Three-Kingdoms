package com.waterball.LegendsOfTheThreeKingdoms.controller;

import com.waterball.LegendsOfTheThreeKingdoms.domain.player.Hand;
import com.waterball.LegendsOfTheThreeKingdoms.service.dto.GameDto;
import com.waterball.LegendsOfTheThreeKingdoms.service.dto.PlayerDto;
import com.waterball.LegendsOfTheThreeKingdoms.domain.generalcard.GeneralCard;
import com.waterball.LegendsOfTheThreeKingdoms.domain.rolecard.Role;
import com.waterball.LegendsOfTheThreeKingdoms.domain.rolecard.RoleCard;

import java.util.ArrayList;
import java.util.List;

public class TestGameBuilder {

    GameDto gameDto = new GameDto();
    String gameId;
    List<PlayerDto> players = new ArrayList<>();


    public TestGameBuilder() {
        this.gameDto.setGameId(gameId);
        this.gameDto.setPlayers(players);
    }

    public static TestGameBuilder newGame() {
        return new TestGameBuilder();
    }

    public GameDto build() {
        return gameDto;
    }

    public TestGameBuilder withGameId(String gameId) {
        gameDto.setGameId(gameId);
        this.gameId = gameId;
        return this;
    }

    public TestGameBuilder players(int requiredPlayers) {
        for (int i = 0; i < requiredPlayers; i++) {
            PlayerDto p = new PlayerDto();
            p.setHand(new Hand());
            players.add(p);
        }
        return this;
    }

    public TestGameBuilder withPlayerRoles(String... roles) {
        for (int i = 0; i < roles.length; i++) {
            this.players.get(i).setRoleCard(new RoleCard(Role.valueOf(roles[i].toUpperCase())));
        }
        return this;
    }

    public TestGameBuilder withPlayerId(String... ids) {
        for (int i = 0; i < ids.length; i++) {
            this.players.get(i).setId(ids[i]);
        }
        return this;
    }

    //觀光景點
    public TestGameBuilder withPlayerGeneral(String... playerGeneral) {
        for (int i = 0; i < playerGeneral.length; i++) {
            this.players.get(i).setGeneralCard(new GeneralCard(playerGeneral[i], playerGeneral[i], 1));
        }
        return this;
    }
}
