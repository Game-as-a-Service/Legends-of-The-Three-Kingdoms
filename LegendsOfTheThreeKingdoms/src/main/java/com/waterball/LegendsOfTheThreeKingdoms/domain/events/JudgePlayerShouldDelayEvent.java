package com.waterball.LegendsOfTheThreeKingdoms.domain.events;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JudgePlayerShouldDelayEvent extends DomainEvent {

    private final String name = "JudgementEvent";
    private String message = "判定結束";
}
