package com.gaas.threeKingdoms.repository.data;


import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.generalcard.GeneralCardDeck;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayCard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Stack;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneralCardDeckData {

    private Stack<String> generalStack = new Stack<>();

    public GeneralCardDeck toDomain() {
        GeneralCardDeck deck = new GeneralCardDeck();
        for (String generalId : this.generalStack) {
            deck.getGeneralStack().push(new GeneralCard(General.findById(generalId)));
        }
        return deck;
    }

    public static GeneralCardDeckData fromDomain(GeneralCardDeck deck) {
        GeneralCardDeckData deckData = new GeneralCardDeckData();
        Stack<String> cardIds = new Stack<>();
        for (GeneralCard generalCard : deck.getGeneralStack()) {
            cardIds.push(generalCard.getGeneralId());
        }
        deckData.setGeneralStack(cardIds);
        return deckData;
    }
}
