package com.gaas.threeKingdoms.handcard;


import com.gaas.threeKingdoms.player.Player;
import lombok.AllArgsConstructor;
import lombok.Data;

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
