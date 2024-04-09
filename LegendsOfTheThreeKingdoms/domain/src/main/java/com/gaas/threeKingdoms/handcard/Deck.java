package com.gaas.threeKingdoms.handcard;

import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.RedRabbitHorse;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.ShadowHorse;
import lombok.AllArgsConstructor;
import lombok.Data;
import com.gaas.threeKingdoms.utils.ShuffleWrapper;

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
            cardDeck.add(new RedRabbitHorse(PlayCard.EH5044));
            cardDeck.add(new ShadowHorse(PlayCard.ES5018));
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
