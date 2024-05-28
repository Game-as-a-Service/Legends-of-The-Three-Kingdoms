package com.gaas.threeKingdoms.events;

import lombok.Getter;

import java.util.List;

@Getter
public class AskChooseMountCardEvent extends DomainEvent{

    private List<String> mountsCardIds;

    public AskChooseMountCardEvent(String name, String message, List<String> mountsCardIds) {
        super(name, message);
        this.mountsCardIds = mountsCardIds;
    }
}
