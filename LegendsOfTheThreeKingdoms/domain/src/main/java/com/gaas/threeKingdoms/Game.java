package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.behavior.PlayCardBehaviorHandler;
import com.gaas.threeKingdoms.behavior.behavior.BorrowedSwordBehavior;
import com.gaas.threeKingdoms.behavior.behavior.DyingAskPeachBehavior;
import com.gaas.threeKingdoms.behavior.handler.*;
import com.gaas.threeKingdoms.effect.EightDiagramTacticEquipmentEffectHandler;
import com.gaas.threeKingdoms.effect.EquipmentEffectHandler;
import com.gaas.threeKingdoms.effect.QilinBowEquipmentEffectHandler;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.gamephase.*;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.generalcard.GeneralCardDeck;
import com.gaas.threeKingdoms.handcard.*;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.WeaponCard;
import com.gaas.threeKingdoms.player.BloodCard;
import com.gaas.threeKingdoms.player.Hand;
import com.gaas.threeKingdoms.player.HealthStatus;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.rolecard.RoleCard;
import com.gaas.threeKingdoms.round.Round;
import com.gaas.threeKingdoms.round.RoundPhase;
import com.gaas.threeKingdoms.round.Stage;
import com.gaas.threeKingdoms.utils.ShuffleWrapper;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Builder
@AllArgsConstructor
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
    private EquipmentEffectHandler equipmentEffectHandler;

    public Game(String gameId, List<Player> players) {
        this();
        setGameId(gameId);
        setPlayers(players);
        enterPhase(new Initial(this));
    }

    public Game() {
        playCardHandler = new DyingAskPeachBehaviorHandler(new PeachBehaviorHandler(new NormalActiveKillBehaviorHandler(new MinusMountsBehaviorHandler(new PlusMountsBehaviorHandler(new EquipWeaponBehaviorHandler(new EquipArmorBehaviorHandler(new BarbarianInvasionBehaviorHandler(new BorrowedSwordBehaviorHandler(null, this), this), this), this), this), this), this), this), this);
        equipmentEffectHandler = new EightDiagramTacticEquipmentEffectHandler(new QilinBowEquipmentEffectHandler(null, this), this);
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
                        .orElseThrow(() -> new IllegalStateException("還有沒有玩家是主公，請確認主公狀態"))
        );

        this.enterPhase(new Normal(this));

        RoundEvent roundEvent = new RoundEvent(currentRound);

        List<PlayerEvent> playerEvents = players.stream().map(p ->
                new PlayerEvent(p.getId(),
                        p.getGeneralCard().getGeneralId(),
                        p.getRoleCard().getRole().getRoleName(),
                        p.getHP(),
                        new HandEvent(p.getHandSize(), p.getHand().getCards().stream().map(handCard -> handCard.getId()).collect(Collectors.toList())),
                        p.getEquipment().getAllEquipmentCardIds(),
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
            int maxHp = p.getGeneralCard().getHealthPoint() + healthPoint;
            p.setBloodCard(new BloodCard(maxHp));
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
                        p.getGeneralCard().getGeneralId(),
                        p.getRoleCard().getRole().getRoleName(),
                        p.getHP(),
                        new HandEvent(p.getHandSize(), p.getHand().getCards().stream().map(HandCard::getId).collect(Collectors.toList())),
                        p.getEquipment().getAllEquipmentCardIds(),
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
        checkIsCurrentRoundValid(playerId);

        if (!topBehavior.isEmpty()) {
            Behavior behavior = topBehavior.peek();
            List<DomainEvent> acceptedEvent = behavior.responseToPlayerAction(playerId, targetPlayerId, cardId, playType);
            //  確認topBehavior是否有需要pop掉的behavior
            removeCompletedBehaviors();
            return acceptedEvent;
        }
        List<String> reActionPlayer = new ArrayList<>();
        reActionPlayer.add(targetPlayerId);
        Behavior behavior = playCardHandler.handle(playerId, cardId, reActionPlayer, playType);
        if (behavior.isTargetPlayerNeedToResponse()) {
            updateTopBehavior(behavior);
        }
        return behavior.playerAction();
    }

    public void removeCompletedBehaviors() {
        while (!topBehavior.isEmpty()) {
            Behavior nextBehavior = topBehavior.peek();
            if (nextBehavior.isOneRound()) {
                topBehavior.pop();
            } else {
                break;
            }
        }
    }

    private void checkIsCurrentRoundValid(String playerId) {
        Player activePlayer = currentRound.getActivePlayer();
        if (!activePlayer.getId().equals(playerId)) {
            throw new IllegalStateException("ActivePlayer is not " + playerId + " , now ActivePlayer is " + activePlayer.getId());
        }
    }

    public List<DomainEvent> playerUseEquipment(String playerId, String cardId, String targetPlayerId, EquipmentPlayType playType) {
        return Optional.ofNullable(equipmentEffectHandler.handle(playerId, cardId, targetPlayerId, playType)).orElse(new ArrayList<>());
    }

    public List<DomainEvent> playerChooseHorseForQilinBow(String playerId, String cardId) {
        Behavior waitingQilinBowResponsebehavior = topBehavior.pop(); //  麒麟弓 behavior
        Behavior nomralKillbehavior = topBehavior.peek(); //  NormalKill
        List<DomainEvent> qilingBowEvents = waitingQilinBowResponsebehavior.responseToPlayerAction(playerId, nomralKillbehavior.getReactionPlayers().get(0), cardId, EquipmentPlayType.ACTIVE.getPlayType());

        List<DomainEvent> normalKillEvents = nomralKillbehavior.responseToPlayerAction(nomralKillbehavior.getReactionPlayers().get(0), waitingQilinBowResponsebehavior.getCurrentReactionPlayer().getId(), cardId, PlayType.QilinBow.getPlayType());
        if (nomralKillbehavior.isOneRound()) { // 沒人死
            topBehavior.pop();
        }

        return Stream.of(qilingBowEvents, normalKillEvents, getGameStatusEventInList("選擇馬")).flatMap(Collection::stream).collect(Collectors.toList());
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

    public boolean isInAttackRange(Player player, Player targetPlayer) {
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

        if (!topBehavior.isEmpty()) {
            throw new IllegalStateException(String.format("current topBehavior is not null size[%s]", topBehavior.size()));
        }

        resetActivePlayer();

        List<PlayerEvent> playerEvents = players.stream().map(p ->
                new PlayerEvent(p.getId(),
                        p.getGeneralCard().getGeneralId(),
                        p.getRoleCard().getRole().getRoleName(),
                        p.getHP(),
                        new HandEvent(p.getHandSize(), p.getHand().getCards().stream().map(HandCard::getId).collect(Collectors.toList())),
                        p.getEquipment().getAllEquipmentCardIds(),
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

    // TODO : 看這些參數可不可以抽到 behavior就好，這樣只需要傳behavior就可以
    public List<DomainEvent> getDamagedEvent(String playerId,
                                             String targetPlayerId,
                                             String cardId,
                                             HandCard card,
                                             String playType,
                                             int originalHp,
                                             Player damagedPlayer,
                                             Round currentRound,
                                             Behavior behavior) {
        card.effect(damagedPlayer);
        PlayerDamagedEvent playerDamagedEvent = createPlayerDamagedEvent(originalHp, damagedPlayer);
        List<DomainEvent> events = new ArrayList<>();
        if (damagedPlayer.isStillAlive()) {
            currentRound.setActivePlayer(currentRound.getCurrentRoundPlayer());
            PlayCardEvent playCardEvent = new PlayCardEvent("不出牌", playerId, targetPlayerId, cardId, playType);
            behavior.setIsOneRound(true);
            events.addAll(List.of(playCardEvent, playerDamagedEvent));
            return events;
        } else {
            PlayerDyingEvent playerDyingEvent = createPlayerDyingEvent(damagedPlayer);
            AskPeachEvent askPeachEvent = createAskPeachEvent(damagedPlayer, damagedPlayer);
            this.enterPhase(new GeneralDying(this));
            currentRound.setDyingPlayer(damagedPlayer);
            currentRound.setActivePlayer(damagedPlayer);
            PlayCardEvent playCardEvent = new PlayCardEvent("不出牌", playerId, targetPlayerId, cardId, playType);
            behavior.setIsOneRound(false);

            if (getGamePhase() instanceof GeneralDying) {
                updateTopBehavior(new DyingAskPeachBehavior(this, damagedPlayer, getPlayers().stream().map(Player::getId).toList(),
                        damagedPlayer, cardId, playType, null));
            }
            events.addAll(List.of(playCardEvent, playerDamagedEvent, playerDyingEvent, askPeachEvent));
            return events;
        }
    }

    private PlayerDyingEvent createPlayerDyingEvent(Player player) {
        return new PlayerDyingEvent(player.getId());
    }

    private AskPeachEvent createAskPeachEvent(Player player, Player dyingPlayer) {
        return new AskPeachEvent(player.getId(), dyingPlayer.getId());
    }

    private PlayerDamagedEvent createPlayerDamagedEvent(int originalHp, Player damagedPlayer) {
        return new PlayerDamagedEvent(damagedPlayer.getId(), originalHp, damagedPlayer.getHP());
    }

    public Stack<Behavior> getTopBehavior() {
        return topBehavior;
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

    public void removeDyingPlayer(Player player) {
        seatingChart.getPlayers().remove(player);
    }

    public void setDeck(Deck deck) {
        this.deck = deck;
    }

    public Player getPlayer(String playerId) {
        return players.stream().filter(p -> p.getId().equals(playerId)).findFirst().orElseThrow();
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

    public Behavior peekTopBehaviorSecondElement() {
        if (topBehavior.size() < 2) {
            return null;
        }
        return topBehavior.get(topBehavior.size() - 2);
    }

    public void removeTopBehavior() {
        topBehavior.pop();
    }

    public void updateTopBehavior(Behavior behavior) {
        if (behavior != null)
            topBehavior.add(behavior);
    }

    public boolean isTopBehaviorEmpty() {
        return topBehavior.empty();
    }

    public HandCard drawCardForEightDiagramTactic() {
        refreshDeckWhenCardsNumLessThen(1);
        List<HandCard> cards = deck.deal(1);
        return cards.get(0);
    }

    public GameStatusEvent getGameStatusEvent(String message) {
        List<PlayerEvent> playerEvents = getPlayers().stream().map(PlayerEvent::new).toList();
        RoundEvent roundEvent = new RoundEvent(currentRound);
        return new GameStatusEvent(gameId, playerEvents, roundEvent, gamePhase.getPhaseName(), message);
    }

    public List<DomainEvent> getGameStatusEventInList(String message) {
        return List.of(getGameStatusEvent(message));
    }

    public List<DomainEvent> useBorrowedSwordEffect(String currentPlayerId, String borrowedPlayerId, String attackTargetPlayerId) {
        Behavior behavior = topBehavior.peek();
        Player borrowedPlayer = getPlayer(borrowedPlayerId);
        if (behavior instanceof BorrowedSwordBehavior &&
            currentRound.getActivePlayer().getId().equals(currentPlayerId) &&
            isPlayerHasWeapon(borrowedPlayerId)
        ) {
            if (!isInAttackRange(borrowedPlayer, getPlayer(attackTargetPlayerId))) {
                throw new IllegalStateException(String.format("%s 不在攻擊範圍", attackTargetPlayerId));
            }

            //判斷B有沒有殺，若玩家B沒出殺，則玩家A取得玩家B當前的武器
            if (borrowedPlayer.getHand().getCards().stream().noneMatch(card -> card instanceof Kill)) {
                List<DomainEvent> acceptedEvent = behavior.responseToPlayerAction(borrowedPlayerId, currentPlayerId, "",  PlayType.SKIP.getPlayType());
                removeCompletedBehaviors();
                return acceptedEvent;
            }

            currentRound.setActivePlayer(borrowedPlayer);
            return List.of(new AskKillEvent(borrowedPlayerId), getGameStatusEvent(String.format("要求 %s 出殺", borrowedPlayerId)));
        }
        throw new IllegalStateException("UseBorrowedSwordEffect error.");
    }

    private boolean isPlayerHasWeapon(String playerId) {
        return getPlayer(playerId).getEquipmentWeaponCard() != null;
    }
}

