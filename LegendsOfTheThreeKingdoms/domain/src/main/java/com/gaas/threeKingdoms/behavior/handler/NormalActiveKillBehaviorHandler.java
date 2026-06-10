package com.gaas.threeKingdoms.behavior.handler;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.behavior.PlayCardBehaviorHandler;
import com.gaas.threeKingdoms.behavior.behavior.NormalActiveKillBehavior;
import com.gaas.threeKingdoms.exception.DistanceErrorException;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.player.Player;

import java.util.List;
import java.util.Optional;

public class NormalActiveKillBehaviorHandler extends PlayCardBehaviorHandler {


    public NormalActiveKillBehaviorHandler(PlayCardBehaviorHandler next, Game game) {
        super(next, game);
    }

    //這個牌型是不是殺、有沒出過殺、是不是當前人員、targetPlayerIds 是不是他可以殺的距離
    @Override
    protected boolean match(String playerId, String cardId, List<String> reactionPlayers, String playType) {
        Optional<HandCard> cardOpt = getCard(cardId, getPlayer(playerId));
        cardOpt.orElseThrow(() -> new IllegalStateException("Player " + playerId + " have no this card: " + cardId));
        return cardOpt.filter(handCard -> handCard instanceof Kill &&
                        isPlayedValidCard(cardId) &&
                        isDistanceTooLong(game.getPlayer(playerId), game.getPlayer(reactionPlayers.get(0))))
                        .isPresent();
    }


    @Override
    protected Behavior doHandle(String playerId, String cardId, List<String> reactionPlayers, String playType) {
        Player player = game.getPlayer(playerId);
        HandCard killCard = player.getHand().getCard(cardId).get();
        // 空城等：目標不能被殺指定
        Player target = game.getPlayer(reactionPlayers.get(0));
        if (com.gaas.threeKingdoms.skill.registry.SkillEngine.isImmuneToCard(target, killCard)) {
            throw new IllegalStateException(
                    String.format("%s cannot be targeted by Kill (target immunity skill)", target.getId()));
        }
        return new NormalActiveKillBehavior(game, player, reactionPlayers, player, cardId, playType, killCard);
    }


    private boolean isPlayedValidCard(String cardId) {
        return game.getCurrentRound().isPlayedValidCard(cardId);
    }

    private boolean isDistanceTooLong(Player player, Player targetPlayer) {
        if (!game.isInAttackRange(player, targetPlayer)) {
            throw new DistanceErrorException("Players are not within range.");
        }
        return true;
    }
}
