package com.waterball.LegendsOfTheThreeKingdoms.domain;

import com.waterball.LegendsOfTheThreeKingdoms.domain.gamephase.GamePhase;
import com.waterball.LegendsOfTheThreeKingdoms.domain.gamephase.GeneralDying;
import com.waterball.LegendsOfTheThreeKingdoms.domain.generalcard.GeneralCard;
import com.waterball.LegendsOfTheThreeKingdoms.domain.generalcard.GeneralCardDeck;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.Deck;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.Graveyard;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.HandCard;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.BloodCard;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.HealthStatus;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.Player;
import com.waterball.LegendsOfTheThreeKingdoms.domain.rolecard.Role;
import com.waterball.LegendsOfTheThreeKingdoms.domain.rolecard.RoleCard;
import com.waterball.LegendsOfTheThreeKingdoms.utils.ShuffleWrapper;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Game {

    private String gameId;
    private List<Player> players;
    private final GeneralCardDeck generalCardDeck = new GeneralCardDeck();
    private Deck deck = new Deck();
    private Graveyard graveyard = new Graveyard();
    private SeatingChart seatingChart;
    private Round currentRound;
    private GamePhase gamePhase;

    private List<Player> winners;

    public List<Player> getWinners() {
        return winners;
    }

    public void setWinners(List<Player> winners) {
        this.winners = winners;
    }

    public GamePhase getGamePhase() {
        return gamePhase;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Graveyard getGraveyard() {
        return graveyard;
    }

    public Round getCurrentRound() {
        return currentRound;
    }

    public void setCurrentRound(Round currentRound) {
        this.currentRound = currentRound;
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

    public void choosePlayerGeneral(String playerId, String generalId) {
        Player player = getPlayer(playerId);
        GeneralCard generalCard = GeneralCard.generals.get(generalId);
        player.setGeneralCard(generalCard);
    }

    public void assignHpToPlayers() {
        players.forEach(p -> {
            int healthPoint = p.getRoleCard().getRole().equals(Role.MONARCH) ? 1 : 0;
            p.setBloodCard(new BloodCard(p.getGeneralCard().getHealthPoint() + healthPoint));
            p.setHealthStatus(HealthStatus.ALIVE);
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
        currentRound.setRoundPhase(RoundPhase.Action);
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
        playerPlayCard(playerId, cardId, targetPlayerId, "");
    }

    public void playerPlayCard(String playerId, String cardId, String targetPlayerId, String playType) {
        if (gamePhase == GamePhase.GeneralDying) {
            if ("skip".equals(playType)) {
                Player dyingPlayer = ((GeneralDying) gamePhase.getAction()).getDyingPlayer();
                if (getActivePlayer() == seatingChart.getPrePlayer(dyingPlayer)) {
                    dyingPlayer.setHealthStatus(HealthStatus.DEATH);
                    playerDeadSettlement();
                    // TODO 死亡結算。
                    return;
                }
                currentRound.setActivePlayer(seatingChart.getNextPlayer(getActivePlayer()));
                gamePhase.execute(this);
            } else {
                // TODO
                // 玩家補血
                // gamePhase 切換成 回合狀態;
                // activePlayer = null;
                // gamePhase.execute(this);
            }
            return;
        }

        Player player = getPlayer(playerId);
        Player targetPlayer = getPlayer(targetPlayerId);
        if (!isWithinDistance(player, targetPlayer)) {
            throw new IllegalStateException("Players are not within range.");
        }
        if (currentRound.checkPlayedCardIsValid(cardId)) {
            HandCard handCard = player.playCard(cardId);
            handCard.effect(targetPlayer);
            graveyard.add(handCard);
            judgementHealthStatus(targetPlayer);
        }
    }

    private void playerDeadSettlement() {
        Player deathPlayer = ((GeneralDying) gamePhase.getAction()).getDyingPlayer();
        if (deathPlayer.getRoleCard().getRole().equals(Role.MONARCH)) {
            gamePhase = GamePhase.GameOver;
            gamePhase.execute(this);
            // TODO 主動推反賊獲勝訊息給前端
        }
    }

    private void judgementHealthStatus(Player targetPlayer) {
        if (targetPlayer.getHP() <= 0) {
            targetPlayer.setHealthStatus(HealthStatus.DYING);
            currentRound.setActivePlayer(targetPlayer);
            gamePhase = GamePhase.GeneralDying;
            ((GeneralDying) gamePhase.getAction()).setPlayer(targetPlayer);
            gamePhase.execute(this);
        }
    }

    private boolean isWithinDistance(Player player, Player targetPlayer) {
        // 攻擊距離 >= 基礎距離(座位表) + 逃走距離
        int dist = seatingChart.calculateDistance(player, targetPlayer);
        int escapeDist = targetPlayer.judgeEscapeDistance();
        int attackDist = player.judgeAttackDistance();
        return attackDist >= dist + escapeDist;
    }

    public void setDiscardRoundPhase(String playerId) {
        if (currentRound == null || !playerId.equals(currentRound.getCurrentRoundPlayer().getId())) {
            throw new IllegalStateException(String.format("currentRound is null or current player not %s", playerId));
        }
        currentRound.setRoundPhase(RoundPhase.Discard);
    }

    public RoundPhase getCurrentRoundPhase() {
        return currentRound.getRoundPhase();
    }

    public Player getCurrentRoundPlayer() {
        return currentRound.getCurrentRoundPlayer();
    }

    public void judgePlayerShouldDelay() {
        Player player = currentRound.getCurrentRoundPlayer();
        if (!player.hasAnyDelayScrollCard()) {
            currentRound.setRoundPhase(RoundPhase.Drawing);
        }
    }

    public void judgePlayerShouldDiscardCard() {
        Player player = currentRound.getCurrentRoundPlayer();
        if (!currentRound.getRoundPhase().equals(RoundPhase.Discard)) {
            throw new RuntimeException();
        }
        if (player.isHandCardSizeBiggerThanHP()) {
            //TODO: 通知玩家需要棄牌
            //TODO: 玩家選擇要丟的牌，通知玩家棄牌，回傳棄牌Event。
        } else {
            goNextRound(player);
        }
    }

    public void playerDiscardCard(List<String> cardIds) {
        Player player = currentRound.getCurrentRoundPlayer();
        int needToDiscardSize = player.getHandSize() - player.getHP();
        if (cardIds.size() < needToDiscardSize) throw new RuntimeException();
        // todo 判斷這個玩家是否有這些牌
        List<HandCard> discardCards = player.discardCards(cardIds);
        graveyard.add(discardCards);
        goNextRound(player);
    }

    private void goNextRound(Player player) {
        Player nextPlayer = seatingChart.getNextPlayer(player);
        currentRound = new Round(nextPlayer);

    }

    public void askActivePlayerPlayPeachCard() {
        // TODO 通知玩家要出桃
    }


    public Player getActivePlayer() {
        return currentRound.getActivePlayer();
    }
}

