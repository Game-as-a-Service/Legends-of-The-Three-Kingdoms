package com.gaas.threeKingdoms.events;

import lombok.Getter;

import java.util.List;

@Getter
public class EffectEvent extends DomainEvent {
    private final boolean isSuccess;

    public EffectEvent(boolean isSuccess, String message, String name) {
        super(name, message);
        this.isSuccess = isSuccess;
    }

}
