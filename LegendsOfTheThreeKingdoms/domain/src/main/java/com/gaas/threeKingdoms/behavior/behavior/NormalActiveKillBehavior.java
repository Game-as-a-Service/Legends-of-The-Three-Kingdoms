package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.Round;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.gamephase.GeneralDying;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.player.Player;

import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.isDodgeCard;


public class NormalActiveKillBehavior extends Behavior {
    public NormalActiveKillBehavior(Game game, Player behaviorPlayer, List<String> reactionPlayers, Player currentReactionPlayer, String cardId, String playType, HandCard card) {
        super(game, behaviorPlayer, reactionPlayers, currentReactionPlayer, cardId, playType, card, true, true);
    }


    @Override
    public List<DomainEvent> askTargetPlayerPlayCard() {
        String targetPlayerId = reactionPlayers.get(0);
        playerPlayCard(behaviorPlayer, game.getPlayer(targetPlayerId), cardId);
        Round currentRound = game.getCurrentRound();

        RoundEvent roundEvent = new RoundEvent(currentRound);

        List<PlayerEvent> playerEvents = game.getPlayers().stream().map(PlayerEvent::new).toList();
        return List.of(new PlayCardEvent("出牌", behaviorPlayer.getId(), targetPlayerId, cardId, playType, game.getGameId(), playerEvents, roundEvent, game.getGamePhase().getPhaseName()));
    }

    @Override
    public List<DomainEvent> doAcceptedTargetPlayerPlayCard(String playerId, String targetPlayerId, String cardId, String playType) {
        Player damagedPlayer = game.getPlayer(playerId);
        int originalHp = damagedPlayer.getHP();

        if (isSkip(playType)) {
            card.effect(damagedPlayer);
            List<PlayerEvent> playerEvents = game.getPlayers().stream().map(PlayerEvent::new).toList();
            PlayerDamagedEvent playerDamagedEvent = createPlayerDamagedEvent(originalHp, damagedPlayer);

            if (isPlayerStillAlive(damagedPlayer)) {
                RoundEvent roundEvent = new RoundEvent(game.getCurrentRound());
                PlayCardEvent playCardEvent = new PlayCardEvent("不出牌", playerId, targetPlayerId, cardId, playType, game.getGameId(), playerEvents, roundEvent, game.getGamePhase().getPhaseName());
                return List.of(playCardEvent, playerDamagedEvent);
            } else {
                PlayerDyingEvent playerDyingEvent = createPlayerDyingEvent(damagedPlayer);
                AskPeachEvent askPeachEvent = createAskPeachEvent(damagedPlayer, damagedPlayer);
                game.enterPhase(new GeneralDying(game));
                Round currentRound = game.getCurrentRound();
                currentRound.setDyingPlayer(damagedPlayer);
                currentRound.setActivePlayer(damagedPlayer);
                RoundEvent roundEvent = new RoundEvent(currentRound);
                PlayCardEvent playCardEvent = new PlayCardEvent("不出牌", playerId, targetPlayerId, cardId, playType, game.getGameId(), playerEvents, roundEvent, game.getGamePhase().getPhaseName());
                isOneRound = false;
                return List.of(playCardEvent, playerDamagedEvent, playerDyingEvent, askPeachEvent);
            }
        } else if (isDodgeCard(cardId)) {
            damagedPlayer.playCard(cardId);
            RoundEvent roundEvent = new RoundEvent(game.getCurrentRound());
            PlayerDamagedEvent playerDamagedEvent = createPlayerDamagedEvent(originalHp, damagedPlayer);
            List<PlayerEvent> playerEvents = game.getPlayers().stream().map(PlayerEvent::new).toList();
            PlayCardEvent playCardEvent = new PlayCardEvent("出牌", playerId, targetPlayerId, cardId, playType, game.getGameId(), playerEvents, roundEvent, game.getGamePhase().getPhaseName());
            return List.of(playCardEvent, playerDamagedEvent);
        } else {
            //TODO:怕有其他效果或殺的其他case
            return null;
        }
    }


    private boolean isPlayerStillAlive(Player damagedPlayer) {
        return damagedPlayer.getHP() > 0;
    }

    private boolean isSkip(String playType) {
        return PlayType.SKIP.getPlayType().equals(playType);
    }

    private PlayerDamagedEvent createPlayerDamagedEvent(int originalHp, Player damagedPlayer) {
        return new PlayerDamagedEvent(damagedPlayer.getId(), originalHp, damagedPlayer.getHP());
    }

    private PlayerDyingEvent createPlayerDyingEvent(Player player) {
        return new PlayerDyingEvent(player.getId());
    }

    private AskPeachEvent createAskPeachEvent(Player player, Player dyingPlayer) {
        return new AskPeachEvent(player.getId(), dyingPlayer.getId());
    }

}
