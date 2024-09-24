package com.gaas.threeKingdoms.repository.data;

import com.gaas.threeKingdoms.player.BloodCard;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BloodCardData {
    private int maxHp;
    private int hp;

    // Convert to domain object
    public BloodCard toDomain() {
        BloodCard bloodCard = new BloodCard(this.hp);
        bloodCard.setMaxHp(this.maxHp);
        return bloodCard;
    }

    // Convert from domain object
    public static BloodCardData fromDomain(BloodCard bloodCard) {
        return BloodCardData.builder()
                .maxHp(bloodCard.getMaxHp())
                .hp(bloodCard.getHp())
                .build();
    }
}
