package com.gaas.threeKingdoms.skill.wei;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.behavior.WaitingSkillEffectBehavior;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.SkillEffectEvent;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.round.Stage;
import com.gaas.threeKingdoms.skill.registry.SkillEngine;
import com.gaas.threeKingdoms.skill.trigger.ChoiceResolvableSkill;

import java.util.ArrayList;
import java.util.List;

/**
 * 甄姬 (WEI007) 洛神 — 回合開始階段可判定：黑色收為手牌並繼續判定，紅色結束（issue #171）。
 *
 * issue #227 改主動觸發：回合開始判定階段先詢問（AskSkillEffectEvent），
 * ACCEPT 後進入判定 loop（黑收自動續判、紅停，不逐輪詢問）；SKIP 直接進原判定/摸牌流程。
 * 詢問推送在 {@link SkillEngine#luoShenAskActivation}；loop 在 {@link SkillEngine#luoShenContinueLoop}
 * （含鬼才暫停點，鬼才 resume 後由 GuiCaiSkill.resumeLuoShen 接手收尾）。
 */
public class LuoShenSkill implements ChoiceResolvableSkill {

    public static final String GENERAL_ID = General.甄姬.getGeneralId();
    public static final String SKILL_NAME = "洛神";

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
        Player owner = waiting.getBehaviorPlayer();
        List<DomainEvent> events = new ArrayList<>();
        // 先 pop 本 waiting：後續 loop 可能 push 鬼才 waiting，收尾的 continueJudgementAndDraw
        // 也需要空 stack 才會推進到摸牌
        waiting.setIsOneRound(true);
        game.removeCompletedBehaviors();

        if ("ACCEPT".equals(choice)) {
            events.addAll(SkillEngine.luoShenContinueLoop(game, owner));
            if (!game.isTopBehaviorEmpty()) {
                return events; // 鬼才介入 → 暫停，後續由 GuiCaiSkill.resumeLuoShen 收尾
            }
        } else if ("SKIP".equals(choice)) {
            events.add(new SkillEffectEvent(SKILL_NAME, owner.getId(), false, List.of(), null));
            events.add(game.getGameStatusEvent("放棄洛神"));
        } else {
            throw new IllegalArgumentException("Invalid LuoShen choice: " + choice);
        }

        game.getCurrentRound().setStage(Stage.Normal);
        game.getCurrentRound().setActivePlayer(owner);
        events.addAll(game.continueJudgementAndDraw(owner, false));
        return events;
    }
}
