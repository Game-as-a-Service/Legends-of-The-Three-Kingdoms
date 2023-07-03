package com.waterball.LegendsOfTheThreeKingdoms.domain.rolecard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoleCard {

    private Role role;

    public static final Map<Integer, RoleCard[]> ROLES = new HashMap<>() {{
        put(4, fromRoles(Role.MONARCH, Role.MINISTER, Role.REBEL, Role.TRAITOR));
        put(5, fromRoles(Role.MONARCH, Role.MINISTER, Role.REBEL, Role.TRAITOR, Role.REBEL));
    }};


    public static RoleCard[] fromRoles(Role ... roles) {
        return Arrays.stream(roles).map(r -> new RoleCard(r)).collect(Collectors.toList()).toArray(new RoleCard[0]);
    }

}
