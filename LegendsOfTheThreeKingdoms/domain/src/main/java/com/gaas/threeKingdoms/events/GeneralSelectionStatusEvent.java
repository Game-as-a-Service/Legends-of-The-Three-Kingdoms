package com.gaas.threeKingdoms.events;

import lombok.Getter;

import java.util.List;

/**
 * 選將進度廣播（issue #237）：每次有人選完武將（含主公）就廣播給全部玩家。
 * <p>
 * 只帶「誰選完了」，不帶選了什麼 — 身分局其他人是暗選，武將 id 由開局的
 * {@link InitialEndEvent} 才揭曉（主公亮將另有 MonarchChooseGeneralCardEvent）。
 */
@Getter
public class GeneralSelectionStatusEvent extends DomainEvent {

    private final String gameId;
    private final List<String> playerIds; // 全部玩家（broadcast 路由用）
    private final List<String> selectedPlayerIds;
    private final List<String> pendingPlayerIds;
    private final boolean allSelected;

    public GeneralSelectionStatusEvent(String gameId, List<String> playerIds,
                                       List<String> selectedPlayerIds, List<String> pendingPlayerIds,
                                       String justSelectedPlayerId) {
        super("GeneralSelectionStatusEvent", String.format("%s 已選擇武將（%d/%d）",
                justSelectedPlayerId, selectedPlayerIds.size(), playerIds.size()));
        this.gameId = gameId;
        this.playerIds = playerIds;
        this.selectedPlayerIds = selectedPlayerIds;
        this.pendingPlayerIds = pendingPlayerIds;
        this.allSelected = pendingPlayerIds.isEmpty();
    }
}
