package com.gaas.threeKingdoms.events;

import lombok.Getter;

import java.util.List;

@Getter
public class QilinBowCardEffectEvent extends EffectEvent {
    private final String mountCardId;

    public QilinBowCardEffectEvent(String message, boolean isSuccess, String mountCardId, String gameId, List<PlayerEvent> seats, RoundEvent round, String gamePhase) {
        super(isSuccess, message, gameId, seats, "QilinBowCardEffectEvent", round, gamePhase);
        this.mountCardId = mountCardId;
    }


}