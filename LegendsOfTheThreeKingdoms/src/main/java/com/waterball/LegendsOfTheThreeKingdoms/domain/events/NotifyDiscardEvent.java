package com.waterball.LegendsOfTheThreeKingdoms.domain.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class NotifyDiscardEvent extends DomainEvent {
    private final String name = "NotifyDiscardEvent";
    private final String message = "通知棄牌數量";
    private int discardCount;

    // Game state
    private String playerId;
    private String gameId;
    private List<PlayerEvent> seats;
    private RoundEvent round;
    private String gamePhase;

}
