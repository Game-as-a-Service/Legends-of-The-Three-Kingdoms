package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.behavior.HuJiaCompatibleAskDodgeBehavior;
import com.gaas.threeKingdoms.events.AskActivateYinYangSwordsEvent;
import com.gaas.threeKingdoms.events.AskDodgeEvent;
import com.gaas.threeKingdoms.events.AskGreenDragonCrescentBladeEffectEvent;
import com.gaas.threeKingdoms.events.AskPlayEquipmentEffectEvent;
import com.gaas.threeKingdoms.events.AskStonePiercingAxeEffectEvent;
import com.gaas.threeKingdoms.events.BlackPommelEffectEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.PlayCardEvent;
import com.gaas.threeKingdoms.skill.registry.SkillEngine;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.equipmentcard.EquipmentCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.BlackPommelCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.GreenDragonCrescentBladeCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.QilinBowCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.StonePiercingAxeCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.YinYangSwordsCard;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.round.Round;
import com.gaas.threeKingdoms.round.Stage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.gaas.threeKingdoms.handcard.PlayCard.isDodgeCard;
import static com.gaas.threeKingdoms.handcard.PlayCard.isSkip;


public class NormalActiveKillBehavior extends Behavior
        implements com.gaas.threeKingdoms.behavior.JianXiongCompatibleTopBehavior,
                   HuJiaCompatibleAskDodgeBehavior {

    /** 鐵騎判定結果（"true"/"false"）：雌雄雙股劍結算完後依此決定直接結算或問閃。BehaviorData 持久化。 */
    public static final String PARAM_TIEQI_SUCCESS = "TIEQI_SUCCESS";
    /** 已對流離作出決定的目標 id（放棄流離後不再詢問同一人）。BehaviorData 持久化。 */
    public static final String PARAM_LIULI_DECIDED_TARGET = "LIULI_DECIDED_TARGET";
    /** 本殺目標是否由流離轉移而來（"true"）。BehaviorData 持久化。 */
    public static final String PARAM_LIULI_REDIRECTED = "LIULI_REDIRECTED";

    public NormalActiveKillBehavior(Game game, Player behaviorPlayer, List<String> reactionPlayers, Player currentReactionPlayer, String cardId, String playType, HandCard card) {
        super(game, behaviorPlayer, reactionPlayers, currentReactionPlayer, cardId, playType, card, true, false, false);
    }

    @Override
    public List<DomainEvent> playerAction() {
        String targetPlayerId = reactionPlayers.get(0);
        Player targetPlayer = game.getPlayer(targetPlayerId);

        playerPlayCard(behaviorPlayer, game.getPlayer(targetPlayerId), cardId);

        Round currentRound = game.getCurrentRound();

        List<DomainEvent> events = new ArrayList<>();
        events.add(new PlayCardEvent("出牌", behaviorPlayer.getId(), targetPlayerId, cardId, playType));

        // 鐵騎（武將技）→ 雌雄雙股劍（武器技）→ 防具／閃（FAQ：先發動武將技能，後發動武器技能）
        emitTieQiThenArmorOrDodge(events, targetPlayer);
        events.add(game.getGameStatusEvent("出牌"));
        return events;
    }

    /**
     * 殺指定目標後：
     * 1. 鐵騎（攻擊者側）**先於防具**：判定紅色 → 目標不能出閃、也不能發動八卦陣，直接結算傷害
     *    （FAQ：馬超先決定是否發動鐵騎，判定為紅色則對方不能發動八卦陣與使用閃）
     *    判定牌抽出後過鬼才暫停點；司馬懿介入時暫停，resume 走 {@link #resumeTieQiJudgement}
     * 2. 鐵騎未生效（或沒有鐵騎）→ 照常問防具（八卦陣）或閃（{@link #emitArmorOrDodge}）
     */
    private void emitTieQiThenArmorOrDodge(List<DomainEvent> events, Player targetPlayer) {
        // 流離（目標側，成為殺的目標時）最先：FAQ 大喬先決定是否發動流離，馬超再決定是否發動鐵騎；
        // 也早於八卦陣（原本放在問閃前，目標有八卦陣時根本不會問流離）
        if (!targetPlayer.getId().equals(getParam(PARAM_LIULI_DECIDED_TARGET))) {
            Optional<List<DomainEvent>> liuLi =
                    com.gaas.threeKingdoms.skill.wu.LiuLiSkill.askIfApplicable(game, targetPlayer, this);
            if (liuLi.isPresent()) {
                events.addAll(liuLi.get());
                return;
            }
        }
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
        afterTieQi(events, targetPlayer, false);
    }

    /**
     * 鐵騎判定完（或沒有鐵騎）之後：
     * <ol>
     *   <li>雌雄雙股劍（異性、且非流離轉移來的目標）→ 先問攻擊者是否發動；結算完由
     *       {@link #continueAfterYinYangSwords} 接回，依記下的鐵騎結果決定</li>
     *   <li>鐵騎生效 → 不能出閃也不能發動八卦陣，直接結算傷害</li>
     *   <li>否則 → 問防具或閃</li>
     * </ol>
     */
    private void afterTieQi(List<DomainEvent> events, Player targetPlayer, boolean tieQiSuccess) {
        if (!isRedirected() && shouldTriggerYinYangSwords(behaviorPlayer, targetPlayer)) {
            isOneRound = false;
            game.getCurrentRound().setActivePlayer(behaviorPlayer);
            game.updateTopBehavior(new WaitingYinYangSwordsActivationBehavior(
                    game, behaviorPlayer, Collections.singletonList(targetPlayer.getId()),
                    behaviorPlayer, cardId, PlayType.ACTIVE.getPlayType(), card));
            events.add(new AskActivateYinYangSwordsEvent(behaviorPlayer.getId(), targetPlayer.getId()));
            return;
        }
        if (tieQiSuccess) {
            damageWithoutDodge(events, targetPlayer);
            return;
        }
        emitArmorOrDodge(events, targetPlayer);
    }

    /** 大喬放棄流離 → 由她自己承受這張殺，接續鐵騎 → 雌雄雙股劍 → 防具／閃。 */
    public List<DomainEvent> continueAfterLiuLiDeclined(Player daQiao) {
        putParam(PARAM_LIULI_DECIDED_TARGET, daQiao.getId());
        game.getCurrentRound().setActivePlayer(daQiao);
        List<DomainEvent> events = new ArrayList<>();
        emitTieQiThenArmorOrDodge(events, daQiao);
        return events;
    }

    /**
     * 雌雄雙股劍結算完（攻擊者放棄、目標無手牌、或目標選完）→ 接回殺的流程：
     * 鐵騎已生效 → 直接結算傷害；否則問防具或閃（含護駕／流離等問閃前 hook）。
     */
    public List<DomainEvent> continueAfterYinYangSwords(Player targetPlayer) {
        List<DomainEvent> events = new ArrayList<>();
        game.getCurrentRound().setActivePlayer(targetPlayer);
        if ("true".equals(getParam(PARAM_TIEQI_SUCCESS))) {
            damageWithoutDodge(events, targetPlayer);
        } else {
            emitArmorOrDodge(events, targetPlayer);
        }
        return events;
    }

    /** 找 stack 中（由上往下）第一個殺 behavior —— 雌雄雙股劍的等待 behavior 疊在它上面。 */
    public static NormalActiveKillBehavior findIn(Game game) {
        java.util.Stack<com.gaas.threeKingdoms.behavior.Behavior> stack = game.getTopBehavior();
        for (int i = stack.size() - 1; i >= 0; i--) {
            if (stack.get(i) instanceof NormalActiveKillBehavior kill) {
                return kill;
            }
        }
        throw new IllegalStateException("No NormalActiveKillBehavior in stack");
    }

    private void damageWithoutDodge(List<DomainEvent> events, Player targetPlayer) {
        int originalHp = targetPlayer.getHP();
        events.addAll(game.getDamagedEvent(targetPlayer.getId(), behaviorPlayer.getId(),
                this.cardId, this.card, PlayType.SYSTEM_INTERNAL.getPlayType(),
                originalHp, targetPlayer, game.getCurrentRound(), Optional.of(this)));
    }

    private boolean isRedirected() {
        return "true".equals(getParam(PARAM_LIULI_REDIRECTED));
    }

    /** 問防具效果（八卦陣）或問閃；青釭劍無視防具時直接問閃。 */
    private void emitArmorOrDodge(List<DomainEvent> events, Player targetPlayer) {
        if (isEquipmentHasSpecialEffect(targetPlayer) && !isAttackerHasBlackPommel(behaviorPlayer)) {
            game.getCurrentRound().setStage(Stage.Wait_Equipment_Effect);
            events.add(new AskPlayEquipmentEffectEvent(targetPlayer.getId(),
                    targetPlayer.getEquipment().getArmor(), List.of(targetPlayer.getId())));
            return;
        }
        // 青釭劍發動：攻擊者有青釭劍且目標有防具時，發出效果事件
        if (isAttackerHasBlackPommel(behaviorPlayer) && isEquipmentHasSpecialEffect(targetPlayer)) {
            events.add(new BlackPommelEffectEvent(behaviorPlayer.getId(), targetPlayer.getId()));
        }
        emitAskDodgeAfterTieQi(events, targetPlayer);
    }

    /** 以指定判定牌結算鐵騎並接續問閃流程（鬼才 resume 亦由此進入）。 */
    public List<DomainEvent> resumeTieQiJudgement(HandCard judgement, Player targetPlayer) {
        List<DomainEvent> events = new ArrayList<>();
        boolean[] tieQiSuccess = new boolean[1];
        events.addAll(SkillEngine.tieQiResolvedEvents(game, behaviorPlayer, targetPlayer, judgement, tieQiSuccess));
        // 記下鐵騎結果（經 BehaviorData 持久化）：雌雄雙股劍跨請求結算完後要依此決定
        putParam(PARAM_TIEQI_SUCCESS, String.valueOf(tieQiSuccess[0]));
        afterTieQi(events, targetPlayer, tieQiSuccess[0]);
        return events;
    }

    private void emitAskDodgeAfterTieQi(List<DomainEvent> events, Player targetPlayer) {
        Optional<List<DomainEvent>> intercepted = SkillEngine.beforeAskDodge(game, targetPlayer, this);
        if (intercepted.isPresent()) {
            events.addAll(intercepted.get());
        } else {
            events.add(new AskDodgeEvent(targetPlayer.getId()));
        }
    }

    private boolean shouldTriggerYinYangSwords(Player attacker, Player target) {
        return attacker.getEquipmentWeaponCard() instanceof YinYangSwordsCard
                && attacker.getGender() != target.getGender();
    }

    @Override
    public List<DomainEvent> doResponseToPlayerAction(String playerId, String targetPlayerId, String cardId, String playType) {
        Player damagedPlayer = game.getPlayer(playerId);
        int originalHp = damagedPlayer.getHP();

        if (isSkip(playType)) {
            Round currentRound = game.getCurrentRound();
            // 麒麟弓要先發動效果，待麒麟弓效果發動後再扣血
            if (isAskPlayerUseQilinBow(behaviorPlayer, damagedPlayer)) {
                isOneRound = false;
                currentRound.setActivePlayer(behaviorPlayer);
                currentRound.setStage(Stage.Wait_Equipment_Effect);
                EquipmentCard equipmentCard = behaviorPlayer.getEquipmentWeaponCard();
                AskPlayEquipmentEffectEvent askPlayEquipmentEffectEvent = new AskPlayEquipmentEffectEvent(behaviorPlayer.getId(), equipmentCard, List.of(playerId));
                PlayCardEvent playCardEvent = new PlayCardEvent("不出牌", playerId, targetPlayerId, cardId, playType);
                return List.of(playCardEvent, askPlayEquipmentEffectEvent, game.getGameStatusEvent("出牌"));
            }

            List<DomainEvent> events = game.getDamagedEvent(playerId, behaviorPlayer.getId(), cardId, card, playType, originalHp, damagedPlayer, currentRound, Optional.of(this));
            String message = game.getGamePhase().getPhaseName().equals("GeneralDying") ? "扣血已瀕臨死亡" : "扣血但還活著";
            events.add(game.getGameStatusEvent(message));
            isOneRound = true;
            return events;
        } else if (isDodgeCard(cardId)) {
            // 用 helper 確保閃進墓地（與 ArrowBarrage / HDH / Duel 等行為一致；
            // 既有 bare playCard(cardId) 寫法只移出手牌、不進墓地，導致取墓地牌的技能行為分歧）
            playerPlayCardNotUpdateActivePlayer(damagedPlayer, cardId);
            return handleDodgeChain(playerId, playerId, cardId, playType);
        } else if (isQilinBowSuccess(playType)) {
            Round currentRound = game.getCurrentRound();
            List<DomainEvent> events = game.getDamagedEvent(playerId, behaviorPlayer.getId(), cardId, card, playType, originalHp, damagedPlayer, currentRound, Optional.of(this));
            //playerDyingEvent
            String message = game.getGamePhase().getPhaseName().equals("GeneralDying") ? "扣血已瀕臨死亡" : "扣血但還活著";
            events.add(game.getGameStatusEvent(message));
            isOneRound = true;
            return events;
        } else {
            // 非閃且非可轉化牌（傾國/龍膽已在 Game.playerPlayCard 攔截）— 明確報錯，
            // 不再默默回空 events 造成前端卡住（issue #229）
            throw new IllegalArgumentException(
                    "被詢問出閃時只能出閃、skip 或發動轉化技（傾國/龍膽）：" + cardId);
        }
    }

    @Override
    public List<DomainEvent> acceptDodgeFromHuJia(String dodgedPlayerId, String weiPlayerId, String dodgeCardId) {
        // 注意：dodge cardId 已在 WaitingHuJiaResponseBehavior 中由 Wei 棄入墓地
        return handleDodgeChain(dodgedPlayerId, weiPlayerId, dodgeCardId, PlayType.ACTIVE.getPlayType());
    }

    /**
     * 共用的 dodge 後處理鏈：emit PlayCardEvent + 處理 GDCB / SPA 鏈、否則結束 behavior。
     * <p>
     * 由兩條路徑共享：
     * <ul>
     *   <li>正常 dodge response（{@code doResponseToPlayerAction} 內，dodgedPlayerId == cardSourcePlayerId）— 出閃者即被詢問者</li>
     *   <li>護駕代閃（{@code acceptDodgeFromHuJia}，cardSourcePlayerId 為 Wei 武將）— 出閃者非被詢問者</li>
     * </ul>
     * 抽出此共用 helper 是為了避免兩條路徑在 GDCB/SPA 鏈邏輯上分歧。
     *
     * @param dodgedPlayerId 被詢問出閃的玩家 ID（曹操在 HuJia 路徑下）
     * @param cardSourcePlayerId 實際打出閃的玩家 ID（HuJia 路徑下為 Wei 武將；正常路徑下與 dodgedPlayerId 相同）
     * @param dodgeCardId 打出的閃 cardId
     * @param playType {@link PlayType#ACTIVE} 字串值
     */
    private List<DomainEvent> handleDodgeChain(String dodgedPlayerId, String cardSourcePlayerId,
                                                String dodgeCardId, String playType) {
        Round currentRound = game.getCurrentRound();
        PlayCardEvent playCardEvent = new PlayCardEvent(
                "出牌", cardSourcePlayerId, behaviorPlayer.getId(), dodgeCardId, playType);

        // 青龍偃月刀效果：攻擊者裝備青龍偃月刀時，可再出一張殺
        if (behaviorPlayer.getEquipmentWeaponCard() instanceof GreenDragonCrescentBladeCard) {
            isOneRound = false;
            currentRound.setActivePlayer(behaviorPlayer);
            // 使用 this.cardId/this.card（殺的），不是 parameter dodgeCardId（閃的）
            game.updateTopBehavior(new WaitingGreenDragonCrescentBladeResponseBehavior(
                    game, behaviorPlayer, List.of(dodgedPlayerId),
                    behaviorPlayer, this.cardId, PlayType.ACTIVE.getPlayType(), this.card));
            return List.of(playCardEvent,
                    new AskGreenDragonCrescentBladeEffectEvent(behaviorPlayer.getId(), dodgedPlayerId),
                    game.getGameStatusEvent("出牌"));
        }

        // 貫石斧效果：攻擊者裝備貫石斧且可棄牌 ≥2 張時，可棄兩牌強制命中
        if (behaviorPlayer.getEquipmentWeaponCard() instanceof StonePiercingAxeCard
                && getDiscardableCardCount(behaviorPlayer) >= 2) {
            isOneRound = false;
            currentRound.setActivePlayer(behaviorPlayer);
            // 使用 this.cardId/this.card（殺的），不是 parameter dodgeCardId（閃的）
            game.updateTopBehavior(new WaitingStonePiercingAxeResponseBehavior(
                    game, behaviorPlayer, List.of(dodgedPlayerId),
                    behaviorPlayer, this.cardId, PlayType.ACTIVE.getPlayType(), this.card));
            return List.of(playCardEvent,
                    new AskStonePiercingAxeEffectEvent(behaviorPlayer.getId(), dodgedPlayerId),
                    game.getGameStatusEvent("出牌"));
        }

        currentRound.setActivePlayer(currentRound.getCurrentRoundPlayer());
        isOneRound = true;
        return List.of(playCardEvent, game.getGameStatusEvent("出牌"));
    }

    /**
     * 流離：把這張殺的目標改為 newTarget，並對新目標重新走「防具詢問或問閃」流程
     * （新目標的護駕/鐵騎/流離等 hook 照常套用 — 流離可被連鎖轉移）。
     */
    public List<DomainEvent> redirectTo(Player newTarget) {
        reactionPlayers.clear();
        reactionPlayers.add(newTarget.getId());
        // 流離轉移來的目標不觸發雌雄雙股劍（維持原本行為）；持久化供鬼才暫停後 resume 判斷
        putParam(PARAM_LIULI_REDIRECTED, "true");
        currentReactionPlayer = newTarget;
        game.getCurrentRound().setActivePlayer(newTarget);

        List<DomainEvent> events = new ArrayList<>();
        // FAQ：大喬先決定是否發動流離，之後馬超再決定是否發動鐵騎 → 對新目標先判鐵騎再問防具／閃
        emitTieQiThenArmorOrDodge(events, newTarget);
        return events;
    }

    private boolean isQilinBowSuccess(String playType) {
        return  PlayType.SYSTEM_INTERNAL.getPlayType().equals(playType);
    }

    private boolean isAskPlayerUseQilinBow(Player attackPlayer, Player damagedPlayer) {
        return attackPlayer.getEquipmentWeaponCard() instanceof QilinBowCard && damagedPlayer.hasMountsCard();
    }

    static public boolean isEquipmentHasSpecialEffect(Player targetPlayer) {
        return targetPlayer.getEquipment().hasSpecialEffect();
    }

    private boolean isAttackerHasBlackPommel(Player attackPlayer) {
        return attackPlayer.getEquipmentWeaponCard() instanceof BlackPommelCard;
    }

    private int getDiscardableCardCount(Player player) {
        return player.getHand().getCards().size() + player.getEquipment().getAllEquipmentCards().size();
    }

}
