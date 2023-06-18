package com.waterball.LegendsOfTheThreeKingdoms.utils;

import com.waterball.LegendsOfTheThreeKingdoms.domain.RoleCard;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GameRoleAssignment {

    public List<RoleCard> assignRoles(int players) {
        String[] strArray = new String[]{"Monarch", "Minister", "Rebel", "Traitor"};
        return Arrays.stream(strArray).map(role -> new RoleCard(role)).collect(Collectors.toList());
    }

}
