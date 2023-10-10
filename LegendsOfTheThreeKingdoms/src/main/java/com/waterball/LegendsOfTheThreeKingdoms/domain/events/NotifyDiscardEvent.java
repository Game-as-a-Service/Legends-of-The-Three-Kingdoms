package com.waterball.LegendsOfTheThreeKingdoms.domain.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NotifyDiscardEvent extends DomainEvent {
    private final String name = "NotifyDiscardEvent";
    private final String message = "通知棄牌數量";
    private int discardCount;

}
