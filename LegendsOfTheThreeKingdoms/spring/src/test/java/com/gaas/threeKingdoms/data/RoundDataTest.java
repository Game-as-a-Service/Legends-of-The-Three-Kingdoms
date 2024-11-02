package com.gaas.threeKingdoms.data;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.PlayCard;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.RepeatingCrossbowCard;
import com.gaas.threeKingdoms.player.HealthStatus;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.repository.data.PlayerData;
import com.gaas.threeKingdoms.repository.data.RoundData;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.round.Round;
import com.gaas.threeKingdoms.round.RoundPhase;
import com.gaas.threeKingdoms.round.Stage;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.e2e.MockUtil.createPlayer;
import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RoundDataTest {

    @Test
    public void testRoundToRoundDataConversion() {
        // Arrange
        Player playerA = createPlayer(
                "player-a",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)
        );

        Player playerB = createPlayer("player-b",
                1,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MINISTER,
                new Kill(BS8008), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028)
        );

        Player playerC = createPlayer(
                "player-c",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.REBEL,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039), new RepeatingCrossbowCard(ECA066)
        );

        Player playerD = createPlayer(
                "player-d",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.TRAITOR,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)
        );

        Kill currentPlayCard = new Kill(PlayCard.BS8008);

        // Create a Round object
        Round round = new Round(playerA);
        round.setRoundPhase(RoundPhase.Judgement);
        round.setActivePlayer(playerB);
        round.setDyingPlayer(playerC);
        round.setCurrentPlayCard(currentPlayCard);
        round.setShowKill(true);
        round.setStage(Stage.Normal);

        // Convert to RoundData
        RoundData roundData = RoundData.fromDomain(round);

        // Assert
        assertEquals("Judgement", roundData.getRoundPhase());  // Default phase in the constructor
        assertEquals("player-a", roundData.getCurrentRoundPlayer());
        assertEquals("player-b", roundData.getActivePlayer());
        assertEquals("player-c", roundData.getDyingPlayer());
        assertEquals("BS8008", roundData.getCurrentPlayCard());
        assertTrue(roundData.isShowKill());
        assertEquals("Normal", roundData.getStage());
    }

    @Test
    public void testRoundDataToRoundConversion() {
        Game game = new Game();

        // Arrange
        Player playerA = createPlayer(
                "player-a",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)
        );

        Player playerB = createPlayer("player-b",
                1,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MINISTER,
                new Kill(BS8008), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028)
        );

        Player playerC = createPlayer(
                "player-c",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.REBEL,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039), new RepeatingCrossbowCard(ECA066)
        );

        Player playerD = createPlayer(
                "player-d",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.TRAITOR,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)
        );
        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);

        // Create RoundData object
        RoundData roundData = RoundData.builder()
                .roundPhase("Judgement")
                .currentRoundPlayer("player-a")
                .activePlayer("player-b")
                .dyingPlayer("player-c")
                .currentPlayCard("BS8008")
                .isShowKill(true)
                .stage("Normal")
                .build();

        // Act: Convert to Round domain object
        Round round = roundData.toDomain(game);

        // Assert
        assertEquals(RoundPhase.Judgement, round.getRoundPhase());
        assertEquals("player-a", round.getCurrentRoundPlayer().getId());
        assertEquals("player-b", round.getActivePlayer().getId());
        assertEquals("player-c", round.getDyingPlayer().getId());
        assertEquals("BS8008", round.getCurrentPlayCard().getId());
        assertTrue(round.isShowKill());
        assertEquals(Stage.Normal, round.getStage());
    }
}
