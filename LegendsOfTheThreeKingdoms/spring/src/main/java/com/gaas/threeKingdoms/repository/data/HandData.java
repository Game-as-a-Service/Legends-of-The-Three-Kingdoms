package com.gaas.threeKingdoms.repository.data;

import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayCard;
import com.gaas.threeKingdoms.player.Hand;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HandData {
    private List<String> cards;

    public Hand toDomain() {
        Hand hand = new Hand();
        hand.setCards(this.cards.stream()
                .map(PlayCard::findById)
                .collect(Collectors.toList()));
        return hand;
    }

    public static HandData fromDomain(Hand hand) {
        return HandData.builder()
                .cards(hand.getCards().stream()
                        .map(HandCard::getId)
                        .collect(Collectors.toList()))
                .build();
    }
}
