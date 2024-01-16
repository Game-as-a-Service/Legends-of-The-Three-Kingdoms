package org.gaas.domain.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class NotifyDiscardEvent extends DomainEvent {
    private final String name = "NotifyDiscardEvent";
    private final String message;
    private int discardCount;
    private String discardPlayerId;

    // Game state
    private String playerId;
    private String gameId;
    private List<PlayerEvent> seats;
    private RoundEvent round;
    private String gamePhase;

}
