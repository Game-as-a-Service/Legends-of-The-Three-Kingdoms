package com.gaas.threeKingdoms.behavior;

/**
 * Marker：top behavior 表示可以 host 奸雄（JianXiongSkill）介入。
 * <p>
 * 實作此介面代表：受傷流程結束時，把 WaitingJianXiongResponseBehavior 插在自己之上
 * （或取代自己）不會破壞既有 stack / activePlayer / polling state。
 * <p>
 * 注意：未來其他 OnDamagedSkill（反饋、苦肉觸發等）若有類似需求，可考慮把此 marker
 * 改名為 {@code OnDamagedSkillCompatibleTopBehavior} 並重用。
 */
public interface JianXiongCompatibleTopBehavior {

    /**
     * polling-style caller 在 damage 後仍需保留底層 behavior、等 WaitingJX 解決後 resume polling。
     * caller 須自行：
     *   1. 在 damage 後偵測 {@code WaitingJianXiongResponseBehavior} 在 stack 頂
     *   2. 把 polling-advance 邏輯註冊為 {@code WaitingJianXiongResponseBehavior.setOnResolved(...)}
     *   3. damage event 之外不立即 emit 後續 polling 事件
     * <p>
     * JianXiongSkill 看到此值為 true 時，不會呼叫 {@code removeCompletedBehaviors()}，
     * 保留 polling behavior 等 callback resume。
     */
    default boolean isPollingCaller() {
        return false;
    }
}
