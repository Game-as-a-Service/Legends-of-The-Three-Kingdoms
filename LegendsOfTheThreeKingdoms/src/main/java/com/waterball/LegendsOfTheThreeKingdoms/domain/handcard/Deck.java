package com.waterball.LegendsOfTheThreeKingdoms.domain.handcard;

import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.HandCard;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.basiccard.Dodge;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.basiccard.Kill;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.basiccard.Peach;
import com.waterball.LegendsOfTheThreeKingdoms.utils.ShuffleWrapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
        IntStream.range(0, 8).forEach(i -> {
            cardDeck.add(new Kill());
            cardDeck.add(new Peach());
            cardDeck.add(new Dodge());
        });
        shuffle();
    }

    public void shuffle(){
        ShuffleWrapper.shuffle(cardDeck);
    }

    public List<HandCard> deal(int number){
        return Stream.generate(cardDeck::pop)
                .limit(number)
                .collect(Collectors.toList());
    }
}
