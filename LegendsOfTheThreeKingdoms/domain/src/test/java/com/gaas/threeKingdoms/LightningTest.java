package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.scrollcard.Dismantle;
import com.gaas.threeKingdoms.handcard.scrollcard.Lightning;
import com.gaas.threeKingdoms.handcard.scrollcard.ScrollCard;
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

import static com.gaas.threeKingdoms.Utils.getEvent;
import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LightningTest {

    @DisplayName("""
                Given
                玩家ABCD
                A的回合
                A有閃電 x 1

                When
                A 出閃電

                Then
                A 的判定牌裡有閃電
            """)
    @Test
    public void givenPlayerABCD_PlayerATurn_PlayerAHasLightning_WhenPlayerAPlaysLightning_ThenPlayerAHasLightningInJudgmentArea() {
        // Given
        Game game = new Game();
        game.initDeck();

        // 玩家A的建立
        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withHealthStatus(HealthStatus.ALIVE)
                .withDelayScrollCards(new Stack<>())
                .build();
        playerA.getHand().addCardToHand(Arrays.asList(new Lightning(SSA014)));

        // 玩家B的建立
        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.關羽))
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withEquipment(new Equipment())
                .build();

        // 玩家C的建立
        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.張飛))
                .withRoleCard(new RoleCard(Role.REBEL))
                .withEquipment(new Equipment())
                .build();

        // 玩家D的建立
        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.趙雲))
                .withRoleCard(new RoleCard(Role.REBEL))
                .withEquipment(new Equipment())
                .build();

        // 遊戲設置
        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        // When
        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), SSA014.getCardId(), "", PlayType.ACTIVE.getPlayType());

        // Then
        assertTrue(playerA.getDelayScrollCards().stream()
                .anyMatch(card -> card instanceof Lightning && card.getId().equals(SSA014.getCardId())));

        LightningTransferredEvent lightningTransferredEvent = getEvent(events, LightningTransferredEvent.class).orElseThrow(RuntimeException::new);
        assertEquals("player-a", lightningTransferredEvent.getSourcePlayerId());
        assertEquals("player-a", lightningTransferredEvent.getTargetPlayerId());
        assertEquals("SSA014", lightningTransferredEvent.getCardId());
    }

    @DisplayName("""
        Given
        玩家ABCD
        A的回合
        A的判定牌閃電 x 1
    
        When
        A 出閃電
    
        Then
                A 的判定牌裡有一張閃電
                因為不可以出第二張閃電
                拋出例外錯誤
    """)
    @Test
    public void givenPlayerABCD_PlayerATurn_PlayerAHasOneLightningInJudgmentArea_WhenPlayerAPlaysLightning_ThenPlayerAHasTwoLightningsInJudgmentArea() {
        // Given
        Game game = new Game();
        game.initDeck();
        Stack<ScrollCard> delayScrollCards = new Stack<>();
        delayScrollCards.addAll(List.of(new Lightning(SSA014)));
        // 玩家 A 設定
        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withHealthStatus(HealthStatus.ALIVE)
                .withDelayScrollCards(delayScrollCards)
                .build();
        playerA.getHand().addCardToHand(Arrays.asList(new Lightning(SSA014)));

        // 玩家 B 設定
        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.關羽))
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withEquipment(new Equipment())
                .build();

        // 玩家 C 設定
        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.張飛))
                .withRoleCard(new RoleCard(Role.REBEL))
                .withEquipment(new Equipment())
                .build();

        // 玩家 D 設定
        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.趙雲))
                .withRoleCard(new RoleCard(Role.REBEL))
                .withEquipment(new Equipment())
                .build();

        // 設置遊戲狀態
        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        // When
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                game.playerPlayCard(playerA.getId(), SSA014.getCardId(), "", PlayType.ACTIVE.getPlayType())
        );

        // Then
        assertEquals(1, playerA.getDelayScrollCards().stream()
                .filter(card -> card instanceof Lightning)
                .count());
    }

    @DisplayName("""
        Given
        玩家ABCD
        A的回合
        A有閃電 x 1
        A 出閃電
    
        When
        A 的回合結束，進入 B 的回合
    
        Then
        B 的回合開始
        A 的判定牌仍然有閃電
        B 沒有判定牌
    """)
    @Test
    public void givenPlayerABCD_PlayerATurn_PlayerAHasLightning_PlayerAPlaysLightning_WhenATurnEndsAndBTurnStarts_ThenBTurnStarts_PlayerAStillHasLightning_PlayerBHasNoJudgmentCards() {
        // Given
        Game game = new Game();
        game.initDeck();

        // 玩家 A 設定
        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withHealthStatus(HealthStatus.ALIVE)
                .build();
        playerA.getHand().addCardToHand(Arrays.asList(new Lightning(SSA014)));

        // 玩家 B 設定
        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.關羽))
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withEquipment(new Equipment())
                .withDelayScrollCards(new Stack<>()) // 沒有判定牌
                .build();

        // 玩家 C 設定
        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.張飛))
                .withRoleCard(new RoleCard(Role.REBEL))
                .withEquipment(new Equipment())
                .build();

        // 玩家 D 設定
        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.趙雲))
                .withRoleCard(new RoleCard(Role.REBEL))
                .withEquipment(new Equipment())
                .build();

        // 設置遊戲狀態
        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        // When: A 打出閃電
        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), SSA014.getCardId(), "", PlayType.ACTIVE.getPlayType());

        // A 結束回合，進入 B 的回合
        game.finishAction("player-a");

        // Then: 確認 B 的回合開始，A 仍然有閃電，B 沒有判定牌
        assertEquals("player-b", game.getCurrentRound().getActivePlayer().getId());
        assertEquals(1, playerA.getDelayScrollCards().stream()
                .filter(card -> card instanceof Lightning)
                .count());
        assertTrue(playerB.getDelayScrollCards().isEmpty());
    }

    @DisplayName("""
    Given
    玩家ABCD
    A的回合
    A出閃電
    ABC 結束回合

    When
    D 結束回合
    A 回合進行判定，判定為紅心

    Then
    B 的判定牌裡有閃電
    A 抽牌 event
    A 的回合
    ABCD 收到閃電轉移的 event
    ABCD 收到判定結束的 event
""")
    @Test
    public void givenPlayerABCD_PlayerAPlaysLightning_ABCEndTurns_WhenPlayerDEndsTurn_AJudgmentPhaseHearts_ThenBHasLightning_AReceivesDrawEvent_AHasTurn_ABCDReceiveLightningTransferEvent_ABCDReceiveJudgmentEndEvent() {
        // Given
        Game game = new Game();
        game.initDeck();
        Deck deck = new Deck(
                List.of(
                        new Peach(BH3029), new Peach(BH3029), new Peach(BH3029),new Peach(BH3029), new Peach(BH3029),
                        new Peach(BH3029), new Peach(BH3029), new Peach(BH3029),new Peach(BH3029), new Peach(BH3029),
                        new Peach(BH3029), new Peach(BH3029), new Peach(BH3029),new Peach(BH3029), new Peach(BH3029)
                )
        );
        game.setDeck(deck);
        // 玩家 A 設定
        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withHealthStatus(HealthStatus.ALIVE)
                .withDelayScrollCards(new Stack<>()) // 尚未有閃電
                .build();
        playerA.getHand().addCardToHand(Arrays.asList(new Lightning(SSA014)));

        // 玩家 B 設定
        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.關羽))
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withEquipment(new Equipment())
                .withDelayScrollCards(new Stack<>()) // 尚未有判定牌
                .build();

        // 玩家 C 設定
        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.張飛))
                .withRoleCard(new RoleCard(Role.REBEL))
                .withEquipment(new Equipment())
                .build();

        // 玩家 D 設定
        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.趙雲))
                .withRoleCard(new RoleCard(Role.REBEL))
                .withEquipment(new Equipment())
                .build();

        // 設置遊戲狀態
        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        // A 出閃電
        game.playerPlayCard(playerA.getId(), SSA014.getCardId(), "", PlayType.ACTIVE.getPlayType());

        // ABC 結束回合
        game.finishAction(playerA.getId());
        game.finishAction(playerB.getId());
        game.finishAction(playerC.getId());

        // When: D 結束回合，進入 A 的回合，判定為紅桃
        List<DomainEvent> events = game.finishAction(playerD.getId());

        // Then:
        // 確認 B 接收到閃電
        assertEquals(1, playerB.getDelayScrollCards().stream()
                .filter(card -> card instanceof Lightning)
                .count());

        // A 收到抽牌事件
        assertTrue(events.stream().anyMatch(event -> event instanceof DrawCardEvent));

        // 確認 A 的回合
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());

        // 確認 ABCD 收到閃電轉移事件
        assertTrue(events.stream().anyMatch(event -> event instanceof LightningTransferredEvent));

        // 確認 ABCD 收到判定結束事件
        assertTrue(events.stream().anyMatch(event -> event instanceof JudgementEvent));
    }

    @DisplayName("""
        Given
        玩家ABCD
        A的回合
        A hp = 4
        A出閃電
        ABC 結束回合
    
        When
        D 結束回合
        A 回合進行判定，判定為黑桃 3
    
        Then
        A hp = 1
        ABCD 收到判定結束的 event
        A 的抽牌 event
        A 的回合
    """)
    @Test
    public void givenPlayerABCD_PlayerATurn_AHas4Hp_PlayerAPlaysLightning_ABCEndTurns_WhenPlayerDEndsTurn_AJudgmentPhaseSpade2_ThenAHas1Hp_ABCDReceiveJudgmentEndEvent_AReceivesDrawEvent_AHasTurn() {
        // Given
        Game game = new Game();
        game.initDeck();
        Deck deck = new Deck(
                List.of(
                        new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003),
                        new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003),
                        new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003)
                )
        );
        game.setDeck(deck);

        // 玩家 A 設定
        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))  // A 初始 4 HP
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withHealthStatus(HealthStatus.ALIVE)
                .withDelayScrollCards(new Stack<>()) // 尚未有閃電
                .build();
        playerA.getHand().addCardToHand(Arrays.asList(new Lightning(SSA014)));

        // 玩家 B 設定
        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.關羽))
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withEquipment(new Equipment())
                .withDelayScrollCards(new Stack<>()) // 尚未有判定牌
                .build();

        // 玩家 C 設定
        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.張飛))
                .withRoleCard(new RoleCard(Role.REBEL))
                .withEquipment(new Equipment())
                .build();

        // 玩家 D 設定
        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.趙雲))
                .withRoleCard(new RoleCard(Role.REBEL))
                .withEquipment(new Equipment())
                .build();

        // 設置遊戲狀態
        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        // A 出閃電
        game.playerPlayCard(playerA.getId(), SSA014.getCardId(), "", PlayType.ACTIVE.getPlayType());

        // ABC 結束回合
        game.finishAction(playerA.getId());
        game.finishAction(playerB.getId());
        game.finishAction(playerC.getId());

        // When: D 結束回合，進入 A 的回合，判定為黑桃 2
        List<DomainEvent> events = game.finishAction(playerD.getId());

        // Then:
        // 確認 A HP 降至 1
        assertEquals(1, playerA.getBloodCard().getHp());

        // 確認 ABCD 收到判定結束事件
        assertTrue(events.stream().anyMatch(event -> event instanceof JudgementEvent));

        // A 收到抽牌事件
        assertTrue(events.stream().anyMatch(event -> event instanceof DrawCardEvent));

        // 確認 A 仍是當前回合玩家
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
    }

    @DisplayName("""
        Given
        玩家ABCD
        A的回合
        A hp = 3
        A出閃電
        A 沒有桃
        ABC 結束回合
    
        When
        D 結束回合
        A 回合進行判定，判定為黑桃 3
    
        Then
        A hp = 0
        要求 A 出桃
    """)
    @Test
    public void givenPlayerABCD_PlayerATurn_AHas3Hp_PlayerAPlaysLightning_AHasNoPeach_ABCEndTurns_WhenPlayerDEndsTurn_AJudgmentPhaseSpade3_ThenAHas0Hp_AskAPlayPeach() {
        // Given
        Game game = new Game();
        game.initDeck();
        Deck deck = new Deck(
                List.of(
                        new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003),
                        new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003),
                        new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003)
                )
        );
        game.setDeck(deck);

        // 玩家 A 設定
        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withHand(new Hand()) // 沒有桃
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(3))  // A 初始 3 HP
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withHealthStatus(HealthStatus.ALIVE)
                .withDelayScrollCards(new Stack<>()) // 尚未有閃電
                .build();
        playerA.getHand().addCardToHand(Arrays.asList(new Lightning(SSA014)));

        // 玩家 B 設定
        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.關羽))
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withEquipment(new Equipment())
                .withDelayScrollCards(new Stack<>()) // 尚未有判定牌
                .build();

        // 玩家 C 設定
        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.張飛))
                .withRoleCard(new RoleCard(Role.REBEL))
                .withEquipment(new Equipment())
                .build();

        // 玩家 D 設定
        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.趙雲))
                .withRoleCard(new RoleCard(Role.REBEL))
                .withEquipment(new Equipment())
                .build();

        // 設置遊戲狀態
        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        // A 出閃電
        game.playerPlayCard(playerA.getId(), SSA014.getCardId(), "", PlayType.ACTIVE.getPlayType());

        // ABC 結束回合
        game.finishAction(playerA.getId());
        game.finishAction(playerB.getId());
        game.finishAction(playerC.getId());

        // When: D 結束回合，進入 A 的回合，判定為黑桃 3
        List<DomainEvent> events = game.finishAction(playerD.getId());

        // Then:
        // 確認 A HP 降至 0
        assertEquals(0, playerA.getBloodCard().getHp());

        // 要求 A 出桃的事件
        assertTrue(events.stream().anyMatch(event -> event instanceof AskPeachEvent));
    }

    @DisplayName("""
        Given
        玩家ABCD
        A的回合
        A hp = 3
        A出閃電
        A 有一個桃
        ABC 結束回合
        D 結束回合
        A 回合進行判定，判定為黑桃 3
    
        When
        A hp = 0
        要求 A 出桃
        A 出桃
    
        Then
        A hp = 1
        ABCD 收到判定結束的 event
        A 抽牌
        A 的回合
    """)
    @Test
    public void givenPlayerABCD_PlayerATurn_AHas3Hp_PlayerAPlaysLightning_AHasOnePeach_ABCDEndTurns_AJudgmentPhaseSpade3_WhenAHas0Hp_AskAPlayPeach_APlaysPeach_ThenAHas1Hp_ABCDReceiveJudgmentEndEvent_AReceivesDrawEvent_AHasTurn() {
        // Given
        Game game = new Game();
        game.initDeck();
        Deck deck = new Deck(
                List.of(
                        new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003),
                        new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003),
                        new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003)
                )
        );
        game.setDeck(deck);

        // 玩家 A 設定
        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(3))  // A 初始 3 HP
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withHealthStatus(HealthStatus.ALIVE)
                .withDelayScrollCards(new Stack<>()) // 尚未有閃電
                .build();
        playerA.getHand().addCardToHand(Arrays.asList(new Lightning(SSA014), new Peach(BH3029))); // A 有一個桃

        // 玩家 B 設定
        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.關羽))
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withEquipment(new Equipment())
                .withDelayScrollCards(new Stack<>()) // 尚未有判定牌
                .build();

        // 玩家 C 設定
        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.張飛))
                .withRoleCard(new RoleCard(Role.REBEL))
                .withEquipment(new Equipment())
                .build();

        // 玩家 D 設定
        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.趙雲))
                .withRoleCard(new RoleCard(Role.REBEL))
                .withEquipment(new Equipment())
                .build();

        // 設置遊戲狀態
        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        // A 出閃電
        game.playerPlayCard(playerA.getId(), SSA014.getCardId(), "", PlayType.ACTIVE.getPlayType());

        // ABC 結束回合
        game.finishAction(playerA.getId());
        game.finishAction(playerB.getId());
        game.finishAction(playerC.getId());

        // D 結束回合
        List<DomainEvent> events = game.finishAction(playerD.getId());

        // When: A 判定黑桃 3，HP 變為 0，系統要求 A 出桃
        assertEquals(0, playerA.getBloodCard().getHp());
        assertTrue(events.stream().anyMatch(event -> event instanceof AskPeachEvent));

        // A 使用桃
        List<DomainEvent> peachEvents = game.playerPlayCard(playerA.getId(), BH3029.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        // Then:
        // 確認 A HP 回復至 1
        assertEquals(1, playerA.getBloodCard().getHp());

        // 確認 A B C D 收到判定結束事件
        assertTrue(peachEvents.stream().anyMatch(event -> event instanceof JudgementEvent));

        // A 收到抽牌事件
        assertTrue(peachEvents.stream().anyMatch(event -> event instanceof DrawCardEvent));

        // 確認 A 仍是當前回合玩家
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
    }

    @DisplayName("""
        Given
        玩家ABCD
        A的回合
        A hp = 2
        A出閃電
        A 有兩個桃
        B 有一個桃
        ABC 結束回合
        D 結束回合
        A 回合進行判定，判定為黑桃 3
    
        When
        A hp = -1
        要求 A 出桃
        A 出桃
    
        Then
        A hp = 0
        要求 A 出桃
    """)
    @Test
    public void givenPlayerABCD_PlayerATurn_AHas2Hp_PlayerAPlaysLightning_AHasTwoPeach_BHasOnePeach_ABCDEndTurns_AJudgmentPhaseSpade3_WhenAHasMinus1Hp_AskAPlayPeach_APlaysPeach_ThenAHas0Hp_AskAPlayPeachAgain() {
        // Given
        Game game = new Game();
        game.initDeck();
        Deck deck = new Deck(
                List.of(
                        new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003),
                        new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003)
                )
        );
        game.setDeck(deck);

        // 玩家 A 設定
        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(2))  // A 初始 2 HP
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withHealthStatus(HealthStatus.ALIVE)
                .withDelayScrollCards(new Stack<>()) // 尚未有閃電
                .build();
        playerA.getHand().addCardToHand(Arrays.asList(new Lightning(SSA014), new Peach(BH3029), new Peach(BH3029))); // A 有兩個桃

        // 玩家 B 設定
        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.關羽))
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withEquipment(new Equipment())
                .withDelayScrollCards(new Stack<>()) // 尚未有判定牌
                .build();
        playerB.getHand().addCardToHand(Arrays.asList(new Peach(BH3029))); // B 有一個桃

        // 玩家 C 設定
        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.張飛))
                .withRoleCard(new RoleCard(Role.REBEL))
                .withEquipment(new Equipment())
                .build();

        // 玩家 D 設定
        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.趙雲))
                .withRoleCard(new RoleCard(Role.REBEL))
                .withEquipment(new Equipment())
                .build();

        // 設置遊戲狀態
        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        // A 出閃電
        game.playerPlayCard(playerA.getId(), SSA014.getCardId(), "", PlayType.ACTIVE.getPlayType());

        // ABC 結束回合
        game.finishAction(playerA.getId());
        game.finishAction(playerB.getId());
        game.finishAction(playerC.getId());

        // D 結束回合
        List<DomainEvent> events = game.finishAction(playerD.getId());

        // When: A 判定黑桃 3，HP 變為 -1，系統要求 A 出桃
        assertEquals(-1, playerA.getBloodCard().getHp());
        assertTrue(events.stream().anyMatch(event -> event instanceof AskPeachEvent));

        // A 使用桃
        List<DomainEvent> firstPeachEvents = game.playerPlayCard(playerA.getId(), BH3029.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        // Then:
        // 確認 A HP 回復至 0
        assertEquals(0, playerA.getBloodCard().getHp());

        // 再次觸發要求 A 出桃的事件
        assertTrue(firstPeachEvents.stream().anyMatch(event -> event instanceof AskPeachEvent));
    }

    @DisplayName("""
        Given
        玩家ABCD
        A的回合
        A hp = 2
        A出閃電
        A 有兩個桃
        B 有一個桃
        ABC 結束回合
        D 結束回合
        A 回合進行判定，判定為黑桃 3
    
        When
        A hp = -1
        要求 A 出桃
        A 出桃
        要求 A 出桃
        A 出桃
    
        Then
        A hp = 1
        ABCD 收到判定結束的 event
        A 抽牌
        A 的回合
    """)
    @Test
    public void givenPlayerABCD_PlayerATurn_AHas2Hp_PlayerAPlaysLightning_AHasTwoPeach_BHasOnePeach_ABCDEndTurns_AJudgmentPhaseSpade3_WhenAHasMinus1Hp_AskAPlayPeach_APlaysPeach_AskAgain_APlaysPeach_ThenAHas1Hp_ABCDReceiveJudgmentEndEvent_AReceivesDrawEvent_AHasTurn() {
        // Given
        Game game = new Game();
        game.initDeck();
        Deck deck = new Deck(
                List.of(
                        new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003),
                        new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003)
                )
        );
        game.setDeck(deck);

        // 玩家 A 設定
        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(2))  // A 初始 2 HP
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withHealthStatus(HealthStatus.ALIVE)
                .withDelayScrollCards(new Stack<>()) // 尚未有閃電
                .build();
        playerA.getHand().addCardToHand(Arrays.asList(new Lightning(SSA014), new Peach(BH3029), new Peach(BH3029))); // A 有兩個桃

        // 玩家 B 設定
        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.關羽))
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withEquipment(new Equipment())
                .withDelayScrollCards(new Stack<>()) // 尚未有判定牌
                .build();
        playerB.getHand().addCardToHand(Arrays.asList(new Peach(BH3029))); // B 有一個桃

        // 玩家 C 設定
        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.張飛))
                .withRoleCard(new RoleCard(Role.REBEL))
                .withEquipment(new Equipment())
                .build();

        // 玩家 D 設定
        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.趙雲))
                .withRoleCard(new RoleCard(Role.REBEL))
                .withEquipment(new Equipment())
                .build();

        // 設置遊戲狀態
        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        // A 出閃電
        game.playerPlayCard(playerA.getId(), SSA014.getCardId(), "", PlayType.ACTIVE.getPlayType());

        // ABC 結束回合
        game.finishAction(playerA.getId());
        game.finishAction(playerB.getId());
        game.finishAction(playerC.getId());

        // D 結束回合
        List<DomainEvent> events = game.finishAction(playerD.getId());

        // When: A 判定黑桃 3，HP 變為 -1，系統要求 A 出桃
        assertEquals(-1, playerA.getBloodCard().getHp());
        assertTrue(events.stream().anyMatch(event -> event instanceof AskPeachEvent));

        // A 使用第一張桃
        List<DomainEvent> firstPeachEvents = game.playerPlayCard(playerA.getId(), BH3029.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        // 再次要求 A 出桃
        assertTrue(firstPeachEvents.stream().anyMatch(event -> event instanceof AskPeachEvent));

        // A 使用第二張桃
        List<DomainEvent> secondPeachEvents = game.playerPlayCard(playerA.getId(), BH3029.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        // Then:
        // 確認 A HP 回復至 1
        assertEquals(1, playerA.getBloodCard().getHp());

        // 確認 ABCD 收到判定結束事件
        assertTrue(secondPeachEvents.stream().anyMatch(event -> event instanceof JudgementEvent));

        // A 收到抽牌事件
        assertTrue(secondPeachEvents.stream().anyMatch(event -> event instanceof DrawCardEvent));

        // 確認 A 仍是當前回合玩家
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
    }

    @DisplayName("""
        Given
        玩家ABCD
        A的回合
        A hp = 2
        A出閃電
        A 有兩個桃
        B 有一個桃
        ABC 結束回合
        D 結束回合
        A 回合進行判定，判定為黑桃 3
    
        When
        A hp = -1
        要求 A 出桃
        A 出桃
        要求 A 出桃
        A 出 skip
        要求 B 出桃
        B 出桃
    
        Then
        A hp = 1
        ABCD 收到判定結束的 event
        A 抽牌
        A 的回合
    """)
    @Test
    public void givenPlayerABCD_PlayerATurn_AHas2Hp_PlayerAPlaysLightning_AHasTwoPeach_BHasOnePeach_ABCDEndTurns_AJudgmentPhaseSpade3_WhenAHasMinus1Hp_AskAPlayPeach_APlaysPeach_AskAgain_APlaysSkip_AskBPlayPeach_BPlaysPeach_ThenAHas1Hp_ABCDReceiveJudgmentEndEvent_AReceivesDrawEvent_AHasTurn() {
        // Given
        Game game = new Game();
        game.initDeck();
        Deck deck = new Deck(
                List.of(
                        new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003),
                        new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003),
                        new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003)
                )
        );
        game.setDeck(deck);

        // 玩家 A 設定
        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(2))  // A 初始 2 HP
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withHealthStatus(HealthStatus.ALIVE)
                .withDelayScrollCards(new Stack<>()) // 尚未有閃電
                .build();
        playerA.getHand().addCardToHand(Arrays.asList(new Lightning(SSA014), new Peach(BH3029), new Peach(BH3029))); // A 有兩個桃

        // 玩家 B 設定
        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.關羽))
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withEquipment(new Equipment())
                .withDelayScrollCards(new Stack<>()) // 尚未有判定牌
                .build();
        playerB.getHand().addCardToHand(Arrays.asList(new Peach(BH3029))); // B 有一個桃

        // 玩家 C 設定
        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.張飛))
                .withRoleCard(new RoleCard(Role.REBEL))
                .withEquipment(new Equipment())
                .build();

        // 玩家 D 設定
        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.趙雲))
                .withRoleCard(new RoleCard(Role.REBEL))
                .withEquipment(new Equipment())
                .build();

        // 設置遊戲狀態
        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        // A 出閃電
        game.playerPlayCard(playerA.getId(), SSA014.getCardId(), "", PlayType.ACTIVE.getPlayType());

        // ABC 結束回合
        game.finishAction(playerA.getId());
        game.finishAction(playerB.getId());
        game.finishAction(playerC.getId());

        // D 結束回合
        List<DomainEvent> events = game.finishAction(playerD.getId());

        // When: A 判定黑桃 3，HP 變為 -1，系統要求 A 出桃
        assertEquals(-1, playerA.getBloodCard().getHp());
        assertTrue(events.stream().anyMatch(event -> event instanceof AskPeachEvent));

        // A 使用第一張桃
        List<DomainEvent> firstPeachEvents = game.playerPlayCard(playerA.getId(), BH3029.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        // 再次要求 A 出桃
        assertTrue(firstPeachEvents.stream().anyMatch(event -> event instanceof AskPeachEvent));

        // A 選擇 skip，不使用第二張桃
        List<DomainEvent> skipPeachEvents = game.playerPlayCard(playerA.getId(), "", playerA.getId(), PlayType.SKIP.getPlayType());

        // 系統要求 B 出桃
        assertTrue(skipPeachEvents.stream().anyMatch(event -> event instanceof AskPeachEvent));

        // B 使用桃
        List<DomainEvent> bPeachEvents = game.playerPlayCard(playerB.getId(), BH3029.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        // Then:
        // 確認 A HP 回復至 1
        assertEquals(1, playerA.getBloodCard().getHp());

        // 確認 ABCD 收到判定結束事件
        assertTrue(bPeachEvents.stream().anyMatch(event -> event instanceof JudgementEvent));

        // A 收到抽牌事件
        assertTrue(bPeachEvents.stream().anyMatch(event -> event instanceof DrawCardEvent));

        // 確認 A 仍是當前回合玩家
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
    }

    @DisplayName("""
        Given
        玩家ABCD
        A的回合
        A hp = 2
        A 出閃電
        ABCD 沒有桃
        ABC 結束回合
        D 結束回合
        A 回合進行判定，判定為黑桃 3
    
        When
        A hp = -1
        要求 A 出桃
        A skip
        要求 B 出桃
        B skip
        要求 C 出桃
        C skip
        要求 D 出桃
        D skip
    
        Then
        結算 A ，A 死亡
        ABCD 收到結算的 event
        B 的回合
    """)
    @Test
    public void givenPlayerABCD_PlayerATurn_AHas2Hp_PlayerAPlaysLightning_ABCDHasNoPeach_ABCDEndTurns_AJudgmentPhaseSpade3_WhenAHasMinus1Hp_AskAPlayPeach_APlaysSkip_AskBPlayPeach_BPlaysSkip_AskCPlayPeach_CPlaysSkip_AskDPlayPeach_DPlaysSkip_ThenADeath_ABCDReceiveJudgmentEndEvent_BHasTurn() {
        // Given
        Game game = new Game();
        game.initDeck();
        Deck deck = new Deck(
                List.of(
                        new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003),
                        new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003),
                        new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003)
                )
        );
        game.setDeck(deck);

        // 玩家 A 設定
        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withHand(new Hand()) // 沒有桃
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(2))  // A 初始 2 HP
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withHealthStatus(HealthStatus.ALIVE)
                .withDelayScrollCards(new Stack<>()) // 尚未有閃電
                .build();
        playerA.getHand().addCardToHand(Arrays.asList(new Lightning(SSA014))); // A 出閃電

        // 玩家 B 設定 (沒有桃)
        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand()) // 沒有桃
                .withGeneralCard(new GeneralCard(General.關羽))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withEquipment(new Equipment())
                .withDelayScrollCards(new Stack<>()) // 尚未有判定牌
                .build();

        // 玩家 C 設定 (沒有桃)
        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand()) // 沒有桃
                .withGeneralCard(new GeneralCard(General.張飛))
                .withRoleCard(new RoleCard(Role.REBEL))
                .withEquipment(new Equipment())
                .build();

        // 玩家 D 設定 (沒有桃)
        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand()) // 沒有桃
                .withGeneralCard(new GeneralCard(General.趙雲))
                .withRoleCard(new RoleCard(Role.REBEL))
                .withEquipment(new Equipment())
                .build();

        // 設置遊戲狀態
        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        // A 出閃電
        game.playerPlayCard(playerA.getId(), SSA014.getCardId(), "", PlayType.ACTIVE.getPlayType());

        // ABC 結束回合
        game.finishAction(playerA.getId());
        game.finishAction(playerB.getId());
        game.finishAction(playerC.getId());

        // D 結束回合
        List<DomainEvent> events = game.finishAction(playerD.getId());

        // When: A 判定黑桃 3，HP 變為 -1，系統要求 A 出桃
        assertEquals(-1, playerA.getBloodCard().getHp());
        assertTrue(events.stream().anyMatch(event -> event instanceof AskPeachEvent));

        // A 選擇 skip
        List<DomainEvent> aSkipEvents = game.playerPlayCard(playerA.getId(), "", playerA.getId(), PlayType.SKIP.getPlayType());

        // 系統要求 B 出桃
        assertTrue(aSkipEvents.stream().anyMatch(event -> event instanceof AskPeachEvent));

        // B 選擇 skip
        List<DomainEvent> bSkipEvents = game.playerPlayCard(playerB.getId(), "", playerA.getId(), PlayType.SKIP.getPlayType());

        // 系統要求 C 出桃
        assertTrue(bSkipEvents.stream().anyMatch(event -> event instanceof AskPeachEvent));

        // C 選擇 skip
        List<DomainEvent> cSkipEvents = game.playerPlayCard(playerC.getId(), "", playerA.getId(), PlayType.SKIP.getPlayType());

        // 系統要求 D 出桃
        assertTrue(cSkipEvents.stream().anyMatch(event -> event instanceof AskPeachEvent));

        // D 選擇 skip
        List<DomainEvent> dSkipEvents = game.playerPlayCard(playerD.getId(), "", playerA.getId(), PlayType.SKIP.getPlayType());

        // Then:
        // 確認 A 死亡
        assertEquals(-1, playerA.getBloodCard().getHp());
        assertTrue(dSkipEvents.stream().anyMatch(event -> event instanceof SettlementEvent));
        assertEquals(3, game.getSeatingChart().getPlayers().size());
        assertTrue(game.getSeatingChart().getPlayers().stream().map(Player::getId).toList()
                .containsAll(Arrays.asList("player-b", "player-c", "player-d")));

        // 確認 ABCD 收到結算事件
        assertTrue(dSkipEvents.stream().anyMatch(event -> event instanceof SettlementEvent));

        // 確認 B 進入回合
        assertEquals("player-b", game.getCurrentRound().getActivePlayer().getId());
    }


    @DisplayName("""
    Given
    玩家ABCD
    A的回合
    A hp = 1
    A出閃電
    B 有三個桃
    ABC 結束回合
    D 結束回合
    A 回合進行判定，判定為黑桃 3

    When
    A hp = -2
    要求 A 出桃
    A 出 skip
    要求 B 出桃， B 出桃
    要求 B 出桃 ，B 出桃
    要求 B 出桃， B 出桃

    Then
    A hp = 1
    ABCD 收到判定結束的 event
    A 抽牌
    A 的回合
""")
    @Test
    public void givenPlayerABCD_PlayerATurn_AHas1Hp_PlayerAPlaysLightning_BHasThreePeach_ABCDEndTurns_AJudgmentPhaseSpade3_WhenAHasMinus2Hp_AskAPlayPeach_APlaysSkip_AskBPlayPeach_BPlaysPeach_AskBPlayPeach_BPlaysPeach_AskBPlayPeach_BPlaysPeach_ThenAHas1Hp_ABCDReceiveJudgmentEndEvent_AReceivesDrawEvent_AHasTurn() {
        // Given
        Game game = new Game();
        game.initDeck();
        Deck deck = new Deck(
                List.of(
                        new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003),
                        new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003),
                        new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003)
                )
        );
        game.setDeck(deck);

        // 玩家 A 設定
        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withHand(new Hand()) // A 沒有桃
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(1))  // A 初始 1 HP
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withHealthStatus(HealthStatus.ALIVE)
                .withDelayScrollCards(new Stack<>()) // 尚未有閃電
                .build();
        playerA.getHand().addCardToHand(Arrays.asList(new Lightning(SSA014))); // A 出閃電

        // 玩家 B 設定 (有三個桃)
        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(7))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.關羽))
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withEquipment(new Equipment())
                .withDelayScrollCards(new Stack<>()) // 尚未有判定牌
                .build();
        playerB.getHand().addCardToHand(Arrays.asList(new Peach(BH3029), new Peach(BH3029), new Peach(BH3029))); // B 有三個桃

        // 玩家 C 設定
        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand()) // C 沒有桃
                .withGeneralCard(new GeneralCard(General.張飛))
                .withRoleCard(new RoleCard(Role.REBEL))
                .withEquipment(new Equipment())
                .build();

        // 玩家 D 設定
        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand()) // D 沒有桃
                .withGeneralCard(new GeneralCard(General.趙雲))
                .withRoleCard(new RoleCard(Role.REBEL))
                .withEquipment(new Equipment())
                .build();

        // 設置遊戲狀態
        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        // A 出閃電
        game.playerPlayCard(playerA.getId(), SSA014.getCardId(), "", PlayType.ACTIVE.getPlayType());

        // ABC 結束回合
        game.finishAction(playerA.getId());
        game.finishAction(playerB.getId());
        game.finishAction(playerC.getId());

        // D 結束回合
        List<DomainEvent> events = game.finishAction(playerD.getId());

        // When: A 判定黑桃 3，HP 變為 -2，系統要求 A 出桃
        assertEquals(-2, playerA.getBloodCard().getHp());
        assertTrue(events.stream().anyMatch(event -> event instanceof AskPeachEvent));

        // A 選擇 skip
        List<DomainEvent> aSkipEvents = game.playerPlayCard(playerA.getId(), "", playerA.getId(), PlayType.SKIP.getPlayType());

        // 系統要求 B 出桃
        assertTrue(aSkipEvents.stream().anyMatch(event -> event instanceof AskPeachEvent));

        // B 使用第一張桃
        List<DomainEvent> firstPeachEvents = game.playerPlayCard(playerB.getId(), BH3029.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        // 再次要求 B 出桃
        assertTrue(firstPeachEvents.stream().anyMatch(event -> event instanceof AskPeachEvent));

        // B 使用第二張桃
        List<DomainEvent> secondPeachEvents = game.playerPlayCard(playerB.getId(), BH3029.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        // 再次要求 B 出桃
        assertTrue(secondPeachEvents.stream().anyMatch(event -> event instanceof AskPeachEvent));

        // B 使用第三張桃
        List<DomainEvent> thirdPeachEvents = game.playerPlayCard(playerB.getId(), BH3029.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        // Then:
        // 確認 A HP 回復至 1
        assertEquals(1, playerA.getBloodCard().getHp());

        // 確認 ABCD 收到判定結束事件
        assertTrue(thirdPeachEvents.stream().anyMatch(event -> event instanceof JudgementEvent));

        // A 收到抽牌事件
        assertTrue(thirdPeachEvents.stream().anyMatch(event -> event instanceof DrawCardEvent));

        // 確認 A 仍是當前回合玩家
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
    }

}