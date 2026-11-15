package com.gaas.threeKingdoms.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.round.RoundPhase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;

public class KeJiSkillTest extends PassiveSkillTestBase {

    @DisplayName("呂蒙本回合未出殺、手牌 6 張 → 克己：棄牌數 0")
    @Test
    public void keJiSkipsDiscardWhenNoKillPlayed() {
        Game game = createGame(General.呂蒙, General.劉備, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Kill(BS9009), new Dodge(BH2028),
                new Dodge(BHK039), new Peach(BH3029), new Peach(BH4030)));
        game.getCurrentRound().setRoundPhase(RoundPhase.Discard);

        assertEquals(0, game.getCurrentRoundPlayerDiscardCount(), "克己：未出殺不棄牌");
    }

    @DisplayName("呂蒙本回合出過殺 → 克己不生效，照常棄牌")
    @Test
    public void keJiDoesNotApplyAfterKillPlayed() {
        Game game = createGame(General.呂蒙, General.劉備, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Kill(BS9009), new Dodge(BH2028),
                new Dodge(BHK039), new Peach(BH3029), new Peach(BH4030)));

        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");
        game.playerPlayCard("player-b", "", "player-a", "skip");
        game.getCurrentRound().setRoundPhase(RoundPhase.Discard);

        assertEquals(1, game.getCurrentRoundPlayerDiscardCount(), "出過殺：5 張手牌 - 4 HP = 1");
    }

    @DisplayName("非呂蒙未出殺 → 照常棄牌")
    @Test
    public void nonLvMengDiscardsNormally() {
        Game game = createGame(General.孫權, General.劉備, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Kill(BS9009), new Dodge(BH2028),
                new Dodge(BHK039), new Peach(BH3029), new Peach(BH4030)));
        game.getCurrentRound().setRoundPhase(RoundPhase.Discard);

        assertEquals(2, game.getCurrentRoundPlayerDiscardCount());
    }

    private static java.util.Optional<com.gaas.threeKingdoms.events.SkillEffectEvent> keJiEvent(
            java.util.List<com.gaas.threeKingdoms.events.DomainEvent> events) {
        return events.stream().filter(e -> e instanceof com.gaas.threeKingdoms.events.SkillEffectEvent se
                        && "克己".equals(se.getSkillName()))
                .map(e -> (com.gaas.threeKingdoms.events.SkillEffectEvent) e).findFirst();
    }

    @DisplayName("呂蒙未出殺、手牌超過上限 → 結束出牌時廣播克己，寫出不用棄幾張")
    @Test
    public void keJiAnnouncedWhenItSavesDiscards() {
        Game game = createGame(General.呂蒙, General.甘寧, General.甘寧, General.甘寧);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Kill(BS9009), new Dodge(BH2028),
                new Dodge(BHK039), new Peach(BH3029), new Peach(BH4030)));

        java.util.List<com.gaas.threeKingdoms.events.DomainEvent> events = game.finishAction("player-a");

        com.gaas.threeKingdoms.events.SkillEffectEvent keJi = keJiEvent(events).orElseThrow();
        assertEquals("player-a", keJi.getPlayerId());
        assertEquals("克己：呂蒙 本回合沒有使用或打出過【殺】，跳過棄牌階段（不用棄 2 張）", keJi.getMessage());
        assertEquals(6, a.getHandSize(), "手牌全部保留");
    }

    @DisplayName("呂蒙手牌沒超過上限 → 有沒有克己都一樣，不廣播")
    @Test
    public void keJiNotAnnouncedWhenNothingToDiscard() {
        Game game = createGame(General.呂蒙, General.甘寧, General.甘寧, General.甘寧);
        game.getPlayer("player-a").getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Dodge(BH2028)));

        assertTrue(keJiEvent(game.finishAction("player-a")).isEmpty());
    }

    @DisplayName("呂蒙出過殺 → 克己不生效，不廣播")
    @Test
    public void keJiNotAnnouncedAfterKill() {
        Game game = createGame(General.呂蒙, General.甘寧, General.甘寧, General.甘寧);
        game.getPlayer("player-a").getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Kill(BS9009),
                new Dodge(BH2028), new Dodge(BHK039), new Peach(BH3029), new Peach(BH4030)));
        game.getCurrentRound().setKillPlayedThisTurn(true);

        assertTrue(keJiEvent(game.finishAction("player-a")).isEmpty());
    }

    private static int discardCountOf(java.util.List<com.gaas.threeKingdoms.events.DomainEvent> events) {
        return events.stream().filter(e -> e instanceof com.gaas.threeKingdoms.events.NotifyDiscardEvent)
                .map(e -> ((com.gaas.threeKingdoms.events.NotifyDiscardEvent) e).getDiscardCount())
                .findFirst().orElseThrow();
    }

    @DisplayName("呂蒙自己出決鬥、回應時打出殺 → 算「打出過殺」，克己不生效，照常棄牌")
    @Test
    public void keJiDisabledAfterPlayingKillInOwnDuel() {
        Game game = createGame(General.呂蒙, General.甘寧, General.甘寧, General.甘寧);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(Arrays.asList(new com.gaas.threeKingdoms.handcard.scrollcard.Duel(SSA001),
                new Kill(BS9009), new Dodge(BH2028), new Dodge(BHK039), new Peach(BH3029), new Peach(BH4030),
                new Peach(BH7033)));
        b.getHand().addCardToHand(Arrays.asList(new Kill(BS8008)));

        String active = com.gaas.threeKingdoms.handcard.PlayType.ACTIVE.getPlayType();
        game.playerPlayCard("player-a", SSA001.getCardId(), "player-b", active);   // 呂蒙出決鬥
        game.playerPlayCard("player-b", BS8008.getCardId(), "player-a", active);   // 甘寧打出殺
        game.playerPlayCard("player-a", BS9009.getCardId(), "player-b", active);   // 呂蒙打出殺（甘寧沒殺了 → 自動結算受傷）

        assertTrue(game.getCurrentRound().isKillPlayedThisTurn(), "回應時打出的殺也要算");
        assertEquals(5, a.getHandSize());
        assertEquals(1, discardCountOf(game.finishAction("player-a")), "克己不生效：5 張 - 4 HP = 棄 1");
    }

    @DisplayName("呂蒙出決鬥、對方沒有殺（自動結算）→ 呂蒙沒打出過殺，克己照常生效")
    @Test
    public void keJiStillAppliesWhenDuelEndsWithoutLuMengKill() {
        Game game = createGame(General.呂蒙, General.甘寧, General.甘寧, General.甘寧);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(Arrays.asList(new com.gaas.threeKingdoms.handcard.scrollcard.Duel(SSA001),
                new Kill(BS9009), new Dodge(BH2028), new Dodge(BHK039), new Peach(BH3029), new Peach(BH4030),
                new Peach(BH7033)));

        // 甘寧手上沒有殺 → 決鬥自動結算（甘寧受傷），呂蒙沒有打出殺
        game.playerPlayCard("player-a", SSA001.getCardId(), "player-b",
                com.gaas.threeKingdoms.handcard.PlayType.ACTIVE.getPlayType());

        assertFalse(game.getCurrentRound().isKillPlayedThisTurn());
        assertEquals(0, discardCountOf(game.finishAction("player-a")), "克己：6 張全留");
    }

    @DisplayName("別人在呂蒙回合打出的殺不影響克己（只記回合玩家自己的殺）")
    @Test
    public void otherPlayersKillDoesNotDisableKeJi() {
        Game game = createGame(General.呂蒙, General.甘寧, General.甘寧, General.甘寧);
        Player b = game.getPlayer("player-b");
        game.getPlayer("player-a").getHand().addCardToHand(Arrays.asList(
                new com.gaas.threeKingdoms.handcard.scrollcard.Duel(SSA001)));
        b.getHand().addCardToHand(Arrays.asList(new Kill(BS8008)));

        game.playerPlayCard("player-a", SSA001.getCardId(), "player-b",
                com.gaas.threeKingdoms.handcard.PlayType.ACTIVE.getPlayType());
        game.playerPlayCard("player-b", BS8008.getCardId(), "player-a",
                com.gaas.threeKingdoms.handcard.PlayType.ACTIVE.getPlayType());   // 甘寧打出殺，呂蒙接著沒殺

        assertFalse(game.getCurrentRound().isKillPlayedThisTurn(), "甘寧打出的殺不算呂蒙的");
    }
}
