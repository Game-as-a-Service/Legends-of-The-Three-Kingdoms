package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.gamephase.GameOver;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.VirtualKill;
import com.gaas.threeKingdoms.handcard.equipmentcard.EquipmentCard;
import com.gaas.threeKingdoms.handcard.scrollcard.Lightning;
import com.gaas.threeKingdoms.player.HealthStatus;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.round.Round;
import com.gaas.threeKingdoms.round.RoundPhase;
import com.gaas.threeKingdoms.skill.context.DamageContext;
import com.gaas.threeKingdoms.skill.registry.SkillEngine;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.gaas.threeKingdoms.handcard.PlayCard.isPeachCard;

@Getter
public class DyingAskPeachBehavior extends Behavior implements com.gaas.threeKingdoms.behavior.JianXiongCompatibleTopBehavior {

    /**
     * 致命傷的 sourceCard.id 與 attackerPlayerId — 在 revival branch 用來 replay
     * SkillEngine.onDamaged（FAQ：曹操瀕死被救回後仍可發動奸雄）。null 表示
     * 不適合 replay（一般情況下 VirtualKill 不在 PlayCard.factory 或非殺類傷害）。
     * <p>
     * 若致命傷源自丈八蛇矛（VirtualKill），改用 pendingViperSpearDiscardCardIds
     * 持有兩張棄牌 id，replay 時 JianXiongSkill 透過 DyingAskPeachBehavior 取得。
     */
    private final String pendingSourceCardId;
    private final String pendingAttackerPlayerId;
    private final List<String> pendingViperSpearDiscardCardIds;

    public DyingAskPeachBehavior(Game game, Player behaviorPlayer, List<String> reactionPlayers, Player currentReactionPlayer, String cardId, String playType, HandCard card) {
        this(game, behaviorPlayer, reactionPlayers, currentReactionPlayer, cardId, playType, card, null, null, null);
    }

    public DyingAskPeachBehavior(Game game, Player behaviorPlayer, List<String> reactionPlayers, Player currentReactionPlayer, String cardId, String playType, HandCard card,
                                  String pendingSourceCardId, String pendingAttackerPlayerId,
                                  List<String> pendingViperSpearDiscardCardIds) {
        super(game, behaviorPlayer, reactionPlayers, currentReactionPlayer, cardId, playType, card, true, false, false);
        this.pendingSourceCardId = pendingSourceCardId;
        this.pendingAttackerPlayerId = pendingAttackerPlayerId;
        this.pendingViperSpearDiscardCardIds = pendingViperSpearDiscardCardIds == null
                ? null
                : List.copyOf(pendingViperSpearDiscardCardIds);
        List<Player> players = game.getSeatingChart().getPlayers();
        int damagedPlayerIndex = players.indexOf(behaviorPlayer);
        List<Player> reorderedPlayerList = new ArrayList<>();
        reorderedPlayerList.addAll(players.subList(damagedPlayerIndex, players.size()));
        reorderedPlayerList.addAll(players.subList(0, damagedPlayerIndex));
        this.reactionPlayers = reorderedPlayerList.stream().map(Player::getId).toList();
    }

    @Override
    public List<DomainEvent> playerAction() {
        return null;
    }

    @Override
    protected List<DomainEvent> doResponseToPlayerAction(String playerId, String targetPlayerId, String cardId, String playType) {
        Player dyingPlayer = game.getPlayer(targetPlayerId);
        Player currentPlayer = game.getPlayer(playerId);
        int originalHp = dyingPlayer.getHP();
        List<DomainEvent> events = new ArrayList<>();
        if (isSkip(playType)) {
            Round currentRound = game.getCurrentRound();
            PlayCardEvent playCardEvent = new PlayCardEvent("不出牌", playerId, targetPlayerId, cardId, playType);
            events.add(playCardEvent);
            if (isLastReactionPlayer(playerId)) {
                isOneRound = true;
                game.removeDyingPlayer(dyingPlayer);

                if (onlyTraitorsRemain()) {
                    SettlementEvent settlementEvent = new SettlementEvent(dyingPlayer);
                    GameOverEvent gameOverEvent = new GameOverEvent("內奸獲勝", getWinners(game.getPlayers(), List.of(Role.TRAITOR)));
                    game.enterPhase(new GameOver(game));
                    events.addAll(List.of(settlementEvent, gameOverEvent));
                    events.add(game.getGameStatusEvent(String.format("%s 死亡", dyingPlayer.getId())));
                    return events;
                } else if (isMonarch(dyingPlayer)) {
                    SettlementEvent settlementEvent = new SettlementEvent(dyingPlayer);
                    GameOverEvent gameOverEvent = new GameOverEvent(game.createGameOverMessage(), getWinners(game.getPlayers(), List.of(Role.REBEL)));
                    game.enterPhase(new GameOver(game));
                    events.addAll(List.of(settlementEvent, gameOverEvent));
                    events.add(game.getGameStatusEvent("主公死亡"));
                    return events;
                } else if (haveNoOtherRebelAndTraitor()) {
                    SettlementEvent settlementEvent = new SettlementEvent(dyingPlayer);
                    GameOverEvent gameOverEvent = new GameOverEvent("主公與忠臣獲勝", getWinners(game.getPlayers(), List.of(Role.MINISTER, Role.MONARCH)));
                    game.enterPhase(new GameOver(game));
                    events.addAll(List.of(settlementEvent, gameOverEvent));
                    events.add(game.getGameStatusEvent("反賊死亡"));
                    return events;
                } else if (isREBEL(dyingPlayer)) {
                    SettlementEvent settlementEvent = new SettlementEvent(dyingPlayer);
                    Player lastDamagedPlayer = game.getLastAttacker();
                    DomainEvent drawCardEvent = game.drawCardToPlayer(lastDamagedPlayer, false,3);
                    events.addAll(List.of(settlementEvent, drawCardEvent));
                    addAskKillEventIfCurrentBehaviorIsBarbarianInvasionBehavior(events);
                    addAskDodgeEventIfCurrentBehaviorIsArrowBarrageBehavior(events);
                    addAskDodgeEventIfCurrentBehaviorIsHeavenlyDoubleHalberdKillBehavior(events);
                } else if (isMINISTER(dyingPlayer)) {
                    SettlementEvent settlementEvent = new SettlementEvent(dyingPlayer);
                    events.add(settlementEvent);
                    // 如果殺忠臣的玩家是主公，則主公棄掉所有的牌
                    if (isMonarch(game.getLastAttacker())) {
                        List<HandCard> discardCards = game.getLastAttacker().discardAllCards();

                        String message = String.format("主公 %s 殺死忠臣 %s，棄所有的牌", game.getLastAttacker().getGeneralName(), dyingPlayer.getGeneralName());
                        DomainEvent discardCardEvent = new DiscardEvent(discardCards, message, game.getLastAttacker().getId());
                        game.getGraveyard().add(discardCards);

                        List<EquipmentCard> discardEquipment = game.getLastAttacker().discardAllEquipment();
                        String messageEquipment = String.format("玩家 %s 棄裝備", game.getLastAttacker().getId());
                        List<String> discardEquipmentIds = discardEquipment.stream().map(HandCard::getId).toList();
                        DomainEvent discardEquipmentEvent = new DiscardEquipmentEvent(game.getLastAttacker().getId(), discardEquipmentIds, messageEquipment);
                        // 裝備牌放進棄牌堆
                        List<HandCard> discardEquipmentList = discardEquipment.stream().map(e -> (HandCard) e).toList();
                        game.getGraveyard().add(discardEquipmentList);

                        events.addAll(List.of(discardCardEvent, discardEquipmentEvent));
                    }
                    addAskKillEventIfCurrentBehaviorIsBarbarianInvasionBehavior(events);
                    addAskDodgeEventIfCurrentBehaviorIsArrowBarrageBehavior(events);
                    addAskDodgeEventIfCurrentBehaviorIsHeavenlyDoubleHalberdKillBehavior(events);
                } else {
                    SettlementEvent settlementEvent = new SettlementEvent(dyingPlayer);
                    events.add(settlementEvent);
                    addAskKillEventIfCurrentBehaviorIsBarbarianInvasionBehavior(events);
                    addAskDodgeEventIfCurrentBehaviorIsArrowBarrageBehavior(events);
                    addAskDodgeEventIfCurrentBehaviorIsHeavenlyDoubleHalberdKillBehavior(events);
                }

                //  需要移除的 Behavior，isOneRound 要設為 true
                JudgementIfRemoveBehavior();
                game.enterPhase(new Normal(game));
                // 如果 events 包含 askKillEvent 與 askDodgeEvent，則不需要再設定 activePlayer
                if (events.stream().noneMatch(e -> e instanceof AskKillEvent) && events.stream().noneMatch(e -> e instanceof AskDodgeEvent)) {
                    if (currentRound.getCurrentRoundPlayer().equals(dyingPlayer)) {
                        List<DomainEvent> newRoundEvent = game.goNextRound(dyingPlayer);
                        events.addAll(newRoundEvent);
                    } else {
                        currentRound.setActivePlayer(currentRound.getCurrentRoundPlayer());
                    }
                }
            } else {
                events.add(createAskPeachEvent(game.getNextPlayer(currentPlayer), dyingPlayer));
                currentRound.setActivePlayer(game.getNextPlayer(currentPlayer));
            }
            events.add(game.getGameStatusEvent("不出牌"));
            return events;
        } else if (isPeachCard(cardId)) {

            // Player use peach card
            HandCard card = currentPlayer.playCard(cardId);
            card.effect(dyingPlayer);
            // 救援（孫權主公技）：主公孫權瀕死時其他吳勢力的桃效果 +1
            int jiuYuanExtra = com.gaas.threeKingdoms.skill.registry.SkillEngine
                    .jiuYuanExtraHeal(dyingPlayer, currentPlayer);
            for (int i = 0; i < jiuYuanExtra; i++) {
                card.effect(dyingPlayer);
            }
            game.getGraveyard().add(card);
            Round currentRound = game.getCurrentRound();
            boolean dyingPlayerIsAlive = dyingPlayer.getHealthStatus().equals(HealthStatus.ALIVE);

            // Create Domain Events
            PlayCardEvent playCardEvent = new PlayCardEvent("出牌", playerId, targetPlayerId, cardId, playType);
            PeachEvent peachEvent = new PeachEvent(targetPlayerId, originalHp, dyingPlayer.getHP());
            events.addAll(List.of(playCardEvent, peachEvent));

            if (dyingPlayerIsAlive) {
                // Dying player is healed
                currentRound.setDyingPlayer(null);
                currentRound.setActivePlayer(currentRound.getCurrentRoundPlayer());
                game.enterPhase(new Normal(game));
                isOneRound = true;

                if (currentRound.getCurrentCard() instanceof Lightning && currentRound.getRoundPhase().equals(RoundPhase.Judgement)) {
                    // 閃電讓玩家死亡 → 被桃救回 → 回到該玩家判定階段繼續流程
                    events.addAll(game.playerTakeTurnStartInJudgement(currentRound.getCurrentRoundPlayer()));
                    // Lightning 路徑下也可能有 JianXiong replay；交由 replay 接續
                    replayOnDamagedAfterRevival(dyingPlayer, events);
                } else {
                    // 致命傷被救回 → replay SkillEngine.onDamaged（FAQ: 曹操可發動奸雄收造成傷害的牌）
                    // 順序：先 replay，若 JianXiong 觸發（push WaitingJX）則把 polling-resume defer
                    // 進 WaitingJX.onResolved；否則立即 emit polling-resume 事件。
                    replayOnDamagedAfterRevival(dyingPlayer, events);

                    if (!game.isTopBehaviorEmpty()
                            && game.peekTopBehavior() instanceof WaitingJianXiongResponseBehavior wjx) {
                        // JianXiong 介入：polling caller (BI / AB / Halberd) 的 resume 推到 WaitingJX 解決後
                        wjx.setOnResolved(() -> {
                            List<DomainEvent> resumeEvents = new ArrayList<>();
                            addAskKillEventIfCurrentBehaviorIsBarbarianInvasionBehavior(resumeEvents);
                            addAskDodgeEventIfCurrentBehaviorIsArrowBarrageBehavior(resumeEvents);
                            addAskDodgeEventIfCurrentBehaviorIsHeavenlyDoubleHalberdKillBehavior(resumeEvents);
                            return resumeEvents;
                        });
                    } else {
                        // 一般情況（非曹操，或 JianXiong 沒觸發）— 立即 emit polling resume
                        addAskKillEventIfCurrentBehaviorIsBarbarianInvasionBehavior(events);
                        addAskDodgeEventIfCurrentBehaviorIsArrowBarrageBehavior(events);
                        addAskDodgeEventIfCurrentBehaviorIsHeavenlyDoubleHalberdKillBehavior(events);
                    }
                }
            } else {
                events.add(createAskPeachEvent(currentPlayer, dyingPlayer));
            }
            events.add(game.getGameStatusEvent("出牌"));
            return events;
        } else {
            //TODO:怕有其他效果或殺的其他case
            return null;
        }
    }

    //TODO
    private void JudgementIfRemoveBehavior() {
        Stack<Behavior> topBehavior = game.getTopBehavior();
        IntStream.range(0, topBehavior.size())
                .mapToObj(i -> topBehavior.get(topBehavior.size() - 1 - i))
                .filter(behavior -> (behavior instanceof NormalActiveKillBehavior
                        // 方天畫戟為多目標輪詢，中間目標死亡不應自動 pop
                        && !(behavior instanceof HeavenlyDoubleHalberdKillBehavior))
                        || behavior instanceof DuelBehavior
                        || behavior instanceof BorrowedSwordBehavior)
                .findFirst()
                .ifPresent(behavior -> behavior.setIsOneRound(true));
    }

    private boolean isLastReactionPlayer(String playerId) {
        return reactionPlayers.get(reactionPlayers.size() - 1).equals(playerId);
    }

    private void addAskKillEventIfCurrentBehaviorIsBarbarianInvasionBehavior(List<DomainEvent> events) {
        game.peekTopBehaviorSecondElement().ifPresent(secondBehavior -> {
            if (secondBehavior instanceof BarbarianInvasionBehavior barbarianInvasionBehavior) {
                Player behaviorCurrentReactionPlayer = barbarianInvasionBehavior.getCurrentReactionPlayer();
                if (barbarianInvasionBehavior.isInReactionPlayers(behaviorCurrentReactionPlayer.getId())) {
                    events.add(new AskKillEvent(behaviorCurrentReactionPlayer.getId()));
                }
                game.getCurrentRound().setActivePlayer(behaviorCurrentReactionPlayer);
            }
        });
    }

    private void addAskDodgeEventIfCurrentBehaviorIsArrowBarrageBehavior(List<DomainEvent> events) {
        game.peekTopBehaviorSecondElement().ifPresent(secondBehavior -> {
            if (secondBehavior instanceof ArrowBarrageBehavior arrowBarrageBehavior) {
                Player arrowBarrageCurrentReactionPlayer = arrowBarrageBehavior.getCurrentReactionPlayer();
                if (arrowBarrageBehavior.isInReactionPlayers(arrowBarrageCurrentReactionPlayer.getId())) {
                    events.add(new AskDodgeEvent(arrowBarrageCurrentReactionPlayer.getId()));
                }
                game.getCurrentRound().setActivePlayer(arrowBarrageCurrentReactionPlayer);
            }
        });
    }

    private void addAskDodgeEventIfCurrentBehaviorIsHeavenlyDoubleHalberdKillBehavior(List<DomainEvent> events) {
        game.peekTopBehaviorSecondElement().ifPresent(secondBehavior -> {
            if (secondBehavior instanceof HeavenlyDoubleHalberdKillBehavior halberdBehavior) {
                // 最後一個目標已處理完（isOneRound=true），不需要再問下一位
                if (halberdBehavior.isOneRound()) return;
                // halberd 在扣血進入瀕死時已將 currentReactionPlayer 推進到下一位
                List<DomainEvent> dodgeOrEquipmentEvents = new ArrayList<>();
                halberdBehavior.askCurrentTargetDodgeOrEquipmentEffect(dodgeOrEquipmentEvents);
                events.addAll(dodgeOrEquipmentEvents);
            }
        });
    }

    private static boolean isMonarch(Player player) {
        return player != null && player.getRoleCard().getRole() == Role.MONARCH;
    }

    private static boolean isREBEL(Player dyingPlayer) {
        return dyingPlayer != null && dyingPlayer.getRoleCard().getRole().equals(Role.REBEL);
    }

    private static boolean isMINISTER(Player dyingPlayer) {
        return dyingPlayer != null && dyingPlayer.getRoleCard().getRole().equals(Role.MINISTER);
    }

    private boolean haveNoOtherRebelAndTraitor() {
        // 確認是否玩家中沒有其他反賊和內奸
        return game.getSeatingChart().getPlayers().stream()
                .noneMatch(p -> p.getRoleCard().getRole().equals(Role.REBEL) || p.getRoleCard().getRole().equals(Role.TRAITOR));
    }

    private boolean onlyTraitorsRemain() {
        return game.getSeatingChart().getPlayers().stream()
                .allMatch(p -> p.getRoleCard().getRole().equals(Role.TRAITOR));
    }

    private boolean onlyRebelRemain() {
        return game.getSeatingChart().getPlayers().stream()
                .allMatch(p -> p.getRoleCard().getRole().equals(Role.REBEL));
    }

    private List<String> getWinners(List<Player> players, List<Role> roleList) {
        return players.stream()
                .filter(player -> roleList.contains(player.getRoleCard().getRole()))
                .map(Player::getId)
                .collect(Collectors.toList());
    }

    private static String findNextPlayer(List<String> players, String currentPlayer) {
        int currentIndex = players.indexOf(currentPlayer);
        if (currentIndex != -1 && currentIndex < players.size() - 1) {
            return players.get(currentIndex + 1);
        }
        return null;
    }

    private AskPeachEvent createAskPeachEvent(Player player, Player dyingPlayer) {
        return new AskPeachEvent(player.getId(), dyingPlayer.getId());
    }


    private boolean isSkip(String playType) {
        return PlayType.SKIP.getPlayType().equals(playType);
    }

    private void replayOnDamagedAfterRevival(Player dyingPlayer, List<DomainEvent> events) {
        Player attacker = (pendingAttackerPlayerId != null && !pendingAttackerPlayerId.isEmpty())
                ? game.getPlayer(pendingAttackerPlayerId)
                : null;

        HandCard sourceCard = null;
        if (pendingViperSpearDiscardCardIds != null && !pendingViperSpearDiscardCardIds.isEmpty()) {
            // 丈八蛇矛致命攻擊：sourceCard 是 VirtualKill；JianXiongSkill 會透過
            // DyingAskPeachBehavior 取得 pending 的兩張棄牌 id
            sourceCard = new VirtualKill();
        } else if (pendingSourceCardId != null) {
            // PlayCard.findById 找不到時返 null（非 throw），用 null check 處理
            sourceCard = PlayCard.findById(pendingSourceCardId);
        }
        if (sourceCard == null) {
            return;
        }
        DamageContext ctx = new DamageContext(dyingPlayer, attacker, sourceCard, 1);
        events.addAll(SkillEngine.onDamaged(game, ctx));
    }
}
