package com.gaas.threeKingdoms.data;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.behavior.behavior.BarbarianInvasionBehavior;
import com.gaas.threeKingdoms.e2e.MockUtil;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.PlayCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.player.HealthStatus;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.repository.data.BehaviorData;
import com.gaas.threeKingdoms.repository.data.PlayerData;
import com.gaas.threeKingdoms.rolecard.Role;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.e2e.MockUtil.createPlayer;
import static com.gaas.threeKingdoms.e2e.MockUtil.initGame;
import static org.junit.jupiter.api.Assertions.*;

public class BehaviorDataTest {

    @Test
    public void testBehaviorToBehaviorDataConversion() {
        // Arrange
        Player playerA = createPlayer(
                "player-a",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Kill(PlayCard.BS8008), new Peach(PlayCard.BH3029)
        );

        Player playerB = createPlayer(
                "player-b",
                3,
                General.張飛,
                HealthStatus.ALIVE,
                Role.REBEL,
                new Kill(PlayCard.BS8009), new Peach(PlayCard.BH4030)
        );

        List<String> reactionPlayers = Arrays.asList("player-b");

        Game game = MockUtil.initGame("123456", List.of(playerA, playerB), playerA);

        Kill currentPlayCard = new Kill(PlayCard.BS8008);
        Behavior behavior = new BarbarianInvasionBehavior(
                game,
                playerA,
                reactionPlayers,
                playerB,
                "BS8008",
                PlayType.ACTIVE.getPlayType(),
                currentPlayCard
        );

        // Act
        BehaviorData behaviorData = BehaviorData.fromDomain(behavior);

        // Assert
        assertEquals("player-a", behaviorData.getBehaviorPlayerId());
        assertEquals("player-b", behaviorData.getCurrentReactionPlayerId());
        assertEquals("BS8008", behaviorData.getCardId());
        assertEquals("active", behaviorData.getPlayType());
        assertEquals(reactionPlayers, behaviorData.getReactionPlayers());
        assertTrue(behaviorData.isTargetPlayerNeedToResponse());
        assertFalse(behaviorData.isOneRound());
    }

    @Test
    public void testBehaviorDataToBehaviorConversion() {
        // Arrange
        Player playerA = createPlayer(
                "player-a",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Kill(PlayCard.BS8008), new Peach(PlayCard.BH3029)
        );

        Player playerB = createPlayer(
                "player-b",
                3,
                General.張飛,
                HealthStatus.ALIVE,
                Role.REBEL,
                new Kill(PlayCard.BS8009), new Peach(PlayCard.BH4030)
        );

        List<String> reactionPlayers = Arrays.asList("player-b");

        BehaviorData behaviorData = BehaviorData.builder()
                .behaviorName(BarbarianInvasionBehavior.class.getSimpleName())
                .behaviorPlayerId(playerA.getId())
                .currentReactionPlayerId(playerB.getId())
                .reactionPlayers(reactionPlayers)
                .cardId("BS8008")
                .playType(PlayType.ACTIVE.getPlayType())
                .isTargetPlayerNeedToResponse(true)
                .isOneRound(false)
                .build();

        Game game = initGame("123456", List.of(playerA, playerB), playerB);

        // Act
        Behavior behavior = behaviorData.toDomain(game);

        // Assert
        assertEquals("player-a", behavior.getBehaviorPlayer().getId());
        assertEquals("player-b", behavior.getCurrentReactionPlayer().getId());
        assertEquals("BS8008", behavior.getCardId());
        assertEquals("active", behavior.getPlayType());
        assertEquals(reactionPlayers, behavior.getReactionPlayers());
        assertTrue(behavior.isTargetPlayerNeedToResponse());
        assertFalse(behavior.isOneRound());
    }
}
