package com.gaas.threeKingdoms.behavior.handler;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.behavior.PlayCardBehaviorHandler;
import com.gaas.threeKingdoms.behavior.behavior.EquipWeaponBehavior;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.WeaponCard;
import com.gaas.threeKingdoms.player.Player;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

public class EquipWeaponBehaviorHandler extends PlayCardBehaviorHandler {

    public EquipWeaponBehaviorHandler(PlayCardBehaviorHandler next, Game game) {
        super(next, game);
    }

    @Override
    protected boolean match(String playerId, String cardId, List<String> targetPlayerId, String playType) {
        Player player = getPlayer(playerId);
        Optional<HandCard> card = getCard(cardId, player);
        return card.filter(handCard -> handCard instanceof WeaponCard).isPresent();
    }

    @Override
    protected Behavior doHandle(String playerId, String cardId, List<String> targetPlayerId, String playType) {
        Player player = game.getPlayer(playerId);

        List<String> players = game.getPlayers().stream()
                .map(Player::getId)
                .collect(Collectors.toList());

        HandCard card = player.getHand().getCard(cardId).orElseThrow(NoSuchElementException::new);

        return new EquipWeaponBehavior(game, player, players, player, cardId, playType, card);
    }
}
