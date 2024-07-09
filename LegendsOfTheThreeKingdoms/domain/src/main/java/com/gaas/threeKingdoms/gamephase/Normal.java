package com.gaas.threeKingdoms.gamephase;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.round.Round;
import com.gaas.threeKingdoms.exception.DistanceErrorException;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.player.Player;

import java.util.List;

public class Normal extends GamePhase {

    public Normal(Game game) {
        super(game);
        this.phaseName = "Normal";
    }

}
