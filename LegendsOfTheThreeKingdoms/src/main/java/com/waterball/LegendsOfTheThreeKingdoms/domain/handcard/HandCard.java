package com.waterball.LegendsOfTheThreeKingdoms.domain.handcard;


import com.waterball.LegendsOfTheThreeKingdoms.domain.player.Player;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
public abstract class HandCard {
    protected String name;
    protected String id;

    public HandCard(PlayCard playCard) {
        this.name = playCard.getCardName();
        this.id = playCard.getCardId();
    }

    public abstract void effect(Player player);
}
