package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
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

    }

}
