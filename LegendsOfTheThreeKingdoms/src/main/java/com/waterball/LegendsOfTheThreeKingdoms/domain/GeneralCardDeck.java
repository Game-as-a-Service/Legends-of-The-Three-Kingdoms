package com.waterball.LegendsOfTheThreeKingdoms.domain;

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
        String general = "general";
        GeneralName[] generalNames = GeneralName.values();
        int cardId = 3;
        //讓劉備,曹操,孫權在最上面
        for (int i = generalNames.length - 1; i >= 3; i--) {
            generalStack.add(new GeneralCard(general + cardId++, generalNames[i].getGeneralName()));
        }
        Collections.shuffle(generalStack);
        cardId = 0; //讓孫權,曹操,劉備 id 是 0 1 2
        for (int i = 0; i < 3; i++) {
            generalStack.add(new GeneralCard(general + cardId++, generalNames[i].getGeneralName()));
        }
    }

    public List<GeneralCard> drawGeneralCards() {
        if (isNotValid()) {
            return null;
        }
        List<GeneralCard> cards = new ArrayList<>();
        for (int i = 0; i < FIXED_CARD_NUMBER; i++) {
            cards.add(generalStack.get(generalStack.size()-1-i));
        }

        return cards;
    }

    private boolean isNotValid() {
        return generalStack.isEmpty() || generalStack.size() < FIXED_CARD_NUMBER;
    }


    public enum GeneralName {
        劉備("劉備"),
        曹操("曹操"),
        孫權("孫權"),
        關羽("關羽"),
        張飛("張飛"),
        馬超("馬超"),
        趙雲("趙雲"),
        黃月英("黃月英"),
        諸葛亮("諸葛亮"),
        黃忠("黃忠"),
        魏延("魏延");

        private final String generalName;
        GeneralName(String generalName) {
            this.generalName = generalName;
        }

        public String getGeneralName() {
            return generalName;
        }
    }

}
