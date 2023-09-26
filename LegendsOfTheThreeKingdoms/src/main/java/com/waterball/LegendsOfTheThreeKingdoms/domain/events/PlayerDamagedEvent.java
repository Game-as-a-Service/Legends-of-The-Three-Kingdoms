package com.waterball.LegendsOfTheThreeKingdoms.domain.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerDamagedEvent extends DomainEvent {
    private final String name = "PlayerDamagedEvent";
    private final String message = "扣血";
    private String playerId;
    private int from;
    private int to;
}
