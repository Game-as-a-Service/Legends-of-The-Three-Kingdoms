package com.waterball.LegendsOfTheThreeKingdoms.controller.dto;

import com.waterball.LegendsOfTheThreeKingdoms.domain.Role;

import java.io.Serializable;

import java.io.Serializable;

public class PlayerDto implements Serializable {
    private String id;
    private Role role;

    public PlayerDto() {
    }

    public PlayerDto(String id) {
        this.id = id;
    }

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
