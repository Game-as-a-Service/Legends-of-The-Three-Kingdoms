package com.gaas.threeKingdoms.events;

import lombok.Getter;

@Getter
public class SettlementEvent extends DomainEvent {
    private String playerId;
    private String role;

    public SettlementEvent(String playerId, String role){
        super("SettlementEvent", "結算");
        this.playerId = playerId;
        this.role = role;
    }
}
