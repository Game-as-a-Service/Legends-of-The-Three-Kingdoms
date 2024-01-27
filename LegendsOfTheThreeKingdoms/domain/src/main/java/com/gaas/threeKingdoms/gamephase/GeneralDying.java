package com.gaas.threeKingdoms.gamephase;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.PlayCardEvent;
import com.gaas.threeKingdoms.events.PlayerEvent;
import com.gaas.threeKingdoms.events.RoundEvent;
import com.gaas.threeKingdoms.player.HealthStatus;
import com.gaas.threeKingdoms.player.Player;
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
