package org.gaas.domain.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayCardEvent extends DomainEvent {

    private final String name = "PlayCardEvent";
    private String message = "出牌";
    private String playerId;
    private String targetPlayerId;
    private String cardId;
    private String playType;
    private String gameId;
    private List<PlayerEvent> seats;
    private RoundEvent round;
    private String gamePhase;


}