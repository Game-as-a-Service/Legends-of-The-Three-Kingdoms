package com.gaas.threeKingdoms.Ward;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.scrollcard.BarbarianInvasion;
import com.gaas.threeKingdoms.handcard.scrollcard.Dismantle;
import com.gaas.threeKingdoms.handcard.scrollcard.PeachGarden;
import com.gaas.threeKingdoms.handcard.scrollcard.Ward;
import com.gaas.threeKingdoms.player.*;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.rolecard.RoleCard;
import com.gaas.threeKingdoms.round.Round;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.Utils.getEvent;
import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class WardWithPeachGardenTest {

    // === Helper: 建立基本 4 人遊戲 ===

    private Game createGame() {
        Game game = new Game();
        game.initDeck();
        game.setDeck(new Deck(List.of(new BarbarianInvasion(SSK013), new Dismantle(SS3003))));
        return game;
    }

    private Player createPlayer(String id, General general, Role role) {
        return PlayerBuilder.construct()
                .withId(id)
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(general))
                .withRoleCard(new RoleCard(role))
                .withHealthStatus(HealthStatus.ALIVE)
                .build();
    }

    private void setupGame(Game game, List<Player> players, Player activePlayer) {
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(activePlayer));
    }

    // === Test 1: A 出桃園結義，B 有無懈可擊 → WaitForWardEvent ===

    @DisplayName("""
            Given
            玩家 A B C D
            A 有桃園結義
            B 有無懈可擊
            C hp=1 max=2

            When
            A 出桃園結義

            Then
            收到 WaitForWardEvent，eligible players 包含 B
            C 的 HP 不變 (尚未生效)
            """)
    @Test
    public void givenPlayerAHasPeachGardenAndPlayerBHasWard_WhenPlayerAPlaysPeachGarden_ThenWaitForWardEvent() {
        Game game = createGame();

        Player playerA = createPlayer("player-a", General.劉備, Role.MONARCH);
        playerA.getHand().addCardToHand(Arrays.asList(new PeachGarden(SHA027)));

        Player playerB = createPlayer("player-b", General.關羽, Role.MINISTER);
        playerB.getHand().addCardToHand(Arrays.asList(new Ward(SSJ011)));

        Player playerC = createPlayer("player-c", General.張飛, Role.MINISTER);
        playerC.damage(1); // hp=3

        Player playerD = createPlayer("player-d", General.孫權, Role.TRAITOR);

        setupGame(game, asList(playerA, playerB, playerC, playerD), playerA);

        // When
        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), SHA027.getCardId(), null, PlayType.ACTIVE.getPlayType());

        // Then
        WaitForWardEvent waitForWardEvent = getEvent(events, WaitForWardEvent.class).orElseThrow();
        assertTrue(waitForWardEvent.getPlayerIds().contains("player-b"));
        assertEquals(1, waitForWardEvent.getPlayerIds().size());
        // C HP should not change yet
        assertEquals(3, game.getPlayer("player-c").getBloodCard().getHp());
    }

    // === Test 2: A/B/C 有無懈可擊 → WaitForWardEvent 排除出牌者 A ===

    @DisplayName("""
            Given
            玩家 A B C D
            A 有桃園結義 + 無懈可擊
            B 有無懈可擊
            C 有無懈可擊

            When
            A 出桃園結義

            Then
            WaitForWardEvent 包含 B, C 但不包含 A
            """)
    @Test
    public void givenABCAllHaveWard_WhenPlayerAPlaysPeachGarden_ThenWaitForWardEventExcludesA() {
        Game game = createGame();

        Player playerA = createPlayer("player-a", General.劉備, Role.MONARCH);
        playerA.getHand().addCardToHand(Arrays.asList(new PeachGarden(SHA027), new Ward(SCQ077)));

        Player playerB = createPlayer("player-b", General.關羽, Role.MINISTER);
        playerB.getHand().addCardToHand(Arrays.asList(new Ward(SSJ011)));

        Player playerC = createPlayer("player-c", General.張飛, Role.MINISTER);
        playerC.getHand().addCardToHand(Arrays.asList(new Ward(SCK078)));

        Player playerD = createPlayer("player-d", General.孫權, Role.TRAITOR);

        setupGame(game, asList(playerA, playerB, playerC, playerD), playerA);

        // When
        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), SHA027.getCardId(), null, PlayType.ACTIVE.getPlayType());

        // Then
        WaitForWardEvent waitForWardEvent = getEvent(events, WaitForWardEvent.class).orElseThrow();
        assertTrue(waitForWardEvent.getPlayerIds().contains("player-b"));
        assertTrue(waitForWardEvent.getPlayerIds().contains("player-c"));
        assertFalse(waitForWardEvent.getPlayerIds().contains("player-a"));
        assertEquals(2, waitForWardEvent.getPlayerIds().size());
    }

    // === Test 3: B 出無懈可擊 → 抵銷桃園結義 ===

    @DisplayName("""
            Given
            玩家 A B C D
            A 有桃園結義
            B 有無懈可擊
            C hp=3 max=4

            When
            A 出桃園結義
            B 出無懈可擊 (1張，奇數 → 抵銷)

            Then
            桃園結義被抵銷，C HP 不變 (仍為 3)
            active player 回到 A
            """)
    @Test
    public void givenPlayerBHasWard_WhenPlayerBPlaysWard_ThenPeachGardenCancelled() {
        Game game = createGame();

        Player playerA = createPlayer("player-a", General.劉備, Role.MONARCH);
        playerA.getHand().addCardToHand(Arrays.asList(new PeachGarden(SHA027)));

        Player playerB = createPlayer("player-b", General.關羽, Role.MINISTER);
        playerB.getHand().addCardToHand(Arrays.asList(new Ward(SSJ011)));

        Player playerC = createPlayer("player-c", General.張飛, Role.MINISTER);
        playerC.damage(1); // hp=3

        Player playerD = createPlayer("player-d", General.孫權, Role.TRAITOR);

        setupGame(game, asList(playerA, playerB, playerC, playerD), playerA);

        // A plays PeachGarden
        game.playerPlayCard(playerA.getId(), SHA027.getCardId(), null, PlayType.ACTIVE.getPlayType());

        // When: B plays Ward
        List<DomainEvent> events = game.playWardCard("player-b", SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());

        // Then: PeachGarden cancelled
        WardEvent wardEvent = getEvent(events, WardEvent.class).orElseThrow();
        assertNotNull(wardEvent);
        // C HP should NOT change (PeachGarden cancelled)
        assertEquals(3, game.getPlayer("player-c").getBloodCard().getHp());
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
    }

    // === Test 4: B 出無懈可擊，C 出無懈可擊 → 桃園結義生效 (偶數) ===

    @DisplayName("""
            Given
            玩家 A B C D
            A 有桃園結義
            B 有無懈可擊
            C 有無懈可擊
            C hp=3 max=4

            When
            A 出桃園結義
            B 出無懈可擊
            C 出無懈可擊 (2張，偶數 → 生效)

            Then
            桃園結義生效，C HP = 4
            """)
    @Test
    public void givenBAndCHaveWard_WhenBothPlayWard_ThenPeachGardenTakesEffect() {
        Game game = createGame();

        Player playerA = createPlayer("player-a", General.劉備, Role.MONARCH);
        playerA.getHand().addCardToHand(Arrays.asList(new PeachGarden(SHA027)));

        Player playerB = createPlayer("player-b", General.關羽, Role.MINISTER);
        playerB.getHand().addCardToHand(Arrays.asList(new Ward(SSJ011)));

        Player playerC = createPlayer("player-c", General.張飛, Role.MINISTER);
        playerC.damage(1); // hp=3
        playerC.getHand().addCardToHand(Arrays.asList(new Ward(SCK078)));

        Player playerD = createPlayer("player-d", General.孫權, Role.TRAITOR);

        setupGame(game, asList(playerA, playerB, playerC, playerD), playerA);

        // A plays PeachGarden
        game.playerPlayCard(playerA.getId(), SHA027.getCardId(), null, PlayType.ACTIVE.getPlayType());
        // B plays Ward
        game.playWardCard("player-b", SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());
        // When: C plays Ward (2 wards, even → effect executes)
        List<DomainEvent> events = game.playWardCard("player-c", SCK078.getCardId(), PlayType.ACTIVE.getPlayType());

        // Then: PeachGarden takes effect
        PeachGardenEvent peachGardenEvent = getEvent(events, PeachGardenEvent.class).orElseThrow();
        assertNotNull(peachGardenEvent);
        assertEquals(4, game.getPlayer("player-c").getBloodCard().getHp());
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
    }

    // === Test 5: 全部 skip 無懈可擊 → 桃園結義正常生效 ===

    @DisplayName("""
            Given
            玩家 A B C D
            A 有桃園結義 + 無懈可擊
            B 有無懈可擊
            C 有無懈可擊
            C hp=3 max=4

            When
            A 出桃園結義
            B skip → C skip

            Then
            桃園結義生效，C HP = 4
            """)
    @Test
    public void givenPlayersHaveWard_WhenAllSkip_ThenPeachGardenTakesEffect() {
        Game game = createGame();

        Player playerA = createPlayer("player-a", General.劉備, Role.MONARCH);
        playerA.getHand().addCardToHand(Arrays.asList(new PeachGarden(SHA027), new Ward(SCQ077)));

        Player playerB = createPlayer("player-b", General.關羽, Role.MINISTER);
        playerB.getHand().addCardToHand(Arrays.asList(new Ward(SSJ011)));

        Player playerC = createPlayer("player-c", General.張飛, Role.MINISTER);
        playerC.damage(1); // hp=3
        playerC.getHand().addCardToHand(Arrays.asList(new Ward(SCK078)));

        Player playerD = createPlayer("player-d", General.孫權, Role.TRAITOR);

        setupGame(game, asList(playerA, playerB, playerC, playerD), playerA);

        // A plays PeachGarden
        game.playerPlayCard(playerA.getId(), SHA027.getCardId(), null, PlayType.ACTIVE.getPlayType());
        // B skips
        game.playWardCard("player-b", "", PlayType.SKIP.getPlayType());
        // When: C skips
        List<DomainEvent> events = game.playWardCard("player-c", "", PlayType.SKIP.getPlayType());

        // Then: PeachGarden takes effect (0 ward played, even → executes)
        PeachGardenEvent peachGardenEvent = getEvent(events, PeachGardenEvent.class).orElseThrow();
        assertNotNull(peachGardenEvent);
        assertEquals(4, game.getPlayer("player-c").getBloodCard().getHp());
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
    }

    // === Test 6: 沒有人有無懈可擊 → 桃園結義直接生效 ===

    @DisplayName("""
            Given
            玩家 A B C D
            A 有桃園結義
            沒有人有無懈可擊
            C hp=3 max=4

            When
            A 出桃園結義

            Then
            桃園結義直接生效，C HP = 4
            """)
    @Test
    public void givenNoOneHasWard_WhenPlayerAPlaysPeachGarden_ThenPeachGardenTakesEffectImmediately() {
        Game game = createGame();

        Player playerA = createPlayer("player-a", General.劉備, Role.MONARCH);
        playerA.getHand().addCardToHand(Arrays.asList(new PeachGarden(SHA027)));

        Player playerB = createPlayer("player-b", General.關羽, Role.MINISTER);

        Player playerC = createPlayer("player-c", General.張飛, Role.MINISTER);
        playerC.damage(1); // hp=3

        Player playerD = createPlayer("player-d", General.孫權, Role.TRAITOR);

        setupGame(game, asList(playerA, playerB, playerC, playerD), playerA);

        // When
        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), SHA027.getCardId(), null, PlayType.ACTIVE.getPlayType());

        // Then: PeachGarden takes effect immediately
        PeachGardenEvent peachGardenEvent = getEvent(events, PeachGardenEvent.class).orElseThrow();
        assertNotNull(peachGardenEvent);
        assertEquals(4, game.getPlayer("player-c").getBloodCard().getHp());
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
    }

    // === Test 7: B 出無懈可擊，A 出無懈可擊，C skip → 桃園結義生效 (偶數) ===

    @DisplayName("""
            Given
            玩家 A B C D
            A 有桃園結義 + 無懈可擊
            B 有無懈可擊
            C 有無懈可擊
            C hp=3 max=4

            When
            A 出桃園結義
            B 出無懈可擊
            A 出無懈可擊 (反制 B 的無懈可擊)
            C skip

            Then
            2 張無懈可擊，偶數 → 桃園結義生效，C HP = 4
            """)
    @Test
    public void givenBPlaysWardAndAPlaysWardAndCSkips_ThenPeachGardenTakesEffect() {
        Game game = createGame();

        Player playerA = createPlayer("player-a", General.劉備, Role.MONARCH);
        playerA.getHand().addCardToHand(Arrays.asList(new PeachGarden(SHA027), new Ward(SCQ077)));

        Player playerB = createPlayer("player-b", General.關羽, Role.MINISTER);
        playerB.getHand().addCardToHand(Arrays.asList(new Ward(SSJ011)));

        Player playerC = createPlayer("player-c", General.張飛, Role.MINISTER);
        playerC.damage(1); // hp=3
        playerC.getHand().addCardToHand(Arrays.asList(new Ward(SCK078)));

        Player playerD = createPlayer("player-d", General.孫權, Role.TRAITOR);

        setupGame(game, asList(playerA, playerB, playerC, playerD), playerA);

        // A plays PeachGarden
        game.playerPlayCard(playerA.getId(), SHA027.getCardId(), null, PlayType.ACTIVE.getPlayType());
        // B plays Ward
        game.playWardCard("player-b", SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());
        // A plays Ward (counter B's Ward)
        game.playWardCard("player-a", SCQ077.getCardId(), PlayType.ACTIVE.getPlayType());
        // When: C skips
        List<DomainEvent> events = game.playWardCard("player-c", "", PlayType.SKIP.getPlayType());

        // Then: 2 wards (even) → PeachGarden takes effect
        PeachGardenEvent peachGardenEvent = getEvent(events, PeachGardenEvent.class).orElseThrow();
        assertNotNull(peachGardenEvent);
        assertEquals(4, game.getPlayer("player-c").getBloodCard().getHp());
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
    }
}
