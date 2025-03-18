package com.gaas.threeKingdoms.handcard;

import java.util.HashMap;
import java.util.Map;

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
    private final int value;

    Rank (char c) {
        this.representation = c;
        this.value = initValue(representation);
    }

    public char getRepresentation() {
        return representation;
    }

    public int getValue() {
        return value;
    }

    private int initValue(char representation) {
        return switch (representation) {
            case '2' -> 2;
            case '3' -> 3;
            case '4' -> 4;
            case '5' -> 5;
            case '6' -> 6;
            case '7' -> 7;
            case '8' -> 8;
            case '9' -> 9;
            case '0' -> 10;
            case 'J' -> 11;
            case 'Q' -> 12;
            case 'K' -> 13;
            case 'A' -> 14;
            default -> throw new IllegalArgumentException("Invalid rank representation: " + representation);
        };
    }

    private static final Map<Character, Rank> REPRESENTATION_TO_RANK = new HashMap<>();

    static {
        for (Rank rank : Rank.values()) {
            REPRESENTATION_TO_RANK.put(rank.representation, rank);
        }
    }

    public static Rank fromRepresentation(char representation) {
        Rank rank = REPRESENTATION_TO_RANK.get(representation);
        if (rank == null) {
            throw new IllegalArgumentException("Invalid rank representation: " + representation);
        }
        return rank;
    }

}
