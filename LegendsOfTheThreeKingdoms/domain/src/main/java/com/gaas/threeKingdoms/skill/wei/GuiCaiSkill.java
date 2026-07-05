package com.gaas.threeKingdoms.skill.wei;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.behavior.behavior.LightningJudgementBehavior;
import com.gaas.threeKingdoms.behavior.behavior.WaitingSkillEffectBehavior;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.SkillEffectEvent;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayCard;
import com.gaas.threeKingdoms.handcard.scrollcard.ScrollCard;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.round.Stage;
import com.gaas.threeKingdoms.skill.trigger.ChoiceResolvableSkill;

import java.util.ArrayList;
import java.util.List;

/**
 * 司馬懿 (WEI002) 鬼才 — 任意角色的判定牌生效前，可打出一張手牌代替之（issue #164）。
 *
 * v1 範圍：閃電判定路徑（含 Ward 路徑 LightningJudgementBehavior）。
 * 樂不思蜀 / 八卦陣 / 洛神 / 鐵騎 / 剛烈判定的替換為 follow-up。
 *
 * 觸發：Game.handleLightningJudgement 抽出判定牌後，若場上有存活且有手牌的司馬懿
 * → push WaitingSkillEffect(鬼才) 暫停結算（原判定牌已在墓地）。
 * ACCEPT（cardIds[0] = 司馬懿手牌）→ 該牌成為判定牌（進墓地），原判定牌留墓地；
 * SKIP → 原判定牌生效。之後 resume：結算閃電 → 恢復判定/摸牌流程。
 */
public class GuiCaiSkill implements ChoiceResolvableSkill {

    public static final String GENERAL_ID = General.司馬懿.getGeneralId();
    public static final String SKILL_NAME = "鬼才";
    public static final String PARAM_LIGHTNING_CARD_ID = "GUICAI_LIGHTNING_CARD_ID";
    public static final String PARAM_OWNER_ID = "GUICAI_OWNER_ID";
    public static final String PARAM_DRAWN_CARD_ID = "GUICAI_DRAWN_CARD_ID";

    @Override
    public String getGeneralId() {
        return GENERAL_ID;
    }

    @Override
    public String getSkillName() {
        return SKILL_NAME;
    }

    @Override
    public List<DomainEvent> resolveChoice(Game game, WaitingSkillEffectBehavior waiting,
                                           String choice, List<String> cardIds, String targetPlayerId) {
        Player simaYi = waiting.getBehaviorPlayer();
        ScrollCard lightning = (ScrollCard) PlayCard.findById((String) waiting.getParam(PARAM_LIGHTNING_CARD_ID));
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

        // pop waiting；若下層是 Ward 路徑的 LightningJudgementBehavior 一併標記完成
        waiting.setIsOneRound(true);
        game.removeCompletedBehaviors();
        Behavior top = game.isTopBehaviorEmpty() ? null : game.peekTopBehavior();
        if (top instanceof LightningJudgementBehavior ljb) {
            ljb.setIsOneRound(true);
            game.removeCompletedBehaviors();
        }

        // 以（可能被替換的）判定牌結算閃電，然後恢復判定/摸牌流程
        events.addAll(game.resolveLightningJudgement(lightning, owner, judgementCard));
        game.getCurrentRound().setStage(Stage.Normal);
        game.getCurrentRound().setActivePlayer(owner);
        events.addAll(game.continueJudgementAndDraw(owner, false));
        return events;
    }
}
