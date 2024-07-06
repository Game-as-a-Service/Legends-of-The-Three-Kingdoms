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

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

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

        List<String> reactivePlayers = game.getPlayers().stream()
                .map(Player::getId)
                .filter(id -> !id.equals(playerId))
                .collect(Collectors.toList());

        Player currentReactionPlayer = game.getNextPlayer(player);

        HandCard card = player.getHand().getCard(cardId).orElseThrow(NoSuchElementException::new);

        return new BarbarianInvasionBehavior(game, player, reactivePlayers, currentReactionPlayer, cardId, playType, card);
    }
}
