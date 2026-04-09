package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.AskDodgeEvent;
import com.gaas.threeKingdoms.events.AskPlayEquipmentEffectEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.ViperSpearKillTriggerEvent;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.VirtualKill;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.round.Round;
import com.gaas.threeKingdoms.round.Stage;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * 丈八蛇矛虛擬殺 Behavior
 * 玩家棄兩張手牌作為虛擬殺使用。繼承 NormalActiveKillBehavior，
 * override playerAction() 避免從手牌移除「殺卡」（因為丈八蛇矛用的是 VirtualKill，不在手牌中）。
 *
 * TODO: Phase 2 - 支援被動出殺場景（被決鬥、南蠻入侵、借刀殺人要求出殺時可用丈八蛇矛回應）
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
        Round currentRound = game.getCurrentRound();

        // 更新 activePlayer 為目標玩家並設定 currentCard（等待出閃/發動防具效果）
        game.updateRoundInformation(targetPlayer, card);

        List<DomainEvent> events = new ArrayList<>();
        events.add(new ViperSpearKillTriggerEvent(behaviorPlayer.getId(), targetPlayerId, discardedCardIds));

        // 進入閃/防具流程
        if (isEquipmentHasSpecialEffect(targetPlayer)) {
            currentRound.setStage(Stage.Wait_Equipment_Effect);
            events.add(new AskPlayEquipmentEffectEvent(
                    targetPlayer.getId(),
                    targetPlayer.getEquipment().getArmor(),
                    List.of(targetPlayer.getId())
            ));
        } else {
            events.add(new AskDodgeEvent(targetPlayerId));
        }

        events.add(game.getGameStatusEvent("丈八蛇矛出殺"));
        return events;
    }
}
