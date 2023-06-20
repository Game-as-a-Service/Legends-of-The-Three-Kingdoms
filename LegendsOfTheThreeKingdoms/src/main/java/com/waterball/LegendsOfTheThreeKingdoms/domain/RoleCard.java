package com.waterball.LegendsOfTheThreeKingdoms.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;



@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoleCard {
    private String name;


     // TODO 待重構整個RoleCard的分配
//    private static final Map<Integer, RoleCard[]> ROLES = new HashMap<>() {{
//        put(4, new RoleCard[]{new RoleCard(RoleCard.Role(MONARCH), Role.MINISTER, Role.REBEL, Role.TRAITOR});
//        put(5, new RoleCard[]{new RoleCard(Role(MONARCH)), Role.MINISTER, Role.REBEL, Role.TRAITOR, Role.REBEL});
//        put(6, new RoleCard[]{MONARCH, Role.MINISTER, Role.REBEL, Role.TRAITOR, Role.REBEL, Role.REBEL});
//        put(7, new RoleCard[]{MONARCH, Role.MINISTER, Role.REBEL, Role.TRAITOR, Role.REBEL, Role.REBEL, Role.MINISTER});
//        put(8, new RoleCard[]{MONARCH, Role.MINISTER, Role.REBEL, Role.TRAITOR, Role.REBEL, Role.REBEL, Role.MINISTER, Role.REBEL});
//        put(9, new RoleCard[]{MONARCH, Role.MINISTER, Role.REBEL, Role.TRAITOR, Role.REBEL, Role.REBEL, Role.MINISTER, Role.REBEL, Role.MINISTER});
//        put(10, new RoleCard[]{MONARCH, Role.MINISTER, Role.REBEL, Role.TRAITOR, Role.REBEL, Role.REBEL, Role.MINISTER, Role.REBEL, Role.MINISTER, Role.TRAITOR});
//    }};
//
//    public enum Role {
//        MONARCH("Monarch"),
//        MINISTER("Minister"),
//        REBEL("Rebel"),
//        TRAITOR("Traitor");
//
//        Role(String role) {
//        }
//    }
}

