package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.behavior.behavior.WaitingJianXiongResponseBehavior;
import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.events.AskJianXiongEffectEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.JianXiongEffectEvent;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.player.*;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.rolecard.RoleCard;
import com.gaas.threeKingdoms.round.Round;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class JianXiongSkillTest {

    private Game setupGameCaoCaoB(General playerBGeneral) {
        Game game = new Game();
        game.initDeck();

        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();
        playerA.getHand().addCardToHand(Arrays.asList(
                new Kill(BS8008), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028), new Dodge(BHK039)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withGeneralCard(new GeneralCard(playerBGeneral))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        List<Player> players = asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.setCurrentRound(new Round(playerA));
        game.enterPhase(new Normal(game));
        return game;
    }

    @DisplayName("""
            Given
            B 玩家為曹操（WEI001），無閃，HP 4
            A 對 B 出殺

            When
            B 不出閃 → 受到 1 點傷害

            Then
            事件中含 AskJianXiongEffectEvent
            stack 頂端為 WaitingJianXiongResponseBehavior
            B 仍為 3 HP，殺仍在棄牌堆
            """)
    @Test
    public void givenCaoCaoTakeDamageFromKill_AskJianXiongEffectEmitted() {
        Game game = setupGameCaoCaoB(General.曹操);

        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");
        List<DomainEvent> events = game.playerPlayCard("player-b", "", "player-a", "skip");

        assertEquals(3, game.getPlayer("player-b").getBloodCard().getHp());
        assertTrue(events.stream().anyMatch(e -> e instanceof AskJianXiongEffectEvent),
                "expected AskJianXiongEffectEvent in events");
        AskJianXiongEffectEvent ask = (AskJianXiongEffectEvent) events.stream()
                .filter(e -> e instanceof AskJianXiongEffectEvent).findFirst().orElseThrow();
        assertEquals("player-b", ask.getPlayerId());
        assertEquals(BS8008.getCardId(), ask.getSourceCardId());

        assertFalse(game.getTopBehavior().isEmpty());
        assertTrue(game.getTopBehavior().peek() instanceof WaitingJianXiongResponseBehavior);
        assertTrue(game.getGraveyard().contains(BS8008.getCardId()));
    }

    @DisplayName("""
            Given
            B 為曹操，受到殺造成的傷害 → AskJianXiongEffectEvent

            When
            B 選 ACCEPT

            Then
            殺從棄牌堆進入 B 手牌
            事件中含 JianXiongEffectEvent(taken=true)
            stack 為空
            """)
    @Test
    public void givenJianXiongAsk_AcceptChoice_KillReturnsToHand() {
        Game game = setupGameCaoCaoB(General.曹操);
        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");
        game.playerPlayCard("player-b", "", "player-a", "skip");

        List<DomainEvent> events = game.playerUseJianXiongEffect("player-b", AskJianXiongEffectEvent.Choice.ACCEPT);

        assertFalse(game.getGraveyard().contains(BS8008.getCardId()));
        assertTrue(game.getPlayer("player-b").getHand().getCards().stream()
                .anyMatch(c -> c.getId().equals(BS8008.getCardId())));
        assertTrue(events.stream().anyMatch(e -> e instanceof JianXiongEffectEvent
                && ((JianXiongEffectEvent) e).isTaken()));
        assertTrue(game.getTopBehavior().isEmpty());
    }

    @DisplayName("""
            Given
            B 為曹操，受到殺造成的傷害 → AskJianXiongEffectEvent

            When
            B 選 SKIP

            Then
            殺仍在棄牌堆，B 手牌不變
            事件中含 JianXiongEffectEvent(taken=false)
            stack 為空
            """)
    @Test
    public void givenJianXiongAsk_SkipChoice_NoChange() {
        Game game = setupGameCaoCaoB(General.曹操);
        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");
        game.playerPlayCard("player-b", "", "player-a", "skip");

        int handSizeBefore = game.getPlayer("player-b").getHand().size();
        List<DomainEvent> events = game.playerUseJianXiongEffect("player-b", AskJianXiongEffectEvent.Choice.SKIP);

        assertTrue(game.getGraveyard().contains(BS8008.getCardId()));
        assertEquals(handSizeBefore, game.getPlayer("player-b").getHand().size());
        assertTrue(events.stream().anyMatch(e -> e instanceof JianXiongEffectEvent
                && !((JianXiongEffectEvent) e).isTaken()));
        assertTrue(game.getTopBehavior().isEmpty());
    }

    @DisplayName("""
            Given
            B 為劉備（非曹操），受到殺造成的傷害

            Then
            不觸發奸雄 — 沒有 AskJianXiongEffectEvent
            stack 為空
            """)
    @Test
    public void givenNonCaoCao_NoJianXiongTrigger() {
        Game game = setupGameCaoCaoB(General.劉備);
        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");
        List<DomainEvent> events = game.playerPlayCard("player-b", "", "player-a", "skip");

        assertEquals(3, game.getPlayer("player-b").getBloodCard().getHp());
        assertFalse(events.stream().anyMatch(e -> e instanceof AskJianXiongEffectEvent));
        assertTrue(game.getTopBehavior().isEmpty());
    }
}
