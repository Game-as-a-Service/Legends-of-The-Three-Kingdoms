package com.gaas.threeKingdoms.events;


import lombok.Getter;

import java.util.List;

@Getter
public class EightDiagramTacticEffectEvent extends EffectEvent {
    private final String drawCardId;

    public EightDiagramTacticEffectEvent(String message, boolean isSuccess,String drawCardId) {
        super(isSuccess, message, "EightDiagramTacticEffectEvent");
        this.drawCardId = drawCardId;
    }
}
