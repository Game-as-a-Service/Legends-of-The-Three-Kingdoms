package com.gaas.threeKingdoms.controller.dto;

import com.gaas.threeKingdoms.usecase.UseSnatchUseCase;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UseSnatchRequest {
    private String currentPlayerId;
    private String targetPlayerId;
    private String cardId;
    private Integer targetCardIndex;

    private void validate() {
        if (cardId == null && targetCardIndex == null) {
            throw new IllegalArgumentException("Either cardId or targetCardIndex must be provided.");
        }
    }

    public UseSnatchUseCase.UseSnatchRequest toUseSnatchRequest() {
        validate();
        return new UseSnatchUseCase.UseSnatchRequest(currentPlayerId, targetPlayerId, cardId, targetCardIndex);
    }
}
