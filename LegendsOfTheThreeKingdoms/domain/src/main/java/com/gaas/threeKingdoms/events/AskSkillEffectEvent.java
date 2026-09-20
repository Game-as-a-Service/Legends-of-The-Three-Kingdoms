package com.gaas.threeKingdoms.events;

import lombok.Getter;

import java.util.List;

/**
 * 通用「詢問武將技」事件。
 *
 * skillName — 技能名（反饋 / 遺計 / 剛烈 ...）
 * playerId  — 被詢問的玩家
 * data      — 技能相關展示資料（如可取的牌、來源玩家），key 依技能而定
 * options   — 本次詢問可回的 choice
 */
@Getter
public class AskSkillEffectEvent extends DomainEvent {

    /** 一般「是否發動」詢問的合法回應。 */
    public static final List<String> ACCEPT_OR_SKIP = List.of("ACCEPT", "SKIP");

    private final String skillName;
    private final String playerId;
    private final List<String> dataCardIds;
    private final String dataPlayerId;
    /**
     * 可回的 choice 值（直接送 {@code player:useSkillEffect} 的 choice）。
     * <p>
     * 不是「是否發動」型的詢問一定要自己帶：前端拿不到選項就只能畫通用的發動／放棄，
     * 而那兩個值對這類詢問都不合法，玩家怎麼按都回不了合法答案（使用者回報：剛烈判定
     * 生效後系統問司馬懿「要不要發動剛烈」，隨後卡局）。
     */
    private final List<String> options;

    public AskSkillEffectEvent(String skillName, String playerId, List<String> dataCardIds, String dataPlayerId) {
        this(skillName, playerId, dataCardIds, dataPlayerId, ACCEPT_OR_SKIP,
                String.format("%s：詢問 %s 是否發動", skillName, playerId));
    }

    public AskSkillEffectEvent(String skillName, String playerId, List<String> dataCardIds, String dataPlayerId,
                               List<String> options, String message) {
        super("AskSkillEffectEvent", message);
        this.skillName = skillName;
        this.playerId = playerId;
        this.dataCardIds = dataCardIds;
        this.dataPlayerId = dataPlayerId;
        this.options = options;
    }
}
