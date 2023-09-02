package com.waterball.LegendsOfTheThreeKingdoms.domain;

import com.waterball.LegendsOfTheThreeKingdoms.domain.events.*;
import com.waterball.LegendsOfTheThreeKingdoms.domain.gamephase.*;
import com.waterball.LegendsOfTheThreeKingdoms.domain.generalcard.GeneralCard;
import com.waterball.LegendsOfTheThreeKingdoms.domain.generalcard.GeneralCardDeck;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.Deck;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.Graveyard;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.HandCard;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.BloodCard;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.Hand;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.HealthStatus;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.Player;
import com.waterball.LegendsOfTheThreeKingdoms.domain.rolecard.Role;
import com.waterball.LegendsOfTheThreeKingdoms.domain.rolecard.RoleCard;
import com.waterball.LegendsOfTheThreeKingdoms.presenter.MonarchChooseGeneralCardPresenter;
import com.waterball.LegendsOfTheThreeKingdoms.utils.ShuffleWrapper;

import java.util.ArrayList;
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

    public Game(String gameId, List<Player> players) {
        setGameId(gameId);
        setPlayers(players);
        enterPhase(new Initial(this));
    }

    public Game() {

    }

    public SeatingChart getSeatingChart() {
        return seatingChart;
    }

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

    public String getMonarchPlayerId() {
        return players.stream()
                .filter(p -> Role.MONARCH.equals(p.getRoleCard().getRole()))
                .findFirst()
                .map(Player::getId)
                .orElseThrow(IllegalArgumentException::new);
    }

    public GeneralCardDeck getGeneralCardDeck() {
        return generalCardDeck;
    }

    public List<DomainEvent> assignRoles() {
        if (players.size() < 4) {
            throw new IllegalStateException("The number of players must bigger than 4.");
        }
        List<RoleCard> roleCards = Arrays.stream(RoleCard.ROLES.get(players.size())).collect(Collectors.toList());
        ShuffleWrapper.shuffle(roleCards);
        for (int i = 0; i < roleCards.size(); i++) {
            players.get(i).setRoleCard(roleCards.get(i));
        }
        AssignRoleEvent assignRoleEvent = new AssignRoleEvent();
        return List.of(assignRoleEvent);
    }

    public GetMonarchGeneralCardsEvent getMonarchCanChooseGeneralCards() {
        GeneralCardDeck generalCardDeck = getGeneralCardDeck();
        List<GeneralCard> generalCards = generalCardDeck.drawGeneralCards(5);
        return new GetMonarchGeneralCardsEvent(generalCards);
    }

    public List<DomainEvent> monarchChoosePlayerGeneral(String playerId, String generalId) {
        Player player = getPlayer(playerId);
        if (!player.getRoleCard().getRole().equals(Role.MONARCH)) {
            throw new RuntimeException(String.format("Player Id %s not MONARCH.", playerId));
        }
        GeneralCard generalCard = GeneralCard.generals.get(generalId);
        player.setGeneralCard(generalCard);
        DomainEvent monarchChooseGeneralCardEvent = new MonarchChooseGeneralCardEvent(generalCard, String.format("主公已選擇 %s", generalCard.getGeneralName()), gameId, players.stream().map(Player::getId).collect(Collectors.toList()));

        List<DomainEvent> getGeneralCardEventByOthers = getOtherCanChooseGeneralCards();
        getGeneralCardEventByOthers.add(monarchChooseGeneralCardEvent);

        return getGeneralCardEventByOthers;
    }

    private List<DomainEvent> getOtherCanChooseGeneralCards() {
        return players.stream()
                .filter(p -> !p.getRoleCard().getRole().equals(Role.MONARCH))
                .map(p -> {
                    List<GeneralCard> generalCards = generalCardDeck.drawGeneralCards(3);
                  return new GetGeneralCardByOthersEvent(p.getId(), generalCards);
             }).collect(Collectors.toList());
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
            player.setHand(new Hand());
            player.getHand().setCards(deck.deal(4));
        });
        currentRound = new Round(players.get(0));
        this.enterPhase(new Normal(this));
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
        gamePhase.execute(playerId, cardId, targetPlayerId, playType);
    }

    public void playerDeadSettlement() {
        Player deathPlayer = currentRound.getDyingPlayer();
        if (deathPlayer.getRoleCard().getRole().equals(Role.MONARCH)) {
            this.enterPhase(new GameOver(this));
            gamePhase.execute();
            // TODO 主動推反賊獲勝訊息給前端
        }
    }

    public void judgementHealthStatus(Player targetPlayer) {
        if (targetPlayer.getHP() <= 0) {
            targetPlayer.setHealthStatus(HealthStatus.DYING);
            currentRound.setActivePlayer(targetPlayer);
            currentRound.setDyingPlayer(targetPlayer);
            this.enterPhase(new GeneralDying(this));
            askActivePlayerPlayPeachCard();
        }
    }

    public boolean isWithinDistance(Player player, Player targetPlayer) {
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

    public Player getPrePlayer(Player player) {
        return seatingChart.getPrePlayer(player);
    }

    public Player getNextPlayer(Player player) {
        return seatingChart.getNextPlayer(player);
    }

    public void enterPhase(GamePhase gamePhase) {
        this.gamePhase = gamePhase;
    }
}

