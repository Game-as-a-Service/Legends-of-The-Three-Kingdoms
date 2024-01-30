package com.gaas.threeKingdoms.events;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RoundEndEvent extends DomainEvent {

    private final String name = "RoundEndEvent";
    private String message = "回合已結束";
}
