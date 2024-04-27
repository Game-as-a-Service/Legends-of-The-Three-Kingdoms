package com.gaas.threeKingdoms.events;

import lombok.Getter;

@Getter
public class EffectEvent extends DomainEvent {
    private final boolean isSuccess;
    public EffectEvent(String name, String message, boolean isSuccess) {
        super(name, message);
        this.isSuccess = isSuccess;
    }

}
