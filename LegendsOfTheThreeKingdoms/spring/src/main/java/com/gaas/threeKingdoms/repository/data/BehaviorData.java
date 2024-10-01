package com.gaas.threeKingdoms.repository.data;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.repository.data.PlayerData;
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
    private String className;
    private PlayerData behaviorPlayer;
    private List<String> reactionPlayers;
    private PlayerData currentReactionPlayer;
    private String cardId;
    private String playType;
    private boolean isTargetPlayerNeedToResponse = true;
    private boolean isOneRound = true;

    public Behavior toDomain(Game game) {
        try {
            Class<?> clazz = Class.forName(className);
            return (Behavior) clazz.getConstructor(Game.class, Player.class, List.class, Player.class,
                            String.class, String.class, HandCard.class,
                            boolean.class, boolean.class)
                    .newInstance(game,
                            behaviorPlayer.toDomain(),
                            reactionPlayers,
                            currentReactionPlayer.toDomain(),
                            cardId,
                            playType,
                            PlayCard.findById(cardId),
                            isTargetPlayerNeedToResponse,
                            isOneRound);
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate behavior class: " + className, e);
        }
    }

    public static BehaviorData fromDomain(Behavior behavior) {
        return BehaviorData.builder()
                .className(behavior.getClass().getName())
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
