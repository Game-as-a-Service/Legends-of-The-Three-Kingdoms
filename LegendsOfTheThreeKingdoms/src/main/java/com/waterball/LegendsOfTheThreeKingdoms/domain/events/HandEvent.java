package com.waterball.LegendsOfTheThreeKingdoms.domain.events;
import java.util.ArrayList;
import java.util.List;

public class HandEvent {
    int size;
    List<String> cardIds;

    public HandEvent(int size, List<String> cardIds) {
        this.size = size;
        this.cardIds = cardIds;
    }

    public int getSize() {
        return size;
    }

    public List<String> getCardIds() {
        return cardIds;
    }

    public static HandEvent deepCopy(HandEvent h) {
        return new HandEvent(h.getSize(), new ArrayList<>(h.getCardIds()));
    }
}
