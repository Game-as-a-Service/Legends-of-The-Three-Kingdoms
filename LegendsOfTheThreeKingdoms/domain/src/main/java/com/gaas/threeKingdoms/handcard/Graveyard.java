package com.gaas.threeKingdoms.handcard;


import com.gaas.threeKingdoms.utils.ShuffleWrapper;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

@Data
public class Graveyard {
    private Stack<HandCard> graveYardDeck = new Stack<>();

    public void shuffle() {
        ShuffleWrapper.shuffle(graveYardDeck);
    }

    public HandCard dealCard() {
        return graveYardDeck.pop();
    }

    public void add(List<HandCard> playedCards) {
        graveYardDeck.addAll(playedCards);
    }

    public void add(HandCard playedCard) {
        graveYardDeck.add(playedCard);
    }

    public List<HandCard> getGraveYardCards() {
        shuffle();
        ArrayList<HandCard> graveYardCars = new ArrayList<>();
        while (!graveYardDeck.isEmpty()){
            graveYardCars.add(graveYardDeck.pop());
        }
        return graveYardCars;
    }
    public boolean isEmpty(){
        return graveYardDeck.isEmpty();
    }

    public int size(){
        return graveYardDeck.size();
    }

    public boolean contains(String cardId) {
        return graveYardDeck.stream().anyMatch(card -> card.getId().equals(cardId));
    }

    public java.util.Optional<HandCard> removeCard(String cardId) {
        for (int i = graveYardDeck.size() - 1; i >= 0; i--) {
            HandCard card = graveYardDeck.get(i);
            if (card.getId().equals(cardId)) {
                graveYardDeck.remove(i);
                return java.util.Optional.of(card);
            }
        }
        return java.util.Optional.empty();
    }
}
