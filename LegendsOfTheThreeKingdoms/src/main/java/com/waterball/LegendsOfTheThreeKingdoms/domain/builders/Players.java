package com.waterball.LegendsOfTheThreeKingdoms.domain.builders;

import com.waterball.LegendsOfTheThreeKingdoms.domain.Player;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;

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
