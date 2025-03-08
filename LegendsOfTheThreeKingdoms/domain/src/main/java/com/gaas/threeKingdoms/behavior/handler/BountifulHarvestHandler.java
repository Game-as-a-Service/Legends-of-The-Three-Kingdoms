package com.gaas.threeKingdoms.behavior.handler;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.behavior.PlayCardBehaviorHandler;
import com.gaas.threeKingdoms.behavior.behavior.BountifulHarvestBehavior;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.scrollcard.BountifulHarvest;
import com.gaas.threeKingdoms.player.Player;

import java.util.*;
import java.util.stream.Collectors;

import static com.gaas.threeKingdoms.behavior.behavior.BountifulHarvestBehavior.BOUNTIFUL_HARVEST_CARDS;

public class BountifulHarvestHandler extends PlayCardBehaviorHandler {
    public BountifulHarvestHandler(PlayCardBehaviorHandler next, Game game) {
        super(next, game);
    }

    @Override
    protected boolean match(String playerId, String cardId, List<String> targetPlayerId, String playType) {
        Player player = getPlayer(playerId);
        Optional<HandCard> card = getCard(cardId, player);
        return card.filter(handCard -> handCard instanceof BountifulHarvest).isPresent();
    }

    @Override
    protected Behavior doHandle(String playerId, String cardId, List<String> targetPlayerIdList, String playType) {
        Player player = game.getPlayer(playerId);
        HandCard card = player.getHand().getCard(cardId).orElseThrow(NoSuchElementException::new);
        List<String> reactivePlayers = new ArrayList<>();

        // 將所有玩家加入 reactivePlayers，且排序為當前玩家之後的玩家
        Player tmpPlayer = player;
        List<Player> players = game.getSeatingChart().getPlayers();
        for (int i = 0; i < players.size(); i++) {
            reactivePlayers.add(tmpPlayer.getId());
            tmpPlayer = game.getNextPlayer(tmpPlayer);
        }
        List<HandCard> cards = game.drawCardForCardEffect(reactivePlayers.size());
        List<String> cardIds = cards.stream().map(HandCard::getId).collect(Collectors.toList());
        BountifulHarvestBehavior bountifulHarvestBehavior = new BountifulHarvestBehavior(game, player, reactivePlayers, player, cardId, playType, card);
        bountifulHarvestBehavior.putParam(BOUNTIFUL_HARVEST_CARDS, cardIds);
        return bountifulHarvestBehavior;
    }
}