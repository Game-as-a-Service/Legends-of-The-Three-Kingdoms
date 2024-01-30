package com.gaas.threeKingdoms.handcard;

public enum Rank {
    TWO('2'),
    THREE('3'),
    FOUR('4'),
    FIVE('5'),
    SIX('6'),
    SEVEN('7'),
    EIGHT('8'),
    NINE('9'),
    TEN('0'),
    J('J'),
    Q('Q'),
    K('K'),
    ACE('A');

    private final char representation;

    Rank (char c) {
        this.representation = c;
    }

}
