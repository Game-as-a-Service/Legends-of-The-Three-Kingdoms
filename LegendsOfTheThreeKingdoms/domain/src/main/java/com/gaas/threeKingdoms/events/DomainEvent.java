package com.gaas.threeKingdoms.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class DomainEvent {
    private final String name;
    private final String message;
}
