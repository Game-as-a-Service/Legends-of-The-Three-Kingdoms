package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.behavior.PlayCardBehaviorHandler;
import com.gaas.threeKingdoms.behavior.behavior.*;
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
import com.gaas.threeKingdoms.handcard.scrollcard.Contentment;
import com.gaas.threeKingdoms.handcard.scrollcard.Lightning;
import com.gaas.threeKingdoms.handcard.scrollcard.ScrollCard;
import com.gaas.threeKingdoms.handcard.scrollcard.Ward;
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

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.gaas.threeKingdoms.handcard.PlayCard.isWardCard;

@AllArgsConstructor
public class Game {

    private String gameId;
    private List<Player> players;
    private GeneralCardDeck generalCardDeck = new GeneralCardDeck();
    private Deck deck = new Deck();
    private Graveyard graveyard = new Graveyard();
    private SeatingChart seatingChart;
    private Round currentRound;
    private GamePhase gamePhase;
    private List<Player> winners;
    private Stack<Behavior> topBehavior = new Stack<>();
    private PlayCardBehaviorHandler playCardHandler;
    private EquipmentEffectHandler equipmentEffectHandler;

    public Game(String gameId, List<Player> players) {
        this();
        setGameId(gameId);
        setPlayers(players);
        enterPhase(new Initial(this));
    }

    public Game() {
        playCardHandler = new DyingAskPeachBehaviorHandler(new PeachBehaviorHandler(new NormalActiveKillBehaviorHandler(new MinusMountsBehaviorHandler(new PlusMountsBehaviorHandler(new EquipWeaponBehaviorHandler(new EquipArmorBehaviorHandler(new BarbarianInvasionBehaviorHandler(new BorrowedSwordBehaviorHandler(new DuelBehaviorHandler(new DismantleBehaviorHandler(new ContentmentBehaviorHandler(new SomethingForNothingHandler(new ArrowBarrageBehaviorHandler(new PeachGardenBehaviorHandler(new BountifulHarvestHandler(new LightningBehaviorHandler(new SnatchBehaviorHandler(null, this), this), this), this), this), this), this), this), this), this), this), this), this), this), this), this), this), this);
        equipmentEffectHandler = new EightDiagramTacticEquipmentEffectHandler(new QilinBowEquipmentEffectHandler(null, this), this);
    }

    public void initDeck() {
        deck.init();
        generalCardDeck.initGeneralCardDeck();
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

    public List<DomainEvent> playerTakeTurn(Player currentRoundPlayer) {
        List<DomainEvent> events = new ArrayList<>();
        DomainEvent roundStartEvent = new RoundStartEvent();
        events.add(roundStartEvent);
        currentRound.setRoundPhase(RoundPhase.Judgement);
        events.addAll(playerTakeTurnStartInJudgement(currentRoundPlayer));
        return events;
    }

    public List<DomainEvent> playerTakeTurnStartInJudgement(Player currentRoundPlayer) {
        List<DomainEvent> events = new ArrayList<>();
        if (RoundPhase.Judgement.equals(currentRound.getRoundPhase())) {
            List<DomainEvent> judgeEvents = judgePlayerShouldDelay();
            events.addAll(judgeEvents);
            boolean contentmentEventSuccess = judgeEvents.stream()
                    .filter(event -> event instanceof ContentmentEvent)
                    .map(event -> (ContentmentEvent) event)
                    .anyMatch(ContentmentEvent::isSuccess);

            if (RoundPhase.Drawing.equals(currentRound.getRoundPhase())) {
                DomainEvent drawCardEvent = drawCardToPlayer(currentRoundPlayer, !contentmentEventSuccess);
                events.add(drawCardEvent);
                if (contentmentEventSuccess) {
                    events.addAll(finishAction(currentRoundPlayer.getId()));
                } else {
                    events.add(getGameStatusEvent(drawCardEvent.getMessage()));
                }
            }
        }
        return events;
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
        return drawCardToPlayer(player, true);
    }

    public DomainEvent drawCardToPlayer(Player player, boolean isChangeRoundPhase, int requiredCardNumber) {
        refreshDeckWhenCardsNumLessThen(requiredCardNumber);
        List<HandCard> cards = deck.deal(requiredCardNumber);
        player.getHand().addCardToHand(cards);

        // 樂不思蜀不需要改變回合階段
        if (isChangeRoundPhase) {
            currentRound.setRoundPhase(RoundPhase.Action);
        }
        List<String> cardIds = cards.stream().map(HandCard::getId).collect(Collectors.toList());
        String message = String.format("玩家 %s 抽了 %d 張牌", player.getId(), requiredCardNumber);

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
                requiredCardNumber,
                cardIds,
                message,
                gameId,
                playerEvents,
                roundEvent,
                gamePhase.getPhaseName(),
                player.getId()
        );
    }

    public DomainEvent drawCardToPlayer(Player player, boolean isChangeRoundPhase) {
        return drawCardToPlayer(player, isChangeRoundPhase, 2);
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

    public void setGeneralCardDeck(GeneralCardDeck generalCardDeck) {
        this.generalCardDeck = generalCardDeck;
    }

    public void setSeatingChart(SeatingChart seatingChart) {this.seatingChart = seatingChart;}

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
        List<DomainEvent> events = behavior.playerAction();
        removeCompletedBehaviors();
        return events;
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

    private void checkIsPlayerHasThisEquipment(String playerId, String cardId) {
        Player player = getPlayer(playerId);
        if (!player.hasThisEquipment(cardId)) {
            throw new IllegalStateException(player.getId() + " has no this equipment " + cardId);
        }
    }

    public List<DomainEvent> playerUseEquipment(String playerId, String cardId, String targetPlayerId, EquipmentPlayType playType) {
        checkIsCurrentRoundValid(playerId);
        checkIsPlayerHasThisEquipment(playerId, cardId);
        List<DomainEvent> events = Optional.ofNullable(equipmentEffectHandler.handle(playerId, cardId, targetPlayerId, playType)).orElse(new ArrayList<>());
        removeCompletedBehaviors();
        return events;
    }

    public List<DomainEvent> playerChooseHorseForQilinBow(String playerId, String cardId) {
        Behavior waitingQilinBowResponsebehavior = topBehavior.pop(); //  麒麟弓 behavior
        Behavior nomralKillbehavior = topBehavior.peek(); //  NormalKill
        List<DomainEvent> qilingBowEvents = waitingQilinBowResponsebehavior.responseToPlayerAction(playerId, nomralKillbehavior.getReactionPlayers().get(0), cardId, EquipmentPlayType.ACTIVE.getPlayType());

        List<DomainEvent> normalKillEvents = nomralKillbehavior.responseToPlayerAction(nomralKillbehavior.getReactionPlayers().get(0), waitingQilinBowResponsebehavior.getCurrentReactionPlayer().getId(), cardId, PlayType.SYSTEM_INTERNAL.getPlayType());

        removeCompletedBehaviors();
        return Stream.of(qilingBowEvents, normalKillEvents).flatMap(Collection::stream).collect(Collectors.toList());
    }

    public void playerDeadSettlement() {
        Player deathPlayer = currentRound.getDyingPlayer();
        if (deathPlayer.getRoleCard().getRole().equals(Role.MONARCH)) {
            this.enterPhase(new GameOver(this));
            gamePhase.execute();
            // TODO 主動推反賊獲勝訊息給前端
        }
    }

    public void setGamePhase(GamePhase gamePhase) {
        this.gamePhase = gamePhase;
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

    public boolean isInSnatchEffectRange(Player player, Player targetPlayer) {
        // 攻擊距離 不用考慮
        // 基礎距離(座位表) + 使用 順手牽羊 玩家的 -1 馬  + 被使用 順手牽羊 玩家的 +1 馬  <= 1
        int dist = seatingChart.calculateDistance(player, targetPlayer);
        int attackDist = player.getMinusOneDistance();
        int escapeDist = targetPlayer.judgeEscapeDistance();
        return dist - attackDist + escapeDist <= 1;
    }

    public List<DomainEvent> finishAction(String playerId) {
        List<DomainEvent> domainEvents = new ArrayList<>();
        Player currentRoundPlayer = currentRound.getCurrentRoundPlayer();

        if (currentRoundPlayer == null || !playerId.equals(currentRoundPlayer.getId()) || !playerId.equals(currentRound.getActivePlayer().getId())) {
            throw new IllegalStateException(String.format("currentRound is null or current player not %s", playerId));
        }

        if (!topBehavior.isEmpty()) {
            topBehavior.forEach(behavior -> System.out.println("current topBehavior: " + behavior.getClass().getName()));
            throw new IllegalStateException(String.format("current topBehavior is not null size[%s]", topBehavior.size()));
        }

        currentRound.setRoundPhase(RoundPhase.Discard);

        FinishActionEvent finishActionEvent = new FinishActionEvent(playerId);
        int currentRoundPlayerDiscardCount = getCurrentRoundPlayerDiscardCount();
        String notifyMessage = String.format("玩家 %s 需要棄 %d 張牌", currentRoundPlayer.getId(), currentRoundPlayerDiscardCount);
        NotifyDiscardEvent notifyDiscardEvent = new NotifyDiscardEvent(notifyMessage, currentRoundPlayerDiscardCount, playerId);
        domainEvents.add(finishActionEvent);
        domainEvents.add(notifyDiscardEvent);

        if (currentRoundPlayerDiscardCount == 0) {
            domainEvents.add(new RoundEndEvent());
            List<DomainEvent> nextRoundDomainEvents = goNextRound(currentRoundPlayer);
            notifyMessage = nextRoundDomainEvents.stream()
                    .filter(event -> event instanceof DrawCardEvent)
                    .map(DomainEvent::getMessage)
                    .findFirst()
                    .orElse(null);
            domainEvents.addAll(nextRoundDomainEvents);
        }

        domainEvents.add(getGameStatusEvent(notifyMessage));
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

    private List<DomainEvent> judgePlayerShouldDelay() {
        Player player = currentRound.getCurrentRoundPlayer();
        List<DomainEvent> judgementEvents = new ArrayList<>();
        if (player.hasAnyDelayScrollCard()) {
            Stack<ScrollCard> delayCards = player.getDelayScrollCards();
            while (!delayCards.isEmpty()) {
                ScrollCard card = delayCards.pop();
                currentRound.setCurrentCard(card);
                if (card instanceof Contentment) {
                    DomainEvent contentmentEvent = handleContentmentJudgement(player);
                    judgementEvents.add(contentmentEvent);
                } else if (card instanceof Lightning) {
                    List<DomainEvent> lightningEvents = handleLightningJudgement(card, player);
                    judgementEvents.addAll(lightningEvents);
                }

                if (!topBehavior.isEmpty()) { // something happened
                    return judgementEvents;
                }
            }
        }

        judgementEvents.add(new JudgementEvent());
        currentRound.setRoundPhase(RoundPhase.Drawing);
        return judgementEvents;
    }

    private List<DomainEvent> handleLightningJudgement(ScrollCard card, Player player) {
        // 抽一張卡判定
        List<HandCard> cards = drawCardForCardEffect(1);
        HandCard drawnCard = cards.get(0);

        // 判定牌是否為黑桃2~9
        boolean isLightningSuccess = Suit.SPADE == drawnCard.getSuit() &&
                drawnCard.getRank().getValue() >= Rank.TWO.getValue() &&
                drawnCard.getRank().getValue() <= Rank.NINE.getValue();

        List<DomainEvent> domainEvents = new ArrayList<>();
        domainEvents.add(new LightningEvent(isLightningSuccess, player.getId(), drawnCard.getId()));

        if (isLightningSuccess) {
            List<DomainEvent> damageEvents = getDamagedEvent(
                    player.getId(),
                    player.getId(),
                    card.getId(),
                    card,
                    PlayType.SYSTEM_INTERNAL.getPlayType(),
                    player.getHP(),
                    player,
                    currentRound,
                    Optional.empty()
            );
            domainEvents.addAll(damageEvents);
        } else {
            Player nextPlayer = seatingChart.getNextPlayer(player);
            domainEvents.add(new LightningTransferredEvent(
                    player.getId(),
                    nextPlayer.getId(),
                    card.getId(),
                    String.format("閃電從 %s 轉移至 %s", player.getGeneralName(), nextPlayer.getGeneralName())
            ));
            // 轉移閃電到下一位
            nextPlayer.addDelayScrollCard(card);
        }

        return domainEvents;
    }

    private ContentmentEvent handleContentmentJudgement(Player player) {
        // 抽一張卡判定
        List<HandCard> cards = drawCardForCardEffect(1);
        HandCard drawnCard = cards.get(0);

        // 判定牌的花色
        boolean contentmentSuccess = Suit.HEART != drawnCard.getSuit();

        // 回傳 Contentment 事件
        return new ContentmentEvent(contentmentSuccess, player.getId(), drawnCard.getId(), drawnCard.getSuit());
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
        List<DomainEvent> nextRoundEvent = new ArrayList<>();
        nextRoundEvent.add(discardEvent);
        nextRoundEvent.add(new RoundEndEvent());
        nextRoundEvent.addAll(goNextRound(player));
        String drawCardMessage = String.format("玩家 %s 抽了 %d 張牌", currentRound.getCurrentRoundPlayer().getId(), calculatePlayerCanDrawCardSize(currentRound.getCurrentRoundPlayer()));
        nextRoundEvent.add(getGameStatusEvent(drawCardMessage));
        return nextRoundEvent;
    }

    public List<DomainEvent> goNextRound(Player player) {
        Player nextPlayer = seatingChart.getNextPlayer(player);
        currentRound = new Round(nextPlayer);
        return playerTakeTurn(nextPlayer);
    }

    public void askActivePlayerPlayPeachCard() {
        // TODO 通知玩家要出桃
    }

    public void updateRoundInformation(Player targetPlayer, HandCard card) {
        currentRound.setActivePlayer(targetPlayer);
        currentRound.setCurrentCard(card);
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
    public List<DomainEvent> getDamagedEvent(String damagedPlayerId,
                                             String attackerPlayerId,
                                             String cardId,
                                             HandCard card,
                                             String playType,
                                             int originalHp,
                                             Player damagedPlayer,
                                             Round currentRound,
                                             Optional<Behavior> behavior) {
        card.effect(damagedPlayer);
        PlayerDamagedEvent playerDamagedEvent = createPlayerDamagedEvent(originalHp, damagedPlayer);
        List<DomainEvent> events = new ArrayList<>();
        if (damagedPlayer.isHPGreaterThanZero()) {
            currentRound.setActivePlayer(currentRound.getCurrentRoundPlayer());
            // 判斷要不要產 PlayCardEvent，如果是 playType 是 "qilinBow" 就不產
            if (!PlayType.SYSTEM_INTERNAL.getPlayType().equals(playType)) {
                PlayCardEvent playCardEvent = new PlayCardEvent("不出牌", damagedPlayerId, attackerPlayerId, cardId, playType);
                events.add(playCardEvent);
            }
            behavior.ifPresent(b -> b.setIsOneRound(true));
            events.add(playerDamagedEvent);
            return events;
        } else {
            PlayerDyingEvent playerDyingEvent = createPlayerDyingEvent(damagedPlayer);
            AskPeachEvent askPeachEvent = createAskPeachEvent(damagedPlayer, damagedPlayer);
            this.enterPhase(new GeneralDying(this));
            currentRound.setDyingPlayer(damagedPlayer);
            currentRound.setActivePlayer(damagedPlayer);
            // 判斷要不要產 PlayCardEvent，如果是 playType 是 "qilinBow" 就不產
            if (!PlayType.SYSTEM_INTERNAL.getPlayType().equals(playType)) {
                PlayCardEvent playCardEvent = new PlayCardEvent("不出牌", damagedPlayerId, attackerPlayerId, cardId, playType);
                events.add(playCardEvent);
            }
            behavior.ifPresent(b -> b.setIsOneRound(false));

            if (getGamePhase() instanceof GeneralDying) {
                updateTopBehavior(new DyingAskPeachBehavior(this, damagedPlayer, getPlayers().stream().map(Player::getId).toList(),
                        damagedPlayer, cardId, playType, null));
            }
            events.addAll(List.of(playerDamagedEvent, playerDyingEvent, askPeachEvent));
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

    public void setTopBehavior(Stack<Behavior> topBehavior) {
        this.topBehavior = topBehavior;
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
        seatingChart = new SeatingChart(new ArrayList<>(players));
    }

    public void removeDyingPlayer(Player player) {
        seatingChart.getPlayers().remove(player);
        player.setHealthStatus(HealthStatus.DEATH);
    }

    public void setDeck(Deck deck) {
        this.deck = deck;
    }

    public Deck getDeck() {
        return deck;
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

    public Optional<Behavior> peekTopBehaviorSecondElement() {
        return topBehavior.size() >= 2 ? Optional.ofNullable(topBehavior.get(topBehavior.size() - 2)) : Optional.empty();
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

    public List<HandCard> drawCardForCardEffect(int needCount) {
        refreshDeckWhenCardsNumLessThen(needCount);
        List<HandCard> cards = deck.deal(needCount);
        graveyard.add(cards);
        return cards;
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
            return List.of(new BorrowedSwordEvent(behavior.getCardId(), borrowedPlayerId,attackTargetPlayerId), getGameStatusEvent(String.format("要求 %s 出殺", borrowedPlayerId)));
        }
        throw new IllegalStateException("UseBorrowedSwordEffect error.");
    }

    public List<DomainEvent> useDismantleEffect(String currentPlayerId, String targetPlayerId, String cardId, Integer targetCardIndex) {
        Behavior behavior = topBehavior.peek();
        List<HandCard> cards = getPlayer(targetPlayerId).getHand().getCards();
        HandCard handCard = null;
        if (targetCardIndex != null && targetCardIndex >= cards.size()) {
            throw new IllegalArgumentException("Hand card index over size");
        } else if (targetCardIndex != null) {
            handCard = cards.get(targetCardIndex);
        }

        if (behavior instanceof DismantleBehavior &&
            currentRound.getActivePlayer().getId().equals(currentPlayerId)
        ) {
            behavior.putParam(UserCommand.CHOOSE_HAND_CARD.name(), Optional.ofNullable(handCard).map(HandCard::getId).orElse(null));
            behavior.putParam(UserCommand.DISMANTLE_BEHAVIOR_USE_DISMANTLE_EFFECT_CARD_ID.name(), cardId);
            behavior.putParam(UserCommand.DISMANTLE_BEHAVIOR_PLAYER_ID.name(), currentPlayerId);
            behavior.putParam(UserCommand.DISMANTLE_BEHAVIOR_TARGET_PLAYER_ID.name(), targetPlayerId);
            List<DomainEvent> acceptedEvent = behavior.responseToPlayerAction(currentPlayerId, targetPlayerId, cardId, PlayType.ACTIVE.getPlayType());
            removeCompletedBehaviors();
            return acceptedEvent;
        }
        throw new IllegalStateException("UseDismantleEffect error.");
    }

    public List<DomainEvent> playWardCard(String playerId, String cardId, String playType) {
        if (!currentRound.getStage().equals(Stage.Wait_Accept_Ward_Effect)) {
            throw new IllegalStateException(String.format("CurrentRound stage not Wait_Accept_Ward_Effect but [%s]", currentRound.getStage()));
        }
        Player player = getPlayer(playerId);
        if (PlayType.ACTIVE.getPlayType().equals(playType) && !player.hasCardOfTypeWithIdInHand(cardId, Ward.class)) {
            throw new IllegalArgumentException(String.format("Current player play card[%s] is not Ward", cardId));
        }

        Behavior behavior = topBehavior.peek();
        List<DomainEvent> domainEvents = new ArrayList<>();
        if (behavior instanceof WardBehavior
                && behavior.getReactionPlayers().contains(playerId)
                && (isWardCard(cardId) || PlayType.SKIP.getPlayType().equals(playType))
        ) {
            domainEvents.addAll(behavior.responseToPlayerAction(playerId, "", cardId, playType));
        }

        domainEvents.add(getGameStatusEvent("出無懈可擊"));
        return domainEvents;

    }

    public List<DomainEvent> playerChooseCardFromBountifulHarvest(String currentPlayerId, String cardId) {
        Behavior behavior = topBehavior.peek();
        if (behavior instanceof BountifulHarvestBehavior) {
            Optional.ofNullable(behavior.getParam(BountifulHarvestBehavior.BOUNTIFUL_HARVEST_CARDS))
                    .filter(List.class::isInstance)
                    .map(List.class::cast)
                    .filter(cardIds -> cardIds.get(0) instanceof String)
                    .map(list -> (List<String>) list)
                    .filter(stringCardIds -> stringCardIds.contains(cardId))
                    .orElseThrow(() -> new IllegalStateException("CardId is not in BOUNTIFUL_HARVEST_CARDS"));
            List<DomainEvent> acceptedEvent = behavior.responseToPlayerAction(currentPlayerId, "", cardId, PlayType.ACTIVE.getPlayType());
            removeCompletedBehaviors();
            return acceptedEvent;
        }
        throw new IllegalStateException("playerChooseCardFromBountifulHarvest error.");
    }

    private boolean isPlayerHasWeapon(String playerId) {
        return getPlayer(playerId).getEquipmentWeaponCard() != null;
    }

    public Player getLastAttacker() {
        // 找出造成傷害者前 萬箭、殺、南蠻入侵、決鬥、借刀殺人 => 找出最近造成傷害的 behavior 的 player
        // 取得最後一個造成傷害的玩家，從 topBehavior 中找到最後一個造成傷害的玩家
        for (int i = topBehavior.size() - 1; i >= 0; i--) {
            Behavior behavior = topBehavior.get(i);
            if (behavior instanceof NormalActiveKillBehavior ||
                behavior instanceof ArrowBarrageBehavior ||
                behavior instanceof BarbarianInvasionBehavior) {
                return behavior.getBehaviorPlayer();
            } else if (behavior instanceof DuelBehavior) {
                String duelBehaviorCurrentReactionPlayerId  = behavior.getCurrentReactionPlayer().getId();
                String killedPlayerId = behavior.getReactionPlayers().stream().filter(id -> !id.equals(duelBehaviorCurrentReactionPlayerId)).findFirst().get();
                return getPlayer(killedPlayerId);
            } else if (behavior instanceof BorrowedSwordBehavior) {
                return getPlayer(behavior.getReactionPlayers().get(0));
            }
        }
        return null;
    }

    public boolean doesAnyPlayerHaveWard() {
        return doesAnyPlayerHaveWard(null);
    }

    public boolean doesAnyPlayerHaveWard(String expectPlayerId) {
        Stream<Player> stream = players.stream();
        if (expectPlayerId != null) {
            stream = stream.filter(player -> !player.getId().equals(expectPlayerId));
        }
        return stream
                .anyMatch(player -> player.getHand().getCards().stream().anyMatch(card -> card instanceof Ward));
    }

    public List<Player> whichPlayersHaveWard() {
        return players.stream()
                .filter(player -> player.getHand().getCards().stream().anyMatch(card -> card instanceof Ward))
                .toList();
    }

    public List<DomainEvent> useSnatchEffect(String currentPlayerId, String targetPlayerId, String cardId, Integer targetCardIndex) {
        Behavior behavior = topBehavior.peek();
        List<HandCard> cards = getPlayer(targetPlayerId).getHand().getCards();
        HandCard handCard = null;
        if (targetCardIndex != null && targetCardIndex >= cards.size()) {
            throw new IllegalArgumentException("Hand card index over size");
        } else if (targetCardIndex != null) {
            handCard = cards.get(targetCardIndex);
        }

        if (behavior instanceof SnatchBehavior &&
                currentRound.getActivePlayer().getId().equals(currentPlayerId)
        ) {
            behavior.putParam(UserCommand.CHOOSE_HAND_CARD.name(), Optional.ofNullable(handCard).map(HandCard::getId).orElse(null));
            behavior.putParam(UserCommand.SNATCH_BEHAVIOR_USE_DISMANTLE_EFFECT_CARD_ID.name(), cardId);
            behavior.putParam(UserCommand.SNATCH_BEHAVIOR_PLAYER_ID.name(), currentPlayerId);
            behavior.putParam(UserCommand.SNATCH_BEHAVIOR_TARGET_PLAYER_ID.name(), targetPlayerId);
            List<DomainEvent> acceptedEvent = behavior.responseToPlayerAction(currentPlayerId, targetPlayerId, cardId, PlayType.ACTIVE.getPlayType());
            removeCompletedBehaviors();
            return acceptedEvent;
        }
        throw new IllegalStateException("useSnatchEffect error.");
    }
}

