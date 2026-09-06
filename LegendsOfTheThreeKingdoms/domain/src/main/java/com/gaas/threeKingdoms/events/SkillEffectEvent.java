package com.gaas.threeKingdoms.events;

import lombok.Getter;

import java.util.List;

/**
 * 通用「武將技發動結果」廣播事件。
 */
@Getter
public class SkillEffectEvent extends DomainEvent {

    private final String skillName;
    private final String playerId;
    private final boolean accepted;
    private final List<String> dataCardIds;
    private final String dataPlayerId;

    public SkillEffectEvent(String skillName, String playerId, boolean accepted,
                            List<String> dataCardIds, String dataPlayerId) {
        this(skillName, playerId, accepted, dataCardIds, dataPlayerId,
                String.format("%s：%s %s", skillName, playerId, accepted ? "發動" : "放棄"));
    }

    /** 帶具體結算訊息的版本 — 前端顯示事件層 message（issue #235）。 */
    public SkillEffectEvent(String skillName, String playerId, boolean accepted,
                            List<String> dataCardIds, String dataPlayerId, String message) {
        super("SkillEffectEvent", message);
        this.skillName = skillName;
        this.playerId = playerId;
        this.accepted = accepted;
        this.dataCardIds = dataCardIds;
        this.dataPlayerId = dataPlayerId;
    }
}
