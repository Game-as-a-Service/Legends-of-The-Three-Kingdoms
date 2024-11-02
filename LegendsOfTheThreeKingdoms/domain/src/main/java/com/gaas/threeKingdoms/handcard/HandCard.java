package com.gaas.threeKingdoms.handcard;


import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.player.Player;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public abstract class HandCard {
    protected String name;
    protected String id;
    protected Suit suit;
    protected Rank rank;

    public HandCard(PlayCard playCard) {
        this.name = playCard.getCardName();
        this.id = playCard.getCardId();
        this.suit = playCard.getSuit();
        this.rank = playCard.getRank();
    }



    public abstract void effect(Player player);

}
