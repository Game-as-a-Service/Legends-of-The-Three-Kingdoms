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

    public static final Map<Integer, RoleCard[]> ROLES = new HashMap<>() {{
        put(4, new RoleCard[]{
                new RoleCard(Role.MONARCH.toString()),
                new RoleCard(Role.MINISTER.toString()),
                new RoleCard(Role.REBEL.toString()),
                new RoleCard(Role.TRAITOR.toString())
        });
        put(5, new RoleCard[]{
                new RoleCard(Role.MONARCH.toString()),
                new RoleCard(Role.MINISTER.toString()),
                new RoleCard(Role.REBEL.toString()),
                new RoleCard(Role.TRAITOR.toString()),
                new RoleCard(Role.REBEL.toString())
        });
    }};


    public enum Role {
        MONARCH("Monarch"),
        MINISTER("Minister"),
        REBEL("Rebel"),
        TRAITOR("Traitor");

        private final String role;

        Role(String role) {
            this.role = role;
        }

        public String getRole() {
            return role;
        }
    }
}

