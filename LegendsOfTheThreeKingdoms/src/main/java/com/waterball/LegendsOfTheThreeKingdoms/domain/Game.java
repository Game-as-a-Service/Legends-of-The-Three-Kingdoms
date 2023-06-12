package com.waterball.LegendsOfTheThreeKingdoms.domain;

import com.waterball.LegendsOfTheThreeKingdoms.controller.dto.PlayerDto;
import com.waterball.LegendsOfTheThreeKingdoms.utils.GameRoleAssignment;

import java.util.List;

public class Game {

    private String gameId;
    private List<Player> players;
    private GeneralCardDeck generalCardDeck = new GeneralCardDeck();

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
        // TODO fix it
        String[] roles = new GameRoleAssignment().assignRoles(4);
        for (int i = 0; i < roles.length; i++) {
            players.get(i).setRole(roles[i]);
        }
    }

    public GeneralCardDeck getGeneralCardDeck() {
        return generalCardDeck;
    }

    public void setPlayerGeneral(String playerId, String generalId) {

    }

    public Player getPlayer(String playerId) {
        return players.stream().filter(p -> p.getId().equals(playerId)).findFirst().orElseThrow();
    }
}
