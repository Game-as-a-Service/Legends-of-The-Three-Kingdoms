package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.BountifulHarvestChooseCardEvent;
import com.gaas.threeKingdoms.events.BountifulHarvestEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.PlayCardEvent;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayCard;
import com.gaas.threeKingdoms.player.Player;

import java.util.ArrayList;
import java.util.List;

public class BountifulHarvestBehavior extends Behavior {

    public static final String BOUNTIFUL_HARVEST_CARDS = "BOUNTIFUL_HARVEST_CARDS";

    public BountifulHarvestBehavior(Game game, Player player, List<String> reactivePlayers, Player currentReactionPlayer, String cardId, String playType, HandCard card) {
        super(game, player, reactivePlayers, currentReactionPlayer, cardId, playType, card, true, false, false);
    }

    @Override
    public boolean isOneRoundDefault() {
        return false;
    }

    @Override
    public List<DomainEvent> playerAction() {
        List<DomainEvent> events = new ArrayList<>();
        String currentReactionPlayerId = currentReactionPlayer.getId();
        playerPlayCard(behaviorPlayer, currentReactionPlayer, cardId);

        events.add(new PlayCardEvent(
                "出牌",
                behaviorPlayer.getId(),
                "",
                cardId,
                playType));
        events.add(new BountifulHarvestEvent("輪到 " + currentReactionPlayer.getGeneralName() + " 選牌", currentReactionPlayerId, (List<String>) getParam(BOUNTIFUL_HARVEST_CARDS)));
        events.add(game.getGameStatusEvent("發動五穀豐登"));

        return events;
    }

    @Override
    protected List<DomainEvent> doResponseToPlayerAction(String playerId, String targetPlayerId, String cardId, String playType) {
        List<String> drawCardIds = (List<String>) params.get(BountifulHarvestBehavior.BOUNTIFUL_HARVEST_CARDS);
        drawCardIds.remove(cardId);
        currentReactionPlayer.getHand().addCardToHand(PlayCard.findById(cardId));
        List<DomainEvent> events = new ArrayList<>();
        events.add(new BountifulHarvestChooseCardEvent(currentReactionPlayer, cardId));
        currentReactionPlayer = game.getNextPlayer(currentReactionPlayer);
        game.getCurrentRound().setActivePlayer(currentReactionPlayer);

        if (isLastReactionPlayers(playerId)) {
            isOneRound = true;
        } else {
            events.add(new BountifulHarvestEvent("輪到 " + currentReactionPlayer.getGeneralName() + " 選牌", currentReactionPlayer.getId(), drawCardIds));
        }
        events.add(game.getGameStatusEvent("五穀豐登選牌"));
        return events;
    }

    public boolean isLastReactionPlayers(String playerId) {
        return reactionPlayers.get(reactionPlayers.size() - 1).equals(playerId);
    }

    
}
