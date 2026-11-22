package com.gaas.threeKingdoms.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.PlayCardEvent;
import com.gaas.threeKingdoms.events.ViperSpearKillTriggerEvent;
import com.gaas.threeKingdoms.handcard.PlayType;

import java.util.List;

/**
 * {@link Behavior#acceptVirtualKillResponse} 的「殺從哪來」：同一條回應通道被三種來源共用，
 * 但要發給前端的事件與遊戲訊息不同（原本一律寫成丈八蛇矛）。
 * <ul>
 *   <li>{@link Kind#VIPER_SPEAR}：丈八蛇矛棄兩張手牌當殺 → {@link ViperSpearKillTriggerEvent}</li>
 *   <li>{@link Kind#CONVERSION}：武聖 / 龍膽把一張牌當殺 → 來源牌的 {@link PlayCardEvent}</li>
 *   <li>{@link Kind#JI_JIANG}：蜀將代劉備打出殺 → 該殺的 {@link PlayCardEvent}（視為劉備打出）</li>
 * </ul>
 * 只影響事件與訊息，不影響結算。
 */
public record VirtualKillResponseSource(Kind kind, String skillName) {

    public enum Kind { VIPER_SPEAR, CONVERSION, JI_JIANG }

    public static final VirtualKillResponseSource VIPER_SPEAR = new VirtualKillResponseSource(Kind.VIPER_SPEAR, "丈八蛇矛");
    public static final VirtualKillResponseSource JI_JIANG = new VirtualKillResponseSource(Kind.JI_JIANG, "激將");

    public static VirtualKillResponseSource conversion(String skillName) {
        return new VirtualKillResponseSource(Kind.CONVERSION, skillName);
    }

    public boolean isViperSpear() {
        return kind == Kind.VIPER_SPEAR;
    }

    /** 出殺的事件：丈八蛇矛 → 發動事件；轉化 / 激將 → 打出的實體牌（cardIds 只有一張）。 */
    public DomainEvent killEvent(String playerId, String targetPlayerId, List<String> cardIds) {
        if (isViperSpear()) {
            return new ViperSpearKillTriggerEvent(playerId, targetPlayerId, cardIds);
        }
        return new PlayCardEvent("出牌", playerId, targetPlayerId, cardIds.get(0), PlayType.ACTIVE.getPlayType());
    }

    /** 出殺的遊戲訊息，例「趙雲 發動龍膽出殺」/「劉備 以激將打出 殺」。 */
    public String statusMessage(Game game, String playerId) {
        return switch (kind) {
            case VIPER_SPEAR -> playerId + "用丈八蛇矛出殺";
            case CONVERSION -> String.format("%s 發動%s出殺", game.getPlayer(playerId).getGeneralName(), skillName);
            case JI_JIANG -> String.format("%s 以激將打出 殺", game.getPlayer(playerId).getGeneralName());
        };
    }
}
