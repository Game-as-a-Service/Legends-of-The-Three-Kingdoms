package com.waterball.LegendsOfTheThreeKingdoms.domain.handcard;

import com.waterball.LegendsOfTheThreeKingdoms.utils.ShuffleWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


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
}
