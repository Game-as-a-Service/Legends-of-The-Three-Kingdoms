package com.gaas.threeKingdoms.skill.wei;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.behavior.behavior.ContentmentJudgementBehavior;
import com.gaas.threeKingdoms.behavior.behavior.LightningJudgementBehavior;
import com.gaas.threeKingdoms.behavior.behavior.NormalActiveKillBehavior;
import com.gaas.threeKingdoms.behavior.behavior.WaitingSkillEffectBehavior;
import com.gaas.threeKingdoms.effect.EightDiagramTacticEquipmentEffectHandler;
import com.gaas.threeKingdoms.events.AskSkillEffectEvent;
import com.gaas.threeKingdoms.events.ContentmentEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.SkillEffectEvent;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayCard;
import com.gaas.threeKingdoms.handcard.scrollcard.ScrollCard;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.round.Stage;
import com.gaas.threeKingdoms.skill.registry.SkillEngine;
import com.gaas.threeKingdoms.skill.trigger.ChoiceResolvableSkill;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 司馬懿 (WEI002) 鬼才 — 任意角色的判定牌生效前，可打出一張手牌代替之（issue #164）。
 *
 * 覆蓋全部判定路徑（依 wiki 標準版敘述「當一名角色的判定牌生效前」）：
 * 閃電 / 樂不思蜀（含 Ward 路徑）/ 八卦陣 / 鐵騎 / 剛烈 / 洛神。
 * 判定型別以 {@link #PARAM_JUDGEMENT_TYPE} 存於 behavior params，resolve 時 dispatch
 * 到對應的 resume 路徑。同一次判定僅詢問一次（官方 FAQ）。
 *
 * 觸發：各判定點抽出判定牌後，若場上有存活且有手牌的司馬懿
 * → {@link #tryPause} push WaitingSkillEffect(鬼才) 暫停結算（原判定牌已在墓地）。
 * ACCEPT（cardIds[0] = 司馬懿手牌）→ 該牌成為判定牌（進墓地），原判定牌留墓地；
 * SKIP → 原判定牌生效。之後依判定型別 resume 對應流程。
 */
public class GuiCaiSkill implements ChoiceResolvableSkill {

    public static final String GENERAL_ID = General.司馬懿.getGeneralId();
    public static final String SKILL_NAME = "鬼才";
    public static final String PARAM_JUDGEMENT_TYPE = "GUICAI_JUDGEMENT_TYPE";
    public static final String PARAM_LIGHTNING_CARD_ID = "GUICAI_LIGHTNING_CARD_ID";
    public static final String PARAM_OWNER_ID = "GUICAI_OWNER_ID";
    public static final String PARAM_DRAWN_CARD_ID = "GUICAI_DRAWN_CARD_ID";
    public static final String PARAM_TIEQI_TARGET_ID = "GUICAI_TIEQI_TARGET_ID";
    public static final String PARAM_GANGLIE_SOURCE_ID = "GUICAI_GANGLIE_SOURCE_ID";

    public static final String TYPE_LIGHTNING = "LIGHTNING";
    public static final String TYPE_CONTENTMENT = "CONTENTMENT";
    public static final String TYPE_EIGHT_DIAGRAM = "EIGHT_DIAGRAM";
    public static final String TYPE_TIE_QI = "TIE_QI";
    public static final String TYPE_GANG_LIE = "GANG_LIE";
    public static final String TYPE_LUO_SHEN = "LUO_SHEN";

    @Override
    public String getGeneralId() {
        return GENERAL_ID;
    }

    @Override
    public String getSkillName() {
        return SKILL_NAME;
    }

    /**
     * 判定暫停點共用入口：抽出判定牌後呼叫。場上有存活且有手牌的司馬懿
     * → push WaitingSkillEffect(鬼才) 並回傳詢問事件（caller 應直接 return，不結算）；
     * 否則回 empty，caller 以 drawnCard 同步結算。
     *
     * @param judgementTypeName 判定名稱（訊息用，如「閃電」「樂不思蜀」）
     */
    public static Optional<List<DomainEvent>> tryPause(Game game, Player owner, HandCard drawnCard,
                                                       String type, String judgementTypeName,
                                                       Map<String, String> extraParams) {
        Player guiCaiHolder = game.getPlayers().stream()
                .filter(p -> !p.isAlreadyDeath()
                        && GENERAL_ID.equals(p.getGeneralCard().getGeneralId())
                        && p.getHandSize() > 0)
                .findFirst().orElse(null);
        if (guiCaiHolder == null) {
            return Optional.empty();
        }
        WaitingSkillEffectBehavior waiting = new WaitingSkillEffectBehavior(game, guiCaiHolder, SKILL_NAME);
        waiting.putParam(PARAM_JUDGEMENT_TYPE, type);
        waiting.putParam(PARAM_OWNER_ID, owner.getId());
        waiting.putParam(PARAM_DRAWN_CARD_ID, drawnCard.getId());
        extraParams.forEach(waiting::putParam);
        game.updateTopBehavior(waiting);
        game.getCurrentRound().setActivePlayer(guiCaiHolder);
        return Optional.of(List.of(
                new AskSkillEffectEvent(SKILL_NAME, guiCaiHolder.getId(),
                        List.of(drawnCard.getId()), owner.getId()),
                game.getGameStatusEvent("鬼才：" + guiCaiHolder.getId() + " 可替換 "
                        + owner.getId() + " 的" + judgementTypeName + "判定牌")));
    }

    @Override
    public List<DomainEvent> resolveChoice(Game game, WaitingSkillEffectBehavior waiting,
                                           String choice, List<String> cardIds, String targetPlayerId) {
        Player simaYi = waiting.getBehaviorPlayer();
        String type = (String) waiting.getParam(PARAM_JUDGEMENT_TYPE);
        if (type == null) {
            type = TYPE_LIGHTNING;
        }
        Player owner = game.getPlayer((String) waiting.getParam(PARAM_OWNER_ID));
        HandCard drawn = PlayCard.findById((String) waiting.getParam(PARAM_DRAWN_CARD_ID));

        List<DomainEvent> events = new ArrayList<>();
        HandCard judgementCard;
        if ("ACCEPT".equals(choice)) {
            if (cardIds == null || cardIds.size() != 1) {
                throw new IllegalArgumentException("鬼才 ACCEPT requires exactly 1 hand card");
            }
            String replacementId = cardIds.get(0);
            if (simaYi.getHand().getCard(replacementId).isEmpty()) {
                throw new IllegalArgumentException("Card not in hand: " + replacementId);
            }
            judgementCard = simaYi.playCard(replacementId);
            game.getGraveyard().add(judgementCard);
            events.add(new SkillEffectEvent(SKILL_NAME, simaYi.getId(), true,
                    List.of(replacementId), owner.getId()));
            events.add(game.getGameStatusEvent(String.format(
                    "%s 發動鬼才，以 %s 替換判定牌", simaYi.getId(), replacementId)));
        } else if ("SKIP".equals(choice)) {
            judgementCard = drawn;
            events.add(new SkillEffectEvent(SKILL_NAME, simaYi.getId(), false, List.of(), owner.getId()));
        } else {
            throw new IllegalArgumentException("Invalid GuiCai choice: " + choice);
        }

        switch (type) {
            case TYPE_LIGHTNING -> events.addAll(resumeLightning(game, waiting, owner, judgementCard));
            case TYPE_CONTENTMENT -> events.addAll(resumeContentment(game, waiting, owner, judgementCard));
            case TYPE_EIGHT_DIAGRAM -> events.addAll(resumeEightDiagram(game, waiting, owner, judgementCard));
            case TYPE_TIE_QI -> events.addAll(resumeTieQi(game, waiting, owner, judgementCard));
            case TYPE_GANG_LIE -> events.addAll(resumeGangLie(game, waiting, owner, judgementCard));
            case TYPE_LUO_SHEN -> events.addAll(resumeLuoShen(game, waiting, owner, judgementCard));
            default -> throw new IllegalStateException("Unknown GuiCai judgement type: " + type);
        }
        return events;
    }

    /** pop 鬼才 waiting；若下層是 Ward 路徑的判定 behavior（閃電/樂不思蜀）一併標記完成。 */
    private void popWaitingAndWardJudgementBehavior(Game game, WaitingSkillEffectBehavior waiting) {
        waiting.setIsOneRound(true);
        game.removeCompletedBehaviors();
        Behavior top = game.isTopBehaviorEmpty() ? null : game.peekTopBehavior();
        if (top instanceof LightningJudgementBehavior || top instanceof ContentmentJudgementBehavior) {
            top.setIsOneRound(true);
            game.removeCompletedBehaviors();
        }
    }

    private List<DomainEvent> resumeLightning(Game game, WaitingSkillEffectBehavior waiting,
                                              Player owner, HandCard judgementCard) {
        ScrollCard lightning = (ScrollCard) PlayCard.findById((String) waiting.getParam(PARAM_LIGHTNING_CARD_ID));
        popWaitingAndWardJudgementBehavior(game, waiting);

        List<DomainEvent> events = new ArrayList<>(
                game.resolveLightningJudgement(lightning, owner, judgementCard));
        game.getCurrentRound().setStage(Stage.Normal);
        game.getCurrentRound().setActivePlayer(owner);
        events.addAll(game.continueJudgementAndDraw(owner, false));
        return events;
    }

    private List<DomainEvent> resumeContentment(Game game, WaitingSkillEffectBehavior waiting,
                                                Player owner, HandCard judgementCard) {
        popWaitingAndWardJudgementBehavior(game, waiting);

        List<DomainEvent> events = new ArrayList<>(
                game.resolveContentmentJudgement(owner, judgementCard));
        boolean contentmentSuccess = events.stream()
                .filter(e -> e instanceof ContentmentEvent)
                .map(ContentmentEvent.class::cast)
                .anyMatch(ContentmentEvent::isSuccess);
        game.getCurrentRound().setStage(Stage.Normal);
        game.getCurrentRound().setActivePlayer(owner);
        events.addAll(game.continueJudgementAndDraw(owner, contentmentSuccess));
        return events;
    }

    private List<DomainEvent> resumeEightDiagram(Game game, WaitingSkillEffectBehavior waiting,
                                                 Player owner, HandCard judgementCard) {
        // 先 pop waiting，讓八卦陣結算的 fan-out（萬箭推進/方天/青龍/貫石斧）看得到底層 behavior
        waiting.setIsOneRound(true);
        game.removeCompletedBehaviors();
        game.getCurrentRound().setActivePlayer(owner);
        return new EightDiagramTacticEquipmentEffectHandler(null, game)
                .resolveJudgementOutcome(owner.getId(), judgementCard);
    }

    private List<DomainEvent> resumeTieQi(Game game, WaitingSkillEffectBehavior waiting,
                                          Player owner, HandCard judgementCard) {
        Player target = game.getPlayer((String) waiting.getParam(PARAM_TIEQI_TARGET_ID));
        waiting.setIsOneRound(true);
        game.removeCompletedBehaviors();
        Behavior top = game.isTopBehaviorEmpty() ? null : game.peekTopBehavior();
        if (!(top instanceof NormalActiveKillBehavior killBehavior)) {
            throw new IllegalStateException("鬼才(鐵騎) resume expects NormalActiveKillBehavior on top");
        }
        // 還原暫停前狀態（updateRoundInformation 設 activePlayer = 殺目標）；damage 路徑會自行重設
        game.getCurrentRound().setActivePlayer(target);
        return killBehavior.resumeTieQiJudgement(judgementCard, target);
    }

    private List<DomainEvent> resumeGangLie(Game game, WaitingSkillEffectBehavior waiting,
                                            Player owner, HandCard judgementCard) {
        String sourceId = (String) waiting.getParam(PARAM_GANGLIE_SOURCE_ID);
        // pop 鬼才 waiting + 底下已完成的剛烈 ASK_XIAHOU waiting
        waiting.setIsOneRound(true);
        game.removeCompletedBehaviors();
        return GangLieSkill.resolveJudgementOutcome(game, owner, sourceId, judgementCard);
    }

    private List<DomainEvent> resumeLuoShen(Game game, WaitingSkillEffectBehavior waiting,
                                            Player owner, HandCard judgementCard) {
        waiting.setIsOneRound(true);
        game.removeCompletedBehaviors();

        List<DomainEvent> events = new ArrayList<>();
        boolean black = SkillEngine.luoShenResolveOne(game, owner, judgementCard, events);
        if (black) {
            // 黑色收牌 → 續判（下一張可能再次觸發鬼才詢問）
            events.addAll(SkillEngine.luoShenContinueLoop(game, owner));
            if (!game.isTopBehaviorEmpty()) {
                return events;
            }
        }
        game.getCurrentRound().setStage(Stage.Normal);
        game.getCurrentRound().setActivePlayer(owner);
        events.addAll(game.continueJudgementAndDraw(owner, false));
        return events;
    }
}
