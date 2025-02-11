package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.EquipmentPlayType;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.EightDiagramTactic;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.RedRabbitHorse;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.ShadowHorse;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.QilinBowCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.RepeatingCrossbowCard;
import com.gaas.threeKingdoms.handcard.scrollcard.ArrowBarrage;
import com.gaas.threeKingdoms.handcard.scrollcard.BarbarianInvasion;
import com.gaas.threeKingdoms.handcard.scrollcard.BorrowedSword;
import com.gaas.threeKingdoms.handcard.scrollcard.Duel;
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
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PlayerDeathTest {

    @DisplayName("""
            Given
            玩家ABCD
            A 為主公 B 為反賊 C 為忠臣 D 為內奸
            B hp = 1
            A 手牌有一張殺
            B 手牌沒有閃
            ABCD 都沒有桃

            When
            A 出牌 殺 B
            B C D A skip 

            Then
            B 死亡，A 抽三張卡到手牌
            active player 仍然是 A 
            檯面上遊戲玩家為 ACD
            """)
    @Test
    public void givenPlayerABCD_PlayerBHas1HP_AHasKillCard_WhenAPlaysKillOnB_AndAllPlayersSkip_ThenBDeadAReceives3CardsAndGamePlayersAreACD() {
        // Given
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
        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(1))
                .withGeneralCard(new GeneralCard(General.孫權))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.REBEL))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.關羽))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.張飛))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.setCurrentRound(new Round(playerA));
        game.enterPhase(new Normal(game));

        // When
        game.playerPlayCard(playerA.getId(), "BS8008", playerB.getId(), PlayType.ACTIVE.getPlayType());
        game.playerPlayCard(playerB.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());

        game.playerPlayCard(playerB.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());
        game.playerPlayCard(playerC.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());
        game.playerPlayCard(playerD.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());
        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());

        // Then
        SettlementEvent settlementEvent = getEvent(events, SettlementEvent.class).orElseThrow(RuntimeException::new);
        DrawCardEvent drawCardEvent = getEvent(events, DrawCardEvent.class).orElseThrow(RuntimeException::new);

        assertEquals("player-b", settlementEvent.getPlayerId());
        assertEquals("Rebel", settlementEvent.getRole().getRoleName());

        assertTrue(game.getSeatingChart().getPlayers().stream().noneMatch(player -> player.getId().equals("player-b")));

        Hand playerAHand = playerA.getHand();
        assertEquals(3, drawCardEvent.getSize()); // Confirm A draws 3 cards
        assertEquals("player-a", drawCardEvent.getDrawCardPlayerId()); // Confirm A draws 3 cards
        assertEquals(3, playerAHand.getCards().size()); // Confirm A draws 3 cards

        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId()); // Active player remains A
        assertEquals(3, game.getSeatingChart().getPlayers().size()); // Only A, C, and D remain
    }

    @DisplayName("""
            Given
            玩家ABCD
            A 為主公 B 為反賊 C 為忠臣 D 為內奸
            B hp = 1
            A 手牌有一張殺
            B 手牌沒有閃
            ABCD 都沒有桃
            
            When
            A 出牌 殺 B
            B C D A skip         
            B 死亡，A 抽三張卡到手牌
            A 結束回合
            
            Then
            C 的回合
            active player 為 C
    """)
    @Test
    public void givenPlayerABCD_PlayerBHas1HP_AHasKillCard_WhenAPlaysKillOnB_AndBSkip_ThenBDeadAEndsTurnAndCStartsTurn() {
        // Given
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
        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(1))
                .withGeneralCard(new GeneralCard(General.孫權))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.REBEL))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.關羽))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.張飛))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.setCurrentRound(new Round(playerA));
        game.enterPhase(new Normal(game));

        // When
        game.playerPlayCard(playerA.getId(), "BS8008", playerB.getId(), PlayType.ACTIVE.getPlayType());
        game.playerPlayCard(playerB.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());

        game.playerPlayCard(playerB.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());
        game.playerPlayCard(playerC.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());
        game.playerPlayCard(playerD.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());
        game.playerPlayCard(playerA.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());
        List<DomainEvent> events = game.finishAction(playerA.getId());

        // Then
        assertTrue(game.getSeatingChart().getPlayers().stream().noneMatch(player -> player.getId().equals("player-b")));
        assertEquals("player-c", game.getCurrentRound().getActivePlayer().getId());
    }

    @DisplayName("""
            Given
            玩家ABCD
            A 為主公 B 為反賊 C 為忠臣 D 為內奸
            B hp = 1
            A 手牌有一張殺 A 裝備麒麟弓
            B 手牌沒有閃 B 有赤兔馬
            ABCD 都沒有桃
            
            When
            A 出牌 殺 B
            B skip 
            A 發動麒麟弓效果
            B 被詢問要不要出桃 skip
            C 被詢問要不要出桃 skip
            D 被詢問要不要出桃 skip
            A 被詢問要不要出桃 skip
            B 死亡，A 抽三張卡到手牌
            A 結束回合
            
            Then
            C 的回合
            active player 為 C
    """)
    @Test
    public void givenPlayerABCD_PlayerBHasRedRabbit_AHasKillCardAndQilinBow_WhenAPlaysKillAndActivatesQilinBow_ThenBDeadAndCRoundStarts() {
        // Given
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
        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008)));
        playerA.getEquipment().setWeapon(new QilinBowCard(EH5031));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(1))
                .withGeneralCard(new GeneralCard(General.孫權))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.REBEL))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();
        playerB.getEquipment().setMinusOne(new RedRabbitHorse(EH5044));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.關羽))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.張飛))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.setCurrentRound(new Round(playerA));
        game.enterPhase(new Normal(game));

        // When
        game.playerPlayCard(playerA.getId(), "BS8008", playerB.getId(), PlayType.ACTIVE.getPlayType()); // A 使用殺攻擊 B
        game.playerPlayCard(playerB.getId(), "", playerA.getId(), PlayType.SKIP.getPlayType()); // B 跳過出閃

        // A 發動麒麟弓效果，移除 B 的赤兔馬
        List<DomainEvent> playerUseEquipmentEvents = game.playerUseEquipment(playerA.getId(), EH5031.getCardId(), playerB.getId(), EquipmentPlayType.ACTIVE);
        List<DomainEvent> playerUseEquipmentEvents1 = game.playerPlayCard(playerB.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());
        List<DomainEvent> playerUseEquipmentEvents2 = game.playerPlayCard(playerC.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());
        List<DomainEvent> playerUseEquipmentEvents3 = game.playerPlayCard(playerD.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());
        List<DomainEvent> playerUseEquipmentEvents4 = game.playerPlayCard(playerA.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());
        List<DomainEvent> playerUseEquipmentEvents5 = game.finishAction(playerA.getId());

        //Then
        assertTrue(game.getSeatingChart().getPlayers().stream().noneMatch(player -> player.getId().equals("player-b")));
        assertEquals("player-c", game.getCurrentRound().getActivePlayer().getId());
    }

    @DisplayName("""
        Given
        玩家ABCD
        A 為主公 ，B 為忠臣， C 為反賊， D 為內奸
        B hp = 1
        A 手牌有一張殺三張閃，裝備有 麒麟弓、赤兔馬、絕影馬、八卦陣
        B 手牌沒有閃
        ABCD 都沒有桃
        
        When
        A 出牌 殺 B
        B C D A skip 
        
        Then
        B 死亡，A 沒有任何裝備、手牌數量為 0
        active player 仍然是 A 
        檯面上遊戲玩家為 ACD
    """)
    @Test
    public void givenPlayerABCD_PlayerBHas1HP_AHasFullEquipment_WhenAPlaysKillOnB_AndAllPlayersSkip_ThenBDeadANoEquipmentAndGamePlayersAreACD() {
        // Given
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
                new Kill(BS8008), new Dodge(BH2028), new Dodge(BH2028), new Dodge(BH2028)));
        playerA.getEquipment().setWeapon(new QilinBowCard(EH5031));
        playerA.getEquipment().setMinusOne(new RedRabbitHorse(EH5044));
        playerA.getEquipment().setPlusOne(new ShadowHorse(ES5018));
        playerA.getEquipment().setArmor(new EightDiagramTactic(EC2067));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(1))
                .withGeneralCard(new GeneralCard(General.孫權))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.關羽))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.REBEL))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.張飛))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.setCurrentRound(new Round(playerA));
        game.enterPhase(new Normal(game));

        // When
        game.playerPlayCard(playerA.getId(), "BS8008", playerB.getId(), PlayType.ACTIVE.getPlayType());
        game.playerPlayCard(playerB.getId(), "", playerA.getId(), PlayType.SKIP.getPlayType());

        game.playerPlayCard(playerB.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());
        game.playerPlayCard(playerC.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());
        game.playerPlayCard(playerD.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());
        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());

        // Then
        SettlementEvent settlementEvent = getEvent(events, SettlementEvent.class).orElseThrow(RuntimeException::new);
        DiscardEvent discardEvent = getEvent(events, DiscardEvent.class).orElseThrow(RuntimeException::new);
        DiscardEquipmentEvent discardEquipmentEvent = getEvent(events, DiscardEquipmentEvent.class).orElseThrow(RuntimeException::new);

        // Validate B's death
        assertEquals("player-b", settlementEvent.getPlayerId());
        assertEquals("Minister", settlementEvent.getRole().getRoleName());
        assertTrue(game.getSeatingChart().getPlayers().stream().noneMatch(player -> player.getId().equals("player-b")));

        // Validate A loses all equipment
        Equipment playerAEquipment = playerA.getEquipment();
        assertNull(playerAEquipment.getArmor());
        assertNull(playerAEquipment.getWeapon());
        assertNull(playerAEquipment.getMinusOne());
        assertNull(playerAEquipment.getPlusOne());
        assertEquals(0, playerAEquipment.getAllEquipmentCards().size());
        assertEquals(4, discardEquipmentEvent.getEquipmentCardIds().size());
        assertEquals("player-a", discardEquipmentEvent.getPlayerId());

        // Validate A's hand is empty
        Hand playerAHand = playerA.getHand();
        assertTrue(playerAHand.getCards().isEmpty());
        assertEquals(3, discardEvent.getDiscardCards().size());
        assertEquals("player-a", discardEvent.getDiscardPlayerId());

        // Validate active player is still A
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());

        // Validate only A, C, and D remain in the game
        assertEquals(3, game.getSeatingChart().getPlayers().size());
        assertTrue(game.getSeatingChart().getPlayers().stream().map(Player::getId).toList()
                .containsAll(Arrays.asList("player-a", "player-c", "player-d")));

        game.finishAction(playerA.getId());
    }


    @DisplayName("""
        Given
        玩家 A B C D
        A 為反賊，B 為忠臣， C 為主公， D 為內奸
        B hp = 1
        A 手牌有一張殺三張閃，裝備有 麒麟弓、赤兔馬、絕影馬、八卦陣
        B 手牌沒有閃
        A B C D 都沒有桃
        
        When
        A 出牌 殺 B
        B C D A skip 
        
        Then
        B 死亡
        A 手牌有三張閃，裝備有 麒麟弓、赤兔馬、絕影馬、八卦陣
        active player 仍然是 A 
        檯面上遊戲玩家為 A C D
    """)
    @Test
    public void givenPlayerABCD_PlayerBHas1HP_AHasFullEquipment_WhenAPlaysKillOnB_AndAllPlayersSkip_ThenBDeadAHasSameHandAndEquipmentAndGamePlayersAreACD() {
        // Given
        Game game = new Game();
        game.initDeck();

        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.REBEL))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();
        playerA.getHand().addCardToHand(Arrays.asList(
                new Kill(BS8008), new Dodge(BH2028), new Dodge(BH2028), new Dodge(BH2028)));
        playerA.getEquipment().setWeapon(new QilinBowCard(EH5031));
        playerA.getEquipment().setMinusOne(new RedRabbitHorse(EH5044));
        playerA.getEquipment().setPlusOne(new ShadowHorse(ES5018));
        playerA.getEquipment().setArmor(new EightDiagramTactic(EC2067));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(1))
                .withGeneralCard(new GeneralCard(General.孫權))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.關羽))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.張飛))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.setCurrentRound(new Round(playerA));
        game.enterPhase(new Normal(game));

        // When
        game.playerPlayCard(playerA.getId(), "BS8008", playerB.getId(), PlayType.ACTIVE.getPlayType());
        game.playerPlayCard(playerB.getId(), "", playerA.getId(), PlayType.SKIP.getPlayType());

        game.playerPlayCard(playerB.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());
        game.playerPlayCard(playerC.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());
        game.playerPlayCard(playerD.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());
        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());

        // Then
        SettlementEvent settlementEvent = getEvent(events, SettlementEvent.class).orElseThrow(RuntimeException::new);

        // Validate B's death
        assertEquals("player-b", settlementEvent.getPlayerId());
        assertEquals("Minister", settlementEvent.getRole().getRoleName());
        assertTrue(game.getSeatingChart().getPlayers().stream().noneMatch(player -> player.getId().equals("player-b")));

        // Validate A's hand remains unchanged
        Hand playerAHand = playerA.getHand();
        assertEquals(3, playerAHand.getCards().size());
        assertTrue(playerAHand.getCards().stream().allMatch(card -> card instanceof Dodge));

        // Validate A's equipment remains unchanged
        Equipment playerAEquipment = playerA.getEquipment();
        assertNotNull(playerAEquipment.getWeapon());
        assertNotNull(playerAEquipment.getMinusOne());
        assertNotNull(playerAEquipment.getPlusOne());
        assertNotNull(playerAEquipment.getArmor());
        assertEquals("EH5031", playerAEquipment.getWeapon().getId());
        assertEquals("EH5044", playerAEquipment.getMinusOne().getId());
        assertEquals("ES5018", playerAEquipment.getPlusOne().getId());
        assertEquals("EC2067", playerAEquipment.getArmor().getId());

        // Validate active player is still A
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());

        // Validate only A, C, and D remain in the game
        assertEquals(3, game.getSeatingChart().getPlayers().size());
        assertTrue(game.getSeatingChart().getPlayers().stream().map(Player::getId).toList()
                .containsAll(Arrays.asList("player-a", "player-c", "player-d")));
    }

    @DisplayName("""
        Given
        玩家A B C D
        A 為主公 B 為反賊 C 為忠臣 D 為內奸
        B hp = 1 C hp = 3 D hp = 1
        A 手牌有一張萬箭齊發
        B D 手牌沒有閃
        A B C D 都沒有桃
        
        When
        A 出 萬箭齊發
        
        B skip 萬箭齊發
        B C D A 被詢問要不要出桃 skip
        B 死亡 A 抽三張卡到手牌
        
        C skip 萬箭齊發
        
        D skip 萬箭齊發
        D A C 被詢問要不要出桃 skip
        
        Then
        D 死亡
        A C 獲勝 (主公與忠臣獲勝)
    """)
    @Test
    public void givenPlayerABCD_PlayerAPlaysArrowBarrage_WhenNoOneCanDodge_ThenBAndDDieAndACWin() {
        // Given
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
        playerA.getHand().addCardToHand(Arrays.asList(new ArrowBarrage(SHA040)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(1))
                .withGeneralCard(new GeneralCard(General.孫權))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.REBEL))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(3))
                .withGeneralCard(new GeneralCard(General.關羽))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(1))
                .withGeneralCard(new GeneralCard(General.張飛))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.setCurrentRound(new Round(playerA));
        game.enterPhase(new Normal(game));

        // When
        game.playerPlayCard(playerA.getId(), "SHA040", null, PlayType.ACTIVE.getPlayType()); // A 出 萬箭齊發

        // B 跳過萬箭齊發
        game.playerPlayCard(playerB.getId(), "", playerA.getId(), PlayType.SKIP.getPlayType());

        game.playerPlayCard(playerB.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());
        game.playerPlayCard(playerC.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());
        game.playerPlayCard(playerD.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());
        List<DomainEvent> domainEvents1 = game.playerPlayCard(playerA.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());

        game.playerPlayCard(playerC.getId(), "", playerA.getId(), PlayType.SKIP.getPlayType());

        game.playerPlayCard(playerD.getId(), "", playerA.getId(), PlayType.SKIP.getPlayType());

        game.playerPlayCard(playerD.getId(), "", playerD.getId(), PlayType.SKIP.getPlayType());
        game.playerPlayCard(playerA.getId(), "", playerD.getId(), PlayType.SKIP.getPlayType());
        List<DomainEvent> events = game.playerPlayCard(playerC.getId(), "", playerD.getId(), PlayType.SKIP.getPlayType());

        // Then
        GameOverEvent gameOverEvent = getEvent(events, GameOverEvent.class).orElseThrow(RuntimeException::new);
        SettlementEvent settlementEvent = getEvent(events, SettlementEvent.class).orElseThrow(RuntimeException::new);

        assertEquals("player-d", settlementEvent.getPlayerId());
        assertEquals("Traitor", settlementEvent.getRole().getRoleName());

        assertTrue(game.getSeatingChart().getPlayers().stream().noneMatch(player -> player.getId().equals("player-d")));
        assertEquals(2, game.getSeatingChart().getPlayers().size()); // Only A, C remain
        assertTrue(gameOverEvent.getWinners().containsAll(Arrays.asList("player-a", "player-c")));
    }

    @DisplayName("""
        Given
        玩家A B C D
        A 為主公 B 為反賊 C 為忠臣 D 為內奸
        B hp = 1 C hp = 3 D hp = 1
        A 手牌有一張南蠻入侵
        B D 手牌沒有閃
        A B C D 都沒有桃
        
        When
        A 出 南蠻入侵
        
        B skip 南蠻入侵
        B C D A 被詢問要不要出桃 skip
        B 死亡 A 抽三張卡到手牌
        
        C skip 南蠻入侵
        
        D skip 南蠻入侵
        D A C 被詢問要不要出桃 skip
        
        Then
        D 死亡
        A C 獲勝 (主公與忠臣獲勝)
    """)
    @Test
    public void givenPlayerABCD_PlayerAPlaysBarbarianInvasion_WhenNoOneCanDodge_ThenBAndDDieAndACWin() {
        // Given
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
        playerA.getHand().addCardToHand(Arrays.asList(new BarbarianInvasion(SS7007)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(1))
                .withGeneralCard(new GeneralCard(General.孫權))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.REBEL))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(3))
                .withGeneralCard(new GeneralCard(General.關羽))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(1))
                .withGeneralCard(new GeneralCard(General.張飛))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.setCurrentRound(new Round(playerA));
        game.enterPhase(new Normal(game));

        // When
        game.playerPlayCard(playerA.getId(), "SS7007", null, PlayType.ACTIVE.getPlayType()); // A 出 萬箭齊發

        // B 跳過南蠻入侵
        game.playerPlayCard(playerB.getId(), "", playerA.getId(), PlayType.SKIP.getPlayType());

        game.playerPlayCard(playerB.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());
        game.playerPlayCard(playerC.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());
        game.playerPlayCard(playerD.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());
        List<DomainEvent> domainEvents1 = game.playerPlayCard(playerA.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());

        game.playerPlayCard(playerC.getId(), "", playerA.getId(), PlayType.SKIP.getPlayType());

        game.playerPlayCard(playerD.getId(), "", playerA.getId(), PlayType.SKIP.getPlayType());

        game.playerPlayCard(playerD.getId(), "", playerD.getId(), PlayType.SKIP.getPlayType());
        game.playerPlayCard(playerA.getId(), "", playerD.getId(), PlayType.SKIP.getPlayType());
        List<DomainEvent> events = game.playerPlayCard(playerC.getId(), "", playerD.getId(), PlayType.SKIP.getPlayType());

        // Then
        GameOverEvent gameOverEvent = getEvent(events, GameOverEvent.class).orElseThrow(RuntimeException::new);
        SettlementEvent settlementEvent = getEvent(events, SettlementEvent.class).orElseThrow(RuntimeException::new);

        assertTrue(getEvent(events, AskKillEvent.class).isEmpty());
        assertEquals("player-d", settlementEvent.getPlayerId());
        assertEquals("Traitor", settlementEvent.getRole().getRoleName());

        assertTrue(game.getSeatingChart().getPlayers().stream().noneMatch(player -> player.getId().equals("player-d")));
        assertEquals(2, game.getSeatingChart().getPlayers().size()); // Only A, C remain
        assertTrue(gameOverEvent.getWinners().containsAll(Arrays.asList("player-a", "player-c")));
    }

    @DisplayName("""
            Given
            玩家ABCDE
            A 為主公，B 為忠臣，C 為反賊，D 為內奸，E 為反賊
            A hp = 1
            A 手牌沒有閃
            B 手牌有一張殺
            ABCDE 都沒有桃
            
            When
            B 出牌 殺 A
            A B C D E skip
            
            Then
            A 死亡，遊戲結束，贏家為 C 和 E
    """)
    @Test
    public void givenPlayerABCDE_PlayerBAttacksAWithKill_WhenNoOneCanDefend_ThenADiesGameEndsAndRebelsWin() {
        // Given
        Game game = new Game();
        game.initDeck();

        // A: 主公
        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withBloodCard(new BloodCard(1))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();

        // B: 忠臣
        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.孫權))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();
        playerB.getHand().addCardToHand(Arrays.asList(new Kill(BS8008)));

        // C: 反賊
        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.關羽))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.REBEL))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();

        // D: 內奸
        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.張飛))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();

        // E: 反賊
        Player playerE = PlayerBuilder.construct()
                .withId("player-e")
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.呂布))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.REBEL))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD, playerE);
        game.setPlayers(players);
        game.setCurrentRound(new Round(playerB));
        game.enterPhase(new Normal(game));

        // When
        game.playerPlayCard(playerB.getId(), "BS8008", playerA.getId(), PlayType.ACTIVE.getPlayType());

        game.playerPlayCard(playerA.getId(), "", playerA.getId(), PlayType.SKIP.getPlayType());

        game.playerPlayCard(playerA.getId(), "", playerA.getId(), PlayType.SKIP.getPlayType());
        game.playerPlayCard(playerB.getId(), "", playerA.getId(), PlayType.SKIP.getPlayType());
        game.playerPlayCard(playerC.getId(), "", playerA.getId(), PlayType.SKIP.getPlayType());
        game.playerPlayCard(playerD.getId(), "", playerA.getId(), PlayType.SKIP.getPlayType());

        List<DomainEvent> events = game.playerPlayCard(playerE.getId(), "", playerA.getId(), PlayType.SKIP.getPlayType());

        // Then
        SettlementEvent settlementEvent = getEvent(events, SettlementEvent.class).orElseThrow(RuntimeException::new);
        GameOverEvent gameOverEvent = getEvent(events, GameOverEvent.class).orElseThrow(RuntimeException::new);

        // Validate A's death
        assertEquals("player-a", settlementEvent.getPlayerId());
        assertEquals(Role.MONARCH, settlementEvent.getRole());
        assertTrue(game.getSeatingChart().getPlayers().stream().noneMatch(player -> player.getId().equals("player-a")));

        assertEquals("GameOver", game.getGamePhase().getPhaseName());
        assertTrue(gameOverEvent.getWinners().containsAll(Arrays.asList("player-c", "player-e")));
        assertEquals(2, gameOverEvent.getWinners().size()); // Only C, E win
    }

    @DisplayName("""
        Given
        玩家ABCD
        A 為反賊 B 為主公 C 為忠臣 D 為內奸
        B hp = 1
        A 手牌有一張殺
        B 手牌沒有閃
        ABCD 都沒有桃
        
        When
        A 出牌 殺 B
        B skip 
        B 被詢問要不要出桃 skip
        C 被詢問要不要出桃 skip
        D 被詢問要不要出桃 skip
        A 被詢問要不要出桃 skip
        
        Then
        A 獲勝 (反賊獲勝)
        遊戲結束
    """)
    @Test
    public void givenPlayerABCD_PlayerAAttacksBWithKill_WhenNoOneUsesPeach_ThenAAndRebelsWin() {
        // Given
        Game game = new Game();
        game.initDeck();

        // A: 反賊
        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.呂布))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.REBEL))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();
        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008)));

        // B: 主公
        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(1))
                .withGeneralCard(new GeneralCard(General.曹操))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();

        // C: 忠臣
        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.趙雲))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();

        // D: 內奸
        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.司馬懿))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.setCurrentRound(new Round(playerA));
        game.enterPhase(new Normal(game));

        // When
        game.playerPlayCard(playerA.getId(), "BS8008", playerB.getId(), PlayType.ACTIVE.getPlayType());

        // B skip
        game.playerPlayCard(playerB.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());

        game.playerPlayCard(playerB.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());
        game.playerPlayCard(playerC.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());
        game.playerPlayCard(playerD.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());
        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());

        // Then
        SettlementEvent settlementEvent = getEvent(events, SettlementEvent.class).orElseThrow(RuntimeException::new);
        GameOverEvent gameOverEvent = getEvent(events, GameOverEvent.class).orElseThrow(RuntimeException::new);

        // Validate B's death
        assertEquals("player-b", settlementEvent.getPlayerId());
        assertEquals(Role.MONARCH, settlementEvent.getRole());
        assertTrue(game.getSeatingChart().getPlayers().stream().noneMatch(player -> player.getId().equals("player-b")));

        // Validate game ends and A (Rebel) wins
        assertEquals("GameOver", game.getGamePhase().getPhaseName());
        assertTrue(gameOverEvent.getWinners().contains("player-a"));
        assertEquals(1, gameOverEvent.getWinners().size()); // Only A wins
    }

    @DisplayName("""
            Given
            玩家ABCDEFG
            A 為主公 ，B 為忠臣， C 為反賊， D 為內奸，E 為反賊
            F 為忠臣，G 為內奸
            A hp = 1
            ABCEF 手牌沒有閃
            D 手牌有五張殺
            ABCDEFG 都沒有桃
            
            When
            D 出牌 殺 B -> 殺 C -> 殺 F -> 殺 E -> 殺 A
            每輪都是 ABCDEFG skip 
            
            Then
            遊戲結束，贏家為 D 和 G
    """)
    @Test
    public void givenPlayerABCDEFG_PlayerDAttacksWithKillMultipleTimes_WhenNoOneUsesPeach_ThenDAndGWin() throws InterruptedException {
        // Given
        Game game = new Game();
        game.initDeck();

        // A: 主公
        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withBloodCard(new BloodCard(1))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();

        // B: 忠臣
        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(1))
                .withGeneralCard(new GeneralCard(General.孫尚香))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();

        // C: 反賊
        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(1))
                .withGeneralCard(new GeneralCard(General.關羽))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.REBEL))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();

        // D: 內奸
        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.司馬懿))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHand(new Hand())
                .build();
        playerD.getHand().addCardToHand(Arrays.asList(
                new Kill(BS8008), new Kill(BS8008), new Kill(BS8008), new Kill(BS8008), new Kill(BS8008)
        ));
        Equipment playerDEquipment = new Equipment();
        playerDEquipment.setMinusOne(new RedRabbitHorse(EH5044));
        playerDEquipment.setWeapon(new RepeatingCrossbowCard(ECA066));
        playerD.setEquipment(playerDEquipment);


        // E: 反賊
        Player playerE = PlayerBuilder.construct()
                .withId("player-e")
                .withBloodCard(new BloodCard(1))
                .withGeneralCard(new GeneralCard(General.張飛))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.REBEL))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();

        // F: 忠臣
        Player playerF = PlayerBuilder.construct()
                .withId("player-f")
                .withBloodCard(new BloodCard(1))
                .withGeneralCard(new GeneralCard(General.趙雲))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();

        // G: 內奸
        Player playerG = PlayerBuilder.construct()
                .withId("player-g")
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.呂布))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD, playerE, playerF, playerG);
        game.setPlayers(players);
        game.setCurrentRound(new Round(playerD));
        game.enterPhase(new Normal(game));

        // When
        // D 出殺攻擊 B
        game.playerPlayCard(playerD.getId(), "BS8008", playerB.getId(), PlayType.ACTIVE.getPlayType());
        List<DomainEvent> domainEvents = game.playerPlayCard(playerB.getId(), "", playerD.getId(), PlayType.SKIP.getPlayType());

        game.playerPlayCard(playerB.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());
        game.playerPlayCard(playerC.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());
        game.playerPlayCard(playerD.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());
        game.playerPlayCard(playerE.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());
        game.playerPlayCard(playerF.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());
        game.playerPlayCard(playerG.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());
        game.playerPlayCard(playerA.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());

        // D 出殺攻擊 C
        game.playerPlayCard(playerD.getId(), "BS8008", playerC.getId(), PlayType.ACTIVE.getPlayType());
        game.playerPlayCard(playerC.getId(), "", playerD.getId(), PlayType.SKIP.getPlayType());

        game.playerPlayCard(playerC.getId(), "", playerC.getId(), PlayType.SKIP.getPlayType());
        game.playerPlayCard(playerD.getId(), "", playerC.getId(), PlayType.SKIP.getPlayType());
        game.playerPlayCard(playerE.getId(), "", playerC.getId(), PlayType.SKIP.getPlayType());
        game.playerPlayCard(playerF.getId(), "", playerC.getId(), PlayType.SKIP.getPlayType());
        game.playerPlayCard(playerG.getId(), "", playerC.getId(), PlayType.SKIP.getPlayType());
        game.playerPlayCard(playerA.getId(), "", playerC.getId(), PlayType.SKIP.getPlayType());

        // D 出殺攻擊 F
        game.playerPlayCard(playerD.getId(), "BS8008", playerF.getId(), PlayType.ACTIVE.getPlayType());
        game.playerPlayCard(playerF.getId(), "", playerD.getId(), PlayType.SKIP.getPlayType());

        game.playerPlayCard(playerF.getId(), "", playerF.getId(), PlayType.SKIP.getPlayType());
        game.playerPlayCard(playerG.getId(), "", playerF.getId(), PlayType.SKIP.getPlayType());
        game.playerPlayCard(playerA.getId(), "", playerF.getId(), PlayType.SKIP.getPlayType());
        game.playerPlayCard(playerD.getId(), "", playerF.getId(), PlayType.SKIP.getPlayType());
        game.playerPlayCard(playerE.getId(), "", playerF.getId(), PlayType.SKIP.getPlayType());

        // D 出殺攻擊 E
        game.playerPlayCard(playerD.getId(), "BS8008", playerE.getId(), PlayType.ACTIVE.getPlayType());
        game.playerPlayCard(playerE.getId(), "", playerD.getId(), PlayType.SKIP.getPlayType());

        game.playerPlayCard(playerE.getId(), "", playerE.getId(), PlayType.SKIP.getPlayType());
        game.playerPlayCard(playerG.getId(), "", playerE.getId(), PlayType.SKIP.getPlayType());
        game.playerPlayCard(playerA.getId(), "", playerE.getId(), PlayType.SKIP.getPlayType());
        game.playerPlayCard(playerD.getId(), "", playerE.getId(), PlayType.SKIP.getPlayType());

        // D 出殺攻擊 A
        game.playerPlayCard(playerD.getId(), "BS8008", playerA.getId(), PlayType.ACTIVE.getPlayType());
        game.playerPlayCard(playerA.getId(), "", playerD.getId(), PlayType.SKIP.getPlayType());

        game.playerPlayCard(playerA.getId(), "", playerA.getId(), PlayType.SKIP.getPlayType());
        game.playerPlayCard(playerD.getId(), "", playerA.getId(), PlayType.SKIP.getPlayType());
        List<DomainEvent> events = game.playerPlayCard(playerG.getId(), "", playerA.getId(), PlayType.SKIP.getPlayType());

        // Then
        GameOverEvent gameOverEvent = getEvent(events, GameOverEvent.class).orElseThrow(RuntimeException::new);

        assertEquals("GameOver", game.getGamePhase().getPhaseName());
        assertTrue(gameOverEvent.getWinners().containsAll(Arrays.asList("player-d", "player-g")));
        assertEquals(2, gameOverEvent.getWinners().size());
    }


    @DisplayName("""
        Given
        玩家A B C D
        A 為主公 B 為反賊 C 為忠臣 D 為內奸
        B hp = 1 C hp = 1 D hp = 1
        A 手牌有一張借刀殺人
        B 有裝備武器，有一張殺，B攻擊範圍內有 C 可以殺
        B D 手牌沒有閃
        A B C D 都沒有桃
        
        When
        A 出借刀殺人，指定 B 殺 C
        
        C skip 瀕臨死亡
        C D A B 被詢問要不要出桃 skip
        
        Then
        C 死亡，A 裝備、手牌數量都還在
        active player 仍然是 A 
        檯面上遊戲玩家為 ABD
    """)
    @Test
    public void givenPlayerABCD_PlayerAPlaysBorrowedSword_WhenNoOneCanDodge_ThenBAndDDieAndACWin() {
        // Given
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
                new Kill(BS8008), new Dodge(BH2028), new Dodge(BH2028), new Dodge(BH2028), new BorrowedSword(SCK065)));
        playerA.getEquipment().setWeapon(new QilinBowCard(EH5031));
        playerA.getEquipment().setMinusOne(new RedRabbitHorse(EH5044));
        playerA.getEquipment().setPlusOne(new ShadowHorse(ES5018));
        playerA.getEquipment().setArmor(new EightDiagramTactic(EC2067));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(1))
                .withGeneralCard(new GeneralCard(General.孫權))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.REBEL))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();
        playerB.getHand().addCardToHand(Arrays.asList(new Kill(BS8008)));
        Equipment equipmentB = new Equipment();
        equipmentB.setWeapon(new RepeatingCrossbowCard(ECA066));
        playerB.setEquipment(equipmentB);

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(1))
                .withGeneralCard(new GeneralCard(General.關羽))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(1))
                .withGeneralCard(new GeneralCard(General.張飛))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.setCurrentRound(new Round(playerA));
        game.enterPhase(new Normal(game));

        // When
        game.playerPlayCard(playerA.getId(), SCK065.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        game.useBorrowedSwordEffect(playerA.getId(), playerB.getId(), playerC.getId());

        //When B 出殺
        game.playerPlayCard(playerB.getId(), BS8008.getCardId(), playerC.getId(), PlayType.ACTIVE.getPlayType());
        //When C 出skip
        game.playerPlayCard(playerC.getId(), "", playerC.getId(), PlayType.SKIP.getPlayType());


        game.playerPlayCard(playerC.getId(), "", playerC.getId(), PlayType.SKIP.getPlayType());
        game.playerPlayCard(playerD.getId(), "", playerC.getId(), PlayType.SKIP.getPlayType());
        game.playerPlayCard(playerA.getId(), "", playerC.getId(), PlayType.SKIP.getPlayType());
        List<DomainEvent> events = game.playerPlayCard(playerB.getId(), "", playerC.getId(), PlayType.SKIP.getPlayType());

        // Then
        SettlementEvent settlementEvent = getEvent(events, SettlementEvent.class).orElseThrow(RuntimeException::new);
        assertFalse(getEvent(events, DiscardEvent.class).isPresent());
        assertFalse(getEvent(events, DiscardEquipmentEvent.class).isPresent());

        // Validate B's death
        assertEquals("player-c", settlementEvent.getPlayerId());
        assertEquals("Minister", settlementEvent.getRole().getRoleName());
        assertTrue(game.getSeatingChart().getPlayers().stream().noneMatch(player -> player.getId().equals("player-c")));

        // Validate A have all equipment
        Equipment playerAEquipment = playerA.getEquipment();
        assertNotNull(playerAEquipment.getArmor());
        assertNotNull(playerAEquipment.getWeapon());
        assertNotNull(playerAEquipment.getMinusOne());
        assertNotNull(playerAEquipment.getPlusOne());
        assertEquals(4, playerAEquipment.getAllEquipmentCards().size());


        // Validate A's hand is not empty
        Hand playerAHand = playerA.getHand();
        assertFalse(playerAHand.getCards().isEmpty());

        // Validate active player is still A
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());

        // Validate only A, C, and D remain in the game
        assertEquals(3, game.getSeatingChart().getPlayers().size());
        assertTrue(game.getSeatingChart().getPlayers().stream().map(Player::getId).toList()
                .containsAll(Arrays.asList("player-a", "player-b", "player-d")));

        game.finishAction(playerA.getId());
    }

    @DisplayName("""
            Given
            玩家 A 主公 B 反賊 C 忠臣 D 內奸
            A的回合
            A有決鬥 x 1,殺 x 1, A 4hp
            
            B 殺 x 2，B 1hp
            
            When
            A 出決鬥，指定 B
            B 出殺
            A 出殺
            B 不出殺
            A 沒扣血, A 4hp
            B 扣血, B 0hp
            B C D A 被詢問要不要出桃 skip
            
            Then
            A 抽三張牌 
                    """)
    @Test
    public void givenPlayerABCD_PlayerATurn_PlayerAHasDuelAndKillWith1HP_BPlayerHasTwoKillsAnd1HP_WhenPlayerAPlaysDuelAndAssignsB_AndPlayersAlternateKillsUntilBDoesNotPlayKill_ThenPlayerADoesNotLoseHPAndRemainsAt4HPWhilePlayerBLoses1HPAndDead() {
        Game game = new Game();
        game.initDeck();
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withEquipment(new Equipment())
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(new Peach(BH3029), new Kill(BS8008), new Dodge(BH2028), new Dodge(BHK039), new Duel(SSA001)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(1))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.REBEL))
                .withEquipment(new Equipment())
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Kill(BS8008), new Dodge(BHK039), new Duel(SSA001)));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MINISTER))
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
        // A plays Duel and assigns B
        game.playerPlayCard(playerA.getId(), SSA001.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        game.playerPlayCard(playerB.getId(), "BS8008", playerA.getId(), PlayType.ACTIVE.getPlayType());
        game.playerPlayCard(playerA.getId(), "BS8008", playerB.getId(), PlayType.ACTIVE.getPlayType());
        game.playerPlayCard(playerB.getId(), "", playerA.getId(), PlayType.SKIP.getPlayType());
        // B C D A are asked if they want to play Peach
        game.playerPlayCard(playerB.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());
        game.playerPlayCard(playerC.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());
        game.playerPlayCard(playerD.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());
        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());

        //Then
        DrawCardEvent drawCardEvent = getEvent(events, DrawCardEvent.class).orElseThrow(RuntimeException::new);
        assertEquals("player-a", drawCardEvent.getDrawCardPlayerId());
        assertEquals(3, game.getSeatingChart().getPlayers().size());

        SettlementEvent settlementEvent = getEvent(events, SettlementEvent.class).orElseThrow(RuntimeException::new);
        assertEquals("player-b", settlementEvent.getPlayerId());

        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
        assertEquals(0, game.getPlayer("player-b").getBloodCard().getHp());

    }
}
