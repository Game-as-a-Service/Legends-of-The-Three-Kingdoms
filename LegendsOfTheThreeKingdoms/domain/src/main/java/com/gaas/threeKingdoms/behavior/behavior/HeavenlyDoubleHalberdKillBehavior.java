package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.AskDodgeEvent;
import com.gaas.threeKingdoms.events.AskPlayEquipmentEffectEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.HeavenlyDoubleHalberdKillTriggerEvent;
import com.gaas.threeKingdoms.events.PlayCardEvent;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.round.Round;
import com.gaas.threeKingdoms.round.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.gaas.threeKingdoms.handcard.PlayCard.isDodgeCard;
import static com.gaas.threeKingdoms.handcard.PlayCard.isSkip;

/**
 * 方天畫戟多目標殺 Behavior。
 *
 * 繼承 NormalActiveKillBehavior 以複用 isEquipmentHasSpecialEffect 等 helper，但完全 override
 * playerAction() 與 doResponseToPlayerAction() 以實作 sequential polling（mirror ArrowBarrage）。
 *
 * 核心設計：
 *  - reactionPlayers = 使用者指定的完整目標列表（[primary, additional1, additional2?]）
 *  - currentReactionPlayer 逐一推進（依 reactionPlayers 順序，不是座位順序）
 *  - 每個目標獨立結算閃/防具/扣血/瀕死
 *  - 因為武器槽唯一，方天畫戟路徑不需要處理 GDCB/SPA/QilinBow/YinYangSwords/BlackPommel 交互
 *
 * 瀕死處理：若中間目標在扣血後進入瀕死流程（getDamagedEvent 會 push DyingAskPeachBehavior），
 * 此 behavior 的 isOneRound 會維持 false，等瀕死結束後由 DyingAskPeachBehavior 的
 * addAskDodgeEventIfCurrentBehaviorIsHeavenlyDoubleHalberdKillBehavior hook 恢復下一個 target
 * 的詢問（mirror ArrowBarrage 的既有機制）。
 */
public class HeavenlyDoubleHalberdKillBehavior extends NormalActiveKillBehavior {

    public HeavenlyDoubleHalberdKillBehavior(Game game,
                                             Player behaviorPlayer,
                                             List<String> reactionPlayers,
                                             Player currentReactionPlayer,
                                             String cardId,
                                             HandCard killCard) {
        super(game, behaviorPlayer, reactionPlayers, currentReactionPlayer,
                cardId, PlayType.ACTIVE.getPlayType(), killCard);
    }

    @Override
    public List<DomainEvent> playerAction() {
        // 殺已在 Game.playerUseHeavenlyDoubleHalberdKill() 從手牌移至墓地，這裡不再呼叫 playerPlayCard
        List<DomainEvent> events = new ArrayList<>();

        // 廣播多目標殺 — targetPlayerId 欄位留空，以 trigger event 的 targetPlayerIds 為準
        events.add(new PlayCardEvent("出牌", behaviorPlayer.getId(), "", cardId, playType));
        events.add(new HeavenlyDoubleHalberdKillTriggerEvent(
                behaviorPlayer.getId(), cardId, new ArrayList<>(reactionPlayers)));

        // 問第一個目標（currentReactionPlayer）出閃或發動防具
        askCurrentTargetDodgeOrEquipmentEffect(events);
        events.add(game.getGameStatusEvent("方天畫戟發動"));
        return events;
    }

    @Override
    public List<DomainEvent> doResponseToPlayerAction(String playerId, String targetPlayerId, String cardId, String playType) {
        Round currentRound = game.getCurrentRound();

        if (isSkip(playType)) {
            int originalHp = currentReactionPlayer.getHP();
            List<DomainEvent> events = new ArrayList<>(
                    game.getDamagedEvent(playerId, targetPlayerId, cardId, card, playType,
                            originalHp, currentReactionPlayer, currentRound, Optional.of(this)));

            boolean isLast = isLastReactionPlayer(playerId);

            if (!game.getGamePhase().getPhaseName().equals("GeneralDying")) {
                // 扣血但沒死：繼續詢問下一個目標
                if (isLast) {
                    isOneRound = true;
                    currentRound.setActivePlayer(currentRound.getCurrentRoundPlayer());
                } else {
                    isOneRound = false;
                    advanceToNextTarget();
                    askCurrentTargetDodgeOrEquipmentEffect(events);
                }
                events.add(game.getGameStatusEvent("扣血但還活著"));
            } else {
                // 進入瀕死流程：下一個目標的詢問交由 DyingAskPeachBehavior 的 hook 處理
                events.add(game.getGameStatusEvent("扣血已瀕臨死亡"));
                if (isLast) {
                    isOneRound = true;
                } else {
                    // 先把 currentReactionPlayer 推進到下一位，DyingAskPeach 結束後會讀取此位置
                    advanceToNextTarget();
                }
            }

            return events;
        } else if (isDodgeCard(cardId)) {
            // 消耗閃到墓地
            playerPlayCardNotUpdateActivePlayer(game.getPlayer(playerId), cardId);

            List<DomainEvent> events = new ArrayList<>();
            events.add(new PlayCardEvent("出牌", playerId, targetPlayerId, cardId, playType));

            boolean isLast = isLastReactionPlayer(playerId);
            if (isLast) {
                isOneRound = true;
                currentRound.setActivePlayer(currentRound.getCurrentRoundPlayer());
            } else {
                isOneRound = false;
                advanceToNextTarget();
                askCurrentTargetDodgeOrEquipmentEffect(events);
            }
            events.add(game.getGameStatusEvent(playerId + " 出閃"));
            return events;
        } else {
            //TODO: 其他 case（Phase 2）
            return new ArrayList<>();
        }
    }

    /**
     * 八卦陣 handler 會呼叫此 method 取代 isOneRound 的判斷邏輯。
     * 方天畫戟為多目標輪詢，八卦陣在中間目標成功並不代表整個 behavior 結束。
     */
    @Override
    public boolean judgeWhetherRemoveTopBehavior() {
        return false;
    }

    /**
     * 判斷某玩家是否為 reactionPlayers 列表的最後一位（注意：依列表順序，非座位順序）。
     */
    public boolean isLastReactionPlayer(String playerId) {
        return reactionPlayers.get(reactionPlayers.size() - 1).equals(playerId);
    }

    /**
     * 將 currentReactionPlayer 推進到 reactionPlayers 列表中的下一位。
     * 注意：使用列表順序 (index + 1)，不是座位順序。
     */
    public void advanceToNextTarget() {
        int currentIndex = reactionPlayers.indexOf(currentReactionPlayer.getId());
        if (currentIndex < 0 || currentIndex + 1 >= reactionPlayers.size()) {
            return;
        }
        String nextId = reactionPlayers.get(currentIndex + 1);
        currentReactionPlayer = game.getPlayer(nextId);
    }

    /**
     * 詢問當前 currentReactionPlayer 出閃（或發動防具效果）。
     * 將 activePlayer 設為該目標。
     */
    public void askCurrentTargetDodgeOrEquipmentEffect(List<DomainEvent> events) {
        Round currentRound = game.getCurrentRound();
        Player targetPlayer = currentReactionPlayer;
        currentRound.setActivePlayer(targetPlayer);

        if (isEquipmentHasSpecialEffect(targetPlayer)) {
            currentRound.setStage(Stage.Wait_Equipment_Effect);
            events.add(new AskPlayEquipmentEffectEvent(
                    targetPlayer.getId(),
                    targetPlayer.getEquipment().getArmor(),
                    List.of(targetPlayer.getId())));
        } else {
            currentRound.setStage(Stage.Normal);
            events.add(new AskDodgeEvent(targetPlayer.getId()));
        }
    }
}
