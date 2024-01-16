package org.gaas.domain.builders;

import org.gaas.domain.generalcard.GeneralCard;
import org.gaas.domain.player.BloodCard;
import org.gaas.domain.player.Hand;
import org.gaas.domain.player.HealthStatus;
import org.gaas.domain.player.Player;
import org.gaas.domain.rolecard.RoleCard;

public class PlayerBuilder {
    private Hand hand;
    private String id;
    private RoleCard roleCard;
    private GeneralCard generalCard;
    private BloodCard bloodCard;
    private HealthStatus healthStatus;

    public static PlayerBuilder construct(){
        return new PlayerBuilder();
    }

    public PlayerBuilder withHand(Hand hand){
        this.hand = hand;
        return this;
    }

    public PlayerBuilder withId(String id){
        this.id = id;
        return this;
    }

    public  PlayerBuilder withRoleCard(RoleCard roleCard){
        this.roleCard = roleCard;
        return this;
    }
    public  PlayerBuilder withGeneralCard(GeneralCard generalCard){
        this.generalCard = generalCard;
        return this;
    }
    public  PlayerBuilder withBloodCard(BloodCard bloodCard){
        this.bloodCard = bloodCard;
        return this;
    }

    public  PlayerBuilder withHealthStatus(HealthStatus healthStatus){
        this.healthStatus = healthStatus;
        return this;
    }

    public Player build(){
        return new Player(hand,id,roleCard,generalCard,bloodCard,healthStatus);
    }
}
