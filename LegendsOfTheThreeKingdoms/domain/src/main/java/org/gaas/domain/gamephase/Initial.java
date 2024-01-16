package org.gaas.domain.gamephase;

import org.gaas.domain.Game;

public class Initial extends GamePhase {
    public Initial(Game game) {
        super(game);
        phaseName = "Initial";
    }
}
