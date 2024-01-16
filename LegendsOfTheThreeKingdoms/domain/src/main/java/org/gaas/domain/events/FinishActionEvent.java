package org.gaas.domain.events;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class FinishActionEvent extends DomainEvent{

    private String name = "FinishActionEvent";
    private String message = "結束出牌";

}
