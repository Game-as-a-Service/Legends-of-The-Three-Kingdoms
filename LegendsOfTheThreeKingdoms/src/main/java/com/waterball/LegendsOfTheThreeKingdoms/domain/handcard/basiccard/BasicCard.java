package com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.basiccard;

import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.HandCard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;


public class BasicCard extends HandCard {

    public BasicCard(String name, String id) {
        super(name, id);
    }

}
