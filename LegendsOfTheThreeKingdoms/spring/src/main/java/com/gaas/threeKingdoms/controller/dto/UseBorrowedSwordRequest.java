package com.gaas.threeKingdoms.controller.dto;

import com.gaas.threeKingdoms.usecase.ChooseHorseUseCase;
import com.gaas.threeKingdoms.usecase.UseBorrowedSwordEffectUseCase;
import com.gaas.threeKingdoms.usecase.UseEquipmentUseCase;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UseBorrowedSwordRequest {
    private String currentPlayerId;
    private String borrowedPlayerId;
    private String attackTargetPlayerId;

    public UseBorrowedSwordEffectUseCase.UseBorrowedSwordRequest toUseBorrowedSwordRequest() {
        return new UseBorrowedSwordEffectUseCase.UseBorrowedSwordRequest(currentPlayerId, borrowedPlayerId, attackTargetPlayerId);
    }
}
