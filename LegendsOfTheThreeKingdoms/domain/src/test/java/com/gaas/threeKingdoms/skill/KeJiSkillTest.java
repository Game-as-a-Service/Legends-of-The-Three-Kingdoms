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
}
