package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.AskDodgeEvent;
import com.gaas.threeKingdoms.events.AskPlayEquipmentEffectEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.HeavenlyDoubleHalberdKillTriggerEvent;
import com.gaas.threeKingdoms.events.PlayCardEvent;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.round.Round;
import com.gaas.threeKingdoms.round.Stage;
import com.gaas.threeKingdoms.skill.registry.SkillEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.gaas.threeKingdoms.handcard.PlayCard.isDodgeCard;
import static com.gaas.threeKingdoms.handcard.PlayCard.isSkip;

/**
 * 方天畫戟多目標殺 Behavior。
 *
 * 繼承 NormalActiveKillBehavior 以複用 isEquipmentHasSpecialEffect 等 helper，但完全 override
 * playerAction() 與 doResponseToPlayerAction() 以實作 sequential polling（mirror ArrowBarrage）。
 *
 * 核心設計：
 *  - reactionPlayers = 使用者指定的完整目標列表（[primary, additional1, additional2?]）
 *  - currentReactionPlayer 逐一推進（依 reactionPlayers 順序，不是座位順序）
 *  - 每個目標獨立結算閃/防具/扣血/瀕死
 *  - 因為武器槽唯一，方天畫戟路徑不需要處理 GDCB/SPA/QilinBow/YinYangSwords/BlackPommel 交互
 *
 * 瀕死處理：若中間目標在扣血後進入瀕死流程（getDamagedEvent 會 push DyingAskPeachBehavior），
 * 此 behavior 的 isOneRound 會維持 false，等瀕死結束後由 DyingAskPeachBehavior 的
 * addAskDodgeEventIfCurrentBehaviorIsHeavenlyDoubleHalberdKillBehavior hook 恢復下一個 target
 * 的詢問（mirror ArrowBarrage 的既有機制）。
 */
public class HeavenlyDoubleHalberdKillBehavior extends NormalActiveKillBehavior {

    public HeavenlyDoubleHalberdKillBehavior(Game game,
                                             Player behaviorPlayer,
                                             List<String> reactionPlayers,
                                             Player currentReactionPlayer,
                                             String cardId,
                                             HandCard killCard) {
        super(game, behaviorPlayer, reactionPlayers, currentReactionPlayer,
                cardId, PlayType.ACTIVE.getPlayType(), killCard);
    }

    @Override
    public List<DomainEvent> playerAction() {
        // 殺已在 Game.playerUseHeavenlyDoubleHalberdKill() 從手牌移至墓地，這裡不再呼叫 playerPlayCard
        List<DomainEvent> events = new ArrayList<>();

        // 廣播多目標殺 — targetPlayerId 欄位留空，以 trigger event 的 targetPlayerIds 為準
        events.add(new PlayCardEvent("出牌", behaviorPlayer.getId(), "", cardId, playType));
        events.add(new HeavenlyDoubleHalberdKillTriggerEvent(
                behaviorPlayer.getId(), cardId, new ArrayList<>(reactionPlayers)));

        // 問第一個目標（currentReactionPlayer）出閃或發動防具
        askCurrentTargetDodgeOrEquipmentEffect(events);
        events.add(game.getGameStatusEvent("方天畫戟發動"));
        return events;
    }

    @Override
    public List<DomainEvent> doResponseToPlayerAction(String playerId, String targetPlayerId, String cardId, String playType) {
        Round currentRound = game.getCurrentRound();

        if (isSkip(playType)) {
            List<DomainEvent> events = new ArrayList<>();
            hitCurrentTarget(events, playerId, cardId, playType);
            return events;
        } else if (isDodgeCard(cardId)) {
            // 消耗閃到墓地
            playerPlayCardNotUpdateActivePlayer(game.getPlayer(playerId), cardId);

            List<DomainEvent> events = new ArrayList<>();
            events.add(new PlayCardEvent("出牌", playerId, targetPlayerId, cardId, playType));

            boolean isLast = isLastReactionPlayer(playerId);
            if (isLast) {
                isOneRound = true;
                currentRound.setActivePlayer(currentRound.getCurrentRoundPlayer());
            } else {
                isOneRound = false;
                advanceToNextTarget();
                askCurrentTargetDodgeOrEquipmentEffect(events);
            }
            events.add(game.getGameStatusEvent(playerId + " 出閃"));
            return events;
        } else {
            // 非閃且非可轉化牌（傾國/龍膽已在 Game.playerPlayCard 攔截）— 明確報錯，
            // 不再默默回空 events 造成前端卡住（issue #229）
            throw new IllegalArgumentException(
                    "被詢問出閃時只能出閃、skip 或發動轉化技（傾國/龍膽）：" + cardId);
        }
    }

    /**
     * 當前目標吃下這張殺（不出閃，或鐵騎紅色不能出閃）：扣血後推進輪詢；
     * 進入瀕死時下一位的詢問交由 DyingAskPeachBehavior 的 hook 接回。
     * 受傷後技能（奸雄／反饋／剛烈／遺計）介入時，推進 defer 到技能結算完（mirror 南蠻／萬箭）。
     */
    private void hitCurrentTarget(List<DomainEvent> events, String playerId, String cardId, String playType) {
        Round currentRound = game.getCurrentRound();
        int originalHp = currentReactionPlayer.getHP();
        events.addAll(game.getDamagedEvent(playerId, behaviorPlayer.getId(), cardId, card, playType,
                originalHp, currentReactionPlayer, currentRound, Optional.of(this)));

        // 奸雄：推進註冊為 callback；callback 經 MongoDB 遺失時由 resumeJianXiongPolling 接回（#209）
        if (!game.isTopBehaviorEmpty()
                && game.peekTopBehavior() instanceof WaitingJianXiongResponseBehavior wjx) {
            wjx.setOnResolved(() -> advanceAfterDamage(playerId));
            events.add(game.getGameStatusEvent("扣血但還活著"));
            return;
        }
        // 反饋／剛烈／遺計：待推進的目標記在 param（BehaviorData 持久化），
        // 由 WaitingSkillEffectBehavior.resolveChoice 的收鏈掃描呼叫 resumeJianXiongPolling
        if (!game.isTopBehaviorEmpty()
                && game.peekTopBehavior() instanceof WaitingSkillEffectBehavior wse
                && "true".equals(wse.getParam(WaitingSkillEffectBehavior.PARAM_RESUME_POLLING))) {
            putParam(PARAM_DEFERRED_ADVANCE_PLAYER_ID, playerId);
            events.add(game.getGameStatusEvent("扣血但還活著"));
            return;
        }
        events.addAll(advanceAfterDamage(playerId));
    }

    /**
     * 目標 damagedPlayerId 結算完後推進輪詢。依「該目標的下一位」計算（而非 currentReactionPlayer），
     * 瀕死救回後 defer 的 resume 再呼叫一次也不會跳過目標。
     */
    private List<DomainEvent> advanceAfterDamage(String damagedPlayerId) {
        Round currentRound = game.getCurrentRound();
        List<DomainEvent> events = new ArrayList<>();
        Player next = nextTargetAfter(damagedPlayerId);
        if (!game.getGamePhase().getPhaseName().equals("GeneralDying")) {
            // 扣血但沒死：繼續詢問下一個目標
            if (next == null) {
                isOneRound = true;
                currentRound.setActivePlayer(currentRound.getCurrentRoundPlayer());
            } else {
                isOneRound = false;
                currentReactionPlayer = next;
                askCurrentTargetDodgeOrEquipmentEffect(events);
            }
            events.add(game.getGameStatusEvent("扣血但還活著"));
        } else {
            // 進入瀕死流程：下一個目標的詢問交由 DyingAskPeachBehavior 的 hook 處理
            events.add(game.getGameStatusEvent("扣血已瀕臨死亡"));
            if (next == null) {
                isOneRound = true;
            } else {
                // 先把 currentReactionPlayer 推進到下一位，DyingAskPeach 結束後會讀取此位置
                currentReactionPlayer = next;
            }
        }
        return events;
    }

    /** 目標列表中 playerId 之後第一位存活的目標；沒有則 null。 */
    private Player nextTargetAfter(String playerId) {
        int index = reactionPlayers.indexOf(playerId);
        for (int i = index + 1; i < reactionPlayers.size(); i++) {
            Player candidate = game.getPlayer(reactionPlayers.get(i));
            if (!candidate.isAlreadyDeath()) {
                return candidate;
            }
        }
        return null;
    }

    /** 方天畫戟逐一輪詢目標：受傷後技能詢問時保留本 behavior，結算完再推進（同南蠻／萬箭）。 */
    @Override
    public boolean isPollingCaller() {
        return true;
    }

    @Override
    public List<DomainEvent> resumeJianXiongPolling(String damagedPlayerId) {
        return advanceAfterDamage(damagedPlayerId);
    }

    /**
     * 八卦陣 handler 會呼叫此 method 取代 isOneRound 的判斷邏輯。
     * 方天畫戟為多目標輪詢，八卦陣在中間目標成功並不代表整個 behavior 結束。
     */
    @Override
    public boolean judgeWhetherRemoveTopBehavior() {
        return false;
    }

    /**
     * 判斷某玩家是否為 reactionPlayers 列表的最後一位（注意：依列表順序，非座位順序）。
     */
    public boolean isLastReactionPlayer(String playerId) {
        return reactionPlayers.get(reactionPlayers.size() - 1).equals(playerId);
    }

    /**
     * 將 currentReactionPlayer 推進到 reactionPlayers 列表中的下一位。
     * 注意：使用列表順序 (index + 1)，不是座位順序。
     */
    public void advanceToNextTarget() {
        int currentIndex = reactionPlayers.indexOf(currentReactionPlayer.getId());
        if (currentIndex < 0 || currentIndex + 1 >= reactionPlayers.size()) {
            return;
        }
        String nextId = reactionPlayers.get(currentIndex + 1);
        currentReactionPlayer = game.getPlayer(nextId);
    }

    /**
     * 詢問當前 currentReactionPlayer 出閃（或發動防具效果）。
     * 將 activePlayer 設為該目標。
     */
    public void askCurrentTargetDodgeOrEquipmentEffect(List<DomainEvent> events) {
        Round currentRound = game.getCurrentRound();
        Player targetPlayer = currentReactionPlayer;
        currentRound.setActivePlayer(targetPlayer);

        // 流離（成為殺的目標時）早於八卦陣：輪到大喬時先問；放棄後由 continueAfterLiuLiDeclined 接回
        if (!targetPlayer.getId().equals(getParam(PARAM_LIULI_DECIDED_TARGET))) {
            Optional<List<DomainEvent>> liuLi =
                    com.gaas.threeKingdoms.skill.wu.LiuLiSkill.askIfApplicable(game, targetPlayer, this);
            if (liuLi.isPresent()) {
                events.addAll(liuLi.get());
                return;
            }
        }

        // 鐵騎（攻擊者側，流離之後、八卦陣之前）：每個目標各判一次；判定牌過鬼才暫停點，
        // 司馬懿介入時暫停，resume 走 resumeTieQiJudgement（GuiCaiSkill 以 NormalActiveKillBehavior 型別呼叫）
        if (SkillEngine.hasTieQi(behaviorPlayer)) {
            HandCard judgement = game.drawCardForCardEffect(1).get(0);
            Optional<List<DomainEvent>> paused = com.gaas.threeKingdoms.skill.wei.GuiCaiSkill.tryPause(
                    game, behaviorPlayer, judgement,
                    com.gaas.threeKingdoms.skill.wei.GuiCaiSkill.TYPE_TIE_QI, "鐵騎",
                    java.util.Map.of(com.gaas.threeKingdoms.skill.wei.GuiCaiSkill.PARAM_TIEQI_TARGET_ID,
                            targetPlayer.getId()));
            if (paused.isPresent()) {
                events.addAll(paused.get());
                return;
            }
            events.addAll(resumeTieQiJudgement(judgement, targetPlayer));
            return;
        }
        askArmorOrDodge(events, targetPlayer);
    }

    /** 鐵騎判定結果：紅色 → 當前目標不能出閃也不能發動八卦陣，直接扣血並推進；否則照常問防具／閃。 */
    @Override
    public List<DomainEvent> resumeTieQiJudgement(HandCard judgement, Player targetPlayer) {
        List<DomainEvent> events = new ArrayList<>();
        boolean[] tieQiSuccess = new boolean[1];
        events.addAll(SkillEngine.tieQiResolvedEvents(game, behaviorPlayer, targetPlayer, judgement, tieQiSuccess));
        if (tieQiSuccess[0]) {
            hitCurrentTarget(events, targetPlayer.getId(), cardId, PlayType.SYSTEM_INTERNAL.getPlayType());
        } else {
            game.getCurrentRound().setActivePlayer(targetPlayer);
            askArmorOrDodge(events, targetPlayer);
        }
        return events;
    }

    private void askArmorOrDodge(List<DomainEvent> events, Player targetPlayer) {
        Round currentRound = game.getCurrentRound();
        if (isEquipmentHasSpecialEffect(targetPlayer)) {
            currentRound.setStage(Stage.Wait_Equipment_Effect);
            events.add(new AskPlayEquipmentEffectEvent(
                    targetPlayer.getId(),
                    targetPlayer.getEquipment().getArmor(),
                    List.of(targetPlayer.getId())));
        } else {
            currentRound.setStage(Stage.Normal);
            // 護駕 hook：若目標為主公曹操且有其他存活 Wei，攔截 AskDodge
            Optional<List<DomainEvent>> intercepted = SkillEngine.beforeAskDodge(game, targetPlayer, this);
            if (intercepted.isPresent()) {
                events.addAll(intercepted.get());
            } else {
                events.add(new AskDodgeEvent(targetPlayer.getId()));
            }
        }
    }

    /** 大喬放棄流離 → 她照常回應這一輪（防具／閃），之後繼續輪詢後面的目標。 */
    @Override
    public List<DomainEvent> continueAfterLiuLiDeclined(Player daQiao) {
        putParam(PARAM_LIULI_DECIDED_TARGET, daQiao.getId());
        List<DomainEvent> events = new ArrayList<>();
        askCurrentTargetDodgeOrEquipmentEffect(events);
        return events;
    }

    /**
     * 流離轉移：新目標在目標列表中「取代大喬的位置」（輪詢順序不變），並立刻詢問新目標。
     * 已是本殺目標的角色不能被選（{@link #canLiuLiRedirectTo}）。
     */
    @Override
    public List<DomainEvent> redirectTo(Player newTarget) {
        int index = reactionPlayers.indexOf(currentReactionPlayer.getId());
        reactionPlayers.set(index, newTarget.getId());
        currentReactionPlayer = newTarget;
        List<DomainEvent> events = new ArrayList<>();
        askCurrentTargetDodgeOrEquipmentEffect(events);
        return events;
    }

    /** 多目標殺：已在目標列表（含已結算過的）的角色不能再被流離指定。 */
    @Override
    public boolean canLiuLiRedirectTo(Player candidate) {
        return !reactionPlayers.contains(candidate.getId());
    }

    @Override
    public List<DomainEvent> acceptDodgeFromHuJia(String dodgedPlayerId, String weiPlayerId, String dodgeCardId) {
        // 方天畫戟不會觸發 GDCB / SPA（武器槽已被佔用）— 直接走 polling 推進
        Round currentRound = game.getCurrentRound();
        List<DomainEvent> events = new ArrayList<>();
        events.add(new PlayCardEvent("出牌", weiPlayerId, behaviorPlayer.getId(),
                dodgeCardId, PlayType.ACTIVE.getPlayType()));

        boolean isLast = isLastReactionPlayer(dodgedPlayerId);
        if (isLast) {
            isOneRound = true;
            currentRound.setActivePlayer(currentRound.getCurrentRoundPlayer());
        } else {
            isOneRound = false;
            advanceToNextTarget();
            askCurrentTargetDodgeOrEquipmentEffect(events);
        }
        events.add(game.getGameStatusEvent(dodgedPlayerId + " 出閃"));
        return events;
    }
}
