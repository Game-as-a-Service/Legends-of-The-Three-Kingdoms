package com.waterball.LegendsOfTheThreeKingdoms.domain.handcard;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
public abstract class HandCard {
    protected String name;
    protected String id;

    public String getCardId(){return "";}
}
