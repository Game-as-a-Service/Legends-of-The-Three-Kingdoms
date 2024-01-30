package com.gaas.threeKingdoms.player;

import lombok.Data;

@Data
public class BloodCard {
    private int maxHp;
    private int hp;

    public BloodCard(int hp) {
        this.maxHp = hp;
        this.hp = hp;
    }
}
