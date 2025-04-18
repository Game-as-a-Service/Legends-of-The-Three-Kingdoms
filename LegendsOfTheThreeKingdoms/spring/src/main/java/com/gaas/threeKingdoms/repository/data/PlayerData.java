package com.gaas.threeKingdoms.repository.data;

import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.PlayCard;
import com.gaas.threeKingdoms.handcard.scrollcard.ScrollCard;
import com.gaas.threeKingdoms.player.BloodCard;
import com.gaas.threeKingdoms.player.HealthStatus;
import com.gaas.threeKingdoms.player.Player;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

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
    private List<String> delayScrolls;

    public static PlayerData fromDomain(com.gaas.threeKingdoms.player.Player player) {
        if (player == null) {
            return null;
        }

        HealthStatus healthStatus = player.getHealthStatus();
        return PlayerData.builder()
                .hand(HandData.fromDomain(player.getHand()))
                .id(player.getId())
                .role(RoleCardData.fromDomain(player.getRoleCard()))
                .general(GeneralCardData.fromDomain(player.getGeneralCard()))
                .blood(BloodCardData.fromDomain(player.getBloodCard()))
                .healthStatus(healthStatus == null ? null : player.getHealthStatus().name())
                .equipment(EquipmentData.fromDomain(player.getEquipment()))
                .delayScrolls(player.getDelayScrollCardIds())
                .build();
    }

    public Player toDomain() {
        Player player = new Player();
        player.setHand(this.hand.toDomain());
        player.setId(this.id);
        player.setRoleCard(this.role.toDomain());
        player.setGeneralCard(general == null ? null : general.toDomain());
        player.setBloodCard(blood == null ? null : blood.toDomain());
        player.setHealthStatus(healthStatus == null ? null : HealthStatus.valueOf(this.healthStatus));
        player.setEquipment(this.equipment.toDomain());
        Stack<ScrollCard> delayScrolls = new Stack<>();
        for (String delayScroll : this.delayScrolls) {
            delayScrolls.push((ScrollCard) PlayCard.findById(delayScroll));
        }
        player.setDelayScrollCards(delayScrolls);
        return player;
    }

}