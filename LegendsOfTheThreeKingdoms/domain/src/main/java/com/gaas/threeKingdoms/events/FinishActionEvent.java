package com.gaas.threeKingdoms.events;

public class FinishActionEvent extends DomainEvent {
    public FinishActionEvent() {
        super("FinishActionEvent", "結束出牌");
    }
}
