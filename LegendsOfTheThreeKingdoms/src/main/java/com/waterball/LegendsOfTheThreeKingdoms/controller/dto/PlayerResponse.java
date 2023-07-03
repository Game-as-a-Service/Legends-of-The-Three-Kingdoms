package com.waterball.LegendsOfTheThreeKingdoms.controller.dto;

import com.waterball.LegendsOfTheThreeKingdoms.domain.generalcard.GeneralCard;
import com.waterball.LegendsOfTheThreeKingdoms.domain.rolecard.RoleCard;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.Hand;
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
    private Hand hand;

    public PlayerResponse(PlayerDto player) {
        id = player.getId();
        roleCard = player.getRoleCard();
        generalCard = player.getGeneralCard();
        hand = player.getHand();
    }
}
