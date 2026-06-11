package com.gaas.threeKingdoms.events;

import lombok.Getter;

import java.util.List;

/**
 * 通用「詢問是否發動武將技」事件。
 *
 * skillName — 技能名（反饋 / 遺計 / 剛烈 ...）
 * playerId  — 被詢問（可發動）的玩家
 * data      — 技能相關展示資料（如可取的牌、來源玩家），key 依技能而定
 */
@Getter
public class AskSkillEffectEvent extends DomainEvent {

    private final String skillName;
    private final String playerId;
    private final List<String> dataCardIds;
    private final String dataPlayerId;

    public AskSkillEffectEvent(String skillName, String playerId, List<String> dataCardIds, String dataPlayerId) {
        super("AskSkillEffectEvent", String.format("%s：詢問 %s 是否發動", skillName, playerId));
        this.skillName = skillName;
        this.playerId = playerId;
        this.dataCardIds = dataCardIds;
        this.dataPlayerId = dataPlayerId;
    }
}
