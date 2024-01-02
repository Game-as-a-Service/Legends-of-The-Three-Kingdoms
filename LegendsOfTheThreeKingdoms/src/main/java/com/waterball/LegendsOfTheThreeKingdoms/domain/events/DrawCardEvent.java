package com.waterball.LegendsOfTheThreeKingdoms.domain.events;

import com.waterball.LegendsOfTheThreeKingdoms.domain.player.Player;
import lombok.*;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class DrawCardEvent extends DomainEvent {
    private int size;
    private List<String> cardIds;
    private String message;
    private String name = "DrawCardToPlayerEvent";
    private String gameId;
    private List<PlayerEvent> seats;
    private RoundEvent round;
    private String gamePhase;
    private String drawCardPlayerId;

    public DrawCardEvent(int size, List<String> cardIds, String message, String gameId, List<PlayerEvent> seats, RoundEvent round, String gamePhase, String drawCardPlayerId) {
        this.size = size;
        this.cardIds = cardIds;
        this.message = message;
        this.gameId = gameId;
        this.seats = seats;
        this.round = round;
        this.gamePhase = gamePhase;
        this.drawCardPlayerId = drawCardPlayerId;
    }
}
