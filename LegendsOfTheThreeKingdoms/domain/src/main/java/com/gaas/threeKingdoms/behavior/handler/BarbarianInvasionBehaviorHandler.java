package com.gaas.threeKingdoms.behavior.handler;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.behavior.PlayCardBehaviorHandler;
import com.gaas.threeKingdoms.behavior.behavior.BarbarianInvasionBehavior;
import com.gaas.threeKingdoms.behavior.behavior.EquipArmorBehavior;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.ArmorCard;
import com.gaas.threeKingdoms.handcard.scrollcard.BarbarianInvasion;
import com.gaas.threeKingdoms.handcard.scrollcard.ScrollCard;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.round.Round;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

        // 將所有玩家加入 reactivePlayers，除了當前玩家，且排序為當前玩家之後的玩家
        Player tmpPlayer = currentReactionPlayer;
        for (int i = 0; i < game.getPlayers().size() - 1; i++) {
            reactivePlayers.add(tmpPlayer.getId());
            tmpPlayer = game.getNextPlayer(tmpPlayer);
        }

        HandCard card = player.getHand().getCard(cardId).orElseThrow(NoSuchElementException::new);

        return new BarbarianInvasionBehavior(game, player, reactivePlayers, currentReactionPlayer, cardId, playType, card);
    }
}
