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

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneralCardDeckData {

    private List<String> generalStack = new ArrayList<>();

    public GeneralCardDeck toDomain() {
        GeneralCardDeck deck = new GeneralCardDeck();
        for (String generalId : this.generalStack) {
            deck.getGeneralStack().push(new GeneralCard(General.findById(generalId)));
        }
        return deck;
    }

    public static GeneralCardDeckData fromDomain(GeneralCardDeck deck) {
        GeneralCardDeckData deckData = new GeneralCardDeckData();
        List<String> cardIds = new ArrayList<>();
        for (GeneralCard generalCard : deck.getGeneralStack()) {
            cardIds.add(generalCard.getGeneralId());
        }
        deckData.setGeneralStack(cardIds);
        return deckData;
    }
}
