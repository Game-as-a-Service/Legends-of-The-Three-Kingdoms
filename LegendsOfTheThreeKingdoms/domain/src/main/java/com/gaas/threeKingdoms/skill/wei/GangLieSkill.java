package com.gaas.threeKingdoms.skill.wei;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.behavior.JianXiongCompatibleTopBehavior;
import com.gaas.threeKingdoms.behavior.behavior.WaitingSkillEffectBehavior;
import com.gaas.threeKingdoms.events.AskSkillEffectEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.SkillEffectEvent;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.Suit;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.skill.context.DamageContext;
import com.gaas.threeKingdoms.skill.registry.SkillEngine;
import com.gaas.threeKingdoms.skill.trigger.ChoiceResolvableSkill;
import com.gaas.threeKingdoms.skill.trigger.OnDamagedSkill;

import java.util.ArrayList;
import java.util.List;

/**
 * 夏侯惇 (WEI003) 剛烈 — 受到傷害後可判定：非紅桃 → 傷害來源選擇「棄兩張手牌」或「受 1 點傷害」（issue #165）。
 *
 * 兩階段：
 *   1. 夏侯惇 ACCEPT → 立即判定；紅桃 → 結束；非紅桃 → push 第二個 WaitingSkillEffect 問來源
 *   2. 來源 choice "DISCARD" + cardIds(2 張手牌) 或 "DAMAGE"（受 1 傷）
 *
 * AOE polling（南蠻/萬箭）中亦觸發（mirror 反饋 #221）：詢問鏈（含 ASK_SOURCE 第二段、
 * 鬼才巢狀）全部收斂後由 WaitingSkillEffectBehavior 收鏈掃描 resume 輪詢。
 * 來源手牌 < 2 時只能受傷；反傷不進瀕死流程（同反間，v1 慣例）。
 * 第二段的詢問事件帶 options = [DISCARD, DAMAGE]，來源的回應只分「有挑兩張手牌（棄牌）」
 * 與「其他（受傷）」兩種，不會因為前端送了 ACCEPT/SKIP 就把遊戲鎖住（使用者回報）。
 */
public class GangLieSkill implements OnDamagedSkill, ChoiceResolvableSkill {

    public static final String GENERAL_ID = General.夏侯惇.getGeneralId();
    public static final String SKILL_NAME = "剛烈";
    public static final String PARAM_SOURCE_ID = "GANGLIE_SOURCE_ID";
    public static final String PARAM_STAGE = "GANGLIE_STAGE"; // ASK_XIAHOU | ASK_SOURCE
    public static final String PARAM_XIAHOU_ID = "GANGLIE_XIAHOU_ID";
    /** ASK_SOURCE 階段的合法回應（詢問事件的 options，前端照這個畫按鈕）。 */
    public static final String CHOICE_DISCARD = "DISCARD";
    public static final String CHOICE_DAMAGE = "DAMAGE";

    @Override
    public String getGeneralId() {
        return GENERAL_ID;
    }

    @Override
    public String getSkillName() {
        return SKILL_NAME;
    }

    @Override
    public List<DomainEvent> onDamaged(Game game, DamageContext ctx) {
        Player damaged = ctx.damagedPlayer();
        Player source = ctx.sourcePlayer();
        if (source == null || source.equals(damaged)) {
            return List.of();
        }
        if (!damaged.isHPGreaterThanZero()) {
            return List.of();
        }
        Behavior top = game.isTopBehaviorEmpty() ? null : game.peekTopBehavior();
        if (top != null && !(top instanceof JianXiongCompatibleTopBehavior)) {
            return List.of();
        }
        // polling caller（南蠻/萬箭）需保留底層 behavior，詢問鏈收斂後 resume 輪詢
        // （mirror 反饋 #221 樣板；兩段式收鏈由 WaitingSkillEffectBehavior 掃描處理）
        boolean isPollingCaller = top instanceof JianXiongCompatibleTopBehavior compatible
                && compatible.isPollingCaller();
        if (!isPollingCaller) {
            game.removeCompletedBehaviors();
        }
        WaitingSkillEffectBehavior waiting = new WaitingSkillEffectBehavior(game, damaged, SKILL_NAME);
        waiting.putParam(PARAM_SOURCE_ID, source.getId());
        waiting.putParam(PARAM_STAGE, "ASK_XIAHOU");
        if (isPollingCaller) {
            waiting.putParam(WaitingSkillEffectBehavior.PARAM_RESUME_POLLING, "true");
        }
        game.updateTopBehavior(waiting);
        game.getCurrentRound().setActivePlayer(damaged);

        return List.of(new AskSkillEffectEvent(SKILL_NAME, damaged.getId(), List.of(), source.getId()));
    }

    @Override
    public List<DomainEvent> resolveChoice(Game game, WaitingSkillEffectBehavior waiting,
                                           String choice, List<String> cardIds, String targetPlayerId) {
        String stage = (String) waiting.getParam(PARAM_STAGE);
        if ("ASK_SOURCE".equals(stage)) {
            return resolveSourceChoice(game, waiting, choice, cardIds);
        }
        return resolveXiaHouChoice(game, waiting, choice);
    }

    private List<DomainEvent> resolveXiaHouChoice(Game game, WaitingSkillEffectBehavior waiting, String choice) {
        Player xiaHou = waiting.getBehaviorPlayer();
        String sourceId = (String) waiting.getParam(PARAM_SOURCE_ID);
        List<DomainEvent> events = new ArrayList<>();

        if ("SKIP".equals(choice)) {
            game.getCurrentRound().setActivePlayer(game.getCurrentRound().getCurrentRoundPlayer());
            events.add(new SkillEffectEvent(SKILL_NAME, xiaHou.getId(), false, List.of(), sourceId));
            events.add(game.getGameStatusEvent("放棄剛烈"));
            return events;
        }
        if (!"ACCEPT".equals(choice)) {
            throw new IllegalArgumentException("Invalid GangLie choice: " + choice);
        }

        // 判定（判定牌抽出後過鬼才暫停點；司馬懿介入時暫停，resume 走 resolveJudgementOutcome）
        HandCard judgement = game.drawCardForCardEffect(1).get(0);
        java.util.Optional<List<DomainEvent>> paused = GuiCaiSkill.tryPause(
                game, xiaHou, judgement, GuiCaiSkill.TYPE_GANG_LIE, "剛烈",
                java.util.Map.of(GuiCaiSkill.PARAM_GANGLIE_SOURCE_ID, sourceId));
        if (paused.isPresent()) {
            events.addAll(paused.get());
            return events;
        }
        events.addAll(resolveJudgementOutcome(game, xiaHou, sourceId, judgement));
        return events;
    }

    /** 以指定判定牌結算剛烈（鬼才 resume 亦由此進入）：非紅桃 → 問來源棄兩張或受 1 傷。 */
    public static List<DomainEvent> resolveJudgementOutcome(Game game, Player xiaHou, String sourceId,
                                                            HandCard judgement) {
        List<DomainEvent> events = new ArrayList<>();
        boolean success = judgement.getSuit() != Suit.HEART;
        // 帶判定牌描述（使用者回報：只看到「判定生效／未生效」，看不出是哪張牌造成的，
        // 同 issue #235 起的慣例，見八卦陣／鐵騎／洛神）
        String judgementDesc = judgement.judgementDescription();
        String resultMessage = success
                ? String.format("剛烈判定：%s → 非紅桃，生效", judgementDesc)
                : String.format("剛烈判定：%s → 紅桃，未生效", judgementDesc);
        events.add(new SkillEffectEvent(SKILL_NAME, xiaHou.getId(), success,
                List.of(judgement.getId()), sourceId, resultMessage));
        events.addAll(SkillEngine.afterJudgement(game, xiaHou, judgement));

        if (!success) {
            game.getCurrentRound().setActivePlayer(game.getCurrentRound().getCurrentRoundPlayer());
            events.add(game.getGameStatusEvent(resultMessage));
            return events;
        }

        // 判定生效 → 問來源：棄兩張手牌或受 1 傷
        Player source = game.getPlayer(sourceId);
        WaitingSkillEffectBehavior askSource = new WaitingSkillEffectBehavior(game, source, SKILL_NAME);
        askSource.putParam(PARAM_STAGE, "ASK_SOURCE");
        askSource.putParam(PARAM_SOURCE_ID, sourceId);
        askSource.putParam(PARAM_XIAHOU_ID, xiaHou.getId());
        game.updateTopBehavior(askSource);
        game.getCurrentRound().setActivePlayer(source);

        // 這一段問的是「棄兩張或受傷」，不是「要不要發動剛烈」：options 與訊息都要講清楚，
        // 否則前端只畫得出發動／放棄，兩個都不是合法答案（使用者回報卡局）
        String question = String.format("剛烈判定生效：請 %s 選擇棄兩張手牌或受 1 點傷害", sourceId);
        events.add(new AskSkillEffectEvent(SKILL_NAME, sourceId, List.of(), xiaHou.getId(),
                List.of(CHOICE_DISCARD, CHOICE_DAMAGE), question));
        events.add(game.getGameStatusEvent(question));
        return events;
    }

    /** 來源受剛烈 1 點傷害的事件。 */
    private static List<DomainEvent> sourceDamageEvents(Game game, Player source, String xiaHouId) {
        int originalHp = source.getHP();
        source.damage(1);
        String damageMessage = source.getId() + " 受剛烈 1 點傷害（" + originalHp + "→" + source.getHP() + "）";
        // v1：剛烈反傷不進瀕死流程整合（HP 仍會歸零，但 dying ask 流程為 follow-up）
        return List.of(
                new SkillEffectEvent(SKILL_NAME, source.getId(), true, List.of(), xiaHouId, damageMessage),
                game.getGameStatusEvent(damageMessage));
    }

    private List<DomainEvent> resolveSourceChoice(Game game, WaitingSkillEffectBehavior waiting,
                                                  String choice, List<String> cardIds) {
        Player source = waiting.getBehaviorPlayer();
        String xiaHouId = (String) waiting.getParam(PARAM_XIAHOU_ID);
        List<DomainEvent> events = new ArrayList<>();
        game.getCurrentRound().setActivePlayer(game.getCurrentRound().getCurrentRoundPlayer());

        // 剛烈是強制二選一，來源不能「不選」。舊版只認 DISCARD／DAMAGE，其他 choice 一律
        // 丟 IllegalArgumentException —— 但前端對這個詢問只畫得出通用的發動／放棄，兩者都被
        // 拒絕，玩家怎麼按都回不了合法答案，遊戲就停在這裡（使用者回報卡局）。
        // 因此：明確要棄牌（或已挑好兩張手牌）→ 棄牌；其餘任何回應 → 受 1 點傷害。
        boolean wantsDiscard = CHOICE_DISCARD.equals(choice) || (cardIds != null && cardIds.size() == 2);
        if (wantsDiscard) {
            if (cardIds == null || cardIds.size() != 2) {
                throw new IllegalArgumentException("DISCARD requires exactly 2 hand cards");
            }
            for (String cardId : cardIds) {
                HandCard discarded = source.playCard(cardId);
                game.getGraveyard().add(discarded);
            }
            events.add(new SkillEffectEvent(SKILL_NAME, source.getId(), true, cardIds, xiaHouId,
                    source.getId() + " 棄兩張手牌回應剛烈"));
            events.add(game.getGameStatusEvent(source.getId() + " 棄兩張手牌回應剛烈"));
        } else {
            events.addAll(sourceDamageEvents(game, source, xiaHouId));
        }
        return events;
    }
}
