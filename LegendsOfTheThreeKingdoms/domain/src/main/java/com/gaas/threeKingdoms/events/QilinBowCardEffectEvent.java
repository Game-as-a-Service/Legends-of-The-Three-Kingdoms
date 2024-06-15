package com.gaas.threeKingdoms.events;

import lombok.Getter;

import java.util.List;

@Getter
public class QilinBowCardEffectEvent extends EffectEvent {
    private final String mountCardId;

    public QilinBowCardEffectEvent(String message, boolean isSuccess, String mountCardId) {
        super(isSuccess, message, "QilinBowCardEffectEvent");
        this.mountCardId = mountCardId;
    }


}