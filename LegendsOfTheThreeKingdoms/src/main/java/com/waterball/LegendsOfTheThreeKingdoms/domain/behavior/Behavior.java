package com.waterball.LegendsOfTheThreeKingdoms.domain.behavior;

import com.waterball.LegendsOfTheThreeKingdoms.domain.Game;
import com.waterball.LegendsOfTheThreeKingdoms.domain.events.DomainEvent;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.Player;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public abstract class Behavior {
    protected Game game;
    protected Player behaviorPlayer;
    protected List<String> reactionPlayers;
    protected Player currentReactionPlayer;
    protected String cardId;
    protected String playType;

    public abstract List<DomainEvent> askTargetPlayerPlayCard();
    public abstract List<DomainEvent> acceptedTargetPlayerPlayCard(String playerId, String targetPlayerIdString, String cardId, String playType);

}
