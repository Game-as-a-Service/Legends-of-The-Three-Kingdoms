package com.waterball.LegendsOfTheThreeKingdoms.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GeneralCard {

    private String generalID;
    private String generalName;
}
