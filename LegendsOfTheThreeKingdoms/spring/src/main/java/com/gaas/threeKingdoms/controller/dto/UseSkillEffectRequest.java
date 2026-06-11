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

    public UseSkillEffectUseCase.UseSkillEffectRequest toUseSkillEffectRequest() {
        return new UseSkillEffectUseCase.UseSkillEffectRequest(playerId, skillName, choice, cardIds, targetPlayerId);
    }
}
