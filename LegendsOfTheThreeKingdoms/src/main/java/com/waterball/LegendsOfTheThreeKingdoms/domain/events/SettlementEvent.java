package com.waterball.LegendsOfTheThreeKingdoms.domain.events;

import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.HandCard;
import lombok.Getter;

import java.util.List;

@Getter
public class SettlementEvent extends DomainEvent {
    private String playerId;
    private String role;

    private final String name = "SettlementEvent";
    private final String message;

    public SettlementEvent(String playerId, String role, String message){
        this.playerId = playerId;
        this.role = role;
        this.message = message;
    }
}
