package com.gaas.threeKingdoms.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.behavior.BorrowedSwordBehavior;
import com.gaas.threeKingdoms.behavior.behavior.WaitingJiJiangResponseBehavior;
import com.gaas.threeKingdoms.events.AskDodgeEvent;
import com.gaas.threeKingdoms.events.AskSkillEffectEvent;
import com.gaas.threeKingdoms.events.BorrowedSwordEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.PlayCardEvent;
import com.gaas.threeKingdoms.events.WeaponUsurpationEvent;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.RepeatingCrossbowCard;
import com.gaas.threeKingdoms.handcard.scrollcard.BorrowedSword;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.round.Round;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 激將（劉備主公技）主動出殺 / 借刀殺人代出殺。
 * 座位 a → b → c → d；預設攻擊範圍 1：a 打得到 b、d，打不到 c。
 */
public class JiJiangActiveKillTest extends PassiveSkillTestBase {

    /** a = 劉備（主公、回合玩家），b = 關羽（蜀），c / d = 甘寧。 */
    private Game liuBeiTurnGame() {
        return createGame(General.劉備, General.關羽, General.甘寧, General.甘寧);
    }

    private static AskSkillEffectEvent findAsk(List<DomainEvent> events) {
        return events.stream().filter(e -> e instanceof AskSkillEffectEvent)
                .map(e -> (AskSkillEffectEvent) e).findFirst().orElseThrow();
    }

    // ===== 主動出殺 =====

    @DisplayName("劉備出牌階段發動激將 → 關羽交殺 → 視為劉備對 d 出殺，d 被問閃、不閃扣血")
    @Test
    public void activeJiJiangShuSuppliesKill() {
        Game game = liuBeiTurnGame();
        Player b = game.getPlayer("player-b");
        b.getHand().addCardToHand(new Kill(BS8008));

        List<DomainEvent> events = game.playerUseSkillEffect("player-a", "激將", "ACCEPT", null, "player-d");
        AskSkillEffectEvent ask = findAsk(events);
        assertEquals("激將", ask.getSkillName());
        assertEquals("player-b", ask.getPlayerId());
        assertEquals("player-a", ask.getDataPlayerId());
        assertInstanceOf(WaitingJiJiangResponseBehavior.class, game.peekTopBehavior());
        assertFalse(game.getCurrentRound().isShowKill(), "還沒有人出殺，不計次數");

        List<DomainEvent> acceptEvents = game.playerUseSkillEffect("player-b", "激將", "ACCEPT",
                List.of(BS8008.getCardId()), null);

        PlayCardEvent play = acceptEvents.stream().filter(e -> e instanceof PlayCardEvent)
                .map(e -> (PlayCardEvent) e).findFirst().orElseThrow();
        assertEquals("player-a", play.getPlayerId(), "殺視為劉備使用");
        assertEquals("player-d", play.getTargetPlayerId());
        assertEquals(BS8008.getCardId(), play.getCardId());
        assertTrue(acceptEvents.stream().anyMatch(e -> e instanceof AskDodgeEvent
                && ((AskDodgeEvent) e).getPlayerId().equals("player-d")));
        assertEquals(0, b.getHandSize(), "關羽的殺被交出");
        assertEquals(0, game.getPlayer("player-a").getHandSize(), "劉備手牌不變");
        assertTrue(game.getGraveyard().contains(BS8008.getCardId()));
        assertTrue(game.getCurrentRound().isShowKill(), "計入劉備本回合的殺");

        game.playerPlayCard("player-d", "", "player-a", PlayType.SKIP.getPlayType());
        assertEquals(3, game.getPlayer("player-d").getHP());
        assertTrue(game.isTopBehaviorEmpty());
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId(), "回到劉備出牌");
    }

    @DisplayName("激將代出的殺計入次數 → 劉備再出自己的殺被拒")
    @Test
    public void activeJiJiangCountsAsLiuBeisKill() {
        Game game = liuBeiTurnGame();
        game.getPlayer("player-b").getHand().addCardToHand(new Kill(BS8008));
        game.getPlayer("player-a").getHand().addCardToHand(new Kill(BS9009));

        game.playerUseSkillEffect("player-a", "激將", "ACCEPT", null, "player-d");
        game.playerUseSkillEffect("player-b", "激將", "ACCEPT", List.of(BS8008.getCardId()), null);
        game.playerPlayCard("player-d", "", "player-a", PlayType.SKIP.getPlayType());

        assertThrows(IllegalStateException.class, () ->
                game.playerPlayCard("player-a", BS9009.getCardId(), "player-b", PlayType.ACTIVE.getPlayType()));
        assertThrows(IllegalStateException.class, () ->
                game.playerUseSkillEffect("player-a", "激將", "ACCEPT", null, "player-b"));
    }

    @DisplayName("劉備本回合已出過殺 → 不能再發動激將出殺")
    @Test
    public void activeJiJiangRejectedWhenKillAlreadyUsed() {
        Game game = liuBeiTurnGame();
        game.getPlayer("player-a").getHand().addCardToHand(new Kill(BS9009));
        game.playerPlayCard("player-a", BS9009.getCardId(), "player-d", PlayType.ACTIVE.getPlayType());
        game.playerPlayCard("player-d", "", "player-a", PlayType.SKIP.getPlayType());

        assertThrows(IllegalStateException.class, () ->
                game.playerUseSkillEffect("player-a", "激將", "ACCEPT", null, "player-b"));
        assertTrue(game.isTopBehaviorEmpty());
    }

    @DisplayName("目標距離以劉備計算：c 不在劉備攻擊範圍 → 拒絕發動")
    @Test
    public void activeJiJiangUsesLiuBeisAttackRange() {
        Game game = liuBeiTurnGame();
        game.getPlayer("player-b").getHand().addCardToHand(new Kill(BS8008));

        // c 與 b 相鄰（關羽打得到），但與劉備距離 2
        assertThrows(IllegalStateException.class, () ->
                game.playerUseSkillEffect("player-a", "激將", "ACCEPT", null, "player-c"));
        assertTrue(game.isTopBehaviorEmpty());
    }

    @DisplayName("劉備非主公 / 沒有其他蜀將 → 不能發動激將")
    @Test
    public void activeJiJiangRequiresMonarchAndShuHelper() {
        Game noShu = createGame(General.劉備, General.甘寧, General.甘寧, General.甘寧);
        assertThrows(IllegalStateException.class, () ->
                noShu.playerUseSkillEffect("player-a", "激將", "ACCEPT", null, "player-b"));

        Game notMonarch = new Game();
        notMonarch.initDeck();
        Player a = buildPlayer("player-a", General.劉備, Role.MINISTER);
        Player b = buildPlayer("player-b", General.關羽, Role.MONARCH);
        Player c = buildPlayer("player-c", General.甘寧, Role.REBEL);
        Player d = buildPlayer("player-d", General.甘寧, Role.TRAITOR);
        notMonarch.setPlayers(Arrays.asList(a, b, c, d));
        notMonarch.setCurrentRound(new Round(a));
        notMonarch.enterPhase(new Normal(notMonarch));
        assertThrows(IllegalStateException.class, () ->
                notMonarch.playerUseSkillEffect("player-a", "激將", "ACCEPT", null, "player-d"));
    }

    @DisplayName("激將全部拒絕（含 SKIP）→ 不計次數、回到劉備出牌，劉備仍可出自己的殺")
    @Test
    public void activeJiJiangAllDecline() {
        Game game = createGame(General.劉備, General.關羽, General.甘寧, General.張飛);
        game.getPlayer("player-a").getHand().addCardToHand(new Kill(BS9009));

        List<DomainEvent> e1 = game.playerUseSkillEffect("player-a", "激將", "ACCEPT", null, "player-d");
        assertEquals("player-b", findAsk(e1).getPlayerId());
        List<DomainEvent> e2 = game.playerUseSkillEffect("player-b", "激將", "DECLINE", null, null);
        assertEquals("player-d", findAsk(e2).getPlayerId(), "下一位蜀將張飛");
        game.playerUseSkillEffect("player-d", "激將", "SKIP", null, null);

        assertTrue(game.isTopBehaviorEmpty());
        assertFalse(game.getCurrentRound().isShowKill());
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());

        List<DomainEvent> own = game.playerPlayCard("player-a", BS9009.getCardId(), "player-d", PlayType.ACTIVE.getPlayType());
        assertTrue(own.stream().anyMatch(e -> e instanceof AskDodgeEvent));
    }

    @DisplayName("激將目標即代出者：諸葛亮交出最後一張殺後雖空城，目標已於發動時指定 → 殺照常結算")
    @Test
    public void activeJiJiangTargetIsHelperWithKongCheng() {
        Game game = createGame(General.劉備, General.諸葛亮, General.甘寧, General.甘寧);
        Player b = game.getPlayer("player-b");
        b.getHand().addCardToHand(new Kill(BS8008));
        int hpBefore = b.getHP();

        game.playerUseSkillEffect("player-a", "激將", "ACCEPT", null, "player-b");
        List<DomainEvent> acceptEvents = game.playerUseSkillEffect("player-b", "激將", "ACCEPT",
                List.of(BS8008.getCardId()), null);

        assertTrue(acceptEvents.stream().anyMatch(e -> e instanceof AskDodgeEvent
                && ((AskDodgeEvent) e).getPlayerId().equals("player-b")));
        assertEquals(0, game.getPlayer("player-a").getHandSize());
        assertTrue(game.getCurrentRound().isShowKill());
        game.playerPlayCard("player-b", "", "player-a", PlayType.SKIP.getPlayType());
        assertEquals(hpBefore - 1, b.getHP());
    }

    // ===== 借刀殺人 =====

    /** a = 甘寧（回合玩家，持借刀殺人），b = 劉備主公（裝諸葛連弩），c = 關羽（蜀），d = 甘寧。 */
    private Game borrowedSwordGame() {
        Game game = new Game();
        game.initDeck();
        Player a = buildPlayer("player-a", General.甘寧, Role.REBEL);
        Player b = buildPlayer("player-b", General.劉備, Role.MONARCH);
        Player c = buildPlayer("player-c", General.關羽, Role.MINISTER);
        Player d = buildPlayer("player-d", General.甘寧, Role.TRAITOR);
        game.setPlayers(Arrays.asList(a, b, c, d));
        game.setCurrentRound(new Round(a));
        game.enterPhase(new Normal(game));
        a.getHand().addCardToHand(new BorrowedSword(SCK065));
        b.getEquipment().setWeapon(new RepeatingCrossbowCard(ECA066));
        return game;
    }

    @DisplayName("借刀殺人令劉備殺 a、劉備無殺 → 不直接交武器；發動激將、關羽代出殺 → a 被問閃，劉備保留武器")
    @Test
    public void borrowedSwordJiJiangShuSuppliesKill() {
        Game game = borrowedSwordGame();
        Player c = game.getPlayer("player-c");
        c.getHand().addCardToHand(new Kill(BS8008));

        game.playerPlayCard("player-a", SCK065.getCardId(), "player-b", PlayType.ACTIVE.getPlayType());
        List<DomainEvent> effect = game.useBorrowedSwordEffect("player-a", "player-b", "player-a");
        assertTrue(effect.stream().anyMatch(e -> e instanceof BorrowedSwordEvent), "劉備無殺但可激將 → 仍問劉備");
        assertFalse(effect.stream().anyMatch(e -> e instanceof WeaponUsurpationEvent));

        List<DomainEvent> e1 = game.playerUseSkillEffect("player-b", "激將", "ACCEPT", null, null);
        assertEquals("player-c", findAsk(e1).getPlayerId());

        List<DomainEvent> e2 = game.playerUseSkillEffect("player-c", "激將", "ACCEPT", List.of(BS8008.getCardId()), null);
        PlayCardEvent play = e2.stream().filter(e -> e instanceof PlayCardEvent)
                .map(e -> (PlayCardEvent) e).findFirst().orElseThrow();
        assertEquals("player-b", play.getPlayerId());
        assertEquals("player-a", play.getTargetPlayerId());
        assertTrue(e2.stream().anyMatch(e -> e instanceof AskDodgeEvent
                && ((AskDodgeEvent) e).getPlayerId().equals("player-a")));
        assertEquals(0, c.getHandSize());

        game.playerPlayCard("player-a", "", "player-b", PlayType.SKIP.getPlayType());
        assertEquals(3, game.getPlayer("player-a").getHP());
        assertNotNull(game.getPlayer("player-b").getEquipmentWeaponCard(), "劉備出了殺 → 保留武器");
        assertFalse(game.getTopBehavior().stream().anyMatch(bh -> bh instanceof BorrowedSwordBehavior));
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
    }

    @DisplayName("借刀殺人激將全部拒絕、劉備無殺 → 武器交給 a")
    @Test
    public void borrowedSwordJiJiangAllDeclineNoKillGivesWeapon() {
        Game game = borrowedSwordGame();
        game.playerPlayCard("player-a", SCK065.getCardId(), "player-b", PlayType.ACTIVE.getPlayType());
        game.useBorrowedSwordEffect("player-a", "player-b", "player-a");
        game.playerUseSkillEffect("player-b", "激將", "ACCEPT", null, null);

        List<DomainEvent> events = game.playerUseSkillEffect("player-c", "激將", "DECLINE", null, null);

        assertTrue(events.stream().anyMatch(e -> e instanceof WeaponUsurpationEvent));
        assertNull(game.getPlayer("player-b").getEquipmentWeaponCard());
        assertTrue(game.getPlayer("player-a").getHand().getCard(ECA066.getCardId()).isPresent());
        assertTrue(game.isTopBehaviorEmpty());
    }

    @DisplayName("借刀殺人激將全部拒絕、劉備有殺 → 回到 BorrowedSwordEvent，劉備自己出殺")
    @Test
    public void borrowedSwordJiJiangAllDeclineLiuBeiPlaysOwnKill() {
        Game game = borrowedSwordGame();
        game.getPlayer("player-b").getHand().addCardToHand(new Kill(BS9009));
        game.playerPlayCard("player-a", SCK065.getCardId(), "player-b", PlayType.ACTIVE.getPlayType());
        game.useBorrowedSwordEffect("player-a", "player-b", "player-a");
        game.playerUseSkillEffect("player-b", "激將", "ACCEPT", null, null);

        List<DomainEvent> events = game.playerUseSkillEffect("player-c", "激將", "DECLINE", null, null);
        assertTrue(events.stream().anyMatch(e -> e instanceof BorrowedSwordEvent
                && ((BorrowedSwordEvent) e).getBorrowedPlayerId().equals("player-b")));
        assertInstanceOf(BorrowedSwordBehavior.class, game.peekTopBehavior());
        assertEquals("player-b", game.getCurrentRound().getActivePlayer().getId());

        List<DomainEvent> kill = game.playerPlayCard("player-b", BS9009.getCardId(), "player-a", PlayType.ACTIVE.getPlayType());
        assertTrue(kill.stream().anyMatch(e -> e instanceof AskDodgeEvent));
    }

    @DisplayName("借刀殺人尚未指定要劉備出殺時不能發動激將")
    @Test
    public void borrowedSwordJiJiangBeforeAskRejected() {
        Game game = borrowedSwordGame();
        game.playerPlayCard("player-a", SCK065.getCardId(), "player-b", PlayType.ACTIVE.getPlayType());
        assertThrows(IllegalStateException.class, () ->
                game.playerUseSkillEffect("player-b", "激將", "ACCEPT", null, null));
    }
}
