package com.gaas.threeKingdoms.events;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.player.Player;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HandEvent {
    int size;
    List<String> cardIds;

    public HandEvent(int size, List<String> cardIds) {
        this.size = size;
        this.cardIds = cardIds;
    }

    public HandEvent(Player player) {
        this.size = player.getHandSize();
        this.cardIds = player.getHand().getCards().stream().map(HandCard::getId).collect(Collectors.toList());
    }

    public static HandEvent deepCopy(HandEvent h) {
        return new HandEvent(h.getSize(), new ArrayList<>(h.getCardIds()));
    }
}
