package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.behavior.PlayCardBehaviorHandler;
import com.gaas.threeKingdoms.behavior.handler.*;
import com.gaas.threeKingdoms.effect.EightDiagramTacticEquipmentEffectHandler;
import com.gaas.threeKingdoms.effect.EquipmentEffectHandler;
import com.gaas.threeKingdoms.effect.QilinBowEquipmentEffectHandler;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.gamephase.*;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.generalcard.GeneralCardDeck;
import com.gaas.threeKingdoms.handcard.*;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.PlusMountsCard;
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
        equipmentEffectHandler = new EightDiagramTacticEquipmentEffectHandler(null, this);
        playCardHandler = new DyingAskPeachBehaviorHandler(new PeachBehaviorHandler(new NormalActiveKillBehaviorHandler(new MinusMountsBehaviorHandler(new PlusMountsBehaviorHandler(new EquipWeaponBehaviorHandler(new EquipArmorBehaviorHandler(null, this), this), this), this), this), this), this);
        setGameId(gameId);
        setPlayers(players);
        enterPhase(new Initial(this));
    }

    public Game() {
        playCardHandler = new DyingAskPeachBehaviorHandler(new PeachBehaviorHandler(new NormalActiveKillBehaviorHandler(new MinusMountsBehaviorHandler(new PlusMountsBehaviorHandler(new EquipWeaponBehaviorHandler(new EquipArmorBehaviorHandler(null, this), this), this), this), this), this), this);
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

        if (!topBehavior.isEmpty()) {
            Behavior behavior = topBehavior.peek();
            List<DomainEvent> acceptedEvent = behavior.acceptedTargetPlayerPlayCard(playerId, targetPlayerId, cardId, playType); //throw Exception When isNotValid
            if (behavior.isOneRound()) {
                topBehavior.pop();
            } else {
                // 把新打出的牌加到 stack ，如果是使用裝備卡則不會放入
                updateTopBehavior(playCardHandler.handle(playerId, cardId, List.of(targetPlayerId), playType));
            }
            return acceptedEvent;
        }
        Behavior behavior = playCardHandler.handle(playerId, cardId, List.of(targetPlayerId), playType);
        if (behavior.isTargetPlayerNeedToResponse()) {
            updateTopBehavior(behavior);
        }
        List<DomainEvent> events = behavior.askTargetPlayerPlayCard();
        return events;
    }

    public List<DomainEvent> playerUseEquipment(String playerId, String cardId, String targetPlayerId, EquipmentPlayType playType) {
        return Optional.ofNullable(equipmentEffectHandler.handle(playerId, cardId, targetPlayerId, playType)).orElse(new ArrayList<>());
    }

    public List<DomainEvent> playerChooseHorseForQilinBow(String playerId, String cardId) {
        PlayCard playCard = PlayCard.valueOf(cardId);
        if (!playCard.isMountsCard()) {
            throw new IllegalStateException("playCard is not mounts playCard type");
        }
        Player damagedPlayer = getPlayer(peekTopBehavior().getReactionPlayers().get(0));
        if (damagedPlayer.getEquipmentMinusOneMountsCard().getId().equals(cardId) && damagedPlayer.getEquipmentPlusOneMountsCard().getId().equals(cardId)) {
            throw new IllegalStateException("player doesn't equip this mount playCard");
        }

        DomainEvent removeHorseEvent = damagedPlayer.removeMountsCard(playerId,cardId);
        Behavior behavior = topBehavior.pop();
        int originalHp = damagedPlayer.getHP();
        HandCard card = behavior.getCard();

        List<DomainEvent> events = getDamagedEventForEquipmentEffect(card, originalHp, damagedPlayer,currentRound, behavior);
        events.add(removeHorseEvent);
        currentRound.setStage(Stage.Normal);
        return events;
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
        List<PlayerEvent> playerEvents = getPlayers().stream().map(PlayerEvent::new).toList();

        if (damagedPlayer.isStillAlive()) {
            currentRound.setActivePlayer(currentRound.getCurrentRoundPlayer());
            RoundEvent roundEvent = new RoundEvent(currentRound);
            PlayCardEvent playCardEvent = new PlayCardEvent("不出牌", playerId, targetPlayerId, cardId, playType, this.gameId, playerEvents, roundEvent, this.getGamePhase().getPhaseName());
            return List.of(playCardEvent, playerDamagedEvent);
        } else {
            PlayerDyingEvent playerDyingEvent = createPlayerDyingEvent(damagedPlayer);
            AskPeachEvent askPeachEvent = createAskPeachEvent(damagedPlayer, damagedPlayer);
            this.enterPhase(new GeneralDying(this));
            currentRound.setDyingPlayer(damagedPlayer);
            currentRound.setActivePlayer(damagedPlayer);
            RoundEvent roundEvent = new RoundEvent(currentRound);
            PlayCardEvent playCardEvent = new PlayCardEvent("不出牌", playerId, targetPlayerId, cardId, playType, this.getGameId(), playerEvents, roundEvent, this.getGamePhase().getPhaseName());
            behavior.setIsOneRound(false);
            return List.of(playCardEvent, playerDamagedEvent, playerDyingEvent, askPeachEvent);
        }
    }

    public List<DomainEvent> getDamagedEventForEquipmentEffect(HandCard card,
                                                               int originalHp,
                                                               Player damagedPlayer,
                                                               Round currentRound,
                                                               Behavior behavior) {
        List<DomainEvent> domainEvents = new ArrayList<>();
        card.effect(damagedPlayer);
        PlayerDamagedEvent playerDamagedEvent = createPlayerDamagedEvent(originalHp, damagedPlayer);

        if (damagedPlayer.isStillAlive()) {
            currentRound.setActivePlayer(currentRound.getCurrentRoundPlayer());
            RoundEvent roundEvent = new RoundEvent(currentRound);
            domainEvents.add(roundEvent);
            domainEvents.add(playerDamagedEvent);
            return domainEvents;
        } else {
            PlayerDyingEvent playerDyingEvent = createPlayerDyingEvent(damagedPlayer);
            AskPeachEvent askPeachEvent = createAskPeachEvent(damagedPlayer, damagedPlayer);
            this.enterPhase(new GeneralDying(this));
            currentRound.setDyingPlayer(damagedPlayer);
            currentRound.setActivePlayer(damagedPlayer);
            RoundEvent roundEvent = new RoundEvent(currentRound);
            behavior.setIsOneRound(false);
            domainEvents.add(roundEvent);
            domainEvents.add(playerDamagedEvent);
            domainEvents.add(playerDyingEvent);
            domainEvents.add(askPeachEvent);
            return domainEvents;
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
}

