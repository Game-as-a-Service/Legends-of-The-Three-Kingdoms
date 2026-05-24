package com.gaas.threeKingdoms.repository.data;

import com.gaas.threeKingdoms.generalcard.Faction;
import com.gaas.threeKingdoms.generalcard.Gender;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneralCardData {
    private String generalId;
    private String generalName;
    private int healthPoint;
    private Gender gender;
    private Faction faction;

    // Backward-compat constructor for tests created before gender field was added
    public GeneralCardData(String generalId, String generalName, int healthPoint) {
        this.generalId = generalId;
        this.generalName = generalName;
        this.healthPoint = healthPoint;
        this.gender = null;
        this.faction = null;
    }

    // Convert to domain object
    public GeneralCard toDomain() {
        GeneralCard generalCard = new GeneralCard();
        generalCard.setGeneralId(this.generalId);
        generalCard.setGeneralName(this.generalName);
        generalCard.setHealthPoint(this.healthPoint);
        generalCard.setGender(this.gender);
        // 對舊資料（無 faction）做 fallback 推導
        generalCard.setFaction(this.faction != null
                ? this.faction
                : (this.generalId != null ? General.findById(this.generalId).getFaction() : null));
        return generalCard;
    }

    // Convert from domain object
    public static GeneralCardData fromDomain(GeneralCard generalCard) {
        if (generalCard == null) {
            return null;
        }

        return GeneralCardData.builder()
                .generalId(generalCard.getGeneralId())
                .generalName(generalCard.getGeneralName())
                .healthPoint(generalCard.getHealthPoint())
                .gender(generalCard.getGender())
                .faction(generalCard.getFaction())
                .build();
    }
}
