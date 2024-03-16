package com.gaas.threeKingdoms.events;

import lombok.Getter;

@Getter
public class RoundEndEvent extends DomainEvent {

    public RoundEndEvent() {
        super("RoundEndEvent", "回合已結束");
    }
}
