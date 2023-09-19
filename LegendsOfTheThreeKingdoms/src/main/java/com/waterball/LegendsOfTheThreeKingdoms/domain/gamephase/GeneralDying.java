package com.waterball.LegendsOfTheThreeKingdoms.domain.gamephase;

import com.waterball.LegendsOfTheThreeKingdoms.domain.Game;
import com.waterball.LegendsOfTheThreeKingdoms.domain.events.DomainEvent;
import com.waterball.LegendsOfTheThreeKingdoms.domain.events.PlayCardEvent;
import com.waterball.LegendsOfTheThreeKingdoms.domain.events.PlayerEvent;
import com.waterball.LegendsOfTheThreeKingdoms.domain.events.RoundEvent;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.HealthStatus;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.Player;
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

                return List.of(new PlayCardEvent(playerId, targetPlayerId, cardId, playType, game.getGameId(), playerEvents, roundEvent, game.getGamePhase().getPhaseName()));
            }
            game.getCurrentRound().setActivePlayer(game.getNextPlayer(game.getActivePlayer()));
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
