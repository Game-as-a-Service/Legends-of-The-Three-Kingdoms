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

}
