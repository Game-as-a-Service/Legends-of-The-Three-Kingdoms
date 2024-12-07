package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.round.Round;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.gamephase.GameOver;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.rolecard.Role;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.gaas.threeKingdoms.handcard.PlayCard.isPeachCard;

public class DyingAskPeachBehavior extends Behavior {
    public DyingAskPeachBehavior(Game game, Player behaviorPlayer, List<String> reactionPlayers, Player currentReactionPlayer, String cardId, String playType, HandCard card) {
        super(game, behaviorPlayer, reactionPlayers, currentReactionPlayer, cardId, playType, card, true, false);
        List<Player> players = game.getSeatingChart().getPlayers();
        int damagedPlayerIndex = players.indexOf(behaviorPlayer);
        List<Player> reorderedPlayerList = new ArrayList<>();
        reorderedPlayerList.addAll(players.subList(damagedPlayerIndex, players.size()));
        reorderedPlayerList.addAll(players.subList(0, damagedPlayerIndex));
        this.reactionPlayers = reorderedPlayerList.stream().map(Player::getId).toList();
    }

    @Override
    public List<DomainEvent> playerAction() {
        return null;
    }

    @Override
    protected List<DomainEvent> doResponseToPlayerAction(String playerId, String targetPlayerId, String cardId, String playType) {
        Player dyingPlayer = game.getPlayer(targetPlayerId);
        Player currentPlayer = game.getPlayer(playerId);
        int originalHp = dyingPlayer.getHP();
        List<DomainEvent> events = new ArrayList<>();
        if (isSkip(playType)) {
            List<PlayerEvent> playerEvents = game.getPlayers().stream().map(PlayerEvent::new).toList();
            Round currentRound = game.getCurrentRound();
            if (isLastReactionPlayer(playerId)) {
                isOneRound = true;
                game.removeDyingPlayer(dyingPlayer);
                if (isMonarchDied(dyingPlayer)) {
                    PlayCardEvent playCardEvent = new PlayCardEvent("不出牌", playerId, targetPlayerId, cardId, playType);
                    SettlementEvent settlementEvent = new SettlementEvent(dyingPlayer.getId(), dyingPlayer.getRoleCard().getRole().getRoleName());
                    GameOverEvent gameOverEvent = new GameOverEvent(game.createGameOverMessage(), getWinners(game.getPlayers()), playerEvents);
                    game.enterPhase(new GameOver(game));
                    addAskKillEventIfCurrentBehaviorIsBarbarianInvasionBehavior(events);
                    events.addAll(List.of(playCardEvent, settlementEvent, gameOverEvent, game.getGameStatusEvent("主公死亡")));
                    return events;
                }
                game.enterPhase(new Normal(game));
                currentRound.setActivePlayer(currentRound.getCurrentRoundPlayer());
            } else {
                events.add(createAskPeachEvent(game.getNextPlayer(currentPlayer), dyingPlayer));
                currentRound.setActivePlayer(game.getNextPlayer(currentPlayer));
            }
            PlayCardEvent playCardEvent = new PlayCardEvent("不出牌", playerId, targetPlayerId, cardId, playType);
            events.addAll(List.of(playCardEvent, game.getGameStatusEvent("不出牌")));
            return events;
        } else if (isPeachCard(cardId)) {

            // Player use peach card
            HandCard card = currentPlayer.playCard(cardId);
            card.effect(dyingPlayer);
            Round currentRound = game.getCurrentRound();

            // Dying player is healed
            currentRound.setDyingPlayer(null);
            currentRound.setActivePlayer(currentRound.getCurrentRoundPlayer());
            game.enterPhase(new Normal(game));
            isOneRound = true;

            // Create Domain Events
            PlayCardEvent playCardEvent = new PlayCardEvent("出牌", playerId, targetPlayerId, cardId, playType);
            PeachEvent peachEvent = new PeachEvent(targetPlayerId, originalHp, dyingPlayer.getHP());
            addAskKillEventIfCurrentBehaviorIsBarbarianInvasionBehavior(events);
            events.addAll(List.of(playCardEvent, peachEvent, game.getGameStatusEvent("出牌")));
            return events;

        } else {
            //TODO:怕有其他效果或殺的其他case
            return null;
        }
    }

    private boolean isLastReactionPlayer(String playerId) {
        return reactionPlayers.get(reactionPlayers.size() - 1).equals(playerId);
    }

    private void addAskKillEventIfCurrentBehaviorIsBarbarianInvasionBehavior(List<DomainEvent> events) {
        Behavior secondBehavior = game.peekTopBehaviorSecondElement();
        if (secondBehavior instanceof BarbarianInvasionBehavior) {
            Player barbarianInvasionCurrentReactionPlayer = secondBehavior.getCurrentReactionPlayer();
            events.add(new AskKillEvent(barbarianInvasionCurrentReactionPlayer.getId()));
            game.getCurrentRound().setActivePlayer(barbarianInvasionCurrentReactionPlayer);
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

    private AskPeachEvent createAskPeachEvent(Player player, Player dyingPlayer) {
        return new AskPeachEvent(player.getId(), dyingPlayer.getId());
    }


    private boolean isSkip(String playType) {
        return PlayType.SKIP.getPlayType().equals(playType);
    }
}
