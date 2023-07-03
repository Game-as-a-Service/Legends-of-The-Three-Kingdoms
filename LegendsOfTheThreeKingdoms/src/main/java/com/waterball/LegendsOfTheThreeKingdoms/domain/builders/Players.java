package com.waterball.LegendsOfTheThreeKingdoms.domain.builders;

import com.waterball.LegendsOfTheThreeKingdoms.domain.player.Player;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Players {

    public static Player.PlayerBuilder defaultPlayerBuilder(String id) {
        return Player.builder()
                .id(id);
    }

    public static Player defaultPlayer(String id) {
        return defaultPlayerBuilder(id).build();
    }
}
