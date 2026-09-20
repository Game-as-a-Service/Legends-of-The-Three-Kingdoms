package com.gaas.threeKingdoms.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.events.AskDodgeEvent;
import com.gaas.threeKingdoms.events.AskKillEvent;
import com.gaas.threeKingdoms.events.AskSkillEffectEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.NotifyDiscardEvent;
import com.gaas.threeKingdoms.events.RoundEndEvent;
import com.gaas.threeKingdoms.events.SkillEffectEvent;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.scrollcard.Dismantle;
import com.gaas.threeKingdoms.handcard.scrollcard.Duel;
import com.gaas.threeKingdoms.player.*;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.rolecard.RoleCard;
import com.gaas.threeKingdoms.round.Round;
import com.gaas.threeKingdoms.round.RoundPhase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 剛烈**結算完之後**的後續操作（使用者回報卡局後的補強）：四條結束路徑
 * （夏侯惇放棄 / 判定紅桃 / 來源棄兩張 / 來源受傷）走完，回合都要能繼續 ——
 * stack 清空、activePlayer 回到回合玩家、還能出牌、能結束回合進棄牌階段並換下一位。
 *
 * <p>AOE polling 中的 resume 已由 {@code GangLieAoePollingTest} 覆蓋；本測試看的是
 * 普通殺／決鬥情境下「剛烈之後整個回合流程還走得下去」。
 */
public class GangLieAfterResolutionTest {

    /** a = 甘寧（回合玩家、決鬥來源）、b = 夏侯惇；判定牌由參數指定。 */
    private Game givenGanNingDuelsXiaHouDun(HandCard judgementCard) {
        Game game = newGame();
        game.getPlayer("player-a").getHand().addCardToHand(List.of(
                new Duel(SSA001), new Kill(BS8008),
                new Peach(BH3029), new Peach(BH4030), new Peach(BH7033)));
        // b 手上有殺 → 決鬥會先問 b 出殺（照使用者的實戰路徑）；無殺時決鬥會直接結算扣血
        game.getPlayer("player-b").getHand().addCardToHand(List.of(new Kill(BS9009)));
        game.setDeck(new Deck(List.of(new Dodge(BHK039), new Dodge(BH2028), judgementCard)));
        return game;
    }

    private Game newGame() {
        Game game = new Game();
        game.initDeck();
        Player a = build("player-a", General.甘寧, Role.MONARCH);
        Player b = build("player-b", General.夏侯惇, Role.MINISTER);
        Player c = build("player-c", General.甘寧, Role.REBEL);
        Player d = build("player-d", General.甘寧, Role.TRAITOR);
        game.setPlayers(asList(a, b, c, d));
        game.setCurrentRound(new Round(a));
        game.enterPhase(new Normal(game));
        return game;
    }

    private Player build(String id, General general, Role role) {
        return PlayerBuilder.construct().withId(id)
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(general))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(role))
                .withHand(new Hand()).withEquipment(new Equipment()).build();
    }

    /** a 出決鬥、b 不出殺受傷 → 停在「問夏侯惇是否發動剛烈」。 */
    private void duelUntilGangLieAsked(Game game) {
        List<DomainEvent> duelEvents = game.playerPlayCard("player-a", SSA001.getCardId(), "player-b",
                PlayType.ACTIVE.getPlayType());
        assertTrue(duelEvents.stream().anyMatch(e -> e instanceof AskKillEvent ask
                && ask.getPlayerId().equals("player-b")), "決鬥先要求 b 出殺");

        List<DomainEvent> skipEvents = game.playerPlayCard("player-b", "", "player-a",
                PlayType.SKIP.getPlayType());
        assertEquals(3, game.getPlayer("player-b").getHP(), "b 不出殺 → 扣 1 血");
        assertTrue(skipEvents.stream().anyMatch(e -> e instanceof AskSkillEffectEvent ask
                && ask.getSkillName().equals("剛烈")), "受傷後詢問剛烈");
    }

    /** 剛烈收束後的共同要求：stack 清空、輪到回合玩家。 */
    private void assertRoundCanGoOn(Game game, String roundPlayerId) {
        assertTrue(game.getTopBehavior().isEmpty(), "剛烈結束後 stack 要清空，否則 finishAction 會丟 IllegalState");
        assertEquals(roundPlayerId, game.getCurrentRound().getActivePlayer().getId(),
                "剛烈結束後 activePlayer 要回到回合玩家");
        assertNotEquals(RoundPhase.Discard, game.getCurrentRound().getRoundPhase(), "回合還沒進棄牌階段");
    }

    /** a 出殺打 d（相鄰）、d 不出閃 → 驗證剛烈之後還能正常出牌。 */
    private void assertCanStillPlayKillOnD(Game game) {
        List<DomainEvent> killEvents = game.playerPlayCard("player-a", BS8008.getCardId(),
                "player-d", PlayType.ACTIVE.getPlayType());
        assertTrue(killEvents.stream().anyMatch(e -> e instanceof AskDodgeEvent ask
                        && ask.getPlayerId().equals("player-d")),
                "剛烈沒有吃掉本回合的出殺次數，殺應正常要求 d 出閃");

        game.playerPlayCard("player-d", "", "player-a", PlayType.SKIP.getPlayType());
        assertEquals(3, game.getPlayer("player-d").getHP(), "d 不出閃 → 扣 1 血");
        assertTrue(game.getTopBehavior().isEmpty());
    }

    private boolean hasRoundEnd(List<DomainEvent> events) {
        return events.stream().anyMatch(e -> e instanceof RoundEndEvent);
    }

    private int notifiedDiscardCount(List<DomainEvent> events) {
        return events.stream().filter(e -> e instanceof NotifyDiscardEvent)
                .map(e -> ((NotifyDiscardEvent) e).getDiscardCount())
                .findFirst().orElseThrow();
    }

    @DisplayName("夏侯惇放棄剛烈後：回合玩家還能出殺，並能結束回合換下一位")
    @Test
    public void xiaHouDunSkipsGangLie_roundPlayerKeepsPlayingAndEndsTurn() {
        Game game = givenGanNingDuelsXiaHouDun(new Dismantle(SS3003));
        duelUntilGangLieAsked(game);

        game.playerUseSkillEffect("player-b", "剛烈", "SKIP", null, null);

        assertRoundCanGoOn(game, "player-a");
        assertCanStillPlayKillOnD(game);

        List<DomainEvent> finish = game.finishAction("player-a");
        assertEquals(0, notifiedDiscardCount(finish), "手牌 3 張 ≤ HP 4，不需棄牌");
        assertTrue(hasRoundEnd(finish));
        assertEquals("player-b", game.getCurrentRoundPlayer().getId(), "回合換到下一位");
    }

    @DisplayName("剛烈判定紅桃未生效後：來源不受影響，回合照常打完")
    @Test
    public void heartJudgement_roundPlayerKeepsPlayingAndEndsTurn() {
        Game game = givenGanNingDuelsXiaHouDun(new Dodge(BH2028)); // 紅心 → 未生效
        duelUntilGangLieAsked(game);

        List<DomainEvent> events = game.playerUseSkillEffect("player-b", "剛烈", "ACCEPT", null, null);

        assertEquals("剛烈判定：紅心2 閃 → 紅桃，未生效", gangLieMessage(events));
        assertEquals(4, game.getPlayer("player-a").getHP(), "未生效 → 來源不受傷");
        assertRoundCanGoOn(game, "player-a");
        assertCanStillPlayKillOnD(game);

        assertTrue(hasRoundEnd(game.finishAction("player-a")));
        assertEquals("player-b", game.getCurrentRoundPlayer().getId());
    }

    @DisplayName("來源棄兩張手牌後：回合玩家還能出殺，並能結束回合換下一位")
    @Test
    public void sourceDiscardsTwoCards_roundPlayerKeepsPlayingAndEndsTurn() {
        Game game = givenGanNingDuelsXiaHouDun(new Dismantle(SS3003)); // 黑桃 → 生效
        duelUntilGangLieAsked(game);
        game.playerUseSkillEffect("player-b", "剛烈", "ACCEPT", null, null);

        game.playerUseSkillEffect("player-a", "剛烈", "DISCARD",
                List.of(BH3029.getCardId(), BH4030.getCardId()), null);

        Player a = game.getPlayer("player-a");
        assertEquals(4, a.getHP(), "棄牌則不受傷");
        assertEquals(2, a.getHandSize(), "決鬥 + 棄兩張後剩殺與一張桃");
        assertTrue(game.getGraveyard().contains(BH3029.getCardId()));
        assertRoundCanGoOn(game, "player-a");
        assertCanStillPlayKillOnD(game);

        List<DomainEvent> finish = game.finishAction("player-a");
        assertEquals(0, notifiedDiscardCount(finish), "手牌 1 張 ≤ HP 4");
        assertTrue(hasRoundEnd(finish));
        assertEquals("player-b", game.getCurrentRoundPlayer().getId());
    }

    @DisplayName("來源受剛烈 1 點傷害後：手牌上限跟著降，棄牌階段仍走得完並換下一位")
    @Test
    public void sourceTakesDamage_discardPhaseFollowsNewHpLimit() {
        Game game = givenGanNingDuelsXiaHouDun(new Dismantle(SS3003)); // 黑桃 → 生效
        duelUntilGangLieAsked(game);
        game.playerUseSkillEffect("player-b", "剛烈", "ACCEPT", null, null);

        game.playerUseSkillEffect("player-a", "剛烈", "DAMAGE", null, null);

        Player a = game.getPlayer("player-a");
        assertEquals(3, a.getHP(), "來源受剛烈 1 點傷害");
        assertEquals(4, a.getHandSize());
        assertRoundCanGoOn(game, "player-a");

        // 手牌 4 > HP 3 → 棄牌階段要棄 1 張（剛烈的傷害有反映在手牌上限）
        List<DomainEvent> finish = game.finishAction("player-a");
        assertEquals(1, notifiedDiscardCount(finish));
        assertFalse(hasRoundEnd(finish), "還要棄牌，回合尚未結束");
        assertEquals("player-a", game.getCurrentRoundPlayer().getId());
        assertEquals(RoundPhase.Discard, game.getCurrentRound().getRoundPhase());

        List<DomainEvent> discard = game.playerDiscardCard(List.of(BH3029.getCardId()));
        assertTrue(hasRoundEnd(discard));
        assertEquals(3, a.getHandSize());
        assertEquals("player-b", game.getCurrentRoundPlayer().getId(), "棄完牌換下一位");
    }

    @DisplayName("夏侯惇自己的回合受傷：剛烈收束後 activePlayer 回到夏侯惇（不是傷害來源），回合照常打完")
    @Test
    public void gangLieDuringXiaHouDunOwnTurn_activePlayerReturnsToXiaHouDun() {
        Game game = newGame();
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        b.getHand().addCardToHand(List.of(new Duel(SSA001), new Kill(BS9009), new Peach(BH3029)));
        a.getHand().addCardToHand(List.of(new Kill(BS8008)));
        game.setCurrentRound(new Round(b));
        game.setDeck(new Deck(List.of(new Dodge(BHK039), new Dodge(BH2028), new Dismantle(SS3003))));

        // b 出決鬥給 a → a 出殺 → b 不出殺受傷（來源 a 不是回合玩家）
        game.playerPlayCard("player-b", SSA001.getCardId(), "player-a", PlayType.ACTIVE.getPlayType());
        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", PlayType.ACTIVE.getPlayType());
        game.playerPlayCard("player-b", "", "player-a", PlayType.SKIP.getPlayType());
        assertEquals(3, b.getHP());

        game.playerUseSkillEffect("player-b", "剛烈", "ACCEPT", null, null);
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId(), "判定生效 → 換來源回答");

        game.playerUseSkillEffect("player-a", "剛烈", "DAMAGE", null, null);

        assertEquals(3, a.getHP(), "來源受剛烈 1 點傷害");
        assertRoundCanGoOn(game, "player-b");

        // b 繼續自己的回合：出殺打 a
        game.playerPlayCard("player-b", BS9009.getCardId(), "player-a", PlayType.ACTIVE.getPlayType());
        game.playerPlayCard("player-a", "", "player-b", PlayType.SKIP.getPlayType());
        assertEquals(2, a.getHP());

        List<DomainEvent> finish = game.finishAction("player-b");
        assertEquals(0, notifiedDiscardCount(finish), "b 手牌 1 張 ≤ HP 3");
        assertTrue(hasRoundEnd(finish));
        assertEquals("player-c", game.getCurrentRoundPlayer().getId(), "b 的回合結束後換 c");
    }

    private String gangLieMessage(List<DomainEvent> events) {
        return events.stream()
                .filter(e -> e instanceof SkillEffectEvent se && se.getSkillName().equals("剛烈"))
                .findFirst().orElseThrow().getMessage();
    }
}
