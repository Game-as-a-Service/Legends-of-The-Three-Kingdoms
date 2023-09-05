package com.waterball.LegendsOfTheThreeKingdoms.controller.dto;

import com.waterball.LegendsOfTheThreeKingdoms.service.GameService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChooseGeneralRequest {
    private String playerId;
    private String generalId;

    public GameService.MonarchChooseGeneralRequest toMonarchChooseGeneralRequest() {
        return new GameService.MonarchChooseGeneralRequest(this.playerId, this.generalId);
    }
    public GameService.OthersChooseGeneralRequest toChooseGeneralRequest() {
        return new GameService.OthersChooseGeneralRequest(this.playerId, this.generalId);
    }
}
