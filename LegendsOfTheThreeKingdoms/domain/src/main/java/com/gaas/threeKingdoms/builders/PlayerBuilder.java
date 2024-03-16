package com.gaas.threeKingdoms.builders;

import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.player.*;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.rolecard.RoleCard;

public class PlayerBuilder {
    private Hand hand;
    private String id;
    private RoleCard roleCard;
    private GeneralCard generalCard;
    private BloodCard bloodCard;
    private HealthStatus healthStatus;
    private Equipment equipment;

    public static PlayerBuilder construct() {
        return new PlayerBuilder();
    }

    public PlayerBuilder withHand(Hand hand) {
        this.hand = hand;
        return this;
    }

    public PlayerBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public PlayerBuilder withRoleCard(RoleCard roleCard) {
        this.roleCard = roleCard;
        return this;
    }

    public PlayerBuilder withGeneralCard(GeneralCard generalCard) {
        this.generalCard = generalCard;
        return this;
    }

    public PlayerBuilder withBloodCard(BloodCard bloodCard) {
        this.bloodCard = bloodCard;
        return this;
    }

    public PlayerBuilder withHealthStatus(HealthStatus healthStatus) {
        this.healthStatus = healthStatus;
        return this;
    }

    public PlayerBuilder withEquipment(Equipment equipment) {
        this.equipment = equipment;
        return this;
    }

    public PlayerBuilder withDefault() {
        this.roleCard = new RoleCard(Role.MONARCH);
        this.generalCard = new GeneralCard(General.劉備);
        this.bloodCard = new BloodCard(4);
        this.hand = new Hand();
        this.healthStatus = HealthStatus.ALIVE;
        return this;
    }

    public Player build(){
        return new Player(hand,id,roleCard,generalCard,bloodCard,healthStatus, equipment);
    }
}
