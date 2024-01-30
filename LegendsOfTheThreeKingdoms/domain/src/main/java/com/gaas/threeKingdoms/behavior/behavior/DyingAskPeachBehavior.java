package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.Round;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.gamephase.GameOver;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.rolecard.Role;

import java.util.List;
import java.util.stream.Collectors;

import static com.gaas.threeKingdoms.handcard.PlayCard.isPeachCard;

public class DyingAskPeachBehavior extends Behavior {
    public DyingAskPeachBehavior(Game game, Player behaviorPlayer, List<String> reactionPlayers, Player currentReactionPlayer, String cardId, String playType, HandCard card) {
        super(game, behaviorPlayer, reactionPlayers, currentReactionPlayer, cardId, playType, card, true, false);
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


    private boolean isSkip(String playType) {
        return PlayType.SKIP.getPlayType().equals(playType);
    }
}
