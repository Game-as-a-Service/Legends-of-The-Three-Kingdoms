package com.gaas.threeKingdoms.events;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RoundStartEvent extends DomainEvent {

    private final String name = "RoundStartEvent";
    private String message = "回合已開始";
}
