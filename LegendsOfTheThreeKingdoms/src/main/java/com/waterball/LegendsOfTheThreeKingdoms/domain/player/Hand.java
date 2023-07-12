package com.waterball.LegendsOfTheThreeKingdoms.domain.player;

import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.HandCard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Hand {
    private List<HandCard> cards = new ArrayList<>();

    public void setCards(List<HandCard> cards) {
        this.cards = cards;
    }

    public int size() {
        return cards.size();
    }

    public void addCardToHand(List<HandCard> cards) {
        this.cards.addAll(cards);
    }

    public void playCard(String cardId) {
        cards.remove(cards.stream()
                .filter(card -> "K".equals(card.getCardId()))
                .findFirst().get());
    }
}
