package org.gaas.domain.behavior.behavior;

import org.gaas.domain.Game;
import org.gaas.domain.Round;
import org.gaas.domain.behavior.Behavior;
import org.gaas.domain.events.*;
import org.gaas.domain.gamephase.GameOver;
import org.gaas.domain.handcard.HandCard;
import org.gaas.domain.handcard.PlayType;
import org.gaas.domain.player.Player;
import org.gaas.domain.rolecard.Role;

import java.util.List;
import java.util.stream.Collectors;

import static org.gaas.domain.handcard.PlayCard.isPeachCard;

public class DyingAskPeachBehavior extends Behavior {
    public DyingAskPeachBehavior(Game game, Player behaviorPlayer, List<String> reactionPlayers, Player currentReactionPlayer, String cardId, String playType, HandCard card) {
        super(game, behaviorPlayer, reactionPlayers, currentReactionPlayer, cardId, playType, card, false);
    }

    @Override
    public List<DomainEvent> askTargetPlayerPlayCard() {
        return null;
    }

    @Override
    protected List<DomainEvent> doAcceptedTargetPlayerPlayCard(String playerId, String targetPlayerId, String cardId, String playType) {
        Player dyingPlayer = game.getPlayer(targetPlayerId);
        Player currentPlayer = game.getPlayer(playerId);

        if (isSkip(playType)) {
            List<PlayerEvent> playerEvents = game.getPlayers().stream().map(PlayerEvent::new).toList();
            AskPeachEvent askPeachEvent = createAskPeachEvent(game.getNextPlayer(currentPlayer));
            if (reactionPlayers.get(reactionPlayers.size() - 1).equals(playerId)) {
                isNeedToPop = true;
                if (isMonarchDied(dyingPlayer)) {
                    Round currentRound = game.getCurrentRound();
                    RoundEvent roundEvent = new RoundEvent(currentRound);
                    PlayCardEvent playCardEvent = new PlayCardEvent("不出牌", playerId, targetPlayerId, cardId, playType, game.getGameId(), playerEvents, roundEvent, game.getGamePhase().getPhaseName());
                    SettlementEvent settlementEvent = new SettlementEvent(dyingPlayer.getId(), dyingPlayer.getRoleCard().getRole().getRoleName(), "死亡玩家為主公，遊戲結束");
                    GameOverEvent gameOverEvent = new GameOverEvent(game.createGameOverMessage(), getWinners(game.getPlayers()), playerEvents);
                    game.enterPhase(new GameOver(game));
                    return List.of(playCardEvent, settlementEvent, gameOverEvent);
                }
            }
            Round currentRound = game.getCurrentRound();
            currentRound.setActivePlayer(game.getNextPlayer(currentPlayer));
            RoundEvent roundEvent = new RoundEvent(currentRound);
            PlayCardEvent playCardEvent = new PlayCardEvent("不出牌", playerId, targetPlayerId, cardId, playType, game.getGameId(), playerEvents, roundEvent, game.getGamePhase().getPhaseName());
            return List.of(playCardEvent, askPeachEvent);
        } else if (isPeachCard(cardId)) {
//            dyingPlayer.playCard(cardId);
//            RoundEvent roundEvent = new RoundEvent(game.getCurrentRound());
//            PlayerDamagedEvent playerDamagedEvent = createPlayerDamagedEvent(originalHp, dyingPlayer);
//            List<PlayerEvent> playerEvents = game.getPlayers().stream().map(PlayerEvent::new).toList();
//            PlayCardEvent playCardEvent = new PlayCardEvent("出牌", playerId, targetPlayerId, cardId, playType, game.getGameId(), playerEvents, roundEvent, game.getGamePhase().getPhaseName());
//            return List.of(playCardEvent, playerDamagedEvent);
            return null;
        } else {
            //TODO:怕有其他效果或殺的其他case
            return null;
        }
    }

    private static boolean isMonarchDied(Player dyingPlayer) {
        return dyingPlayer.getRoleCard().getRole().equals(Role.MONARCH);
    }

    private List<String> getWinners(List<Player> players) {
        return players.stream()
                .filter(player -> player.getRoleCard().getRole().equals(Role.REBEL))
                .map(Player::getId)
                .collect(Collectors.toList());
    }

    private static String findNextPlayer(List<String> players, String currentPlayer) {
        int currentIndex = players.indexOf(currentPlayer);
        if (currentIndex != -1 && currentIndex < players.size() - 1) {
            return players.get(currentIndex + 1);
        }
        return null;
    }

    private AskPeachEvent createAskPeachEvent(Player player) {
        return new AskPeachEvent(player.getId());
    }

    private void playerPlayCard(Player player, Player targetPlayer, String cardId) {
        HandCard handCard = player.playCard(cardId);
        card = handCard;
        game.updateRoundInformation(targetPlayer, handCard);
        game.getGraveyard().add(handCard);
    }

    private boolean isSkip(String playType) {
        return PlayType.SKIP.getPlayType().equals(playType);
    }
}
