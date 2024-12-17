package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.events.ContentmentEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.JudgementEvent;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.scrollcard.Contentment;
import com.gaas.threeKingdoms.player.*;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.rolecard.RoleCard;
import com.gaas.threeKingdoms.round.Round;
import com.gaas.threeKingdoms.round.RoundPhase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.Utils.getEvent;
import static com.gaas.threeKingdoms.handcard.PlayCard.BH8034;
import static com.gaas.threeKingdoms.handcard.PlayCard.SH6045;
import static org.junit.jupiter.api.Assertions.*;

public class ContentmentTest {

    @DisplayName("""
                Given
                玩家 A B C D
                A的回合
                A有有樂不思蜀 x 1
                When
                A 出樂不思蜀，指定自己
                Then
                拋出錯誤
            """)
    @Test
    public void givenPlayerABCD_PlayerATurn_PlayerAHasContentment_WhenPlayerAPlaysContentmentAndTargetsSelf_ThenThrowException() throws Exception {
        Game game = new Game();
        game.initDeck();

        // 玩家A的建立
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(new Contentment(SH6045)));

        // 玩家B的建立
        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(new Equipment())
                .build();

        // 玩家C的建立
        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(new Equipment())
                .build();

        // 玩家D的建立
        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(new Equipment())
                .build();

        // 遊戲設置
        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));


        // When & Then
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                game.playerPlayCard(playerA.getId(), SH6045.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType())
        );
    }


    @DisplayName("""
                Given
                玩家ABCD
                A的回合
                A有樂不思蜀x 1
                B的判定區已經有一張樂不思蜀
                When
                A 出樂不思蜀，指定 B
                Then
                拋出錯誤
            """)
    @Test
    public void givenPlayerABCD_PlayerAHasContentment_PlayerBAlreadyHasContentmentInJudgmentArea_WhenPlayerAPlaysContentmentAndTargetsB_ThenThrowException() throws Exception {
        Game game = new Game();
        game.initDeck();

        // 玩家A
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(new Contentment(SH6045)));

        // 玩家B
        Player playerB = PlayerBuilder
                .construct()
                .withId("player-b")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.關羽))
                .withRoleCard(new RoleCard(Role.REBEL))
                .withDelayScrollCards(List.of(new Contentment(SH6045)))
                .withHealthStatus(HealthStatus.ALIVE)
                .build();

        // 玩家C
        Player playerC = PlayerBuilder
                .construct()
                .withId("player-c")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.張飛))
                .withRoleCard(new RoleCard(Role.REBEL))
                .build();

        // 玩家D
        Player playerD = PlayerBuilder
                .construct()
                .withId("player-d")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.趙雲))
                .withRoleCard(new RoleCard(Role.REBEL))
                .build();

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        // When & Then
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                game.playerPlayCard(playerA.getId(), SH6045.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType())
        );
    }

    @DisplayName("""
                Given
                玩家ABCD
                A的回合
                A有樂不思蜀 x 1
                When
                A 出樂不思蜀，指定B
                Then
                B 的判定區有樂不思蜀
            """)
    @Test
    public void givenPlayerABCD_PlayerAHasContentment_WhenPlayerAPlaysContentmentAndTargetsB_ThenPlayerBHasContentmentInJudgmentArea() throws Exception {
        Game game = new Game();
        game.initDeck();

        // 玩家A
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withHealthStatus(HealthStatus.ALIVE)
                .withDelayScrollCards(new ArrayList<>())
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(new Contentment(SH6045)));

        // 玩家B
        Player playerB = PlayerBuilder
                .construct()
                .withId("player-b")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.關羽))
                .withRoleCard(new RoleCard(Role.REBEL))
                .withHealthStatus(HealthStatus.ALIVE)
                .withDelayScrollCards(new ArrayList<>())
                .build();

        // 玩家C
        Player playerC = PlayerBuilder
                .construct()
                .withId("player-c")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.張飛))
                .withRoleCard(new RoleCard(Role.REBEL))
                .build();

        // 玩家D
        Player playerD = PlayerBuilder
                .construct()
                .withId("player-d")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.趙雲))
                .withRoleCard(new RoleCard(Role.REBEL))
                .build();

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        // When 玩家A打出樂不思蜀，指定玩家B
        game.playerPlayCard(playerA.getId(), SH6045.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());

        // Then 判斷玩家B的判定區是否有樂不思蜀
        assertTrue(playerB.getDelayScrollCards().stream()
                .anyMatch(card -> card instanceof Contentment && card.getId().equals(SH6045.getCardId())));
    }

    @DisplayName("""
                Given
                玩家 A B C D
                A的回合
                A有樂不思蜀x 1
                A 出樂不思蜀，指定 B

                When
                A 結束回合
                B 的回合，系統進行樂不思蜀判定，抽出一張紅桃卡

                Then
                B 不能出牌，進入到棄牌階段
                DiscardPhase
            """)
    @Test
    public void givenPlayerABCD_PlayerAHasContentmentAndTargetsB_WhenBJudgesAndDrawsHeartCard_ThenBEnterDiscardPhase() {
        // Given
        Game game = new Game();

        Deck deck = new Deck();
        deck.add(Arrays.asList(new Peach(BH8034), new Peach(BH8034), new Peach(BH8034), new Peach(BH8034), new Peach(BH8034)));
        game.setDeck(deck);

        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .withHand(new Hand())
                .withDelayScrollCards(new ArrayList<>())
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(new Contentment(SH6045)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(1))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withDelayScrollCards(new ArrayList<>())
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(new Peach(BH8034), new Peach(BH8034), new Peach(BH8034), new Peach(BH8034)));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withHealthStatus(HealthStatus.ALIVE)
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withDelayScrollCards(new ArrayList<>())
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withHealthStatus(HealthStatus.ALIVE)
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withDelayScrollCards(new ArrayList<>())
                .build();

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.setCurrentRound(new Round(playerA));
        game.enterPhase(new Normal(game));

        // A 出 Contentment 指定 B
        game.playerPlayCard(playerA.getId(), SH6045.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());

        // When
        List<DomainEvent> events = game.finishAction(playerA.getId());

        // Then
        JudgementEvent judgementEvent = getEvent(events, JudgementEvent.class).orElseThrow(RuntimeException::new);
        ContentmentEvent contentmentEvent = getEvent(events, ContentmentEvent.class).orElseThrow(RuntimeException::new);
        assertNotNull(judgementEvent);
        assertTrue(contentmentEvent.isSuccess()); // 樂不思蜀判定成功 (紅桃卡)
        assertEquals(RoundPhase.Discard, game.getCurrentRound().getRoundPhase()); // 進入棄牌階段
    }


}
