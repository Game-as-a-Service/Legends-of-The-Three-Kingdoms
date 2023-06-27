package com.waterball.LegendsOfTheThreeKingdoms.domain;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Player {
    private String id;
    private RoleCard roleCard;
    private GeneralCard generalCard;
    private BloodCard bloodCard;

    public void setBloodCard() {
        int healthPoint = roleCard.getRole().equals(Role.MONARCH) ? 1 : 0;
        bloodCard = new BloodCard(generalCard.getHealthPoint() + healthPoint);
    }

    public int getHP() {
        return bloodCard.getHp();
    }
}
