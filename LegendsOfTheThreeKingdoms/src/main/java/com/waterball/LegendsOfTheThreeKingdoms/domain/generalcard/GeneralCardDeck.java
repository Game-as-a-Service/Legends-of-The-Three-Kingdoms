package com.waterball.LegendsOfTheThreeKingdoms.domain.generalcard;

import com.waterball.LegendsOfTheThreeKingdoms.utils.ShuffleWrapper;
import lombok.Data;

import java.util.*;

@Data
public class GeneralCardDeck {

    private static final int FIXED_CARD_NUMBER = 5;

    private Stack<GeneralCard> generalStack = new Stack<>();

    public GeneralCardDeck() {
        initGeneralCardDeck();
    }

    private void initGeneralCardDeck() {
        General[] generalNames = General.values();
        for (int i = generalNames.length - 1; i >= 3; i--) {
            GeneralCard generalCard = new GeneralCard(generalNames[i].getGeneralId(), generalNames[i].getGeneralName(), generalNames[i].getHealthPoint());
            generalStack.add(generalCard);
            GeneralCard.generals.put(generalNames[i].getGeneralId(), generalCard);
        }
        ShuffleWrapper.shuffle(generalStack);
        //讓劉備,曹操,孫權在最上面
        for (int i = 0; i < 3; i++) {
            GeneralCard generalCard = new GeneralCard(generalNames[i].getGeneralId(), generalNames[i].getGeneralName(), generalNames[i].getHealthPoint());
            generalStack.add(generalCard);
            GeneralCard.generals.put(generalNames[i].getGeneralId(), generalCard);
        }
    }

    public List<GeneralCard> drawGeneralCards(int needCardCount) {
        if (isNotValid()) {
            return null;
        }
        List<GeneralCard> cards = new ArrayList<>();
        for (int i = 0; i < needCardCount; i++) {
            cards.add(generalStack.pop());
        }

        return cards;
    }

    private boolean isNotValid() {
        return generalStack.isEmpty();
    }

}
