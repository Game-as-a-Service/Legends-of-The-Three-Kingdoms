package com.gaas.threeKingdoms.events;


import com.gaas.threeKingdoms.handcard.PlayCard;
import lombok.Getter;

@Getter
public class WeaponUsurpationEvent extends DomainEvent{

    private final String givenWeaponPlayerId;
    private final String takenWeaponPlayerId;
    private final String weaponCardId;

    public WeaponUsurpationEvent(String givenWeaponPlayerId, String takenWeaponPlayerId, String weaponCardId) {
        super("WeaponUsurpationEvent", String.format("玩家 %s 搶奪了 玩家 %s 的 %s", takenWeaponPlayerId, givenWeaponPlayerId, PlayCard.getCardName(weaponCardId)));
        this.givenWeaponPlayerId = givenWeaponPlayerId;
        this.takenWeaponPlayerId = takenWeaponPlayerId;
        this.weaponCardId = weaponCardId;
    }
}
