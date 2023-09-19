package com.waterball.LegendsOfTheThreeKingdoms.domain.gamephase;

import com.waterball.LegendsOfTheThreeKingdoms.domain.Game;
import com.waterball.LegendsOfTheThreeKingdoms.domain.events.DomainEvent;
import com.waterball.LegendsOfTheThreeKingdoms.domain.events.PlayCardEvent;

import java.util.List;

public abstract class GamePhase {
    protected Game game;
    protected String phaseName;

    public GamePhase(Game game) {
        this.game = game;
    }

    public List<DomainEvent> playCard(String playerId, String cardId, String targetPlayerId, String playType) {
        throw new UnsupportedOperationException("Method not implemented for this class");
    }

    public void execute() {
        throw new UnsupportedOperationException("Method not implemented for this class");
    }

    public void exit() {
        game.enterPhase(new Normal(game));
    }

    public String getPhaseName() {
        return phaseName;
    }
}
