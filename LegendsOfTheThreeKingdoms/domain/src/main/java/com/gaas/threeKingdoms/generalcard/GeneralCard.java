package com.gaas.threeKingdoms.generalcard;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class GeneralCard {

    private String generalId;
    private String generalName;
    private int healthPoint;

    public GeneralCard(General general) {
        this.generalId = general.getGeneralId();
        this.generalName = general.getGeneralName();
        this.healthPoint = general.getHealthPoint();
    }

    public static final Map<String, GeneralCard> generals = new HashMap<>();
}
