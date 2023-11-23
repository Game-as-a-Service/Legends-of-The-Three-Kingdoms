package com.waterball.LegendsOfTheThreeKingdoms.domain.events;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.Player;

public class HandEvent {
    int size;
    List<String> cardIds;

    public HandEvent(int size, List<String> cardIds) {
        this.size = size;
        this.cardIds = cardIds;
    }

    public HandEvent(Player palyer) {
        this.size = palyer.getHandSize();
        this.cardIds = palyer.getHand().getCards().stream().map(handCard -> handCard.getId()).collect(Collectors.toList());
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
