package com.waterball.LegendsOfTheThreeKingdoms.controller.dto;

import com.waterball.LegendsOfTheThreeKingdoms.domain.GeneralCard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlayerDto implements Serializable {
    private String id;
    private String role;
    private GameDto gameDto;
    private GeneralCard generalCard;

}
