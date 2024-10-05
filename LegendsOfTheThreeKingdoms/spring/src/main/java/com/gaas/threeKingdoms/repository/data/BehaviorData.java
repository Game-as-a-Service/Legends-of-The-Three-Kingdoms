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
    private PlayerData behaviorPlayer;
    private List<String> reactionPlayers;
    private PlayerData currentReactionPlayer;
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
                        behaviorPlayer.toDomain(),
                        reactionPlayers,
                        currentReactionPlayer.toDomain(),
                        cardId,
                        playType,
                        PlayCard.findById(cardId)
                );
            case "BorrowedSwordBehavior" -> new BorrowedSwordBehavior(
                    game,
                    behaviorPlayer.toDomain(),
                    reactionPlayers,
                    currentReactionPlayer.toDomain(),
                    cardId,
                    playType,
                    PlayCard.findById(cardId)
            );
            case "DyingAskPeachBehavior" -> new DyingAskPeachBehavior(
                    game,
                    behaviorPlayer.toDomain(),
                    reactionPlayers,
                    currentReactionPlayer.toDomain(),
                    cardId,
                    playType,
                    PlayCard.findById(cardId)
            );
            case "EquipArmorBehavior" -> new EquipArmorBehavior(
                    game,
                    behaviorPlayer.toDomain(),
                    reactionPlayers,
                    currentReactionPlayer.toDomain(),
                    cardId,
                    playType,
                    PlayCard.findById(cardId)
            );
            case "EquipWeaponBehavior" -> new EquipWeaponBehavior(
                    game,
                    behaviorPlayer.toDomain(),
                    reactionPlayers,
                    currentReactionPlayer.toDomain(),
                    cardId,
                    playType,
                    PlayCard.findById(cardId)
            );
            case "MinusMountsBehavior" -> new MinusMountsBehavior(
                    game,
                    behaviorPlayer.toDomain(),
                    reactionPlayers,
                    currentReactionPlayer.toDomain(),
                    cardId,
                    playType,
                    PlayCard.findById(cardId)
            );
            case "NormalActiveKillBehavior" -> new NormalActiveKillBehavior(
                    game,
                    behaviorPlayer.toDomain(),
                    reactionPlayers,
                    currentReactionPlayer.toDomain(),
                    cardId,
                    playType,
                    PlayCard.findById(cardId)
            );
            case "PeachBehavior" -> new PeachBehavior(
                    game,
                    behaviorPlayer.toDomain(),
                    reactionPlayers,
                    currentReactionPlayer.toDomain(),
                    cardId,
                    playType,
                    PlayCard.findById(cardId)
            );
            case "PlusMountsBehavior" -> new PlusMountsBehavior(
                    game,
                    behaviorPlayer.toDomain(),
                    reactionPlayers,
                    currentReactionPlayer.toDomain(),
                    cardId,
                    playType,
                    PlayCard.findById(cardId)
            );
            case "WaitingQilinBowResponseBehavior" -> new WaitingQilinBowResponseBehavior(
                    game,
                    behaviorPlayer.toDomain(),
                    reactionPlayers,
                    currentReactionPlayer.toDomain(),
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
                .behaviorPlayer(PlayerData.fromDomain(behavior.getBehaviorPlayer()))
                .reactionPlayers(behavior.getReactionPlayers())
                .currentReactionPlayer(PlayerData.fromDomain(behavior.getCurrentReactionPlayer()))
                .cardId(behavior.getCardId())
                .playType(behavior.getPlayType())
                .isTargetPlayerNeedToResponse(behavior.isTargetPlayerNeedToResponse())
                .isOneRound(behavior.isOneRound())
                .build();
    }
}
