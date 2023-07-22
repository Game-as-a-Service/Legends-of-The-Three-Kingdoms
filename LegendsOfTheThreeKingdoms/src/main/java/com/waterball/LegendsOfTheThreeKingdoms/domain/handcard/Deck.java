package com.waterball.LegendsOfTheThreeKingdoms.domain.handcard;

import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.basiccard.Dodge;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.basiccard.Kill;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.basiccard.Peach;
import com.waterball.LegendsOfTheThreeKingdoms.utils.ShuffleWrapper;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Stack;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Data
@AllArgsConstructor
public class Deck {
    private Stack<HandCard> cardDeck = new Stack<>();

    public Deck() {
        // TODO: Enum values should be used instead of hard-coded values.
        IntStream.range(0, 8).forEach(i -> {
            cardDeck.add(new Kill(PlayCard.BS8008));
            cardDeck.add(new Peach(PlayCard.BH4030));
            cardDeck.add(new Dodge(PlayCard.BHK039));
        });
        shuffle();
    }

    public void shuffle() {
        ShuffleWrapper.shuffle(cardDeck);
    }

    public List<HandCard> deal(int number) {
        return Stream.generate(cardDeck::pop)
                .limit(number)
                .collect(Collectors.toList());
    }

    public boolean isDeckLessThanCardNum(int requiredCardNum) {
        return cardDeck.size() < requiredCardNum;
    }

    public void add(List<HandCard> handCards) {
        cardDeck.addAll(handCards);
    }
}
