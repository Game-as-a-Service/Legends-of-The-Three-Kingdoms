package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.ViperSpearKillTriggerEvent;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.VirtualKill;
import com.gaas.threeKingdoms.player.Player;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * 丈八蛇矛虛擬殺 Behavior
 * 玩家棄兩張手牌作為虛擬殺使用。繼承 NormalActiveKillBehavior，
 * override playerAction() 避免從手牌移除「殺卡」（因為丈八蛇矛用的是 VirtualKill，不在手牌中）。
 */
@Getter
public class ViperSpearKillBehavior extends NormalActiveKillBehavior {

    private final List<String> discardedCardIds;

    public ViperSpearKillBehavior(Game game, Player behaviorPlayer, List<String> reactionPlayers,
                                   Player currentReactionPlayer, HandCard virtualKillCard,
                                   List<String> discardedCardIds) {
        super(game, behaviorPlayer, reactionPlayers, currentReactionPlayer,
                VirtualKill.VIRTUAL_CARD_ID, PlayType.ACTIVE.getPlayType(), virtualKillCard);
        this.discardedCardIds = discardedCardIds;
    }

    @Override
    public List<DomainEvent> playerAction() {
        // 不呼叫父類的 playerPlayCard（因為虛擬殺不在手牌中）
        // 棄牌已在 Game.playerUseViperSpearKill() 處理
        String targetPlayerId = reactionPlayers.get(0);
        Player targetPlayer = game.getPlayer(targetPlayerId);

        // 更新 activePlayer 為目標玩家並設定 currentCard（等待出閃/發動防具效果）
        game.updateRoundInformation(targetPlayer, card);

        List<DomainEvent> events = new ArrayList<>();
        events.add(new ViperSpearKillTriggerEvent(behaviorPlayer.getId(), targetPlayerId, discardedCardIds));

        // 與普通殺同一條流程：流離 → 鐵騎 → 防具／閃（含護駕）
        // （原本直接問防具或閃，大喬的流離、馬超的鐵騎、曹操的護駕都被跳過）
        emitTieQiThenArmorOrDodge(events, targetPlayer);

        events.add(game.getGameStatusEvent("丈八蛇矛出殺"));
        return events;
    }
}
