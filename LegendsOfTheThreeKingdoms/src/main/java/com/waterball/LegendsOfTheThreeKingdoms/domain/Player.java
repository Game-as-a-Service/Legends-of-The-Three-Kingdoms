package com.waterball.LegendsOfTheThreeKingdoms.domain;

public class Player {
    private String id;
    private Role role;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
