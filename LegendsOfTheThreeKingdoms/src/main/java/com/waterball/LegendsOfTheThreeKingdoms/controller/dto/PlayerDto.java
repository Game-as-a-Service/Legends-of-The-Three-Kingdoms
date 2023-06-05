package com.waterball.LegendsOfTheThreeKingdoms.controller.dto;

import java.io.Serializable;

public class PlayerDto implements Serializable {
    private String id;

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
}
