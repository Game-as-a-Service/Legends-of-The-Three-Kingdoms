package com.waterball.LegendsOfTheThreeKingdoms.domain;

import com.waterball.LegendsOfTheThreeKingdoms.domain.behavior.Behavior;
import com.waterball.LegendsOfTheThreeKingdoms.domain.behavior.PlayCardBehaviorHandler;
import com.waterball.LegendsOfTheThreeKingdoms.domain.behavior.handler.DyingAskPeachBehaviorHandler;
import com.waterball.LegendsOfTheThreeKingdoms.domain.behavior.handler.NormalActiveKillBehaviorHandler;
import com.waterball.LegendsOfTheThreeKingdoms.domain.events.*;
import com.waterball.LegendsOfTheThreeKingdoms.domain.gamephase.*;
import com.waterball.LegendsOfTheThreeKingdoms.domain.generalcard.GeneralCard;
import com.waterball.LegendsOfTheThreeKingdoms.domain.generalcard.GeneralCardDeck;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.Deck;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.Graveyard;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.HandCard;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.PlayType;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.BloodCard;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.Hand;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.HealthStatus;
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
    private GamePhase gamePhase;
    private List<Player> winners;
    private PlayCardBehaviorHandler playCardHandler;
    private Stack<Behavior> topBehavior = new Stack<>();

    public Game(String gameId, List<Player> players) {
        playCardHandler = new NormalActiveKillBehaviorHandler(new DyingAskPeachBehaviorHandler(null, this), this);
        setGameId(gameId);
        setPlayers(players);
        enterPhase(new Initial(this));
    }

    public Game() {
        playCardHandler = new NormalActiveKillBehaviorHandler(new DyingAskPeachBehaviorHandler(null, this), this);
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

    public List<DomainEvent> othersChoosePlayerGeneral(String playerId, String generalId) {
        Player player = getPlayer(playerId);
        GeneralCard generalCard = GeneralCard.generals.get(generalId);
        player.setGeneralCard(generalCard);

        if (players.stream().anyMatch(currentPlayer -> currentPlayer.getGeneralCard() == null)) {
            return Collections.emptyList();
        }

        assignHpToPlayers();
        assignHandCardToPlayers();

        currentRound = new Round(
                players.stream()
                .filter(currentPlayer -> currentPlayer.getRoleCard().getRole().equals(Role.MONARCH))
                .findFirst()
                .orElseThrow(()->new IllegalStateException("還有沒有玩家是主公，請確認主公狀態"))
        );

        this.enterPhase(new Normal(this));

        RoundEvent roundEvent = new RoundEvent(currentRound);

        List<PlayerEvent> playerEvents = players.stream().map(p ->
                new PlayerEvent(p.getId(),
                        p.getGeneralCard().getGeneralID(),
                        p.getRoleCard().getRole().getRoleName(),
                        p.getHP(),
                        new HandEvent(p.getHandSize(), p.getHand().getCards().stream().map(handCard -> handCard.getId()).collect(Collectors.toList())),
                        Collections.emptyList(),
                        Collections.emptyList())).toList();

        DomainEvent initialEndEvent = new InitialEndEvent(gameId, playerEvents, roundEvent, this.getGamePhase().getPhaseName());

        List<DomainEvent> domainEvents = playerTakeTurn(getCurrentRoundPlayer());
        List<DomainEvent> combineEvents = new ArrayList<>(List.of(initialEndEvent));
        combineEvents.addAll(domainEvents);

        return combineEvents;
    }

    private List<DomainEvent> playerTakeTurn(Player currentRoundPlayer) {
        DomainEvent roundStartEvent = new RoundStartEvent();
        DomainEvent judgeEvent = judgePlayerShouldDelay();
        DomainEvent drawCardEvent = drawCardToPlayer(currentRoundPlayer);
        return List.of(roundStartEvent, judgeEvent, drawCardEvent);
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
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public DomainEvent drawCardToPlayer(Player player) {
        refreshDeckWhenCardsNumLessThen(2);
        int size = calculatePlayerCanDrawCardSize(player);
        List<HandCard> cards = deck.deal(size);
        player.getHand().addCardToHand(cards);
        currentRound.setRoundPhase(RoundPhase.Action);
        List<String> cardIds = cards.stream().map(HandCard::getId).collect(Collectors.toList());
        String message = String.format("玩家 %s 抽了 %d 張牌", player.getId(), size);

        List<PlayerEvent> playerEvents = players.stream().map(p ->
                new PlayerEvent(p.getId(),
                        p.getGeneralCard().getGeneralID(),
                        p.getRoleCard().getRole().getRoleName(),
                        p.getHP(),
                        new HandEvent(p.getHandSize(), p.getHand().getCards().stream().map(HandCard::getId).collect(Collectors.toList())),
                        Collections.emptyList(),
                        Collections.emptyList())).toList();

        RoundEvent roundEvent = new RoundEvent(
                currentRound.getRoundPhase().toString(),
                currentRound.getCurrentRoundPlayer().getId(),
                Optional.ofNullable(currentRound.getActivePlayer()).map(Player::getId).orElse(""),
                Optional.ofNullable(currentRound.getDyingPlayer()).map(Player::getId).orElse(""),
                currentRound.isShowKill()
        );

        return new DrawCardEvent(
                size,
                cardIds,
                message,
                gameId,
                playerEvents,
                roundEvent,
                gamePhase.getPhaseName(),
                player.getId()
        );
    }

    private int calculatePlayerCanDrawCardSize(Player player) {
        return 2;
    }

    private void refreshDeckWhenCardsNumLessThen(int requiredCardNumber) {
        if (isDeckLessThanCardNum(requiredCardNumber)) deck.add(graveyard.getGraveYardCards());
    }

    private boolean isDeckLessThanCardNum(int requiredCardNum) {
        return deck.isDeckLessThanCardNum(requiredCardNum);
    }

    public void setGraveyard(Graveyard graveyard) {
        this.graveyard = graveyard;
    }

    public List<DomainEvent> playerPlayCard(String playerId, String cardId, String targetPlayerId, String playType) {
        PlayType.checkPlayTypeIsValid(playType);

        if (!topBehavior.isEmpty()) {
            Behavior behavior = topBehavior.peek();
            List<DomainEvent> acceptedEvent = behavior.acceptedTargetPlayerPlayCard(playerId, targetPlayerId, cardId, playType); //throw Exception When isNotValid
            if (behavior.isNeedToPop()) {
                topBehavior.pop();
            } else {
                updateTopBehavior(playCardHandler.handle(playerId, cardId, List.of(targetPlayerId), playType));
            }
            return acceptedEvent;
        }
        Behavior behavior = playCardHandler.handle(playerId, cardId, List.of(targetPlayerId), playType);
        updateTopBehavior(behavior);
        return behavior.askTargetPlayerPlayCard();
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

    public List<DomainEvent> finishAction(String playerId) {
        List<DomainEvent> domainEvents = new ArrayList<>();
        Player currentRoundPlayer = currentRound.getCurrentRoundPlayer();

        if (currentRoundPlayer == null || !playerId.equals(currentRoundPlayer.getId())) {
            throw new IllegalStateException(String.format("currentRound is null or current player not %s", playerId));
        }

        resetActivePlayer();

        List<PlayerEvent> playerEvents = players.stream().map(p ->
                new PlayerEvent(p.getId(),
                        p.getGeneralCard().getGeneralID(),
                        p.getRoleCard().getRole().getRoleName(),
                        p.getHP(),
                        new HandEvent(p.getHandSize(), p.getHand().getCards().stream().map(HandCard::getId).collect(Collectors.toList())),
                        Collections.emptyList(),
                        Collections.emptyList())).toList();

        currentRound.setRoundPhase(RoundPhase.Discard);
        RoundEvent roundEvent = new RoundEvent(currentRound);


        FinishActionEvent finishActionEvent = new FinishActionEvent();
        int currentRoundPlayerDiscardCount = getCurrentRoundPlayerDiscardCount();
        String notifyMessage = String.format("玩家 %s 需要棄 %d 張牌", currentRoundPlayer.getId(), currentRoundPlayerDiscardCount);
        NotifyDiscardEvent notifyDiscardEvent = new NotifyDiscardEvent(notifyMessage, currentRoundPlayerDiscardCount, playerId, currentRoundPlayer.getId(), gameId, playerEvents, roundEvent, gamePhase.getPhaseName());
        domainEvents.add(finishActionEvent);
        domainEvents.add(notifyDiscardEvent);

        if (currentRoundPlayerDiscardCount == 0) {
            domainEvents.add(new RoundEndEvent());
            List<DomainEvent> nextRoundDomainEvents = goNextRound(currentRoundPlayer);
            domainEvents.addAll(nextRoundDomainEvents);
        }

        return domainEvents;
    }

    private void resetActivePlayer() {
        currentRound.setActivePlayer(null);
    }

    public RoundPhase getCurrentRoundPhase() {
        return currentRound.getRoundPhase();
    }

    public Player getCurrentRoundPlayer() {
        return currentRound.getCurrentRoundPlayer();
    }

    private DomainEvent judgePlayerShouldDelay() {
        Player player = currentRound.getCurrentRoundPlayer();
        if (!player.hasAnyDelayScrollCard()) {
            currentRound.setRoundPhase(RoundPhase.Drawing);
        }
        return new JudgementEvent();
    }

    public int getCurrentRoundPlayerDiscardCount() {
        Player player = currentRound.getCurrentRoundPlayer();
        if (!currentRound.getRoundPhase().equals(RoundPhase.Discard)) {
            throw new RuntimeException();
        }
        return player.getDiscardCount();
    }

    public List<DomainEvent> playerDiscardCard(List<String> cardIds) {
        Player player = currentRound.getCurrentRoundPlayer();
        int needToDiscardSize = player.getHandSize() - player.getHP();
        if (cardIds.size() < needToDiscardSize) throw new RuntimeException();
        // todo 判斷這個玩家是否有這些牌
        List<HandCard> discardCards = player.discardCards(cardIds);
        graveyard.add(discardCards);
        String message = String.format("玩家 %s 棄牌", player.getId());
        DomainEvent discardEvent = new DiscardEvent(discardCards, message, player.getId());
        List<DomainEvent> nextRoundEvent = new ArrayList<>(goNextRound(player));
        nextRoundEvent.add(discardEvent);
        nextRoundEvent.add(new RoundEndEvent());
        return nextRoundEvent;
    }

    private List<DomainEvent> goNextRound(Player player) {
        Player nextPlayer = seatingChart.getNextPlayer(player);
        currentRound = new Round(nextPlayer);
        return playerTakeTurn(nextPlayer);
    }

    public void askActivePlayerPlayPeachCard() {
        // TODO 通知玩家要出桃
    }

    public void updateRoundInformation(Player targetPlayer, HandCard card) {
        currentRound.setActivePlayer(targetPlayer);
        currentRound.setCurrentPlayCard(card);
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

    public Behavior peekTopBehavior() {
        return topBehavior.peek();
    }

    public void removeTopBehavior() {
        topBehavior.pop();
    }

    public void updateTopBehavior(Behavior behavior) {
        topBehavior.add(behavior);
    }

    public boolean isTopBehaviorEmpty() {
        return topBehavior.empty();
    }

    public String createGameOverMessage() {
        Player deadPlayer = players.stream().filter(player -> player.getHP() == 0).findFirst().get();
        String gameOverMessage = "";
        if (deadPlayer.getRoleCard().getRole() == Role.MONARCH) {
            for (Player player : players) {
                gameOverMessage += player.getId() + " " + player.getRoleCard().getRole().getRoleName() + "\n";
            }
            gameOverMessage += "反賊獲勝";
        }
        return gameOverMessage;
    }
}

