package com.waterball.LegendsOfTheThreeKingdoms.controller.dto;

import java.io.Serializable;

import java.io.Serializable;

public class PlayerDto implements Serializable {
    private String id;
    private String role;

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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
