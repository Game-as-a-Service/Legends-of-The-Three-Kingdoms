package com.gaas.threeKingdoms.events;


import lombok.Getter;

@Getter
public class BorrowedSwordEvent extends DomainEvent {

    private String cardId;
    private String playerId;
    private String askKillPlayerId;

    public BorrowedSwordEvent(String playerId, String cardId, String askKillPlayerId) {
        super("BorrowedSwordEvent", String.format("%s 出借刀殺人", playerId));
        this.playerId = playerId;
        this.cardId = cardId;
        this.askKillPlayerId = askKillPlayerId;
    }
}
