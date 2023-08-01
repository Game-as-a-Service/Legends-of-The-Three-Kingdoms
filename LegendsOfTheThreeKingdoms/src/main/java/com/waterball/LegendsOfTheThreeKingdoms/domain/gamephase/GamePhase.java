package com.waterball.LegendsOfTheThreeKingdoms.domain.gamephase;

import com.waterball.LegendsOfTheThreeKingdoms.domain.Game;

public enum GamePhase {
    GeneralDying("GeneralDying", new GeneralDying()),
    GameOver("GameOver", new GameOver());

    private final String phaseName;
    private final GamePhaseAction action;

    GamePhase(String phaseName, GamePhaseAction action) {
        this.phaseName = phaseName;
        this.action = action;
    }

    public void execute(Game game) {
        action.execute(game);
    }

    public String getPhaseName() {
        return phaseName;
    }

    public GamePhaseAction getAction() {
        return action;
    }
}
