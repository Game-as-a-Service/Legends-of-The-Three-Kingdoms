package com.gaas.threeKingdoms.skill.wei;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.SkillEffectEvent;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.skill.trigger.AfterJudgementSkill;

import java.util.List;
import java.util.Optional;

/**
 * 郭嘉 (WEI006) 天妒 — 自己的判定牌生效後，可收為手牌（issue #168）。
 * v1 自動收取（嚴格有利，不詢問）；判定牌從墓地移入手牌。
 */
public class TianDuSkill implements AfterJudgementSkill {

    public static final String GENERAL_ID = General.郭嘉.getGeneralId();
    public static final String SKILL_NAME = "天妒";

    @Override
    public String getGeneralId() {
        return GENERAL_ID;
    }

    @Override
    public String getSkillName() {
        return SKILL_NAME;
    }

    @Override
    public List<DomainEvent> afterJudgement(Game game, Player judgementOwner, HandCard judgementCard) {
        Optional<HandCard> taken = game.getGraveyard().removeCard(judgementCard.getId());
        if (taken.isEmpty()) {
            return List.of(); // 判定牌已被其他效果取走
        }
        judgementOwner.getHand().addCardToHand(taken.get());
        return List.of(new SkillEffectEvent(SKILL_NAME, judgementOwner.getId(), true,
                List.of(judgementCard.getId()), null));
    }
}
