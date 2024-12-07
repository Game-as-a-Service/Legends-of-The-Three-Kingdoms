package com.gaas.threeKingdoms.controller.dto;


import com.gaas.threeKingdoms.usecase.UseDismantleUseCase;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UseDismantleRequest {
    private String currentPlayerId;
    private String targetPlayerId;
    private String cardId;
    private Integer targetCardIndex;

    private void validate() {
        if (cardId == null && targetCardIndex == null) {
            throw new IllegalArgumentException("Either cardId or targetCardIndex must be provided.");
        }
    }
    public UseDismantleUseCase.UseDismantleRequest toUseDismantleRequest() {
        validate();
        return new UseDismantleUseCase.UseDismantleRequest(currentPlayerId, targetPlayerId, cardId, targetCardIndex);
    }


}
