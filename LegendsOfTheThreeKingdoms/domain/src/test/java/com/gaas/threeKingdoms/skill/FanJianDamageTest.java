package com.gaas.threeKingdoms.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.behavior.DyingAskPeachBehavior;
import com.gaas.threeKingdoms.events.AskPeachEvent;
import com.gaas.threeKingdoms.events.AskSkillEffectEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.PlayerDyingEvent;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.player.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 反間猜錯造成的傷害：來源是周瑜，要進瀕死流程、觸發受傷後技能、擊殺獎勵／懲罰歸周瑜。
 * 原本只 target.damage(1)：HP 歸 0 也不問桃、反饋剛烈不觸發。
 * createGame：player-a 主公、player-b 忠臣、player-c 反賊、player-d 內奸。
 */
public class FanJianDamageTest extends PassiveSkillTestBase {

    private static final String SKIP = PlayType.SKIP.getPlayType();

    /** 周瑜（主公）把黑桃8 殺給 targetId，目標猜紅心 → 花色不符受 1 點傷害。 */
    private List<DomainEvent> fanJianWrongGuess(Game game, String targetId) {
        game.getPlayer("player-a").getHand().addCardToHand(List.of(new Kill(BS8008)));
        game.playerUseSkillEffect("player-a", "反間", "ACCEPT", List.of(BS8008.getCardId()), targetId);
        return game.playerUseSkillEffect(targetId, "反間", "HEART", null, null);
    }

    @DisplayName("目標 1 血猜錯 → 進入瀕死求桃（原本 HP 歸 0 卻不問桃）")
    @Test
    public void wrongGuessAtOneHpEntersDying() {
        Game game = createGame(General.周瑜, General.甘寧, General.甘寧, General.甘寧);
        game.getPlayer("player-b").getBloodCard().setHp(1);

        List<DomainEvent> events = fanJianWrongGuess(game, "player-b");

        assertEquals(0, game.getPlayer("player-b").getHP());
        assertTrue(events.stream().anyMatch(e -> e instanceof PlayerDyingEvent));
        assertTrue(events.stream().anyMatch(e -> e instanceof AskPeachEvent));
        assertInstanceOf(DyingAskPeachBehavior.class, game.peekTopBehavior());
        assertEquals("player-b", game.getCurrentRound().getActivePlayer().getId());
    }

    @DisplayName("瀕死被桃救回 → 回 1 血，回到周瑜的出牌階段")
    @Test
    public void rescuedTargetGivesTurnBackToZhouYu() {
        Game game = createGame(General.周瑜, General.甘寧, General.甘寧, General.甘寧);
        game.getPlayer("player-b").getBloodCard().setHp(1);
        game.getPlayer("player-d").getHand().addCardToHand(List.of(new Peach(BH3029)));
        fanJianWrongGuess(game, "player-b");

        game.playerPlayCard("player-b", "", "player-b", SKIP);
        game.playerPlayCard("player-c", "", "player-b", SKIP);
        game.playerPlayCard("player-d", BH3029.getCardId(), "player-b", PlayType.ACTIVE.getPlayType());

        assertEquals(1, game.getPlayer("player-b").getHP());
        assertTrue(game.getGamePhase() instanceof Normal);
        assertTrue(game.isTopBehaviorEmpty());
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
    }

    @DisplayName("反間打死反賊 → 擊殺者是周瑜，摸 3 張獎勵")
    @Test
    public void killingRebelRewardsZhouYu() {
        Game game = createGame(General.周瑜, General.甘寧, General.甘寧, General.甘寧);
        Player zhouYu = game.getPlayer("player-a");
        game.getPlayer("player-c").getBloodCard().setHp(1);
        fanJianWrongGuess(game, "player-c");
        int handBefore = zhouYu.getHandSize();   // 黑桃8 已給出

        for (String p : List.of("player-c", "player-d", "player-a", "player-b")) {
            game.playerPlayCard(p, "", "player-c", SKIP);
        }

        assertTrue(game.getPlayer("player-c").isAlreadyDeath());
        assertEquals(handBefore + 3, zhouYu.getHandSize(), "反間的傷害來源是周瑜 → 擊殺反賊摸 3");
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
    }

    @DisplayName("主公周瑜用反間打死忠臣 → 周瑜棄光所有牌")
    @Test
    public void monarchKillingMinisterIsPenalized() {
        Game game = createGame(General.周瑜, General.甘寧, General.甘寧, General.甘寧);
        Player zhouYu = game.getPlayer("player-a");
        zhouYu.getHand().addCardToHand(List.of(new Dodge(BH2028), new Peach(BH4030)));
        game.getPlayer("player-b").getBloodCard().setHp(1);
        fanJianWrongGuess(game, "player-b");

        for (String p : List.of("player-b", "player-c", "player-d", "player-a")) {
            game.playerPlayCard(p, "", "player-b", SKIP);
        }

        assertTrue(game.getPlayer("player-b").isAlreadyDeath());
        assertEquals(0, zhouYu.getHandSize(), "主公殺忠臣：棄光所有牌");
    }

    @DisplayName("目標是司馬懿（2 血以上）猜錯 → 觸發反饋，可以拿周瑜的牌")
    @Test
    public void wrongGuessTriggersFanKuiAgainstZhouYu() {
        Game game = createGame(General.周瑜, General.司馬懿, General.甘寧, General.甘寧);
        game.getPlayer("player-a").getHand().addCardToHand(List.of(new Dodge(BH2028)));   // 周瑜還有牌可被拿
        int hpBefore = game.getPlayer("player-b").getHP();

        List<DomainEvent> events = fanJianWrongGuess(game, "player-b");

        assertEquals(hpBefore - 1, game.getPlayer("player-b").getHP());
        AskSkillEffectEvent ask = events.stream().filter(e -> e instanceof AskSkillEffectEvent)
                .map(e -> (AskSkillEffectEvent) e).filter(e -> "反饋".equals(e.getSkillName()))
                .findFirst().orElseThrow(() -> new AssertionError("應詢問司馬懿是否發動反饋，實際：" + events));
        assertEquals("player-b", ask.getPlayerId());
        assertEquals("player-a", ask.getDataPlayerId(), "反饋的來源是周瑜");
    }
}
