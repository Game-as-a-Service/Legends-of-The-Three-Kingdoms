package com.gaas.threeKingdoms.behavior.handler;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.behavior.PlayCardBehaviorHandler;
import com.gaas.threeKingdoms.behavior.behavior.ContentmentBehavior;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.scrollcard.Contentment;
import com.gaas.threeKingdoms.player.Player;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

public class ContentmentBehaviorHandler extends PlayCardBehaviorHandler {
    public ContentmentBehaviorHandler(PlayCardBehaviorHandler next, Game game) {
        super(next, game);
    }

    @Override
    protected boolean match(String playerId, String cardId, List<String> targetPlayerId, String playType) {
        Player player = getPlayer(playerId);
        Optional<HandCard> card = getCard(cardId, player);
        return card.filter(handCard -> handCard instanceof Contentment).isPresent();
    }

    @Override
    protected Behavior doHandle(String playerId, String cardId, List<String> targetPlayerIdList, String playType) {
        Player player = game.getPlayer(playerId);
        Player currentReactionPlayer = game.getPlayer(targetPlayerIdList.get(0));
        HandCard card = player.getHand().getCard(cardId).orElseThrow(NoSuchElementException::new);

        String errorMessage1 = "不可以對自己出牌者出樂不思蜀";
        String errorMessage2 = String.format("%s 已經有樂不思蜀", currentReactionPlayer.getId());

        if (Objects.equals(player.getId(), currentReactionPlayer.getId())) {
            throw new IllegalArgumentException(errorMessage1);
        }

        if (currentReactionPlayer.hasAnyContentmentCard()) {
            throw new IllegalArgumentException(errorMessage2);
        }

        return new ContentmentBehavior(game, player, targetPlayerIdList, currentReactionPlayer, cardId, playType, card);
    }
}
