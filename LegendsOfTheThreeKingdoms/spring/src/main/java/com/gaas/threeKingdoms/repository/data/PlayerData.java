package com.gaas.threeKingdoms.repository.data;

import com.gaas.threeKingdoms.player.Hand;
import com.gaas.threeKingdoms.player.HealthStatus;
import com.gaas.threeKingdoms.player.Player;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerData {

    private HandData hand;
    private String id;
    private RoleCardData role;
    private GeneralCardData general;
    private BloodCardData blood;
    private String healthStatus;
    private EquipmentData equipment;

    public static PlayerData fromDomain(com.gaas.threeKingdoms.player.Player player) {
        return PlayerData.builder()
                .hand(HandData.fromDomain(player.getHand()))
                .id(player.getId())
                .role(RoleCardData.fromDomain(player.getRoleCard()))
                .general(GeneralCardData.fromDomain(player.getGeneralCard()))
                .blood(BloodCardData.fromDomain(player.getBloodCard()))
                .healthStatus(player.getHealthStatus().name())
                .equipment(EquipmentData.fromDomain(player.getEquipment()))
                .build();
    }

    public Player toDomain() {
        Player player = new Player();
        player.setHand(this.hand.toDomain());
        player.setId(this.id);
        player.setRoleCard(this.role.toDomain());
        player.setGeneralCard(this.general.toDomain());
        player.setBloodCard(this.blood.toDomain());
        player.setHealthStatus(HealthStatus.valueOf(this.healthStatus));
        player.setEquipment(this.equipment.toDomain());
        return player;
    }

}