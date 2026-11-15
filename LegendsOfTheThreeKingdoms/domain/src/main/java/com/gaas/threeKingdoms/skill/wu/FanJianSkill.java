package com.gaas.threeKingdoms.skill.wu;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.behavior.WaitingSkillEffectBehavior;
import com.gaas.threeKingdoms.events.AskSkillEffectEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.SkillEffectEvent;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.Suit;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.skill.trigger.ActivePhaseSkill;
import com.gaas.threeKingdoms.skill.trigger.ChoiceResolvableSkill;

import java.util.ArrayList;
import java.util.List;

/**
 * 周瑜 (WU005) 反間 — 出牌階段（每回合 1 次）：指定一名其他角色選一花色後，
 * 將你一張手牌給他；若所選花色不符則他受 1 點傷害（issue #189）。
 *
 * 兩段式：
 *   1. 周瑜 activate：cardIds[0] = 要給的手牌、targetPlayerId = 目標 →
 *      push WaitingSkillEffect(反間) 問目標選花色（牌先暗扣在 params）
 *   2. 目標 choice = SPADE / HEART / DIAMOND / CLUB → 揭示並轉移牌；花色不符 → 目標受 1 傷
 *
 * v1：目標受傷不進瀕死整合（HP 仍會歸零，dying flow follow-up）。
 */
public class FanJianSkill implements ActivePhaseSkill, ChoiceResolvableSkill {

    public static final String GENERAL_ID = General.周瑜.getGeneralId();
    public static final String SKILL_NAME = "反間";
    public static final String PARAM_CARD_ID = "FANJIAN_CARD_ID";
    public static final String PARAM_ZHOUYU_ID = "FANJIAN_ZHOUYU_ID";
    /** 目標可回的 choice：四種花色 */
    public static final List<String> SUIT_OPTIONS = List.of("SPADE", "HEART", "DIAMOND", "CLUB");

    @Override
    public String getGeneralId() {
        return GENERAL_ID;
    }

    @Override
    public String getSkillName() {
        return SKILL_NAME;
    }

    @Override
    public List<DomainEvent> activate(Game game, Player self, String choice,
                                      List<String> cardIds, String targetPlayerId) {
        if (game.getCurrentRound().getUsedOncePerTurnSkills().contains(SKILL_NAME)) {
            throw new IllegalStateException("反間 每回合限發動一次");
        }
        if (cardIds == null || cardIds.size() != 1) {
            throw new IllegalArgumentException("反間 requires exactly 1 hand card");
        }
        if (targetPlayerId == null || targetPlayerId.equals(self.getId())) {
            throw new IllegalArgumentException("反間 requires another player as target");
        }
        String cardId = cardIds.get(0);
        if (self.getHand().getCard(cardId).isEmpty()) {
            throw new IllegalArgumentException("Card not in hand: " + cardId);
        }
        Player target = game.getPlayer(targetPlayerId);
        game.getCurrentRound().getUsedOncePerTurnSkills().add(SKILL_NAME);

        WaitingSkillEffectBehavior waiting = new WaitingSkillEffectBehavior(game, target, SKILL_NAME);
        waiting.putParam(PARAM_CARD_ID, cardId);
        waiting.putParam(PARAM_ZHOUYU_ID, self.getId());
        game.updateTopBehavior(waiting);
        game.getCurrentRound().setActivePlayer(target);

        // 目標要回的是花色，不是「是否發動」：options 必須帶四種花色（同剛烈的教訓，否則照 options 畫按鈕的前端回不了合法答案）
        String askMessage = String.format("反間：%s 對 %s 發動反間，請 %s 選擇一種花色（猜錯受到 1 點傷害）",
                self.getGeneralName(), target.getGeneralName(), target.getGeneralName());
        return List.of(new AskSkillEffectEvent(SKILL_NAME, targetPlayerId, List.of(), self.getId(),
                        SUIT_OPTIONS, askMessage),
                game.getGameStatusEvent(askMessage));
    }

    @Override
    public List<DomainEvent> resolveChoice(Game game, WaitingSkillEffectBehavior waiting,
                                           String choice, List<String> cardIds, String targetPlayerId) {
        Suit guessedSuit;
        try {
            guessedSuit = Suit.valueOf(choice);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("反間 choice must be SPADE/HEART/DIAMOND/CLUB, got: " + choice);
        }
        Player target = waiting.getBehaviorPlayer();
        String zhouYuId = (String) waiting.getParam(PARAM_ZHOUYU_ID);
        String cardId = (String) waiting.getParam(PARAM_CARD_ID);
        Player zhouYu = game.getPlayer(zhouYuId);

        // 揭示並轉移牌
        HandCard given = zhouYu.playCard(cardId);
        target.getHand().addCardToHand(given);

        game.getCurrentRound().setActivePlayer(game.getCurrentRound().getCurrentRoundPlayer());

        List<DomainEvent> events = new ArrayList<>();
        boolean suitMatches = given.getSuit() == guessedSuit;
        // 前端 log 只顯示事件的 message：結果要寫在 SkillEffectEvent 上（原本只有預設的「反間：X 發動」）
        String resultMessage = String.format("反間：%s 選擇%s，獲得 %s → %s",
                target.getGeneralName(), guessedSuit.getDisplayName(), given.judgementDescription(),
                suitMatches ? "猜中花色，不受傷" : "花色不符，受到 1 點傷害");
        events.add(new SkillEffectEvent(SKILL_NAME, target.getId(), !suitMatches,
                List.of(cardId), zhouYuId, resultMessage));
        if (!suitMatches) {
            target.damage(1);
        }
        events.add(game.getGameStatusEvent(resultMessage));
        return events;
    }
}
