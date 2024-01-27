package com.gaas.threeKingdoms.generalcard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GeneralCard {

    private String generalID;
    private String generalName;
    private int healthPoint;

    public static final Map<String, GeneralCard> generals = new HashMap<>() {
    };


}
