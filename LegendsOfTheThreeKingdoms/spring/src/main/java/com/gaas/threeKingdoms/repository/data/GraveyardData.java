package com.gaas.threeKingdoms.repository.data;

import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.Graveyard;
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
public class GraveyardData {

    private Stack<String> graveYardDeck = new Stack<>();

    public Graveyard toDomain() {
        Graveyard graveyard = new Graveyard();
        for (String cardId : this.graveYardDeck) {
            graveyard.getGraveYardDeck().add(PlayCard.findById(cardId));
        }
        return graveyard;
    }

    public static GraveyardData fromDomain(Graveyard graveyard) {
        GraveyardData graveyardData = new GraveyardData();
        Stack<String> cardIds = new Stack<>();
        for (HandCard handCard : graveyard.getGraveYardDeck()) {
            cardIds.push(handCard.getId());  // Assuming HandCard has getId method
        }
        graveyardData.setGraveYardDeck(cardIds);
        return graveyardData;
    }
}
