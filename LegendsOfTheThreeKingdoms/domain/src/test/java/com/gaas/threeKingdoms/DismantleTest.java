package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.behavior.behavior.DismantleBehavior;
import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.RedRabbitHorse;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.QilinBowCard;
import com.gaas.threeKingdoms.handcard.scrollcard.BarbarianInvasion;
import com.gaas.threeKingdoms.handcard.scrollcard.Contentment;
import com.gaas.threeKingdoms.handcard.scrollcard.Dismantle;
import com.gaas.threeKingdoms.handcard.scrollcard.Duel;
import com.gaas.threeKingdoms.player.*;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.rolecard.RoleCard;
import com.gaas.threeKingdoms.round.Round;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static com.gaas.threeKingdoms.handcard.PlayCard.SSA001;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class DismantleTest {

    @DisplayName("""
        Given
        玩家ABCD
        B沒有裝備，沒有手牌
        A有過河拆橋
        When
        A 出過河拆橋，指定 B
        Then
        拋出錯誤
    """)
    @Test
    public void givenPlayerABCD_PlayerBHasNoEquipmentsOrCards_PlayerAHasDismantle_WhenPlayerAPlaysDismantleAndTargetsB_ThenThrowException() throws Exception {
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

        playerA.getHand().addCardToHand(Arrays.asList(new Dismantle(SS4004), new Dodge(BH2028), new Dodge(BHK039), new Duel(SSA001)));

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
        Assertions.assertThrows(IllegalArgumentException.class, () ->  game.playerPlayCard(playerA.getId(), SS4004.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType()));

    }



    @DisplayName("""
        Given
        玩家ABCD
        B有一張手牌
        A有過河拆橋
        When
        A 出過河拆橋，指定 B
        Then
        回傳 PlayCardEvent
            """)
    @Test
    public void givenPlayerABCD_PlayerBHasOneCard_PlayerAHasDismantle_WhenPlayerAPlaysDismantleAndTargetsPlayerB_ThenReturnPlayCardEvent() throws Exception {
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

        playerA.getHand().addCardToHand(Arrays.asList(new Dismantle(SS4004), new Dodge(BH2028), new Dodge(BHK039), new Duel(SSA001)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(new Equipment())
                .build();
        playerB.getHand().addCardToHand(Arrays.asList(new Dodge(BH2028)));

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
        List<DomainEvent> events =  game.playerPlayCard(playerA.getId(), SS4004.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        assertTrue(events.stream().anyMatch(event -> event instanceof PlayCardEvent));
    }

    @DisplayName("""
        Given
        玩家ABCD
        B有一麒麟弓，五張手牌，第一張是 KILL
        第二張到五張是 Peach
        A有過河拆橋
        When
        A 出過河拆橋，指定 B
        A 指定 index 0
        Then
        B 的手牌沒有 KILL ，並剩下四張
            """)
    @Test
    public void givenPlayerABCD_PlayerBHasKylinBowAndFiveCards_FirstCardIsKillOthersArePeach_PlayerAHasDismantle_WhenPlayerAPlaysDismantleAndTargetsPlayerBWithIndex0_ThenPlayerBDoesNotHaveKillAndHasFourCardsLeft() throws Exception {
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

        playerA.getHand().addCardToHand(Arrays.asList(new Dismantle(SS4004), new Dodge(BH2028), new Dodge(BHK039), new Duel(SSA001)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(new Equipment())
                .build();
        playerB.getEquipment().setWeapon(new QilinBowCard(EH5031));
        playerB.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new Peach(BH3029), new Peach(BH3029), new Peach(BH3029)));

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
        List<DomainEvent> events =  game.playerPlayCard(playerA.getId(), SS4004.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        assertTrue(events.stream().anyMatch(event -> event instanceof PlayCardEvent));
        assertFalse(events.stream().anyMatch(event -> event instanceof AskKillEvent));
        events =  game.useDismantleEffect(playerA.getId(), playerB.getId(), "", 0);
        assertTrue(events.stream().anyMatch(event -> event instanceof DismantleEvent));
        assertEquals(4, playerB.getHandSize());
        assertFalse(playerB.getHand().getCards().stream().anyMatch(card -> card instanceof Kill));
        assertEquals("player-a", game.getActivePlayer().getId());
        assertFalse(game.getTopBehavior().stream().anyMatch(behavior -> behavior instanceof DismantleBehavior));
    }

    @DisplayName("""
        Given
        玩家ABCD
        B有一麒麟弓，五張手牌，第一張是 KILL
        第二張到五張是 Peach
        A有過河拆橋
        When
        A 出過河拆橋，指定 B
        A 指定 index 5
        Then
        拋出錯誤
            """)
    @Test
    public void givenPlayerABCD_PlayerBHasKylinBowAndFiveCards_FirstCardIsKillOthersArePeach_PlayerAHasDismantle_WhenPlayerAPlaysDismantleAndTargetsPlayerBWithIndex5_ThenThrowError() throws Exception {
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

        playerA.getHand().addCardToHand(Arrays.asList(new Dismantle(SS4004), new Dodge(BH2028), new Dodge(BHK039), new Duel(SSA001)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(new Equipment())
                .build();
        playerB.getEquipment().setWeapon(new QilinBowCard(EH5031));
        playerB.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new Peach(BH3029), new Peach(BH3029), new Peach(BH3029)));

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
        List<DomainEvent> events =  game.playerPlayCard(playerA.getId(), SS4004.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        Assertions.assertThrows(IllegalArgumentException.class, () -> game.useDismantleEffect(playerA.getId(), playerB.getId(), "", 5));

    }

    @DisplayName("""
        Given
        玩家ABCD
        B有裝備麒麟弓、赤兔馬，沒有手牌
        A有過河拆橋
        A 出過河拆橋，指定 B
        When
        A 選擇麒麟弓
        Then
        B 裝備剩 赤兔馬
            """)
    @Test
    public void givenPlayerABCD_PlayerBHasEquippedKylinBowAndChituNoHandCards_PlayerAHasDismantle_WhenPlayerAPlaysDismantleAndChoosesKylinBow_ThenPlayerBHasOnlyChituEquipped() throws Exception {
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

        playerA.getHand().addCardToHand(Arrays.asList(new Dismantle(SS4004), new Dodge(BH2028), new Dodge(BHK039), new Duel(SSA001)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(new Equipment())
                .build();
        playerB.getEquipment().setWeapon(new QilinBowCard(EH5031));
        playerB.getEquipment().setMinusOne(new RedRabbitHorse(EH5044));

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
        List<DomainEvent> events =  game.playerPlayCard(playerA.getId(), SS4004.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        assertTrue(events.stream().anyMatch(event -> event instanceof PlayCardEvent));
        assertFalse(events.stream().anyMatch(event -> event instanceof AskKillEvent));
        events =  game.useDismantleEffect(playerA.getId(), playerB.getId(), EH5031.getCardId(), null);
        assertTrue(events.stream().anyMatch(event -> event instanceof DismantleEvent));
        assertEquals("player-a", game.getActivePlayer().getId());
        assertNull(playerB.getEquipment().getWeapon());
        assertEquals("EH5044", playerB.getEquipmentMinusOneMountsCard().getId());
        assertEquals("player-a", game.getActivePlayer().getId());
        assertFalse(game.getTopBehavior().stream().anyMatch(behavior -> behavior instanceof DismantleBehavior));
    }

    @DisplayName("""
        Given
        玩家 A B C D
        A 的回合
        A 有樂不思蜀 x 1
        A 出樂不思蜀，指定 C
        A 結束回合
    
        When
        B 的回合，出過河拆橋，指定 C
    
        Then
        C 判定區沒有過河拆橋
    """)
    @Test
    public void givenPlayerABCD_PlayerAHasContentmentAndPlaysItOnC_WhenPlayerBPlaysDismantleOnC_ThenCJudgmentAreaHasNoDismantle() throws Exception {
        Game game = new Game();
        game.initDeck();
        game.setDeck(new Deck(List.of(new BarbarianInvasion(SSK013), new Dismantle(SS3003))));
        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();
        playerA.getHand().addCardToHand(Arrays.asList(new Contentment(SS6006), new Dodge(BH2028), new Dodge(BHK039), new Duel(SSA001)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.REBEL))
                .withEquipment(new Equipment())
                .build();
        playerB.getHand().addCardToHand(Arrays.asList(new Dismantle(SS4004)));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.REBEL))
                .withEquipment(new Equipment())
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.REBEL))
                .withEquipment(new Equipment())
                .build();

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        // A 出樂不思蜀，指定 C
        game.playerPlayCard(playerA.getId(), SS6006.getCardId(), playerC.getId(), PlayType.ACTIVE.getPlayType());
        game.finishAction(playerA.getId());

        // B 出過河拆橋，指定 C
        game.playerPlayCard(playerB.getId(), SS4004.getCardId(), playerC.getId(), PlayType.ACTIVE.getPlayType());
        List<DomainEvent> events = game.useDismantleEffect(playerB.getId(), playerC.getId(), SS6006.getCardId(), null);
        assertTrue(events.stream().anyMatch(event -> event instanceof DismantleEvent));
        // Assert that C has no dismantle in the judgment area
        assertFalse(playerC.hasAnyContentmentCard());
    }

}
