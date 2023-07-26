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

    public HandCard getCard(String cardId) {
        return cards.stream()
                .filter(card -> cardId.equals(card.getId()))
                .findFirst().get();
    }
//這邊的排是不是本來就棄掉了? 要改測資
    public HandCard playCard(String cardId) {
        HandCard handCard = getCard(cardId);
        return cards.remove(cards.indexOf(handCard));
    }
}
