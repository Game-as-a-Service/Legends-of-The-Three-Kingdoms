package com.waterball.LegendsOfTheThreeKingdoms.domain;

import com.waterball.LegendsOfTheThreeKingdoms.domain.player.Player;
import lombok.Data;


@Data
public class Round {
    private Phase phase;
    private Player currentPlayer;

    public Round (Player currentPlayer) {
        this.phase = Phase.Judgement;
        this.currentPlayer = currentPlayer;
    }


}
