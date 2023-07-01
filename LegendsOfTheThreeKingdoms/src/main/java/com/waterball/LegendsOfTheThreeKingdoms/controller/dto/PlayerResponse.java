package com.waterball.LegendsOfTheThreeKingdoms.controller.dto;

import com.waterball.LegendsOfTheThreeKingdoms.domain.GeneralCard;
import com.waterball.LegendsOfTheThreeKingdoms.domain.RoleCard;
import com.waterball.LegendsOfTheThreeKingdoms.service.dto.PlayerDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlayerResponse implements Serializable {
    private String id;
    private RoleCard roleCard;
    private GeneralCard generalCard;

    public PlayerResponse(PlayerDto player) {
        id = player.getId();
        roleCard = player.getRoleCard();
        generalCard = player.getGeneralCard();
    }
}
