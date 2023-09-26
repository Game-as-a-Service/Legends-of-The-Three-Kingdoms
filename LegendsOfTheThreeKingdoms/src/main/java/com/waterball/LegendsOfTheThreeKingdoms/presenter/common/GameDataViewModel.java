package com.waterball.LegendsOfTheThreeKingdoms.presenter.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameDataViewModel {
    private List<PlayerDataViewModel> seats;
    private RoundDataViewModel round;
    private String gamePhase;
}
