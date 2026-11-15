package com.gaas.threeKingdoms.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.behavior.behavior.DyingAskPeachBehavior;
import com.gaas.threeKingdoms.behavior.behavior.HeavenlyDoubleHalberdKillBehavior;
import com.gaas.threeKingdoms.behavior.behavior.WaitingJianXiongResponseBehavior;
import com.gaas.threeKingdoms.behavior.behavior.WaitingSkillEffectBehavior;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.HeavenlyDoubleHalberdCard;
import com.gaas.threeKingdoms.player.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 方天畫戟中途目標受傷觸發受傷後技能（反饋／奸雄）：要等技能結算完才輪到下一位目標
 * （mirror 南蠻／萬箭的 polling caller 機制）。原本受傷後技能詢問時會把方天畫戟從 stack pop 掉，
 * 下一位目標的問閃沒有 behavior 承接。
 * 座位 a → b → c → d；a 為攻擊者（裝備方天畫戟，反饋可拿裝備）。
 */
public class HalberdDamagedSkillTest extends PassiveSkillTestBase {

    private Game givenHalberd(General b) {
        Game game = createGame(General.甘寧, b, General.甘寧, General.甘寧);
        Player a = game.getPlayer("player-a");
        a.getEquipment().setWeapon(new HeavenlyDoubleHalberdCard(EDQ103));
        a.getHand().addCardToHand(List.of(new Kill(BS8008)));
        return game;
    }

    private static boolean askedDodge(List<DomainEvent> events, String playerId) {
        return events.stream().anyMatch(e -> e instanceof AskDodgeEvent a && playerId.equals(a.getPlayerId()));
    }

    private static boolean halberdInStack(Game game) {
        return game.getTopBehavior().stream().anyMatch(b -> b instanceof HeavenlyDoubleHalberdKillBehavior);
    }

    @DisplayName("方天畫戟 [司馬懿, c]：司馬懿不出閃受傷 → 先問反饋、還不問 c；反饋結算後才問 c")
    @Test
    public void fanKuiResolvesBeforeNextTarget() {
        Game game = givenHalberd(General.司馬懿);
        game.playerUseHeavenlyDoubleHalberdKill("player-a", BS8008.getCardId(), List.of("player-b", "player-c"));

        List<DomainEvent> events = game.playerPlayCard("player-b", "", "player-a", "skip");
        assertEquals(3, game.getPlayer("player-b").getHP());
        WaitingSkillEffectBehavior fanKui = assertInstanceOf(WaitingSkillEffectBehavior.class, game.peekTopBehavior());
        assertEquals("反饋", fanKui.getSkillName());
        assertFalse(askedDodge(events, "player-c"), "反饋還沒結算，不能先問 c：" + events);
        assertTrue(halberdInStack(game), "方天畫戟不能被 pop 掉：" + game.getTopBehavior());
        assertEquals("player-b", game.getCurrentRound().getActivePlayer().getId());

        events = game.playerUseSkillEffect("player-b", "反饋", "SKIP", null, null);
        assertTrue(askedDodge(events, "player-c"), "反饋結算後輪到 c：" + events);
        assertEquals("player-c", game.getCurrentRound().getActivePlayer().getId());

        game.playerPlayCard("player-c", "", "player-a", "skip");
        assertEquals(3, game.getPlayer("player-c").getHP());
        assertTrue(game.isTopBehaviorEmpty(), game.getTopBehavior().toString());
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
    }

    @DisplayName("方天畫戟 [司馬懿, c]：反饋拿走攻擊者的方天畫戟 → 這張殺照樣結算完 c")
    @Test
    public void fanKuiTakesHalberdStillFinishesKill() {
        Game game = givenHalberd(General.司馬懿);
        game.playerUseHeavenlyDoubleHalberdKill("player-a", BS8008.getCardId(), List.of("player-b", "player-c"));
        game.playerPlayCard("player-b", "", "player-a", "skip");

        List<DomainEvent> events = game.playerUseSkillEffect("player-b", "反饋", "ACCEPT",
                List.of(EDQ103.getCardId()), null);
        assertTrue(askedDodge(events, "player-c"), events.toString());
        game.playerPlayCard("player-c", "", "player-a", "skip");
        assertEquals(3, game.getPlayer("player-c").getHP());
        assertTrue(game.isTopBehaviorEmpty());
    }

    @DisplayName("方天畫戟 [曹操, c]：曹操受傷 → 先問奸雄、還不問 c；奸雄結算後才問 c")
    @Test
    public void jianXiongResolvesBeforeNextTarget() {
        Game game = givenHalberd(General.曹操);
        game.playerUseHeavenlyDoubleHalberdKill("player-a", BS8008.getCardId(), List.of("player-b", "player-c"));

        List<DomainEvent> events = game.playerPlayCard("player-b", "", "player-a", "skip");
        assertInstanceOf(WaitingJianXiongResponseBehavior.class, game.peekTopBehavior());
        assertFalse(askedDodge(events, "player-c"), "奸雄還沒結算，不能先問 c：" + events);
        assertTrue(halberdInStack(game));

        events = game.playerUseJianXiongEffect("player-b", AskJianXiongEffectEvent.Choice.ACCEPT);
        assertTrue(askedDodge(events, "player-c"), "奸雄結算後輪到 c：" + events);
        assertTrue(game.getPlayer("player-b").getHand().getCards().stream()
                .anyMatch(c -> c.getId().equals(BS8008.getCardId())), "曹操拿到殺");

        game.playerPlayCard("player-c", "", "player-a", "skip");
        assertEquals(3, game.getPlayer("player-c").getHP());
        assertTrue(game.isTopBehaviorEmpty());
    }

    @DisplayName("方天畫戟 [司馬懿(1血), c]：瀕死被救回 → 補問反饋 → 結算後才問 c")
    @Test
    public void fanKuiAfterRevivalResolvesBeforeNextTarget() {
        Game game = givenHalberd(General.司馬懿);
        game.getPlayer("player-b").damage(3); // 4 血 → 1 血
        game.playerUseHeavenlyDoubleHalberdKill("player-a", BS8008.getCardId(), List.of("player-b", "player-c"));

        game.playerPlayCard("player-b", "", "player-a", "skip");
        assertInstanceOf(DyingAskPeachBehavior.class, game.peekTopBehavior());
        Player asked = game.getCurrentRound().getActivePlayer();
        asked.getHand().addCardToHand(List.of(new Peach(BH3029)));

        List<DomainEvent> events = game.playerPlayCard(asked.getId(), BH3029.getCardId(), "player-b", "active");
        assertEquals(1, game.getPlayer("player-b").getHP());
        WaitingSkillEffectBehavior fanKui = assertInstanceOf(WaitingSkillEffectBehavior.class, game.peekTopBehavior());
        assertEquals("反饋", fanKui.getSkillName());
        assertFalse(askedDodge(events, "player-c"), "反饋還沒結算，不能先問 c：" + events);

        events = game.playerUseSkillEffect("player-b", "反饋", "SKIP", null, null);
        assertTrue(askedDodge(events, "player-c"), "只推進一位（c），不能跳過：" + events);
        game.playerPlayCard("player-c", "", "player-a", "skip");
        assertEquals(3, game.getPlayer("player-c").getHP());
        assertTrue(game.isTopBehaviorEmpty());
    }

    @DisplayName("方天畫戟 [c, 司馬懿]：最後一位受傷問反饋 → 結算後殺結束、回到攻擊者")
    @Test
    public void fanKuiOnLastTargetEndsKill() {
        Game game = createGame(General.甘寧, General.甘寧, General.司馬懿, General.甘寧);
        Player a = game.getPlayer("player-a");
        a.getEquipment().setWeapon(new HeavenlyDoubleHalberdCard(EDQ103));
        a.getHand().addCardToHand(List.of(new Kill(BS8008)));
        game.playerUseHeavenlyDoubleHalberdKill("player-a", BS8008.getCardId(), List.of("player-b", "player-c"));
        game.playerPlayCard("player-b", "", "player-a", "skip");
        game.playerPlayCard("player-c", "", "player-a", "skip");
        assertInstanceOf(WaitingSkillEffectBehavior.class, game.peekTopBehavior());

        game.playerUseSkillEffect("player-c", "反饋", "SKIP", null, null);
        assertTrue(game.isTopBehaviorEmpty(), game.getTopBehavior().toString());
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
    }

    @SuppressWarnings("unused")
    private static List<Behavior> stack(Game game) {
        return List.copyOf(game.getTopBehavior());
    }
}
