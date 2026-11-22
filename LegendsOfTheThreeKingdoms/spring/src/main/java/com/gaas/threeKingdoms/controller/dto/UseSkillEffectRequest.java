package com.gaas.threeKingdoms.controller.dto;

import com.gaas.threeKingdoms.usecase.UseSkillEffectUseCase;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class UseSkillEffectRequest {
    private String playerId;
    private String skillName;
    private String choice;
    private List<String> cardIds;
    private String targetPlayerId;
    /** 選填：與 cardIds 逐張對應的接收者（仁德一次分給多人）；未提供時沿用 targetPlayerId。 */
    private List<String> targetPlayerIds;

    public UseSkillEffectUseCase.UseSkillEffectRequest toUseSkillEffectRequest() {
        return new UseSkillEffectUseCase.UseSkillEffectRequest(playerId, skillName, choice, cardIds, targetPlayerId, targetPlayerIds);
    }
}
