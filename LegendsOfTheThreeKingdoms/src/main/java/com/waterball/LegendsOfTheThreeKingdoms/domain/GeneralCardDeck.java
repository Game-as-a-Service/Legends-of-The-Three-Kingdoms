package com.waterball.LegendsOfTheThreeKingdoms.domain;

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
        String general = "general";
        GeneralName[] generalNames = GeneralName.values();
        int cardId = 3;
        //讓劉備,曹操,孫權在最上面
        for (int i = generalNames.length - 1; i >= 3; i--) {
            String generalID = general + cardId++;
            GeneralCard generalCard = new GeneralCard(generalID, generalNames[i].getGeneralName());
            generalStack.add(generalCard);
            GeneralCard.generals.put(generalID, generalCard);
        }
        ShuffleWrapper.shuffle(generalStack);
        cardId = 0; //讓孫權,曹操,劉備 id 是 0 1 2
        for (int i = 0; i < 3; i++) {
            String generalID = general + cardId++;
            GeneralCard generalCard = new GeneralCard(generalID, generalNames[i].getGeneralName());
            generalStack.add(generalCard);
            GeneralCard.generals.put(generalID, generalCard);
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
        魏延("魏延"),
        司馬懿("司馬懿"),
        夏侯敦("夏侯敦"),
        許褚("許褚"),
        郭嘉("郭嘉"),
        甄姬("甄姬"),
        張遼("張遼"),
        甘寧("甘寧"),
        呂蒙("呂蒙"),
        黃蓋("黃蓋"),
        大喬("大喬"),
        周瑜("周瑜"),
        孫尚香("孫尚香"),
        陸遜("陸遜");

//        身份牌: 共10張,其中主公1張,內奸2張,忠臣3張,反賊4張
//        體力牌: 共10張,其中5滴血牌2張(背面為4滴血),4滴血牌8張(背面為3滴血)
//        角色牌: 共25張,其中群武將3張:華佗、貂蟬、呂布
//        蜀國武將7張:劉備、關羽、張飛、趙雲、黃月英、諸葛亮、馬超
//        魏國武將7張:曹操、司馬懿、夏侯敦、許褚、郭嘉、甄姬、張遼
//        吳國武將8張:孫權、甘寧、呂蒙、黃蓋、大喬、周瑜、孫尚香、陸遜


        private final String generalName;
        GeneralName(String generalName) {
            this.generalName = generalName;
        }

        public String getGeneralName() {
            return generalName;
        }
    }

}
