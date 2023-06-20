package com.waterball.LegendsOfTheThreeKingdoms.domain;

import com.waterball.LegendsOfTheThreeKingdoms.controller.dto.PlayerDto;

import java.util.*;
import java.util.stream.IntStream;

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
        if (players.size() < 4) {
            throw new IllegalStateException("The number of players must bigger than 4.");
        }
        List<RoleCard> roleCards = Arrays.asList(RoleCard.ROLES.get(players.size()));
        Collections.shuffle(roleCards, new Random(0));
        for (int i = 0; i < roleCards.size(); i++) {
            players.get(i).setRole(roleCards.get(i));
        }
    }

    public GeneralCardDeck getGeneralCardDeck() {
        return generalCardDeck;
    }

    public void setPlayerGeneral(String playerId, String generalId) {
        Player player = players.stream().filter(p -> p.getId().equals(playerId)).findFirst().orElseThrow();
        int ind = IntStream.range(0, generalCardDeck.getGeneralStack().size())
                        .filter(i -> generalCardDeck.getGeneralStack().get(i).getGeneralID().equals(generalId))
                                .findFirst().orElseThrow();
        GeneralCard generalCard = generalCardDeck.getGeneralStack().remove(ind);
        player.setGeneralCard(generalCard);
    }

    public Player getPlayer(String playerId) {
        return players.stream().filter(p -> p.getId().equals(playerId)).findFirst().orElseThrow();
    }
}
