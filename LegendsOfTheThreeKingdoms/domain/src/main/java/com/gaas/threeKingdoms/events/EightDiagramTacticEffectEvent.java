package com.gaas.threeKingdoms.events;


import lombok.Getter;

import java.util.List;

@Getter
public class EightDiagramTacticEffectEvent extends EffectEvent {
    private final String drawCardId;

    public EightDiagramTacticEffectEvent(String message, boolean isSuccess,String drawCardId, String gameId, List<PlayerEvent> seats, RoundEvent round, String gamePhase) {
        super(isSuccess, message, gameId, seats, "EightDiagramTacticEffectEvent", round, gamePhase);
        this.drawCardId = drawCardId;
    }


}
