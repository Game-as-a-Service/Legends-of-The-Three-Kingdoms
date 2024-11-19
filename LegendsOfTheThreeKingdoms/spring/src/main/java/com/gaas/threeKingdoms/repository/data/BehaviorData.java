package com.gaas.threeKingdoms.repository.data;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.behavior.behavior.*;
import com.gaas.threeKingdoms.handcard.PlayCard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BehaviorData {
    private String behaviorName;
    private String behaviorPlayerId;
    private List<String> reactionPlayers;
    private String currentReactionPlayerId;
    private String cardId;
    private String playType;
    private boolean isTargetPlayerNeedToResponse;
    private boolean isOneRound;

    public Behavior toDomain(Game game) {
        return createBehavior(game, behaviorName);
    }

    private Behavior createBehavior(Game game, String behaviorName) {
        Behavior behavior = switch (behaviorName) {
            case "BarbarianInvasionBehavior" ->
                new BarbarianInvasionBehavior(
                        game,
                        game.getPlayer(behaviorPlayerId),
                        reactionPlayers,
                        game.getPlayer(currentReactionPlayerId),
                        cardId,
                        playType,
                        PlayCard.findById(cardId)
                );
            case "BorrowedSwordBehavior" -> new BorrowedSwordBehavior(
                    game,
                    game.getPlayer(behaviorPlayerId),
                    reactionPlayers,
                    game.getPlayer(currentReactionPlayerId),
                    cardId,
                    playType,
                    PlayCard.findById(cardId)
            );
            case "DyingAskPeachBehavior" -> new DyingAskPeachBehavior(
                    game,
                    game.getPlayer(behaviorPlayerId),
                    reactionPlayers,
                    game.getPlayer(currentReactionPlayerId),
                    cardId,
                    playType,
                    PlayCard.findById(cardId)
            );
            case "EquipArmorBehavior" -> new EquipArmorBehavior(
                    game,
                    game.getPlayer(behaviorPlayerId),
                    reactionPlayers,
                    game.getPlayer(currentReactionPlayerId),
                    cardId,
                    playType,
                    PlayCard.findById(cardId)
            );
            case "EquipWeaponBehavior" -> new EquipWeaponBehavior(
                    game,
                    game.getPlayer(behaviorPlayerId),
                    reactionPlayers,
                    game.getPlayer(currentReactionPlayerId),
                    cardId,
                    playType,
                    PlayCard.findById(cardId)
            );
            case "MinusMountsBehavior" -> new MinusMountsBehavior(
                    game,
                    game.getPlayer(behaviorPlayerId),
                    reactionPlayers,
                    game.getPlayer(currentReactionPlayerId),
                    cardId,
                    playType,
                    PlayCard.findById(cardId)
            );
            case "NormalActiveKillBehavior" -> new NormalActiveKillBehavior(
                    game,
                    game.getPlayer(behaviorPlayerId),
                    reactionPlayers,
                    game.getPlayer(currentReactionPlayerId),
                    cardId,
                    playType,
                    PlayCard.findById(cardId)
            );
            case "PeachBehavior" -> new PeachBehavior(
                    game,
                    game.getPlayer(behaviorPlayerId),
                    reactionPlayers,
                    game.getPlayer(currentReactionPlayerId),
                    cardId,
                    playType,
                    PlayCard.findById(cardId)
            );
            case "PlusMountsBehavior" -> new PlusMountsBehavior(
                    game,
                    game.getPlayer(behaviorPlayerId),
                    reactionPlayers,
                    game.getPlayer(currentReactionPlayerId),
                    cardId,
                    playType,
                    PlayCard.findById(cardId)
            );
            case "WaitingQilinBowResponseBehavior" -> new WaitingQilinBowResponseBehavior(
                    game,
                    game.getPlayer(behaviorPlayerId),
                    reactionPlayers,
                    game.getPlayer(currentReactionPlayerId),
                    cardId,
                    playType,
                    PlayCard.findById(cardId)
            );
            case "DuelBehavior" -> new DuelBehavior(
                    game,
                    game.getPlayer(behaviorPlayerId),
                    reactionPlayers,
                    game.getPlayer(currentReactionPlayerId),
                    cardId,
                    playType,
                    PlayCard.findById(cardId)
            );
            default -> throw new RuntimeException("Unknown behavior name: " + behaviorName);
        };
        behavior.setIsOneRound(isOneRound);
        behavior.setIsTargetPlayerNeedToResponse(isTargetPlayerNeedToResponse);
        return behavior;
    }


    public static BehaviorData fromDomain(Behavior behavior) {
        return BehaviorData.builder()
                .behaviorName(behavior.getClass().getSimpleName())
                .behaviorPlayerId(behavior.getBehaviorPlayer().getId())
                .reactionPlayers(behavior.getReactionPlayers())
                .currentReactionPlayerId(behavior.getCurrentReactionPlayer().getId())
                .cardId(behavior.getCardId())
                .playType(behavior.getPlayType())
                .isTargetPlayerNeedToResponse(behavior.isTargetPlayerNeedToResponse())
                .isOneRound(behavior.isOneRound())
                .build();
    }
}
