package com.waterball.LegendsOfTheThreeKingdoms.controller.dto;

import com.waterball.LegendsOfTheThreeKingdoms.service.dto.GeneralCardDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GeneralCardResponse implements Serializable {
    private String generalID;
    private String generalName;

    public GeneralCardResponse(GeneralCardDto generalCardDto) {
        generalID = generalCardDto.getGeneralID();
        generalName = generalCardDto.getGeneralName();
    }

}
