package com.gaas.threeKingdoms.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.rolecard.RoleCard;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlayerViewModel implements Serializable {
    private String id;
    private RoleCard roleCard;

    public PlayerViewModel(Player player) {
        id = player.getId();
        roleCard = player.getRoleCard();
    }
}
