package com.gaas.threeKingdoms.events;

import com.gaas.threeKingdoms.player.Player;

import java.util.Collections;
import java.util.List;

public class PlayerEvent extends DomainEvent {

    private String id;
    private String generalId;
    private String roleId;
    private int hp;
    private HandEvent hand;
    private List<String> equipments;
    private List<String> delayScrolls;

    public PlayerEvent(String id, String generalId, String roleId, int hp, HandEvent hand, List<String> equipments, List<String> delayScrolls) {
        this.id = id;
        this.generalId = generalId;
        this.roleId = roleId;
        this.hp = hp;
        this.hand = hand;
        this.equipments = equipments;
        this.delayScrolls = delayScrolls;
    }

    public PlayerEvent(Player player) {
        this.id = player.getId();
        this.generalId = player.getGeneralCard().getGeneralId();
        this.roleId = player.getRoleCard().getRole().getRoleName();
        this.hp = player.getHP();
        this.hand = new HandEvent(player);
        this.equipments = Collections.emptyList();
        this.delayScrolls = Collections.emptyList();
    }

    public String getId() {
        return id;
    }

    public String getGeneralId() {
        return generalId;
    }

    public String getRoleId() {
        return roleId;
    }

    public int getHp() {
        return hp;
    }

    public HandEvent getHand() {
        return hand;
    }

    public List<String> getEquipments() {
        return equipments;
    }

    public List<String> getDelayScrolls() {
        return delayScrolls;
    }
}
