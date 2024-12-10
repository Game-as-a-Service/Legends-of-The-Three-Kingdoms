package com.gaas.threeKingdoms.events;


import lombok.Getter;

@Getter
public class BorrowedSwordEvent extends DomainEvent {

    private final String cardId;
    private final String borrowedPlayerId;
    private final String attackTargetPlayerId;

    public BorrowedSwordEvent(String cardId, String borrowedPlayerId, String attackTargetPlayerId) {
        super("BorrowedSwordEvent", String.format("要求 %s 對 %s 出殺", borrowedPlayerId, attackTargetPlayerId));
        this.borrowedPlayerId = borrowedPlayerId;
        this.attackTargetPlayerId = attackTargetPlayerId;
        this.cardId = cardId;
    }
}
