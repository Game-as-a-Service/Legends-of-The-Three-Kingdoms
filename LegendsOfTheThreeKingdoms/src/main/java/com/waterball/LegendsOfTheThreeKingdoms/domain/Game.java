package com.waterball.LegendsOfTheThreeKingdoms.domain;

import com.waterball.LegendsOfTheThreeKingdoms.domain.generalcard.GeneralCard;
import com.waterball.LegendsOfTheThreeKingdoms.domain.generalcard.GeneralCardDeck;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.Deck;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.Graveyard;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.HandCard;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.BloodCard;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.Player;
import com.waterball.LegendsOfTheThreeKingdoms.domain.rolecard.Role;
import com.waterball.LegendsOfTheThreeKingdoms.domain.rolecard.RoleCard;
import com.waterball.LegendsOfTheThreeKingdoms.utils.ShuffleWrapper;

import java.util.*;
import java.util.stream.Collectors;

public class Game {

    private String gameId;
    private List<Player> players;
    private final GeneralCardDeck generalCardDeck = new GeneralCardDeck();
    private Deck deck = new Deck();
    private Graveyard graveyard = new Graveyard();
    private SeatingChart seatingChart;
    private Round currentRound;

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
        seatingChart = new SeatingChart(players);
    }

    public void setDeck(Deck deck) {
        this.deck = deck;
    }

    public Player getPlayer(String playerId) {
        return players.stream().filter(p -> p.getId().equals(playerId)).findFirst().orElseThrow();
    }

    public GeneralCardDeck getGeneralCardDeck() {
        return generalCardDeck;
    }

    public void assignRoles() {
        if (players.size() < 4) {
            throw new IllegalStateException("The number of players must bigger than 4.");
        }
        List<RoleCard> roleCards = Arrays.stream(RoleCard.ROLES.get(players.size())).collect(Collectors.toList());
        ShuffleWrapper.shuffle(roleCards);
        for (int i = 0; i < roleCards.size(); i++) {
            players.get(i).setRoleCard(roleCards.get(i));
        }
    }

    public void setPlayerGeneral(String playerId, String generalId) {
        Player player = getPlayer(playerId);
        GeneralCard generalCard = GeneralCard.generals.get(generalId);
        player.setGeneralCard(generalCard);
    }

    public void assignHpToPlayers() {
        players.forEach(p -> {
            int healthPoint = p.getRoleCard().getRole().equals(Role.MONARCH) ? 1 : 0;
            p.setBloodCard(new BloodCard(p.getGeneralCard().getHealthPoint() + healthPoint));
        });
    }

    //連 websocket Server 做好狀態推給前端 ?
    public void assignHandCardToPlayers() {
        players.forEach(player -> {
            player.getHand().setCards(deck.deal(4));
        });
        currentRound = new Round(players.get(0));
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public void drawCardToPlayer(String playerId) {
        Player player = getPlayer(playerId);
        refreshDeckWhenCardsNumLessThen(2);
        player.getHand().addCardToHand(deck.deal(2));
        currentRound.setPhase(Phase.Action);
    }

    private void refreshDeckWhenCardsNumLessThen(int requiredCardNum) {
        if (isDeckLessThanCardNum(requiredCardNum)) deck.add(graveyard.getGraveYardCards());
    }

    private boolean isDeckLessThanCardNum(int requiredCardNum) {
        return deck.isDeckLessThanCardNum(requiredCardNum);
    }

    public void setGraveyard(Graveyard graveyard) {
        this.graveyard = graveyard;
    }

    public void playerPlayCard(String playerId, String cardId, String targetPlayerId) {
        Player player = getPlayer(playerId);
        Player targetPlayer = getPlayer(targetPlayerId);
        if (!isWithinDistance(player, targetPlayer)) {
            throw new IllegalStateException("Players are not within range.");
        }
        HandCard handCard = player.playCard(cardId);
        handCard.effect(targetPlayer);
    }

    private boolean isWithinDistance(Player player, Player targetPlayer) {
        // 攻擊距離 >= 基礎距離(座位表) + 逃走距離
        int dist = seatingChart.calculateDistance(player, targetPlayer);
        int escapeDist = targetPlayer.judgeEscapeDistance();
        int attackDist = player.judgeAttackDistance();
        return attackDist >= dist + escapeDist;
    }

    public void setDiscardRoundPhase(String playerId) {
        if (currentRound == null || !playerId.equals(currentRound.getCurrentPlayer().getId())) {
            throw new IllegalStateException(String.format("currentRound is null or current player not %s", playerId));
        }
        currentRound.setPhase(Phase.Discard);
    }

    public Phase getCurrentRoundPhase() {
        return currentRound.getPhase();
    }

    public Player getCurrentRoundPlayer() {
        return currentRound.getCurrentPlayer();
    }

    public void judgePlayerShouldDelay() {
        Player player = currentRound.getCurrentPlayer();
        if (!player.hasAnyDelayScrollCard()){
            currentRound.setPhase(Phase.Drawing);
        }
    }

    public void judgePlayerShouldDiscardCard() {
        Player player = currentRound.getCurrentPlayer();
        if (!currentRound.getPhase().equals(Phase.Discard)){
            throw new RuntimeException();
        }
        if (player.handCardSizeBiggerThanHP()){
            //TODO: 玩家選擇要丟的牌
        }
        Player nextPlayer = seatingChart.getNextPlayer(player);
        currentRound = new Round(nextPlayer);
    }
}

