package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.AskKillEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.DuelEvent;
import com.gaas.threeKingdoms.events.PlayCardEvent;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.player.Player;

import java.util.ArrayList;
import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.isKillCard;
import static com.gaas.threeKingdoms.handcard.PlayCard.isSkip;

public class DuelBehavior extends Behavior {
    public DuelBehavior(Game game, Player behaviorPlayer, List<String> reactionPlayers, Player currentReactionPlayer, String cardId, String playType, HandCard card) {
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
                currentReactionPlayerId,
                cardId,
                playType));
        events.add(new DuelEvent(behaviorPlayer.getId(), currentReactionPlayerId, cardId));

        // 判斷 currentReactionPlayer 是否有殺
        if (!currentReactionPlayer.getHand().hasTypeInHand(Kill.class)) {
            int originalHp = currentReactionPlayer.getHP();
            List<DomainEvent> damagedEvents = game.getDamagedEvent(
                    behaviorPlayer.getId(),
                    currentReactionPlayerId,
                    cardId,
                    card,
                    playType,
                    originalHp,
                    currentReactionPlayer,
                    game.getCurrentRound(),
                    this);
            events.addAll(damagedEvents);
        } else {
            events.add(new AskKillEvent(currentReactionPlayerId));
        }

        events.add(game.getGameStatusEvent("發動決鬥"));

        return events;
    }

    @Override
    protected List<DomainEvent> doResponseToPlayerAction(String playerId, String targetPlayerId, String cardId, String playType) {

        if (isSkip(playType)) {
            int originalHp = currentReactionPlayer.getHP();
            List<DomainEvent> damagedEvent = game.getDamagedEvent(playerId, targetPlayerId, cardId, card, playType, originalHp, currentReactionPlayer, game.getCurrentRound(), this);
            damagedEvent.add(game.getGameStatusEvent("扣血"));
            return damagedEvent;
        } else if (isKillCard(cardId)) {
            List<DomainEvent> events = new ArrayList<>();
            String behaviorPlayerId = behaviorPlayer.getId();
            playerPlayCardNotUpdateActivePlayer(game.getPlayer(playerId), cardId);

            boolean currentReactionPlayerIsDuelPlayer = currentReactionPlayer.getId().equals(behaviorPlayerId);
            currentReactionPlayer = currentReactionPlayerIsDuelPlayer ? game.getPlayer(reactionPlayers.get(0)) : behaviorPlayer;
            game.getCurrentRound().setActivePlayer(currentReactionPlayer);
            events.add(new PlayCardEvent("出牌", playerId, targetPlayerId, cardId, playType));

            String gameMessage = "";
            if (!currentReactionPlayer.getHand().hasTypeInHand(Kill.class)) {
                int originalHp = currentReactionPlayer.getHP();
                List<DomainEvent> damagedEvents = game.getDamagedEvent(
                        behaviorPlayer.getId(),
                        currentReactionPlayer.getId(),
                        cardId,
                        card,
                        playType,
                        originalHp,
                        currentReactionPlayer,
                        game.getCurrentRound(),
                        this);
                events.addAll(damagedEvents);
                gameMessage = "扣血";
            } else {
                AskKillEvent askKillEvent = new AskKillEvent(currentReactionPlayer.getId());
                events.add(askKillEvent);
                gameMessage = playerId + "已出殺";
            }
            events.add(game.getGameStatusEvent(gameMessage));
            return events;
        } else {
            //TODO:怕有其他效果或殺的其他case
        }
        return null;
    }

}