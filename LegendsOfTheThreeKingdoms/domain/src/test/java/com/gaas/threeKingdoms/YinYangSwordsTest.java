package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.generalcard.Gender;
import com.gaas.threeKingdoms.handcard.PlayCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.EightDiagramTactic;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.RepeatingCrossbowCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.YinYangSwordsCard;
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

public class YinYangSwordsTest {

    @DisplayName("""
            Given
            A (劉備, MALE) 的回合
            A 手牌有雌雄雙股劍 ES2002
            A 武器欄沒有裝備

            When
            A 出雌雄雙股劍

            Then
            A 裝備卡武器欄有雌雄雙股劍
            A 攻擊距離 = 2 + 1 = 3 (含初始)
                """)
    @Test
    public void givenPlayerAHasYinYangSwords_WhenEquip_ThenAttackRangeIs2() {
        Game game = initGameWith4Players(General.劉備, General.甄姬, General.劉備, General.劉備);
        Player playerA = game.getPlayer("player-a");
        playerA.getHand().addCardToHand(new YinYangSwordsCard(ES2002));

        game.playerPlayCard(playerA.getId(), ES2002.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        assertEquals(new YinYangSwordsCard(ES2002), playerA.getEquipmentWeaponCard());
        assertEquals(3, playerA.judgeAttackDistance()); // weapon(2) + base(1)
    }

    @DisplayName("""
            Given
            A (劉備, MALE) 裝備雌雄雙股劍
            B (甄姬, FEMALE) 有手牌

            When
            A 對 B 出殺

            Then
            觸發雌雄雙股劍效果
            產生 AskYinYangSwordsEffectEvent 要求 B 選擇：棄一張牌 或 讓 A 摸一張牌
                """)
    @Test
    public void givenOppositeGender_WhenKill_ThenTriggerYinYangSwordsEffect() {
        Game game = initGameWith4Players(General.劉備, General.甄姬, General.劉備, General.劉備);
        Player playerA = game.getPlayer("player-a");
        Player playerB = game.getPlayer("player-b");

        playerA.getEquipment().setWeapon(new YinYangSwordsCard(ES2002));
        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008)));
        playerB.getHand().addCardToHand(Arrays.asList(new Dodge(BH2028), new Peach(BH3029)));

        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());

        // Should contain AskYinYangSwordsEffectEvent
        assertTrue(events.stream().anyMatch(e -> e instanceof AskYinYangSwordsEffectEvent));
        AskYinYangSwordsEffectEvent askEvent = events.stream()
                .filter(e -> e instanceof AskYinYangSwordsEffectEvent)
                .map(e -> (AskYinYangSwordsEffectEvent) e)
                .findFirst().orElseThrow();
        assertEquals("player-b", askEvent.getTargetPlayerId());
        assertEquals("player-a", askEvent.getAttackerPlayerId());
    }

    @DisplayName("""
            Given
            雌雄雙股劍效果觸發中
            B (甄姬, FEMALE) 選擇棄一張手牌

            When
            B 棄牌

            Then
            B 手牌少一張
            然後進入 AskDodge 階段
                """)
    @Test
    public void givenYinYangSwordsTriggered_WhenTargetDiscardsCard_ThenProceedToAskDodge() {
        Game game = initGameWith4Players(General.劉備, General.甄姬, General.劉備, General.劉備);
        Player playerA = game.getPlayer("player-a");
        Player playerB = game.getPlayer("player-b");

        playerA.getEquipment().setWeapon(new YinYangSwordsCard(ES2002));
        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008)));
        playerB.getHand().addCardToHand(Arrays.asList(new Dodge(BH2028), new Peach(BH3029)));

        // A plays Kill on B -> triggers YinYangSwords
        game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());

        // B chooses to discard BH3029 (Peach)
        List<DomainEvent> events = game.playerPlayCard(playerB.getId(), BH3029.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        // B should have 1 card left (BH2028)
        assertEquals(1, playerB.getHandSize());
        // Should now ask B to dodge
        assertTrue(events.stream().anyMatch(e -> e instanceof AskDodgeEvent));
    }

    @DisplayName("""
            Given
            雌雄雙股劍效果觸發中
            B (甄姬, FEMALE) 選擇讓 A 摸一張牌

            When
            B 選擇讓 A 摸牌

            Then
            A 手牌多一張
            然後進入 AskDodge 階段
                """)
    @Test
    public void givenYinYangSwordsTriggered_WhenTargetLetsAttackerDraw_ThenAttackerDrawsAndProceedToAskDodge() {
        Game game = initGameWith4Players(General.劉備, General.甄姬, General.劉備, General.劉備);
        Player playerA = game.getPlayer("player-a");
        Player playerB = game.getPlayer("player-b");

        playerA.getEquipment().setWeapon(new YinYangSwordsCard(ES2002));
        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008)));
        playerB.getHand().addCardToHand(Arrays.asList(new Dodge(BH2028), new Peach(BH3029)));

        int playerAHandSizeBefore = playerA.getHandSize(); // 1 (Kill) -> 0 after playing Kill

        // A plays Kill on B -> triggers YinYangSwords
        game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());

        // B chooses to let A draw (skip)
        List<DomainEvent> events = game.playerPlayCard(playerB.getId(), "", playerA.getId(), PlayType.SKIP.getPlayType());

        // A should have drawn 1 card (was 0 after Kill, now 1)
        assertEquals(1, playerA.getHandSize());
        // B hand unchanged (still 2)
        assertEquals(2, playerB.getHandSize());
        // Should now ask B to dodge
        assertTrue(events.stream().anyMatch(e -> e instanceof AskDodgeEvent));
    }

    @DisplayName("""
            Given
            A (劉備, MALE) 裝備雌雄雙股劍
            B (甄姬, FEMALE) 沒有手牌

            When
            A 對 B 出殺

            Then
            B 沒手牌，自動讓 A 摸一張牌
            然後進入 AskDodge 階段
                """)
    @Test
    public void givenOppositeGenderTargetHasNoHandCards_WhenKill_ThenAttackerAutoDraws() {
        Game game = initGameWith4Players(General.劉備, General.甄姬, General.劉備, General.劉備);
        Player playerA = game.getPlayer("player-a");
        Player playerB = game.getPlayer("player-b");

        playerA.getEquipment().setWeapon(new YinYangSwordsCard(ES2002));
        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008)));
        // B has no hand cards

        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());

        // A should have drawn 1 card (was 0 after playing Kill, now 1)
        assertEquals(1, playerA.getHandSize());
        // Should ask B to dodge (skipping the YinYangSwords choice since B has no cards)
        assertTrue(events.stream().anyMatch(e -> e instanceof AskDodgeEvent));
    }

    @DisplayName("""
            Given
            A (劉備, MALE) 裝備雌雄雙股劍
            B (劉備, MALE) 同性

            When
            A 對 B 出殺

            Then
            不觸發雌雄雙股劍效果
            直接進入 AskDodge 階段
                """)
    @Test
    public void givenSameGender_WhenKill_ThenNoYinYangSwordsEffect() {
        Game game = initGameWith4Players(General.劉備, General.劉備, General.劉備, General.劉備);
        Player playerA = game.getPlayer("player-a");
        Player playerB = game.getPlayer("player-b");

        playerA.getEquipment().setWeapon(new YinYangSwordsCard(ES2002));
        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008)));
        playerB.getHand().addCardToHand(Arrays.asList(new Dodge(BH2028)));

        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());

        // No YinYangSwords event
        assertFalse(events.stream().anyMatch(e -> e instanceof AskYinYangSwordsEffectEvent));
        // Directly ask dodge
        assertTrue(events.stream().anyMatch(e -> e instanceof AskDodgeEvent));
    }

    @DisplayName("""
            Given
            A (劉備, MALE) 沒有裝備武器
            B (甄姬, FEMALE) 異性

            When
            A 對 B 出殺

            Then
            不觸發雌雄雙股劍效果 (沒裝備)
            直接進入 AskDodge 階段
                """)
    @Test
    public void givenNoWeapon_WhenKillOppositeGender_ThenNoYinYangSwordsEffect() {
        Game game = initGameWith4Players(General.劉備, General.甄姬, General.劉備, General.劉備);
        Player playerA = game.getPlayer("player-a");
        Player playerB = game.getPlayer("player-b");

        // No weapon equipped
        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008)));
        playerB.getHand().addCardToHand(Arrays.asList(new Dodge(BH2028)));

        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());

        assertFalse(events.stream().anyMatch(e -> e instanceof AskYinYangSwordsEffectEvent));
        assertTrue(events.stream().anyMatch(e -> e instanceof AskDodgeEvent));
    }

    @DisplayName("""
            Given
            A (劉備, MALE) 裝備雌雄雙股劍
            B (甄姬, FEMALE) 裝備八卦陣

            When
            A 對 B 出殺

            Then
            先觸發雌雄雙股劍效果 (棄牌或摸牌)
            再觸發八卦陣效果 (判定閃)
                """)
    @Test
    public void givenYinYangSwordsAndEightDiagram_WhenKillOppositeGender_ThenYinYangSwordsFirstThenEightDiagram() {
        Game game = initGameWith4Players(General.劉備, General.甄姬, General.劉備, General.劉備);
        Player playerA = game.getPlayer("player-a");
        Player playerB = game.getPlayer("player-b");

        playerA.getEquipment().setWeapon(new YinYangSwordsCard(ES2002));
        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008)));
        playerB.getEquipment().setArmor(new EightDiagramTactic(ES2015));
        playerB.getHand().addCardToHand(Arrays.asList(new Dodge(BH2028), new Peach(BH3029)));

        // A plays Kill on B -> triggers YinYangSwords first
        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());

        assertTrue(events.stream().anyMatch(e -> e instanceof AskYinYangSwordsEffectEvent));
    }

    @DisplayName("""
            Given
            A 使用借刀殺人讓 B (劉備, MALE, 裝備雌雄雙股劍) 對 C (甄姬, FEMALE) 出殺

            When
            B 對 C 出殺

            Then
            觸發雌雄雙股劍效果
                """)
    @Test
    public void givenBorrowedSwordWithYinYangSwords_WhenKillOppositeGender_ThenTriggerEffect() {
        Game game = initGameWith4Players(General.劉備, General.劉備, General.甄姬, General.劉備);
        Player playerA = game.getPlayer("player-a");
        Player playerB = game.getPlayer("player-b");
        Player playerC = game.getPlayer("player-c");

        playerB.getEquipment().setWeapon(new YinYangSwordsCard(ES2002));
        playerB.getHand().addCardToHand(Arrays.asList(new Kill(BS8008)));
        playerC.getHand().addCardToHand(Arrays.asList(new Dodge(BH2028), new Peach(BH3029)));

        playerA.getHand().addCardToHand(Arrays.asList(
                new com.gaas.threeKingdoms.handcard.scrollcard.BorrowedSword(SCK065)));

        // A plays BorrowedSword targeting B to kill C
        game.playerPlayCard(playerA.getId(), SCK065.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());

        // Use doBehaviorAction for BorrowedSword
        game.peekTopBehavior().putParam("BORROWED_SWORD_PLAYER_ID", playerA.getId());
        game.peekTopBehavior().putParam("BORROWED_SWORD_BORROWED_PLAYER_ID", playerB.getId());
        game.peekTopBehavior().putParam("BORROWED_SWORD_ATTACK_TARGET_PLAYER_ID", playerC.getId());
        List<DomainEvent> borrowedEvents = game.peekTopBehavior().doBehaviorAction();

        // B plays Kill on C
        List<DomainEvent> killEvents = game.playerPlayCard(playerB.getId(), BS8008.getCardId(), playerC.getId(), PlayType.ACTIVE.getPlayType());

        // Should trigger YinYangSwords effect
        assertTrue(killEvents.stream().anyMatch(e -> e instanceof AskYinYangSwordsEffectEvent));
    }

    // Helper method to initialize a 4-player game
    private Game initGameWith4Players(General generalA, General generalB, General generalC, General generalD) {
        Game game = new Game();
        game.initDeck();

        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(generalA))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withHealthStatus(HealthStatus.ALIVE)
                .build();

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(generalB))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .build();

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(generalC))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(generalD))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .build();

        List<Player> players = asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        return game;
    }
}
