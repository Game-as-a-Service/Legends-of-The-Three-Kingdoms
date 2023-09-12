package com.waterball.LegendsOfTheThreeKingdoms.domain.events;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
public class DrawCardToPlayerEvent extends DomainEvent {
    private int size;
    private List<String> cardIds;
    private String name = "DrawCardToPlayerEvent";
    private String message;

    public DrawCardToPlayerEvent(int size, List<String> cardIds, String message) {
        this.size = size;
        this.cardIds = cardIds;
        this.message = message;
    }
}
