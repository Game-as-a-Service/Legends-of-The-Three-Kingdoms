package com.waterball.LegendsOfTheThreeKingdoms.domain.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InitialEndEvent extends DomainEvent {
    private final String name = "OtherChooseGeneralCardEvent";

    private String gameId;

    private List<PlayerEvent> seats;

    private RoundEvent round;

    private String gamePhase;

}
