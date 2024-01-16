package org.gaas.domain.gamephase;

import org.gaas.domain.Game;
import org.gaas.domain.events.DomainEvent;
import org.gaas.domain.events.PlayCardEvent;
import org.gaas.domain.events.PlayerEvent;
import org.gaas.domain.events.RoundEvent;
import org.gaas.domain.player.HealthStatus;
import org.gaas.domain.player.Player;
import java.util.List;

public class GeneralDying extends GamePhase {

    public GeneralDying(Game game) {
        super(game);
        this.phaseName  = "GeneralDying";
    }

    @Override
    public List<DomainEvent> playCard(String playerId, String cardId, String targetPlayerId, String playType) {
        if ("skip".equals(playType)){
            Player dyingPlayer = game.getCurrentRound().getDyingPlayer();
            if (game.getActivePlayer().equals(game.getPrePlayer(dyingPlayer))) {
                dyingPlayer.setHealthStatus(HealthStatus.DEATH);
                game.playerDeadSettlement();
                // TODO 死亡結算。
                List<PlayerEvent> playerEvents =  game.getPlayers().stream().map(PlayerEvent::new).toList();
                RoundEvent roundEvent = new RoundEvent(game.getCurrentRound());

                return List.of(new PlayCardEvent("出牌",playerId, targetPlayerId, cardId, playType, game.getGameId(), playerEvents, roundEvent, game.getGamePhase().getPhaseName()));
            }
            game.getCurrentRound().setActivePlayer(game.getNextPlayer(game.getActivePlayer()));
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
