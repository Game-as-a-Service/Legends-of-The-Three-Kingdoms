package com.gaas.threeKingdoms.events;

import lombok.*;


public class JudgementEvent extends DomainEvent {
    public JudgementEvent (){super("JudgementEvent","判定結束");}
}
