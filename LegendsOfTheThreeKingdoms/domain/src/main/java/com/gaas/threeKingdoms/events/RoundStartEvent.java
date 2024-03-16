package com.gaas.threeKingdoms.events;

import lombok.Getter;

@Getter
public class RoundStartEvent extends DomainEvent {


    public RoundStartEvent() {
        super("RoundStartEvent", "回合已開始");
    }
}
