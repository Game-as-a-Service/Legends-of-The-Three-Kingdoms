package com.gaas.threeKingdoms.handcard;

import com.gaas.threeKingdoms.utils.ShuffleWrapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Deck {
    private Stack<HandCard> cardDeck = new Stack<>();

    public void init() {
        Arrays.stream(PlayCard.values())
                .map(playCard -> PlayCard.findById(playCard.getCardId()))
                .filter(Objects::nonNull)
                .forEach(cardDeck::add);
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

    public int size() {
        return cardDeck.size();
    }

    /** 觀星：把牌放回牌堆頂（list 第一張 = 下一張被抽的牌）。 */
    public void putBackOnTop(List<HandCard> cards) {
        for (int i = cards.size() - 1; i >= 0; i--) {
            cardDeck.push(cards.get(i));
        }
    }

    /** 觀星：把牌放到牌堆底。 */
    public void putAtBottom(List<HandCard> cards) {
        for (HandCard card : cards) {
            cardDeck.insertElementAt(card, 0);
        }
    }
}
