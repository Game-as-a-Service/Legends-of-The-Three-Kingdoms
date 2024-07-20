package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.AskKillEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.PlayCardEvent;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.player.Player;

import java.util.ArrayList;
import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.isKillCard;
import static com.gaas.threeKingdoms.handcard.PlayCard.isSkip;

public class BarbarianInvasionBehavior extends Behavior {
    public BarbarianInvasionBehavior(Game game, Player behaviorPlayer, List<String> reactionPlayers, Player currentReactionPlayer, String cardId, String playType, HandCard card) {
        super(game, behaviorPlayer, reactionPlayers, currentReactionPlayer, cardId, playType, card, true, false);
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
        events.add(new AskKillEvent(currentReactionPlayerId));
        events.add(game.getGameStatusEvent("發動南蠻入侵"));

        return events;
    }

    @Override
    protected List<DomainEvent> doResponseToPlayerAction(String playerId, String targetPlayerId, String cardId, String playType) {

        if (isSkip(playType)) {
            int originalHp = currentReactionPlayer.getHP();
            List<DomainEvent> damagedEvent = game.getDamagedEvent(playerId, targetPlayerId, cardId, card, playType, originalHp, currentReactionPlayer, game.getCurrentRound(), this);

            // Remove the current player to next player
            currentReactionPlayer = game.getNextPlayer(currentReactionPlayer);

            List<DomainEvent> events = new ArrayList<>(damagedEvent);

            if (!game.getGamePhase().getPhaseName().equals("GeneralDying")) { // 如果受到傷害且沒死亡
                AskKillEvent askKillEvent = new AskKillEvent(currentReactionPlayer.getId());
                events.add(new AskKillEvent(currentReactionPlayer.getId()));
                game.getCurrentRound().setActivePlayer(currentReactionPlayer);

                // 最後一個人
                if (reactionPlayers.get(reactionPlayers.size() - 1).equals(playerId)) {
                    isOneRound = true;
                    events.remove(askKillEvent);
                }
            }

            return events;
        } else if (isKillCard(cardId)) {
            List<DomainEvent> events = new ArrayList<>();
            currentReactionPlayer = game.getNextPlayer(currentReactionPlayer);
            game.getCurrentRound().setActivePlayer(currentReactionPlayer);
            events.add(game.getGameStatusEvent(playerId + "出skip"));
            AskKillEvent askKillEvent = new AskKillEvent(currentReactionPlayer.getId());
            events.add(new PlayCardEvent("出牌", playerId, targetPlayerId, cardId, playType));
            events.add(askKillEvent);
            // 最後一個人，結束此behavior，askKillEvent不再出現
            if (reactionPlayers.get(reactionPlayers.size() - 1).equals(playerId)) {
                isOneRound = true;
                events.remove(askKillEvent);
            }
            return events;
        } else {
            //TODO:怕有其他效果或殺的其他case
        }
        return null;
    }

}
