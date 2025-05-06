package com.gaas.threeKingdoms.repository.data;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.behavior.behavior.*;
import com.gaas.threeKingdoms.handcard.PlayCard;
import com.gaas.threeKingdoms.player.Player;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    private Map<String, Object> params;

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
            case "ArrowBarrageBehavior" ->
                    new ArrowBarrageBehavior(
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
            case "DismantleBehavior" -> new DismantleBehavior(
                    game,
                    game.getPlayer(behaviorPlayerId),
                    reactionPlayers,
                    game.getPlayer(currentReactionPlayerId),
                    cardId,
                    playType,
                    PlayCard.findById(cardId)
            );
            case "ContentmentBehavior" -> new ContentmentBehavior(
                    game,
                    game.getPlayer(behaviorPlayerId),
                    reactionPlayers,
                    game.getPlayer(currentReactionPlayerId),
                    cardId,
                    playType,
                    PlayCard.findById(cardId)
            );
            case "BountifulHarvestBehavior" -> {
                BountifulHarvestBehavior bountifulHarvestBehavior = new BountifulHarvestBehavior(
                        game,
                        game.getPlayer(behaviorPlayerId),
                        reactionPlayers,
                        game.getPlayer(currentReactionPlayerId),
                        cardId,
                        playType,
                        PlayCard.findById(cardId)
                );
                bountifulHarvestBehavior.putParam(
                        BountifulHarvestBehavior.BOUNTIFUL_HARVEST_CARDS,
                        params.get(BountifulHarvestBehavior.BOUNTIFUL_HARVEST_CARDS)
                );
                yield bountifulHarvestBehavior;
            }
            case "WardBehavior" -> new WardBehavior(
                    game,
                    behaviorPlayerId != null ? game.getPlayer(behaviorPlayerId) : null,
                    reactionPlayers,
                    currentReactionPlayerId != null ? game.getPlayer(currentReactionPlayerId) : null,
                    cardId,
                    playType,
                    PlayCard.findById(cardId),
                    isTargetPlayerNeedToResponse
            );
            case "SomethingForNothingBehavior" -> new SomethingForNothingBehavior(
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
                .behaviorPlayerId(
                        Optional.ofNullable(behavior.getBehaviorPlayer())
                                .map(Player::getId)
                                .orElse(null))
                .reactionPlayers(behavior.getReactionPlayers())
                .currentReactionPlayerId(
                        Optional.ofNullable(behavior.getCurrentReactionPlayer())
                                .map(Player::getId)
                                .orElse(null))
                .cardId(behavior.getCardId())
                .playType(behavior.getPlayType())
                .isTargetPlayerNeedToResponse(behavior.isTargetPlayerNeedToResponse())
                .isOneRound(behavior.isOneRound())
                .params(behavior.getParams())
                .build();
    }
}
