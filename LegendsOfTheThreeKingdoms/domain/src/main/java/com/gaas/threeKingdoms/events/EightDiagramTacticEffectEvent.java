package com.gaas.threeKingdoms.events;


import lombok.Getter;

@Getter
public class EightDiagramTacticEffectEvent extends EffectEvent {
    private final String drawCardId;

    public EightDiagramTacticEffectEvent(String message, String drawCardId, boolean isSuccess) {
        super("EightDiagramTacticEffectEvent", message, isSuccess);
        this.drawCardId = drawCardId;
    }
}
