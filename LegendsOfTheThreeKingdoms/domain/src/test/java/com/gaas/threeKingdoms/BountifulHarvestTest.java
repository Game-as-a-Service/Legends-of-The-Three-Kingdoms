package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.events.BountifulHarvestChooseCardEvent;
import com.gaas.threeKingdoms.events.BountifulHarvestEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.RedRabbitHorse;
import com.gaas.threeKingdoms.handcard.scrollcard.BountifulHarvest;
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

public class BountifulHarvestTest {

    @DisplayName("""
            Given
            玩家ABC
            A的回合

            When

            A玩家出五穀豐登

            Then
            ABC 玩家收到五穀豐登的 event
            """)
    @Test
    public void givenPlayerABC_WhenPlayerAPlayBountifulHarvest_ThenPlayerABCReceiveBountifulHarvestEvent() {
        Game game = new Game();
        game.initDeck();
        Deck deck = new Deck(
                List.of(
                        new RedRabbitHorse(ES2002), new Kill(BC2054), new Peach(BH3029)
                )
        );
        game.setDeck(deck);

        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();
        playerA.getHand().addCardToHand(Arrays.asList(new Dodge(BDJ089), new BountifulHarvest(SH3042)));


        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.曹操))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(new Equipment())
                .build();

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.孫權))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        List<Player> players = asList(playerA, playerB, playerC);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        //When
        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), SH3042.getCardId(), "", PlayType.ACTIVE.getPlayType());

        //Then
        assertTrue(events.stream().anyMatch(event -> event instanceof BountifulHarvestEvent));

        BountifulHarvestEvent bountifulHarvestEvent = events.stream()
                .filter(event -> event instanceof BountifulHarvestEvent)
                .map(event -> (BountifulHarvestEvent) event)
                .findFirst()
                .orElseThrow();

        assertEquals("player-a", bountifulHarvestEvent.getNextChoosingPlayerId());

        assertEquals(3, bountifulHarvestEvent.getAssignmentCardIds().size());
        List<String> cardIds = bountifulHarvestEvent.getAssignmentCardIds();
        assertTrue(cardIds.containsAll(List.of("ES2002", "BC2054", "BH3029")));

    }

    @DisplayName("""
        Given
        玩家ABCD
        B的回合
        B玩家出五穀豐登，抽出 (ES2002、BC2054、BH3029、BH0036)
        
        When
        B 選擇 ES2002 赤兔馬
        
        Then
        ABCD 玩家收到 B 選擇 ES2002 的 event
        ABCD 玩家收到五穀豐登的 event 並且是輪到 C 選擇
    """)
    @Test
    public void givenPlayerBTurn_PlayerBPlaysBountifulHarvestAndDrawsCards_WhenPlayerBChoosesES2002_ThenAllPlayersReceiveSelectionEventAndNextPlayerChooses() {
        // Given
        Game game = new Game();
        game.initDeck();
        Deck deck = new Deck(
                List.of(
                        new RedRabbitHorse(EH5044), new Kill(BC2054), new Peach(BH3029), new Kill(BH0036)
                )
        );
        game.setDeck(deck);
        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withEquipment(new Equipment())
                .build();

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withEquipment(new Equipment())
                .build();

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
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

        List<Player> players = asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerB));

        playerB.getHand().addCardToHand(Arrays.asList(new BountifulHarvest(SH3042)));

        // When
        List<DomainEvent> harvestEvents = game.playerPlayCard(playerB.getId(), SH3042.getCardId(), null, PlayType.ACTIVE.getPlayType());

        // B 玩家選擇 EH5044 赤兔馬
        List<DomainEvent> selectionEvents = game.playerChooseCardFromBountifulHarvest(playerB.getId(), EH5044.getCardId());

        // Then
        // 檢查是否發送 B 選擇 ES2002 的事件
        assertTrue(selectionEvents.stream().anyMatch(event -> event instanceof BountifulHarvestChooseCardEvent &&
                ((BountifulHarvestChooseCardEvent) event).getPlayerId().equals("player-b") &&
                ((BountifulHarvestChooseCardEvent) event).getCardId().equals(EH5044.getCardId())));

        assertEquals(1, playerB.getHandSize());
        assertTrue(playerB.getHand().getCards().stream().anyMatch(card -> card.getId().equals("EH5044")));

        // 檢查 ABCD 是否收到五穀豐登事件，並確認輪到 C 選擇
        assertTrue(selectionEvents.stream().anyMatch(event -> event instanceof BountifulHarvestEvent &&
                ((BountifulHarvestEvent) event).getNextChoosingPlayerId().equals("player-c")));
    }

    @DisplayName("""
        Given
        玩家ABCD
        B的回合
        B玩家出五穀豐登，抽出 (ES2002、BC2054、BH3029、BH0036)
        
        When
        B 選擇 ES2002 赤兔馬
        C 選擇 BC2054
        D 選擇 BH3029
        A 選擇 BH0036
        
        Then
        ABCD 玩家收到 A 選擇 BH0036 的 event
        ABCD 玩家沒有收到五穀豐登事件
    """)
    @Test
    public void givenPlayerBTurn_PlayerBPlaysBountifulHarvestAndDrawsCards_WhenPlayerD_ThenPlayerBountifulHarvestEnd() {
        // Given
        Game game = new Game();
        game.initDeck();
        Deck deck = new Deck(
                List.of(
                        new RedRabbitHorse(ES2002), new Kill(BC2054), new Peach(BH3029), new Kill(BH0036)
                )
        );
        game.setDeck(deck);
        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withEquipment(new Equipment())
                .build();

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withEquipment(new Equipment())
                .build();

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
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

        List<Player> players = asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerB));

        playerB.getHand().addCardToHand(Arrays.asList(new BountifulHarvest(SH3042)));

        // When
        List<DomainEvent> harvestEvents = game.playerPlayCard(playerB.getId(), SH3042.getCardId(), null, PlayType.ACTIVE.getPlayType());

        // B 玩家選擇 ES2002 赤兔馬
        List<DomainEvent> selectionEventOfPlayerB = game.playerChooseCardFromBountifulHarvest(playerB.getId(), ES2002.getCardId());

        // C 玩家選擇 BC2054 赤兔馬
        List<DomainEvent> selectionEventOfPlayerC = game.playerChooseCardFromBountifulHarvest(playerC.getId(), BC2054.getCardId());

        // D 玩家選擇 BH3029 赤兔馬
        List<DomainEvent> selectionEventOfPlayerD = game.playerChooseCardFromBountifulHarvest(playerD.getId(), BH3029.getCardId());

        // A 玩家選擇 BH0036 赤兔馬
        List<DomainEvent> selectionEventOfPlayerA = game.playerChooseCardFromBountifulHarvest(playerA.getId(), BH0036.getCardId());

        // Then
        // 檢查是否發送 A 選擇 BH0036 的事件
        assertTrue(selectionEventOfPlayerA.stream().anyMatch(event -> event instanceof BountifulHarvestChooseCardEvent &&
                ((BountifulHarvestChooseCardEvent) event).getPlayerId().equals("player-a") &&
                ((BountifulHarvestChooseCardEvent) event).getCardId().equals(BH0036.getCardId())));

        // 檢查 ABCD 是否收到五穀豐登事件，並確認輪到 C 選擇
        assertFalse(selectionEventOfPlayerA.stream().anyMatch(event -> event instanceof BountifulHarvestEvent));

    }

}