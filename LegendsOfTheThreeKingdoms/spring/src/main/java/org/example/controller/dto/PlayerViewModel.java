package org.example.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.gaas.domain.player.Player;
import org.gaas.domain.rolecard.RoleCard;

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
