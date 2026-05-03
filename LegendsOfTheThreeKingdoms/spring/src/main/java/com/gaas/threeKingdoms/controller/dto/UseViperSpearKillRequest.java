package com.gaas.threeKingdoms.controller.dto;

import com.gaas.threeKingdoms.usecase.UseViperSpearKillUseCase;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class UseViperSpearKillRequest {
    private String playerId;
    /**
     * 攻擊目標。
     * - Active（出牌階段）：必填
     * - Passive 回應：BarbarianInvasion / Duel 不需要；BorrowedSword 必填（指定攻擊目標）
     */
    private String targetPlayerId;
    private List<String> discardCardIds;

    public UseViperSpearKillUseCase.UseViperSpearKillRequest toUseViperSpearKillRequest() {
        return new UseViperSpearKillUseCase.UseViperSpearKillRequest(playerId, targetPlayerId, discardCardIds);
    }
}
