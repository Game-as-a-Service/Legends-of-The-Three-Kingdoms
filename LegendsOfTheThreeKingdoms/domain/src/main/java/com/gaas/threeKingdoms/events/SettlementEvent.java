package com.gaas.threeKingdoms.events;

import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.rolecard.Role;
import lombok.Getter;

@Getter
public class SettlementEvent extends DomainEvent {
    private final String playerId;
    private final Role role;

    public SettlementEvent(String playerId, Role role){
        super("SettlementEvent", String.format("%s 死亡，身分是 %s", playerId, role.getRoleName()));
        this.playerId = playerId;
        this.role = role;
    }
}
