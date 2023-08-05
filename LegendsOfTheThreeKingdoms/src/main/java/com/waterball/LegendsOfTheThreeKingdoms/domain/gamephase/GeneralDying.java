package com.waterball.LegendsOfTheThreeKingdoms.domain.gamephase;

import com.waterball.LegendsOfTheThreeKingdoms.domain.Game;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.HealthStatus;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.Player;

public class GeneralDying extends GamePhase {


    public GeneralDying(Game game) {
        super(game);
    }

    @Override
    public void execute(String playerId, String cardId, String targetPlayerId, String playType) {
        if ("skip".equals(playType)){
            Player dyingPlayer = game.getCurrentRound().getDyingPlayer();
            if (game.getActivePlayer() == game.getPrePlayer(dyingPlayer)) {
                dyingPlayer.setHealthStatus(HealthStatus.DEATH);
                game.playerDeadSettlement();
                // TODO 死亡結算。
                return;
            }
            game.getCurrentRound().setActivePlayer(game.getNextPlayer(game.getActivePlayer()));
        }
    }

}
