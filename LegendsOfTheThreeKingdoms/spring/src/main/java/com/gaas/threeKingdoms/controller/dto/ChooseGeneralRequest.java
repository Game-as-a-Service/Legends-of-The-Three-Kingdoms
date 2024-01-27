package com.gaas.threeKingdoms.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.gaas.threeKingdoms.usecase.MonarchChooseGeneralUseCase;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChooseGeneralRequest {
    private String playerId;
    private String generalId;

    public MonarchChooseGeneralUseCase.MonarchChooseGeneralRequest toMonarchChooseGeneralRequest() {
        return new MonarchChooseGeneralUseCase.MonarchChooseGeneralRequest(this.playerId, this.generalId);
    }
    public MonarchChooseGeneralUseCase.OthersChooseGeneralRequest toChooseGeneralRequest() {
        return new MonarchChooseGeneralUseCase.OthersChooseGeneralRequest(this.playerId, this.generalId);
    }

}

