package com.gaas.threeKingdoms.controller.dto;

import com.gaas.threeKingdoms.usecase.UseHeavenlyDoubleHalberdKillUseCase;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class UseHeavenlyDoubleHalberdKillRequest {
    private String playerId;
    private String cardId;
    /**
     * 全部目標玩家 id（含主要與額外目標）；size 1~3、不重複、不能含自己、皆需在攻擊範圍。
     * Index 0 為主要目標。
     */
    private List<String> targetPlayerIds;

    public UseHeavenlyDoubleHalberdKillUseCase.UseHeavenlyDoubleHalberdKillRequest toUseHeavenlyDoubleHalberdKillRequest() {
        return new UseHeavenlyDoubleHalberdKillUseCase.UseHeavenlyDoubleHalberdKillRequest(
                playerId, cardId, targetPlayerIds);
    }
}
