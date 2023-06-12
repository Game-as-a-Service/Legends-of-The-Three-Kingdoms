package com.waterball.LegendsOfTheThreeKingdoms.utils;

import com.waterball.LegendsOfTheThreeKingdoms.domain.Role;

import java.util.*;

public class GameRoleAssignment {

    private static final Map<Integer, Role[]> ROLES = new HashMap<>() {{
        put(4, new Role[]{Role.MONARCH, Role.MINISTER, Role.REBEL, Role.TRAITOR});
        put(5, new Role[]{Role.MONARCH, Role.MINISTER, Role.REBEL, Role.TRAITOR, Role.REBEL});
        put(6, new Role[]{Role.MONARCH, Role.MINISTER, Role.REBEL, Role.TRAITOR, Role.REBEL, Role.REBEL});
        put(7, new Role[]{Role.MONARCH, Role.MINISTER, Role.REBEL, Role.TRAITOR, Role.REBEL, Role.REBEL, Role.MINISTER});
        put(8, new Role[]{Role.MONARCH, Role.MINISTER, Role.REBEL, Role.TRAITOR, Role.REBEL, Role.REBEL, Role.MINISTER, Role.REBEL});
        put(9, new Role[]{Role.MONARCH, Role.MINISTER, Role.REBEL, Role.TRAITOR, Role.REBEL, Role.REBEL, Role.MINISTER, Role.REBEL, Role.MINISTER});
        put(10, new Role[]{Role.MONARCH, Role.MINISTER, Role.REBEL, Role.TRAITOR, Role.REBEL, Role.REBEL, Role.MINISTER, Role.REBEL, Role.MINISTER, Role.TRAITOR});
    }};

    public static Role[] assignRoles(int players) {
        List<Role> availableRoles = new ArrayList<>(List.of(ROLES.get(players)));
        Collections.shuffle(availableRoles);
        return availableRoles.toArray(new Role[0]);
    }

}
