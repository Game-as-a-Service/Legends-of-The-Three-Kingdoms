package com.waterball.LegendsOfTheThreeKingdoms.service.dto;

import com.waterball.LegendsOfTheThreeKingdoms.domain.generalcard.GeneralCard;
import com.waterball.LegendsOfTheThreeKingdoms.domain.rolecard.RoleCard;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.Hand;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlayerDto {
    private String id;
    private RoleCard roleCard;
    private GeneralCard generalCard;
    private Hand hand;


}
