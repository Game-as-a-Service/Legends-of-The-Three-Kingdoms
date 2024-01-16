package org.gaas.domain.handcard;

public enum Suit {

    CLUB('C'), DIAMOND('D'), HEART('H'), SPADE('S');

    private final char representation;

    Suit (char c) {
        this.representation = c;
    }

    @Override
    public String toString() {
        return String.valueOf(representation);
    }

}
