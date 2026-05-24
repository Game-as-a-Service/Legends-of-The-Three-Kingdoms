package com.gaas.threeKingdoms.behavior;

import com.gaas.threeKingdoms.events.DomainEvent;

import java.util.List;

/**
 * Marker + capability：能 host 護駕 substitute 的「會 emit AskDodgeEvent」behavior。
 *
 * 當 Wei 武將代替主公曹操出閃後，{@link com.gaas.threeKingdoms.behavior.behavior.WaitingHuJiaResponseBehavior}
 * 會 pop 自己並 dispatch 到 parent (this) 的 {@code acceptDodgeFromHuJia}，由 parent 負責執行
 * 後續流程（青龍偃月刀鏈 / 貫石斧鏈 / AOE polling 推進）。
 *
 * 實作類別：
 *   - {@link com.gaas.threeKingdoms.behavior.behavior.NormalActiveKillBehavior}
 *   - {@link com.gaas.threeKingdoms.behavior.behavior.HeavenlyDoubleHalberdKillBehavior}（繼承上者）
 *   - {@link com.gaas.threeKingdoms.behavior.behavior.ArrowBarrageBehavior}
 */
public interface HuJiaCompatibleAskDodgeBehavior {

    /**
     * Wei 武將已經把代閃 cardId 從手中棄到墓地後，parent 在此繼續未完的 dodge 流程。
     *
     * @param dodgedPlayerId 被代替的玩家（曹操）ID
     * @param weiPlayerId    實際打出閃的 Wei 武將 ID
     * @param dodgeCardId    被打出的閃 cardId（已在墓地）
     */
    List<DomainEvent> acceptDodgeFromHuJia(String dodgedPlayerId, String weiPlayerId, String dodgeCardId);
}
