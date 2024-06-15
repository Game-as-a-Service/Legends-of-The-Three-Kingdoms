package com.gaas.threeKingdoms.events;

import lombok.Getter;

import java.util.List;

@Getter
public class AskChooseMountCardEvent extends DomainEvent{

    private List<String> mountsCardIds;
    private String chooseMountCardPlayerId;
    private String targetPlayerId;

    public AskChooseMountCardEvent(String name, String message, List<String> mountsCardIds, String chooseMountCardPlayerId, String targetPlayerId) {
        super(name, message);
        this.chooseMountCardPlayerId = chooseMountCardPlayerId;
        this.targetPlayerId = targetPlayerId;
        this.mountsCardIds = mountsCardIds;
    }
}
