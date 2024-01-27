package com.gaas.threeKingdoms.events;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FinishActionEvent extends DomainEvent{

    private String name = "FinishActionEvent";
    private String message = "結束出牌";

}
