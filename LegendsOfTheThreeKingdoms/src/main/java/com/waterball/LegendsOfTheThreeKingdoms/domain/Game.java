package com.waterball.LegendsOfTheThreeKingdoms.domain;

import com.waterball.LegendsOfTheThreeKingdoms.controller.dto.PlayerDto;
import com.waterball.LegendsOfTheThreeKingdoms.utils.GameRoleAssignment;

import java.util.List;

public class Game {

    private String gameId;
    private List<Player> players;

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public void assignRoles() {
        Role[] roles = GameRoleAssignment.assignRoles(players.size());
        for (int i = 0; i < roles.length; i++) {
            players.get(i).setRole(roles[i]);
        }
    }
}
