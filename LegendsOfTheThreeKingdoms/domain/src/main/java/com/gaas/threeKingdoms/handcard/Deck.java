package com.gaas.threeKingdoms.handcard;

import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.EightDiagramTactic;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.QilinBowCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.RepeatingCrossbowCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.RedRabbitHorse;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.ShadowHorse;
import com.gaas.threeKingdoms.handcard.scrollcard.BarbarianInvasion;
import com.gaas.threeKingdoms.handcard.scrollcard.BorrowedSword;
import lombok.AllArgsConstructor;
import lombok.Data;
import com.gaas.threeKingdoms.utils.ShuffleWrapper;
import lombok.NoArgsConstructor;

import java.util.Stack;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Deck {
    private Stack<HandCard> cardDeck = new Stack<>();

    public void init() {
        // TODO: Enum values should be used instead of hard-coded values.
        IntStream.range(0, 8).forEach(i -> {
            cardDeck.add(new Kill(PlayCard.BS8008));
            cardDeck.add(new Peach(PlayCard.BH4030));
            cardDeck.add(new Dodge(PlayCard.BHK039));
            cardDeck.add(new RedRabbitHorse(PlayCard.EH5044));
            cardDeck.add(new ShadowHorse(PlayCard.ES5018));
            cardDeck.add(new QilinBowCard(PlayCard.EH5031));
            cardDeck.add(new BarbarianInvasion(SSK013));
            cardDeck.add(new BarbarianInvasion(SC7072));
            cardDeck.add(new BorrowedSword(SCK065));
            cardDeck.add(new BorrowedSword(SCQ064));
        });
        IntStream.range(0, 4).forEach(i -> {
            cardDeck.add(new RepeatingCrossbowCard(ECA066));
            cardDeck.add(new EightDiagramTactic(ES2015));
        });
        shuffle();
    }



    public Deck(List<HandCard> handCards) {
        cardDeck.addAll(handCards);
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
