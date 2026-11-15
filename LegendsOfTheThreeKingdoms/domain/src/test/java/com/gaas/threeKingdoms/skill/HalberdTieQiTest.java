package com.gaas.threeKingdoms.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.behavior.behavior.DyingAskPeachBehavior;
import com.gaas.threeKingdoms.behavior.behavior.HeavenlyDoubleHalberdKillBehavior;
import com.gaas.threeKingdoms.behavior.behavior.WaitingSkillEffectBehavior;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.HeavenlyDoubleHalberdCard;
import com.gaas.threeKingdoms.player.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Stack;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 方天畫戟的殺也要判鐵騎（原本整段跳過）：每個目標各判一次，紅色 → 該目標不能出閃也不能發動八卦陣，直接扣血。
 * 另外鎖住「前一位目標死亡後接回輪詢」時，接回流程立刻造成的新瀕死／新詢問不會被舊瀕死蓋掉。
 * 座位 a → b → c → d；a 為主公、b 忠、c 反、d 內。
 */
public class HalberdTieQiTest extends PassiveSkillTestBase {

    private static final HandCard RED = new Peach(BH4030);
    private static final HandCard BLACK = new Kill(BS9009);

    private Game givenHalberd(General attacker, General b, General c, General d) {
        Game game = createGame(attacker, b, c, d);
        Player a = game.getPlayer("player-a");
        a.getEquipment().setWeapon(new HeavenlyDoubleHalberdCard(EDQ103));
        a.getHand().addCardToHand(List.of(new Kill(BS8008)));
        return game;
    }

    private static List<SkillEffectEvent> tieQi(List<DomainEvent> events) {
        return events.stream().filter(e -> e instanceof SkillEffectEvent se && "鐵騎".equals(se.getSkillName()))
                .map(e -> (SkillEffectEvent) e).toList();
    }

    private static boolean askedDodge(List<DomainEvent> events, String playerId) {
        return events.stream().anyMatch(e -> e instanceof AskDodgeEvent a && playerId.equals(a.getPlayerId()));
    }

    /** 瀕死求桃：所有人依序不出桃，直到這次瀕死結束。 */
    private static List<DomainEvent> everyoneSkipsPeach(Game game, String dyingPlayerId) {
        List<DomainEvent> events = List.of();
        while (!game.isTopBehaviorEmpty() && game.peekTopBehavior() instanceof DyingAskPeachBehavior
                && game.getCurrentRound().getDyingPlayer() != null
                && dyingPlayerId.equals(game.getCurrentRound().getDyingPlayer().getId())) {
            String asked = game.getCurrentRound().getActivePlayer().getId();
            events = game.playerPlayCard(asked, "", dyingPlayerId, "skip");
        }
        return events;
    }

    @DisplayName("馬超方天畫戟殺 [b, c]：b 判紅 → 直接扣血不問閃；接著 c 判黑 → 照常問 c 出閃")
    @Test
    public void tieQiJudgedForEachTarget() {
        Game game = givenHalberd(General.馬超, General.甘寧, General.甘寧, General.甘寧);
        game.getDeck().putBackOnTop(List.of(RED, BLACK));

        List<DomainEvent> events = game.playerUseHeavenlyDoubleHalberdKill("player-a", BS8008.getCardId(),
                List.of("player-b", "player-c"));

        List<SkillEffectEvent> judged = tieQi(events);
        assertEquals(2, judged.size(), "兩個目標各判一次鐵騎：" + events);
        assertEquals("player-b", judged.get(0).getDataPlayerId());
        assertTrue(judged.get(0).isAccepted());
        assertEquals("player-c", judged.get(1).getDataPlayerId());
        assertFalse(judged.get(1).isAccepted());
        assertFalse(askedDodge(events, "player-b"), "紅色 → b 不能出閃");
        assertEquals(3, game.getPlayer("player-b").getHP());
        assertTrue(askedDodge(events, "player-c"));
        assertEquals("player-c", game.getCurrentRound().getActivePlayer().getId());

        game.playerPlayCard("player-c", "", "player-a", "skip");
        assertEquals(3, game.getPlayer("player-c").getHP());
        assertTrue(game.isTopBehaviorEmpty());
    }

    @DisplayName("鐵騎在輪到該目標時才判：b 判黑問閃 → b 出閃後才判 c")
    @Test
    public void tieQiJudgedWhenTargetsTurnComes() {
        Game game = givenHalberd(General.馬超, General.甘寧, General.甘寧, General.甘寧);
        game.getPlayer("player-b").getHand().addCardToHand(List.of(new Dodge(BH2028)));
        game.getDeck().putBackOnTop(List.of(BLACK, RED));

        List<DomainEvent> events = game.playerUseHeavenlyDoubleHalberdKill("player-a", BS8008.getCardId(),
                List.of("player-b", "player-c"));
        assertEquals(1, tieQi(events).size());
        assertTrue(askedDodge(events, "player-b"));

        events = game.playerPlayCard("player-b", BH2028.getCardId(), "player-a", "active");
        assertEquals(1, tieQi(events).size(), "b 結算完才判 c 的鐵騎");
        assertTrue(tieQi(events).get(0).isAccepted());
        assertEquals(4, game.getPlayer("player-b").getHP());
        assertEquals(3, game.getPlayer("player-c").getHP(), "紅色 → c 直接扣血");
        assertTrue(game.isTopBehaviorEmpty());
    }

    @DisplayName("c 被鐵騎打到瀕死死亡 → 接回輪詢時 d 也被鐵騎打到瀕死：新瀕死正常進行、桃救回後殺結束")
    @Test
    public void tieQiDamageWhileResumingFromDeath() {
        Game game = givenHalberd(General.馬超, General.甘寧, General.甘寧, General.甘寧);
        game.getPlayer("player-c").damage(3);
        game.getPlayer("player-d").damage(3);
        // c 的判定 → c 死亡時反賊獎勵摸 3 → d 的判定
        game.getDeck().putBackOnTop(List.of(RED, BLACK, BLACK, BLACK, RED));

        game.playerUseHeavenlyDoubleHalberdKill("player-a", BS8008.getCardId(), List.of("player-c", "player-d"));
        assertInstanceOf(DyingAskPeachBehavior.class, game.peekTopBehavior(), "c 瀕死");

        everyoneSkipsPeach(game, "player-c");
        assertTrue(game.getPlayer("player-c").isAlreadyDeath());

        assertEquals(0, game.getPlayer("player-d").getHP(), "接回後 d 判紅直接扣血");
        assertEquals("GeneralDying", game.getGamePhase().getPhaseName(), "d 的瀕死不能被蓋回 Normal");
        Stack<Behavior> stack = game.getTopBehavior();
        assertEquals(2, stack.size(), "舊瀕死已移出，只剩 halberd + d 的瀕死：" + stack);
        assertInstanceOf(HeavenlyDoubleHalberdKillBehavior.class, stack.get(0));
        assertInstanceOf(DyingAskPeachBehavior.class, stack.get(1));
        assertEquals("player-d", game.getCurrentRound().getDyingPlayer().getId());

        // 被問桃的人出桃救 d
        Player asked = game.getCurrentRound().getActivePlayer();
        asked.getHand().addCardToHand(List.of(new Peach(BH3029)));
        game.playerPlayCard(asked.getId(), BH3029.getCardId(), "player-d", "active");
        assertEquals(1, game.getPlayer("player-d").getHP());
        assertTrue(game.isTopBehaviorEmpty(), "d 是最後一位 → 殺結束：" + game.getTopBehavior());
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
    }

    @DisplayName("c 死亡後接回輪詢、輪到大喬問流離 → activePlayer 是大喬（原本被重設回攻擊者）")
    @Test
    public void liuLiAskAfterPreviousTargetDiesKeepsDaQiaoActive() {
        Game game = givenHalberd(General.甘寧, General.甘寧, General.甘寧, General.大喬);
        game.getPlayer("player-c").damage(3);
        game.getPlayer("player-d").getHand().addCardToHand(List.of(new Peach(BH3029), new Dodge(BH2028)));

        game.playerUseHeavenlyDoubleHalberdKill("player-a", BS8008.getCardId(), List.of("player-c", "player-d"));
        game.playerPlayCard("player-c", "", "player-a", "skip");
        everyoneSkipsPeach(game, "player-c");
        assertTrue(game.getPlayer("player-c").isAlreadyDeath());

        WaitingSkillEffectBehavior liuLi = assertInstanceOf(WaitingSkillEffectBehavior.class, game.peekTopBehavior());
        assertEquals("流離", liuLi.getSkillName());
        assertEquals("player-d", game.getCurrentRound().getActivePlayer().getId());

        // c 死後 d 的鄰座變成 a（攻擊者）與 b → 轉給 b
        List<DomainEvent> events = game.playerUseSkillEffect("player-d", "流離", "ACCEPT",
                List.of(BH3029.getCardId()), "player-b");
        assertTrue(askedDodge(events, "player-b"), events.toString());
    }
}
