package com.gaas.threeKingdoms.behavior.handler;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.behavior.PlayCardBehaviorHandler;
import com.gaas.threeKingdoms.behavior.behavior.BarbarianInvasionBehavior;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.scrollcard.BarbarianInvasion;
import com.gaas.threeKingdoms.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public class BarbarianInvasionBehaviorHandler extends PlayCardBehaviorHandler {

    public BarbarianInvasionBehaviorHandler(PlayCardBehaviorHandler next, Game game) {
        super(next, game);
    }

    @Override
    protected boolean match(String playerId, String cardId, List<String> targetPlayerId, String playType) {
        Player player = getPlayer(playerId);
        Optional<HandCard> card = getCard(cardId, player);
        return card.filter(handCard -> handCard instanceof BarbarianInvasion).isPresent();
    }

    @Override
    protected Behavior doHandle(String playerId, String cardId, List<String> targetPlayerId, String playType) {
        Player player = game.getPlayer(playerId);
        Player currentReactionPlayer = game.getNextPlayer(player);
        List<String> reactivePlayers = new ArrayList<>();

        HandCard card = player.getHand().getCard(cardId).orElseThrow(NoSuchElementException::new);

        // 將所有玩家加入 reactivePlayers，除了當前玩家，且排序為當前玩家之後的玩家；
        // 謙遜等目標免疫技能的玩家直接排除（不成為 AOE 目標）
        Player tmpPlayer = currentReactionPlayer;
        List<Player> players = game.getSeatingChart().getPlayers();
        for (int i = 0; i < players.size() - 1; i++) {
            if (!com.gaas.threeKingdoms.skill.registry.SkillEngine.isImmuneToCard(tmpPlayer, card)) {
                reactivePlayers.add(tmpPlayer.getId());
            }
            tmpPlayer = game.getNextPlayer(tmpPlayer);
        }
        if (reactivePlayers.isEmpty()) {
            throw new IllegalStateException("AOE has no valid targets (all immune)");
        }
        currentReactionPlayer = game.getPlayer(reactivePlayers.get(0));

        return new BarbarianInvasionBehavior(game, player, reactivePlayers, currentReactionPlayer, cardId, playType, card);
    }
}
