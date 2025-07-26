package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.PlayCardEvent;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.RedRabbitHorse;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.ShadowHorse;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.QilinBowCard;
import com.gaas.threeKingdoms.handcard.scrollcard.*;
import com.gaas.threeKingdoms.player.*;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.rolecard.RoleCard;
import com.gaas.threeKingdoms.round.Round;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static com.gaas.threeKingdoms.handcard.PlayCard.SSA001;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class SnatchTest {


    @DisplayName("""
        Given
        玩家ABCD
        B沒有裝備，沒有手牌
        A有順手牽羊

        When
        A 出過河拆橋，指定 B

        Then
        拋出錯誤
    """)
    @Test
    public void givenPlayerABCD_PlayerBHasNoEquipmentsOrCards_PlayerAHasSnatch_WhenPlayerAPlaysSnatchAndTargetsB_ThenThrowException() {

        Game game = new Game();
        game.initDeck();
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(new Snatch(SSJ024), new Dodge(BH2028), new Dodge(BHK039), new Duel(SSA001)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(new Equipment())
                .build();

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        List<Player> players = asList(
                playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));


        //When
        Assertions.assertThrows(IllegalArgumentException.class, () ->  game.playerPlayCard(playerA.getId(), SSJ024.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType()));
    }

    @DisplayName("""
        Given
        玩家ABCD
        B沒有裝備，沒有手牌，判定區有樂不思蜀
        A有順手牽羊
    
        When
        A 出順手牽羊，指定 B
    
        Then
        拋出錯誤
    """)
    @Test
    public void givenPlayerBHasOnlyJudgementCard_WhenPlayerAPlaySnatchOnB_ThenThrowException() throws Exception {
        Game game = new Game();
        game.initDeck();

        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(
                new Snatch(SS3016), new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)));

        Stack<ScrollCard> delayScrollCards = new Stack<>();
        delayScrollCards.add(new Contentment(SH6045));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withDelayScrollCards(delayScrollCards)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(new Equipment())
                .build();

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        List<Player> players = asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> game.playerPlayCard(playerA.getId(), SS3016.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType()));
    }


    @DisplayName("""
        Given
        玩家ABCD
        B有一張手牌
        A 有順手牽羊
        
        When
        A 出順手牽羊，指定 B
        
        Then
        回傳 PlayCardEvent
    """)
    @Test
    public void givenPlayerABCD_PlayerBHasOneCard_PlayerAHasSnatch_WhenPlayerAPlaysSnatchAndTargetsB_ThenReturnPlayCardEvent() throws Exception {
        Game game = new Game();
        game.initDeck();
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(
                new Snatch(SS3016), new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(new Equipment())
                .build();

        playerB.getHand().addCardToHand(List.of(new Dodge(BH2028)));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        List<Player> players = asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        // When
        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), SS3016.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());

        // Then
        assertTrue(events.stream().anyMatch(e -> e instanceof PlayCardEvent));
    }

    @DisplayName("""
    Given
        玩家ABCD
        B有一張裝備 麒麟弓
        A有順手牽羊
    
        When
        A 出順手牽羊，指定 B
    
        Then
        回傳 PlayCardEvent
    """)
    @Test
    public void givenPlayerBHasWeapon_PlayerAHasSnatch_WhenPlayerAPlaysSnatchOnB_ThenReturnPlayCardEvent() throws Exception {
        Game game = new Game();
        game.initDeck();

        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(
                new Snatch(SS3016), new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)));

        Equipment equipmentB = new Equipment();
        equipmentB.setWeapon(new QilinBowCard(EH5031));
        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withEquipment(equipmentB)
                .withGeneralCard(new GeneralCard(General.張飛))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .build();

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.關羽))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.趙雲))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        List<Player> players = asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        // When
        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), SS3016.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());

        // Then
        assertTrue(events.stream().anyMatch(e -> e instanceof PlayCardEvent));
    }

    @DisplayName("""
    Given
    玩家ABCD
    A沒有 - 1馬、有麒麟弓
    A有順手牽羊

    When
    A 出順手牽羊，指定 C

    Then
    拋出錯誤
""")
    @Test
    public void givenPlayerAHasQilinBowButNoMinusOneHorse_WhenPlayerAPlaySnatchOnFarPlayer_ThenThrowException() throws Exception {
        Game game = new Game();
        game.initDeck();

        Equipment equipmentA = new Equipment();
        equipmentA.setWeapon(new QilinBowCard(EH5031));
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(equipmentA)
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(
                new Snatch(SS3016), new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.張飛))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.關羽))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.趙雲))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        List<Player> players = asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> game.playerPlayCard(playerA.getId(), SS3016.getCardId(), playerC.getId(), PlayType.ACTIVE.getPlayType()));
    }

    @DisplayName("""
        Given
        玩家ABCD
        A沒有馬、 B有 + 1 馬，
        A有順手牽羊
    
        When
        A 出順手牽羊，指定 B
    
        Then
        拋出錯誤
    """)
    @Test
    public void givenPlayerAHasNoMount_PlayerBHasPlusOneMount_WhenPlayerAPlaySnatchOnB_ThenThrowException() throws Exception {
        Game game = new Game();
        game.initDeck();

        Equipment equipmentA = new Equipment(); // 無馬
        Equipment equipmentB = new Equipment();
        equipmentB.setPlusOne(new ShadowHorse(ES5018)); // B 有 +1 馬

        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(equipmentA)
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(
                new Snatch(SS3016), new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.張飛))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(equipmentB)
                .build();

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.關羽))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.趙雲))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        List<Player> players = asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> game.playerPlayCard(playerA.getId(), SS3016.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType()));
    }

    @DisplayName("""
        Given
        玩家ABCD
        A有 - 1馬、沒有麒麟弓
        A有順手牽羊
    
        When
        A 出順手牽羊，指定 C
    
        Then
        回傳 PlayCardEvent
    """)
    @Test
    public void givenPlayerAHasMinusOneMountButNoQilinBow_WhenPlayerAPlaySnatchOnC_ThenReturnPlayCardEvent() throws Exception {
        Game game = new Game();
        game.initDeck();

        Equipment equipmentA = new Equipment();
        equipmentA.setMinusOne(new RedRabbitHorse(EH5044)); // 有 -1 馬，無武器

        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(equipmentA)
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(
                new Snatch(SS3016), new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.張飛))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.關羽))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.趙雲))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        List<Player> players = asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        // When
        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), SS3016.getCardId(), playerC.getId(), PlayType.ACTIVE.getPlayType());

        // Then
        assertTrue(events.stream().anyMatch(e -> e instanceof PlayCardEvent));
    }

    @DisplayName("""
        Given
        玩家ABCD
        B有一麒麟弓，五張手牌，第一張是 KILL
        第二張到五張是 Peach
        A有順手牽羊、沒有其他手牌
    
        When
        A 出順手牽羊，指定 B
        A 指定 index 0
    
        Then
        B 的手牌沒有 KILL ，並剩下四張
        A 手牌有 KILL
    """)
    @Test
    public void givenBHasWeaponAndFiveCards_WhenAPlaySnatchTargetingBAndSelectsIndex0_ThenAKeepsKillAndBHasFourPeachCards() throws Exception {
        Game game = new Game();
        game.initDeck();

        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();
        playerA.getHand().addCardToHand(List.of(new Snatch(SS3016)));

        Equipment equipmentB = new Equipment();
        equipmentB.setWeapon(new QilinBowCard(EH5031));
        Player playerB = PlayerBuilder
                .construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withEquipment(equipmentB)
                .withGeneralCard(new GeneralCard(General.張飛))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(
                new Kill(BS8008),
                new Peach(BH3029),
                new Peach(BH4030),
                new Peach(BH6032),
                new Peach(BH7033)
        ));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.關羽))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.趙雲))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        List<Player> players = asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        // When
        game.playerPlayCard(playerA.getId(), SS3016.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        game.useSnatchEffect(playerA.getId(), playerB.getId(), "", 0);

        // Then
        assertEquals(4, playerB.getHandSize());
        assertFalse(playerB.getHand().getCards().stream().anyMatch(c -> c instanceof Kill));
        assertEquals(1, playerA.getHandSize());
        assertTrue(playerA.getHand().getCards().stream().anyMatch(c -> c instanceof Kill));
    }

    @DisplayName("""
        Given
        玩家ABCD
        B有一麒麟弓，五張手牌，第一張是 KILL
        第二張到五張是 Peach
        A有順手牽羊
    
        When
        A 出順手牽羊，指定 B
        A 指定 index 5
    
        Then
        拋出錯誤
    """)
    @Test
    public void givenBHasFiveCards_WhenAPlaySnatchAndChoosesInvalidIndex5_ThenThrowException() throws Exception {
        Game game = new Game();
        game.initDeck();

        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();
        playerA.getHand().addCardToHand(List.of(new Snatch(SS3016)));

        Equipment equipmentB = new Equipment();
        equipmentB.setWeapon(new QilinBowCard(EH5031));
        Player playerB = PlayerBuilder
                .construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withEquipment(equipmentB)
                .withGeneralCard(new GeneralCard(General.張飛))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(
                new Kill(BS8008),
                new Peach(BH3029),
                new Peach(BH4030),
                new Peach(BH6032),
                new Peach(BH7033)
        ));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.關羽))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.趙雲))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        List<Player> players = asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        // When
        game.playerPlayCard(playerA.getId(), SS3016.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());

        // Then
        assertThrows(IndexOutOfBoundsException.class, () ->
                game.useSnatchEffect(playerA.getId(), playerB.getId(), "", 5));
    }

}
