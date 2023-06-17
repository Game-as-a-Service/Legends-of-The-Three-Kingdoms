package com.waterball.LegendsOfTheThreeKingdoms.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

@Data
public class GeneralCardDeck {

    private static final int FIXED_CARD_NUMBER = 5;

    private Stack<GeneralCard> generalStack = new Stack<>();

    public GeneralCardDeck() {
        initGeneralCardDeck();
    }

    private void initGeneralCardDeck() {
        generalStack.add(new GeneralCard("a","a"));
        generalStack.add(new GeneralCard("b","b"));
        generalStack.add(new GeneralCard("c","c"));
        generalStack.add(new GeneralCard("d","d"));
        generalStack.add(new GeneralCard("e","e"));
    }

    public List<GeneralCard> drawGeneralCards() {
        if (isNotValid()) {
            return null;
        }
        List<GeneralCard> cards = new ArrayList<>();
        for (int i = 0; i < FIXED_CARD_NUMBER; i++) {
            cards.add(generalStack.pop());
        }

        return cards;
    }

    private boolean isNotValid() {
        return generalStack.isEmpty() || generalStack.size() < FIXED_CARD_NUMBER;
    }

}
