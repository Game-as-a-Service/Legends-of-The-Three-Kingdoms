package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.events.BorrowedSwordEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.PlayCardEvent;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.QilinBowCard;
import com.gaas.threeKingdoms.handcard.scrollcard.BorrowedSword;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BorrowedSwordTest {

    @DisplayName("""
            Given
            玩家ABCD
            A的回合
            A有借刀殺人
            B 有裝備武器，有一張殺，B攻擊範圍內
            有 C 可以殺
            When
            A 出借刀殺人，指定 B 殺 C
                        
            Then
            ABCD 玩家收到借刀殺人的 event
            B 玩家收到要求出殺的 event
            """)
    @Test
    public void givenPlayerAHasBorrowedSword_WhenPlayerAPlayBorrowedSword_ThenPlayerABCDAcceptBorrowedSwordEvent() {
        Game game = new Game();
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(new BorrowedSword(SCK065)));

        Equipment equipment = new Equipment();
        equipment.setWeapon(new QilinBowCard(EH5031));
        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(equipment)
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(new Kill(BS8008)));

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

        //When 先出借刀殺人卡牌
        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), SCK065.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());

        PlayCardEvent playCardEvent = events.stream()
                .filter(event -> event instanceof PlayCardEvent)
                .map(event -> (PlayCardEvent) event)
                .findFirst()
                .orElse(null);

        //Then
        assertTrue(events.stream().anyMatch(event -> event instanceof PlayCardEvent));
        assertEquals("player-a", playCardEvent.getPlayerId());
        assertEquals("player-b", playCardEvent.getTargetPlayerId());

        //When 指定借刀殺人操作的玩家
        List<DomainEvent> secondEvents = game.useBorrowedSwordEffect(playerA.getId(), playerB.getId(), playerC.getId());

    }
}
