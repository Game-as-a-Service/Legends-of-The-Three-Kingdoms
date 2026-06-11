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
import com.gaas.threeKingdoms.handcard.basiccard.VirtualKill;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.EighteenSpanViperSpearCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.HeavenlyDoubleHalberdCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.RepeatingCrossbowCard;
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
import com.gaas.threeKingdoms.skill.context.DamageContext;
import com.gaas.threeKingdoms.skill.registry.SkillEngine;
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
            // 洛神：回合開始（延遲錦囊判定之前）黑色判定牌全收
            events.addAll(SkillEngine.luoShenJudgementLoop(this, currentRoundPlayer));
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

    public List<DomainEvent> continueJudgementAndDraw(Player currentRoundPlayer, boolean contentmentSuccess) {
        List<DomainEvent> events = new ArrayList<>();
        if (RoundPhase.Judgement.equals(currentRound.getRoundPhase())) {
            List<DomainEvent> judgeEvents = judgePlayerShouldDelay();
            events.addAll(judgeEvents);

            if (!topBehavior.isEmpty()) {
                return events;
            }

            if (RoundPhase.Drawing.equals(currentRound.getRoundPhase())) {
                DomainEvent drawCardEvent = drawCardToPlayer(currentRoundPlayer, !contentmentSuccess);
                events.add(drawCardEvent);
                if (contentmentSuccess) {
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
        return drawCardToPlayer(player, isChangeRoundPhase, calculatePlayerCanDrawCardSize(player));
    }

    private int calculatePlayerCanDrawCardSize(Player player) {
        // 基礎 2 張 + 技能修正（英姿 +1 / 裸衣 -1），下限 0
        return Math.max(0, 2 + SkillEngine.drawPhaseDelta(player));
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
        Player actingPlayer = getPlayer(playerId);
        int handSizeBefore = actingPlayer.getHandSize();

        if (!topBehavior.isEmpty()) {
            Behavior behavior = topBehavior.peek();
            List<DomainEvent> acceptedEvent = behavior.responseToPlayerAction(playerId, targetPlayerId, cardId, playType);
            //  確認topBehavior是否有需要pop掉的behavior
            removeCompletedBehaviors();
            // 連營：response 出牌（出閃/出殺）失去最後一張手牌 → 摸牌
            if (acceptedEvent != null && handSizeBefore > 0 && actingPlayer.getHandSize() == 0
                    && SkillEngine.drawCountAfterLoseLastHandCard(actingPlayer) > 0) {
                acceptedEvent = new ArrayList<>(acceptedEvent);
                appendDrawIfLostLastHandCard(actingPlayer, acceptedEvent);
            }
            return acceptedEvent;
        }
        // 集智判定要在 handler 之前取牌型（出牌後牌已離手）
        boolean isScrollPlay = actingPlayer.getHand().getCard(cardId)
                .map(c -> c instanceof ScrollCard)
                .orElse(false);
        List<String> reActionPlayer = new ArrayList<>();
        reActionPlayer.add(targetPlayerId);
        Behavior behavior = playCardHandler.handle(playerId, cardId, reActionPlayer, playType);
        if (behavior.isTargetPlayerNeedToResponse()) {
            updateTopBehavior(behavior);
        }
        List<DomainEvent> events = behavior.playerAction();
        removeCompletedBehaviors();
        // 集智：使用錦囊 → 摸牌（在連營檢查之前摸，補回的牌會讓連營不觸發 — 符合時序：摸牌先發生）
        if (isScrollPlay) {
            int jiZhiDraw = SkillEngine.drawCountAfterScrollUsed(actingPlayer);
            if (jiZhiDraw > 0) {
                events = new ArrayList<>(events);
                events.add(drawCardToPlayer(actingPlayer, false, jiZhiDraw));
            }
        }
        // 連營：出牌用掉最後一張手牌 → 摸牌
        if (handSizeBefore > 0) {
            events = new ArrayList<>(events);
            appendDrawIfLostLastHandCard(actingPlayer, events);
        }
        return events;
    }

    /** 連營 hook：player 手牌歸零時補摸（量由技能決定，無技能則 no-op）。 */
    private void appendDrawIfLostLastHandCard(Player player, List<DomainEvent> events) {
        if (player.getHandSize() != 0) {
            return;
        }
        int drawCount = SkillEngine.drawCountAfterLoseLastHandCard(player);
        if (drawCount > 0) {
            events.add(drawCardToPlayer(player, false, drawCount));
        }
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
        // 攻擊距離 >= 基礎距離(座位表) + 逃走距離 + 技能距離修正（馬術 -1；下限 1）
        int dist = seatingChart.calculateDistance(player, targetPlayer);
        int escapeDist = targetPlayer.judgeEscapeDistance();
        int attackDist = player.judgeAttackDistance();
        int effectiveDist = Math.max(1, dist + escapeDist + SkillEngine.distanceDeltaToOthers(player));
        return attackDist >= effectiveDist;
    }

    public boolean isInSnatchEffectRange(Player player, Player targetPlayer) {
        // 攻擊距離 不用考慮
        // 基礎距離(座位表) + 使用 順手牽羊 玩家的 -1 馬  + 被使用 順手牽羊 玩家的 +1 馬 + 技能修正（馬術 -1） <= 1
        int dist = seatingChart.calculateDistance(player, targetPlayer);
        int attackDist = player.getMinusOneDistance();
        int escapeDist = targetPlayer.judgeEscapeDistance();
        return Math.max(1, dist - attackDist + escapeDist + SkillEngine.distanceDeltaToOthers(player)) <= 1;
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
                    if (doesAnyPlayerHaveWard(null)) {
                        ContentmentJudgementBehavior cjb = new ContentmentJudgementBehavior(
                                this, player, List.of(player.getId()), null,
                                card.getId(), PlayType.INACTIVE.getPlayType(), card
                        );
                        topBehavior.push(cjb);
                        judgementEvents.addAll(cjb.playerAction());
                    } else {
                        ContentmentEvent contentmentEvent = handleContentmentJudgement(player);
                        judgementEvents.add(contentmentEvent);
                        // 天妒等：判定牌生效後技能
                        judgementEvents.addAll(SkillEngine.afterJudgement(this, player,
                                PlayCard.findById(contentmentEvent.getDrawCardId())));
                    }
                } else if (card instanceof Lightning) {
                    if (doesAnyPlayerHaveWard(null)) {
                        LightningJudgementBehavior ljb = new LightningJudgementBehavior(
                                this, player, List.of(player.getId()), null,
                                card.getId(), PlayType.INACTIVE.getPlayType(), card
                        );
                        topBehavior.push(ljb);
                        judgementEvents.addAll(ljb.playerAction());
                    } else {
                        List<DomainEvent> lightningEvents = handleLightningJudgement(card, player);
                        judgementEvents.addAll(lightningEvents);
                    }
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

    public List<DomainEvent> handleLightningJudgement(ScrollCard card, Player player) {
        // 抽一張卡判定
        List<HandCard> cards = drawCardForCardEffect(1);
        HandCard drawnCard = cards.get(0);

        // 判定牌是否為黑桃2~9
        boolean isLightningSuccess = Suit.SPADE == drawnCard.getSuit() &&
                drawnCard.getRank().getValue() >= Rank.TWO.getValue() &&
                drawnCard.getRank().getValue() <= Rank.NINE.getValue();

        List<DomainEvent> domainEvents = new ArrayList<>();
        domainEvents.add(new LightningEvent(isLightningSuccess, player.getId(), drawnCard.getId()));
        // 天妒等：判定牌生效後技能
        domainEvents.addAll(SkillEngine.afterJudgement(this, player, drawnCard));

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
            // 謙遜等：跳過不能成為閃電目標的玩家（最壞情況繞回原判定者自己）
            Player nextPlayer = seatingChart.getNextPlayer(player);
            int hops = seatingChart.getPlayers().size();
            while (hops-- > 0 && !nextPlayer.equals(player)
                    && SkillEngine.isImmuneToCard(nextPlayer, card)) {
                nextPlayer = seatingChart.getNextPlayer(nextPlayer);
            }
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

    public ContentmentEvent handleContentmentJudgement(Player player) {
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
        // 手牌上限預設 = HP；技能可覆寫（英姿 = max(HP, 4)）
        return Math.max(0, player.getHandSize() - SkillEngine.handCardLimit(player));
    }

    public List<DomainEvent> playerDiscardCard(List<String> cardIds) {
        Player player = currentRound.getCurrentRoundPlayer();
        int needToDiscardSize = player.getHandSize() - SkillEngine.handCardLimit(player);
        if (cardIds.size() < needToDiscardSize) throw new RuntimeException();
        // todo 判斷這個玩家是否有這些牌
        List<HandCard> discardCards = player.discardCards(cardIds);
        graveyard.add(discardCards);
        String message = String.format("玩家 %s 棄牌", player.getId());
        DomainEvent discardEvent = new DiscardEvent(discardCards, message, player.getId());
        List<DomainEvent> nextRoundEvent = new ArrayList<>();
        nextRoundEvent.add(discardEvent);
        // 連營：棄牌階段棄光最後手牌 → 摸牌
        appendDrawIfLostLastHandCard(player, nextRoundEvent);
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
        // 裸衣等傷害加成：attacker 在自己回合用殺/決鬥造成傷害時 +N
        Player boostAttacker = (attackerPlayerId == null || attackerPlayerId.isEmpty())
                ? null
                : players.stream().filter(p -> attackerPlayerId.equals(p.getId())).findFirst().orElse(null);
        int extraDamage = SkillEngine.extraDamage(this, boostAttacker, card);
        if (extraDamage > 0) {
            damagedPlayer.damage(extraDamage);
        }
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

            // 武將技：受傷後觸發
            Player attackerPlayer = (attackerPlayerId == null || attackerPlayerId.isEmpty())
                    ? null
                    : players.stream().filter(p -> attackerPlayerId.equals(p.getId())).findFirst().orElse(null);
            int damagePoints = originalHp - damagedPlayer.getHP();
            DamageContext damageContext = new DamageContext(damagedPlayer, attackerPlayer, card, damagePoints);
            events.addAll(SkillEngine.onDamaged(this, damageContext));

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
                // 為救回後的 SkillEngine replay 帶 pending context（FAQ：曹操救回後可發動奸雄）
                // VirtualKill (e.g. ViperSpear) 不在 PlayCard factory：用 pendingViperSpearDiscardCardIds 帶兩張棄牌
                String pendingSourceCardId;
                List<String> pendingViperSpearDiscardCardIds = null;
                if (card instanceof VirtualKill) {
                    pendingSourceCardId = null;
                    Behavior topAtDamage = behavior.orElse(null);
                    if (topAtDamage instanceof ViperSpearKillBehavior viper) {
                        pendingViperSpearDiscardCardIds = viper.getDiscardedCardIds();
                    }
                } else {
                    pendingSourceCardId = card.getId();
                }
                updateTopBehavior(new DyingAskPeachBehavior(this, damagedPlayer, getPlayers().stream().map(Player::getId).toList(),
                        damagedPlayer, cardId, playType, null,
                        pendingSourceCardId, attackerPlayerId, pendingViperSpearDiscardCardIds));
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

    public List<DomainEvent> playerActivateYinYangSwords(String playerId, AskActivateYinYangSwordsEvent.Choice choice) {
        if (topBehavior.isEmpty()) {
            throw new IllegalStateException("No active behavior waiting for YinYangSwords activation");
        }
        Behavior behavior = topBehavior.peek();
        if (!(behavior instanceof WaitingYinYangSwordsActivationBehavior)) {
            throw new IllegalStateException("Current behavior is not WaitingYinYangSwordsActivationBehavior");
        }
        WaitingYinYangSwordsActivationBehavior activationBehavior = (WaitingYinYangSwordsActivationBehavior) behavior;
        List<DomainEvent> events = activationBehavior.resolveChoice(playerId, choice);
        removeCompletedBehaviors();
        return events;
    }

    public List<DomainEvent> playerUseYinYangSwordsEffect(String playerId, YinYangSwordsEffectEvent.Choice choice, String cardId) {
        if (topBehavior.isEmpty()) {
            throw new IllegalStateException("No active behavior waiting for YinYangSwords effect response");
        }
        Behavior behavior = topBehavior.peek();
        if (!(behavior instanceof WaitingYinYangSwordsResponseBehavior)) {
            throw new IllegalStateException("Current behavior is not WaitingYinYangSwordsResponseBehavior");
        }
        WaitingYinYangSwordsResponseBehavior yinYangBehavior = (WaitingYinYangSwordsResponseBehavior) behavior;
        List<DomainEvent> events = yinYangBehavior.resolveChoice(playerId, choice, cardId);
        removeCompletedBehaviors();
        return events;
    }

    public List<DomainEvent> playerUseGreenDragonCrescentBladeEffect(String playerId, AskGreenDragonCrescentBladeEffectEvent.Choice choice, String killCardId) {
        if (topBehavior.isEmpty()) {
            throw new IllegalStateException("No active behavior waiting for GreenDragonCrescentBlade effect response");
        }
        Behavior behavior = topBehavior.peek();
        if (!(behavior instanceof WaitingGreenDragonCrescentBladeResponseBehavior)) {
            throw new IllegalStateException("Current behavior is not WaitingGreenDragonCrescentBladeResponseBehavior");
        }
        WaitingGreenDragonCrescentBladeResponseBehavior gdcbBehavior = (WaitingGreenDragonCrescentBladeResponseBehavior) behavior;
        List<DomainEvent> events = gdcbBehavior.resolveChoice(playerId, choice, killCardId);
        removeCompletedBehaviors();
        return events;
    }

    public List<DomainEvent> playerUseJianXiongEffect(String playerId, AskJianXiongEffectEvent.Choice choice) {
        if (topBehavior.isEmpty()) {
            throw new IllegalStateException("No active behavior waiting for JianXiong effect response");
        }
        Behavior behavior = topBehavior.peek();
        if (!(behavior instanceof WaitingJianXiongResponseBehavior)) {
            throw new IllegalStateException("Current behavior is not WaitingJianXiongResponseBehavior");
        }
        WaitingJianXiongResponseBehavior jxBehavior = (WaitingJianXiongResponseBehavior) behavior;
        List<DomainEvent> events = jxBehavior.resolveChoice(playerId, choice);
        removeCompletedBehaviors();
        return events;
    }

    public List<DomainEvent> playerUseSkillEffect(String playerId, String skillName, String choice,
                                                  List<String> cardIds, String targetPlayerId) {
        if (topBehavior.isEmpty()) {
            throw new IllegalStateException("No active behavior waiting for skill effect response");
        }
        Behavior behavior = topBehavior.peek();
        if (!(behavior instanceof com.gaas.threeKingdoms.behavior.behavior.WaitingSkillEffectBehavior waiting)) {
            throw new IllegalStateException("Current behavior is not WaitingSkillEffectBehavior");
        }
        if (!waiting.getSkillName().equals(skillName)) {
            throw new IllegalStateException(String.format(
                    "Waiting skill is %s, not %s", waiting.getSkillName(), skillName));
        }
        List<DomainEvent> events = waiting.resolveChoice(playerId, choice, cardIds, targetPlayerId);
        removeCompletedBehaviors();
        return events;
    }

    public List<DomainEvent> playerUseHuJiaEffect(String playerId,
                                                  com.gaas.threeKingdoms.events.AskHuJiaEffectEvent.Choice choice,
                                                  String cardId) {
        if (topBehavior.isEmpty()) {
            throw new IllegalStateException("No active behavior waiting for HuJia effect response");
        }
        Behavior behavior = topBehavior.peek();
        if (!(behavior instanceof com.gaas.threeKingdoms.behavior.behavior.WaitingHuJiaResponseBehavior huJia)) {
            throw new IllegalStateException("Current behavior is not WaitingHuJiaResponseBehavior");
        }
        List<DomainEvent> events = huJia.resolveChoice(playerId, choice, cardId);
        removeCompletedBehaviors();
        return events;
    }

    public List<DomainEvent> playerUseStonePiercingAxeEffect(String playerId, AskStonePiercingAxeEffectEvent.Choice choice, List<String> discardCardIds) {
        if (topBehavior.isEmpty()) {
            throw new IllegalStateException("No active behavior waiting for StonePiercingAxe effect response");
        }
        Behavior behavior = topBehavior.peek();
        if (!(behavior instanceof WaitingStonePiercingAxeResponseBehavior)) {
            throw new IllegalStateException("Current behavior is not WaitingStonePiercingAxeResponseBehavior");
        }
        WaitingStonePiercingAxeResponseBehavior spaBehavior = (WaitingStonePiercingAxeResponseBehavior) behavior;
        List<DomainEvent> events = spaBehavior.resolveChoice(playerId, choice, discardCardIds);
        removeCompletedBehaviors();
        return events;
    }

    public List<DomainEvent> playerUseViperSpearKill(String playerId, String targetPlayerId, List<String> discardCardIds) {
        Player attacker = getPlayer(playerId);

        // 驗證：玩家是當前 activePlayer（active = 自己回合; passive = 被詢問者）
        checkIsCurrentRoundValid(playerId);

        // 驗證：裝備丈八蛇矛
        if (!(attacker.getEquipmentWeaponCard() instanceof EighteenSpanViperSpearCard)) {
            throw new IllegalStateException("Player is not equipped with EighteenSpanViperSpear");
        }

        validateViperSpearDiscardCardIds(attacker, discardCardIds);

        // 分流：top behavior 決定是 active 還是 passive
        if (topBehavior.isEmpty()) {
            return playerUseViperSpearKillActive(attacker, targetPlayerId, discardCardIds);
        }
        Behavior top = topBehavior.peek();
        if (top instanceof BarbarianInvasionBehavior
                || top instanceof DuelBehavior
                || top instanceof BorrowedSwordBehavior) {
            return playerUseViperSpearKillPassive(attacker, targetPlayerId, discardCardIds, top);
        }
        throw new IllegalStateException(
                "Cannot use ViperSpear kill while behavior=" + top.getClass().getSimpleName() + " is pending");
    }

    private void validateViperSpearDiscardCardIds(Player attacker, List<String> discardCardIds) {
        if (discardCardIds == null || discardCardIds.size() != 2) {
            throw new IllegalArgumentException("ViperSpear kill requires exactly 2 discard cardIds");
        }
        for (String discardCardId : discardCardIds) {
            if (attacker.getHand().getCards().stream().noneMatch(c -> c.getId().equals(discardCardId))) {
                throw new IllegalArgumentException("Attacker does not have card in hand: " + discardCardId);
            }
        }
        if (discardCardIds.get(0).equals(discardCardIds.get(1))) {
            throw new IllegalArgumentException("Cannot discard the same card twice");
        }
    }

    private List<DomainEvent> playerUseViperSpearKillActive(Player attacker, String targetPlayerId, List<String> discardCardIds) {
        Player target = getPlayer(targetPlayerId);

        // 驗證：目標在攻擊範圍
        if (!isInAttackRange(attacker, target)) {
            throw new IllegalStateException(String.format("%s is not in attack range", targetPlayerId));
        }

        // 驗證：殺次數限制（無諸葛連弩時一回合一次）
        // 丈八蛇矛的殺視為一般殺，計入回合限制
        if (currentRound.isShowKill() && !(attacker.getEquipmentWeaponCard() instanceof RepeatingCrossbowCard)
                && !SkillEngine.isKillCountUnlimited(attacker)) {
            throw new IllegalStateException("Player already played Kill Card");
        }

        // 棄兩張手牌到墓地
        for (String discardCardId : discardCardIds) {
            HandCard discardedCard = attacker.playCard(discardCardId);
            graveyard.add(discardedCard);
        }

        // 標記本回合已出殺
        currentRound.setShowKill(true);

        // 建立虛擬殺，push ViperSpearKillBehavior 到 stack
        VirtualKill virtualKill = new VirtualKill();
        List<String> reactionPlayers = new ArrayList<>();
        reactionPlayers.add(targetPlayerId);
        ViperSpearKillBehavior behavior = new ViperSpearKillBehavior(
                this, attacker, reactionPlayers, target, virtualKill, discardCardIds);
        updateTopBehavior(behavior);

        List<DomainEvent> events = behavior.playerAction();
        removeCompletedBehaviors();
        return events;
    }

    private List<DomainEvent> playerUseViperSpearKillPassive(Player attacker, String targetPlayerId, List<String> discardCardIds, Behavior top) {
        // 場景特定 pre-validation（必須在 discard 前，避免 invalid input 留下手牌已棄的部分狀態）
        top.validateBeforeVirtualKillResponse(attacker.getId(), targetPlayerId);

        // 棄兩張手牌到墓地（被動使用不計入殺次數限制 — 這不是出牌階段的殺）
        for (String discardCardId : discardCardIds) {
            HandCard discardedCard = attacker.playCard(discardCardId);
            graveyard.add(discardedCard);
        }

        // 交給對應 behavior 處理：advance reaction state / push ViperSpearKillBehavior（BorrowedSword）
        VirtualKill virtualKill = new VirtualKill();
        List<DomainEvent> events = top.acceptVirtualKillResponse(attacker.getId(), targetPlayerId, virtualKill, discardCardIds);
        removeCompletedBehaviors();
        return events;
    }

    public List<DomainEvent> playerUseHeavenlyDoubleHalberdKill(String playerId,
                                                                 String cardId,
                                                                 List<String> targetPlayerIds) {
        Player attacker = getPlayer(playerId);

        // 1. activePlayer 驗證
        checkIsCurrentRoundValid(playerId);

        // 2. 不得有 pending behavior
        if (!topBehavior.isEmpty()) {
            throw new IllegalStateException("Cannot use halberd kill while another behavior is pending");
        }

        // 3. 裝備方天畫戟
        if (!(attacker.getEquipmentWeaponCard() instanceof HeavenlyDoubleHalberdCard)) {
            throw new IllegalStateException("Player is not equipped with HeavenlyDoubleHalberd");
        }

        // 4. 卡牌在手牌且為 Kill
        HandCard handCard = attacker.getHand().getCard(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not in hand: " + cardId));
        if (!(handCard instanceof Kill)) {
            throw new IllegalArgumentException("Card is not a Kill: " + cardId);
        }

        // 5. 此殺必須是最後一張手牌
        if (attacker.getHandSize() != 1) {
            throw new IllegalStateException("Halberd effect only usable when the Kill is the last hand card");
        }

        // 6. 目標列表驗證：null / size 1~3 / 不重複 / 不含自己
        if (targetPlayerIds == null) {
            throw new IllegalArgumentException("targetPlayerIds is required");
        }
        if (targetPlayerIds.isEmpty() || targetPlayerIds.size() > 3) {
            throw new IllegalArgumentException("targetPlayerIds size must be 1~3, got " + targetPlayerIds.size());
        }
        if (new HashSet<>(targetPlayerIds).size() != targetPlayerIds.size()) {
            throw new IllegalArgumentException("Duplicate targets");
        }
        if (targetPlayerIds.contains(playerId)) {
            throw new IllegalArgumentException("Cannot target self");
        }

        // 7. 攻擊範圍 + 目標免疫（空城等）驗證（兩條路徑都套用，避免 short-circuit 隱式依賴下游 handler）
        for (String targetId : targetPlayerIds) {
            Player hdhTarget = getPlayer(targetId);
            if (!isInAttackRange(attacker, hdhTarget)) {
                throw new IllegalStateException(String.format("%s is not in attack range", targetId));
            }
            if (SkillEngine.isImmuneToCard(hdhTarget, handCard)) {
                throw new IllegalStateException(
                        String.format("%s cannot be targeted by Kill (target immunity skill)", targetId));
            }
        }

        // 8. 短路：只有 1 個目標 → 行為等同普通殺，走正常 playCard 路徑
        if (targetPlayerIds.size() == 1) {
            return playerPlayCard(playerId, cardId, targetPlayerIds.get(0), PlayType.ACTIVE.getPlayType());
        }

        // 9. 多目標 — 結算依座位（行動）順序而非 request 傳入順序（issue #202）
        List<String> seatingOrderedTargets = sortBySeatingOrderFrom(attacker, targetPlayerIds);
        Player primaryTarget = getPlayer(seatingOrderedTargets.get(0));

        // 10. 回合出殺次數限制（考慮諸葛連弩——實際上不會同時裝兩把武器，但保留邏輯對稱性）
        if (currentRound.isShowKill() && !(attacker.getEquipmentWeaponCard() instanceof RepeatingCrossbowCard)
                && !SkillEngine.isKillCountUnlimited(attacker)) {
            throw new IllegalStateException("Player already played Kill Card");
        }

        // 11. 棄殺到墓地，標記本回合已出殺
        HandCard killCard = attacker.playCard(cardId);
        graveyard.add(killCard);
        currentRound.setShowKill(true);

        // 12. push HeavenlyDoubleHalberdKillBehavior
        HeavenlyDoubleHalberdKillBehavior behavior = new HeavenlyDoubleHalberdKillBehavior(
                this, attacker, seatingOrderedTargets, primaryTarget, cardId, killCard);
        updateTopBehavior(behavior);

        List<DomainEvent> events = behavior.playerAction();
        removeCompletedBehaviors();
        return events;
    }

    /**
     * 把 targetIds 重排為座位（行動）順序：以 anchor 的下家為起點沿座位鏈走，
     * 依序收集出現在 targetIds 中的玩家。
     */
    private List<String> sortBySeatingOrderFrom(Player anchor, List<String> targetIds) {
        List<String> ordered = new ArrayList<>();
        Set<String> remaining = new HashSet<>(targetIds);
        Player cursor = anchor;
        int total = seatingChart.getPlayers().size();
        for (int i = 0; i < total && !remaining.isEmpty(); i++) {
            cursor = getNextPlayer(cursor);
            if (remaining.remove(cursor.getId())) {
                ordered.add(cursor.getId());
            }
        }
        return ordered;
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

            // Store params for doBehaviorAction (Ward resolution)
            behavior.putParam(UserCommand.BORROWED_SWORD_PLAYER_ID.name(), currentPlayerId);
            behavior.putParam(UserCommand.BORROWED_SWORD_BORROWED_PLAYER_ID.name(), borrowedPlayerId);
            behavior.putParam(UserCommand.BORROWED_SWORD_ATTACK_TARGET_PLAYER_ID.name(), attackTargetPlayerId);

            // Check if any player (excluding the card player) has Ward
            if (doesAnyPlayerHaveWard(currentPlayerId)) {
                currentRound.setStage(Stage.Wait_Accept_Ward_Effect);
                behavior.setIsOneRound(false);

                Behavior wardBehavior = new WardBehavior(
                        this,
                        null,
                        whichPlayersHaveWard(currentPlayerId).stream().map(Player::getId).collect(Collectors.toList()),
                        null,
                        behavior.getCardId(),
                        PlayType.INACTIVE.getPlayType(),
                        behavior.getCard(),
                        true
                );
                wardBehavior.putParam(WardBehavior.WARD_TRIGGER_PLAYER_ID, currentPlayerId);
                wardBehavior.putParam(WardBehavior.WARD_TARGET_PLAYER_IDS, List.of(borrowedPlayerId));
                updateTopBehavior(wardBehavior);

                List<DomainEvent> events = new ArrayList<>(wardBehavior.playerAction());
                events.add(getGameStatusEvent("發動借刀殺人"));
                return events;
            }

            //判斷B有沒有殺（或可用丈八蛇矛當殺）；都沒有 → 玩家A取得玩家B當前的武器
            boolean noKillInHand = borrowedPlayer.getHand().getCards().stream().noneMatch(card -> card instanceof Kill);
            boolean canUseViperSpear = EighteenSpanViperSpearCard.canUseAsKill(borrowedPlayer);
            if (noKillInHand && !canUseViperSpear) {
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
        return whichPlayersHaveWard(null);
    }

    public List<Player> whichPlayersHaveWard(String playerId) {
        Stream<Player> stream = players.stream();
        if (playerId != null) {
            stream = stream.filter(player -> !player.getId().equals(playerId));
        }
        return stream.filter(player -> player.getHand().getCards().stream().anyMatch(card -> card instanceof Ward))
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

