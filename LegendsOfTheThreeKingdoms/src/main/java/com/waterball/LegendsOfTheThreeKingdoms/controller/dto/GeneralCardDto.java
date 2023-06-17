package com.waterball.LegendsOfTheThreeKingdoms.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GeneralCardDto {
    private String generalID;
    private String generalName;
}
