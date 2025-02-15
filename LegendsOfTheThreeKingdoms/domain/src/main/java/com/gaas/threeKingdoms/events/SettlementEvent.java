package com.gaas.threeKingdoms.events;

import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.rolecard.Role;
import lombok.Getter;

@Getter
public class SettlementEvent extends DomainEvent {
    private final String playerId;
    private final Role role;

    public SettlementEvent(Player player){
        super("SettlementEvent", String.format("%s 死亡，身分是 %s", player.getGeneralCard().getGeneralName(), player.getRoleCard().getRole().getChinesRoleName()));
        this.playerId = player.getId();
        this.role = player.getRoleCard().getRole();
    }
}
