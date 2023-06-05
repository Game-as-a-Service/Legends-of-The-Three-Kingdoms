package com.waterball.LegendsOfTheThreeKingdoms.controller.dto;

import java.io.Serializable;
import java.util.List;

public class GameDto implements Serializable {
    private String gameId;
    private List<PlayerDto> players;

    public GameDto() {
    }

    public GameDto(String gameId, List<PlayerDto> players) {
        this.gameId = gameId;
        this.players = players;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public List<PlayerDto> getPlayers() {
        return players;
    }

    public void setPlayers(List<PlayerDto> players) {
        this.players = players;
    }
}
