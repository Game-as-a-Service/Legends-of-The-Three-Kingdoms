package com.gaas.threeKingdoms.repository.data;

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

    // Convert to domain object
    public GeneralCard toDomain() {
        GeneralCard generalCard = new GeneralCard();
        generalCard.setGeneralId(this.generalId);
        generalCard.setGeneralName(this.generalName);
        generalCard.setHealthPoint(this.healthPoint);
        return generalCard;
    }

    // Convert from domain object
    public static GeneralCardData fromDomain(GeneralCard generalCard) {
        return GeneralCardData.builder()
                .generalId(generalCard.getGeneralId())
                .generalName(generalCard.getGeneralName())
                .healthPoint(generalCard.getHealthPoint())
                .build();
    }
}
