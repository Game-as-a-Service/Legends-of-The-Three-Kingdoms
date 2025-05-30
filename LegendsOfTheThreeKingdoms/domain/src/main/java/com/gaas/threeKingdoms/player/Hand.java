package com.gaas.threeKingdoms.player;

import com.gaas.threeKingdoms.handcard.HandCard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

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

    public void addCardToHand(HandCard card) {
        this.cards.add(card);
    }

    public Optional<HandCard> getCard(String cardId) {
        return cards.stream()
                .filter(card -> cardId != null && cardId.equals(card.getId()))
                .findFirst();
    }

    public List<String> getAllCardIds() {
        return cards.stream()
                .map(HandCard::getId)
                .toList();
    }
//這邊的排是不是本來就棄掉了? 要改測資
    public HandCard playCard(String cardId) {
        HandCard handCard = getCard(cardId).orElseThrow(NoSuchElementException::new);
        int index = cards.indexOf(handCard);
        return cards.remove(index);
    }

    public boolean hasTypeInHand(Class<?> classType) {
        return cards.stream().anyMatch(classType::isInstance);
    }

}
