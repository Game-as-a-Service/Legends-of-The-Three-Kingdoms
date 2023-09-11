package com.waterball.LegendsOfTheThreeKingdoms.controller;

import com.waterball.LegendsOfTheThreeKingdoms.controller.dto.GameResponse;
import com.waterball.LegendsOfTheThreeKingdoms.controller.dto.PlayerViewModel;
import com.waterball.LegendsOfTheThreeKingdoms.domain.generalcard.GeneralCard;
import com.waterball.LegendsOfTheThreeKingdoms.domain.rolecard.Role;
import com.waterball.LegendsOfTheThreeKingdoms.domain.rolecard.RoleCard;

import java.util.ArrayList;
import java.util.List;

public class TestGameBuilder {

    GameResponse gameResponse = new GameResponse();
    String gameId;

    List<PlayerViewModel> players = new ArrayList<>();


    public TestGameBuilder() {
        this.gameResponse.setGameId(gameId);
        this.gameResponse.setPlayers(players);
    }

    public static TestGameBuilder newGame() {
        return new TestGameBuilder();
    }

    public GameResponse build() {
        return gameResponse;
    }

    public TestGameBuilder withGameId(String gameId) {
        gameResponse.setGameId(gameId);
        this.gameId = gameId;
        return this;
    }

    public TestGameBuilder players(int requiredPlayers) {
        for (int i = 0; i < requiredPlayers; i++) {
            PlayerViewModel p = new PlayerViewModel();
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

}
