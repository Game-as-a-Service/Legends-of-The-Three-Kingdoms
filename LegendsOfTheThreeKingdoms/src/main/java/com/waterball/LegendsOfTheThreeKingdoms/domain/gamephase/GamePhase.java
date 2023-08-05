package com.waterball.LegendsOfTheThreeKingdoms.domain.gamephase;

import com.waterball.LegendsOfTheThreeKingdoms.domain.Game;

public abstract class GamePhase {
    protected Game game;

    public GamePhase(Game game) {
        this.game = game;
    }

    public void execute(String playerId, String cardId, String targetPlayerId, String playType) {
        throw new UnsupportedOperationException("Method not implemented for this class");
    }

    public void execute() {
        throw new UnsupportedOperationException("Method not implemented for this class");
    }

    public void exit() {
        game.enterPhase(new Normal(game));
    }
}
