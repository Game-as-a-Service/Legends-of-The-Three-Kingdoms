package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.scrollcard.Ward;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.round.Round;
import com.gaas.threeKingdoms.round.Stage;
import com.gaas.threeKingdoms.utils.MutableTriple;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import static com.gaas.threeKingdoms.handcard.PlayCard.isActive;
import static com.gaas.threeKingdoms.handcard.PlayCard.isSkip;

public class WardBehavior extends Behavior {

    public static final String WARD_TRIGGER_PLAYER_ID = "WARD_TRIGGER_PLAYER_ID";

    public WardBehavior(Game game, Player behaviorPlayer, List<String> reactionPlayers, Player currentReactionPlayer, String cardId, String playType, HandCard card, boolean isTargetPlayerNeedToResponse) {
        super(game, behaviorPlayer, reactionPlayers, currentReactionPlayer, cardId, playType, card, isTargetPlayerNeedToResponse, false, false);
    }

    @Override
    public List<DomainEvent> playerAction() {
        List<DomainEvent> events = new ArrayList<>();
        List<String> playerIds = new ArrayList<>();
        String wardTriggerPlayerId = (String) getParam(WARD_TRIGGER_PLAYER_ID);
        game.getPlayers().forEach(player -> {
                    if (player.getHand().getCards().stream().anyMatch(card -> card instanceof Ward) && !player.getId().equals(wardTriggerPlayerId)) {
                        playerIds.add(player.getId());
                    }
                }
        );
        events.add(new WaitForWardEvent(new HashSet<>(playerIds), wardTriggerPlayerId, cardId));
        return events;
    }

    @Override
    protected List<DomainEvent> doResponseToPlayerAction(String playerId, String targetPlayerId, String cardId, String playType) {
        List<DomainEvent> domainEvents = new ArrayList<>();
        Player player = game.getPlayer(playerId);
        Round currentRound = game.getCurrentRound();

        // Active
        // 出無懈可擊反制 -> 如果有人有無懈可擊 -> new 出一個新的 WardBehavior
        //              -> 沒有人有無懈可擊 -> 根據 stack 中 WardBehavior 的數量：
        //                                  奇數：清掉所有的 WardBehavior ，並且不發動任何 behavior 效果。
        //                                  偶數：清掉所有的 WardBehavior ，發動 stack 中除了 WardBehavior 外第一個遇到的 behavior 效果，發動這個效果可以另外開一隻 doBehaviorAction 的 api。
        if (isActive(playType)) {
            playerPlayCardNotUpdateActivePlayer(player, cardId);

            PlayCardEvent playCardEvent = new PlayCardEvent(
                    "出牌",
                    playerId,
                    "",
                    cardId,
                    playType);

            domainEvents.add(playCardEvent);

            currentRound.setStage(Stage.Wait_Accept_Ward_Effect);
            setIsOneRound(false);

            Behavior wardBehavior = new WardBehavior(
                    game,
                    player,
                    game.whichPlayersHaveWard().stream().map(Player::getId).collect(Collectors.toList()),
                    null,
                    cardId,
                    PlayType.INACTIVE.getPlayType(),
                    card,
                    true
            );
            wardBehavior.putParam(WARD_TRIGGER_PLAYER_ID, playerId);
            game.updateTopBehavior(wardBehavior);

            if (game.doesAnyPlayerHaveWard(playerId)) {
                domainEvents.addAll(wardBehavior.playerAction());
            } else {
                wardBehavior.setIsTargetPlayerNeedToResponse(false);
                domainEvents.addAll(doBehaviorAction());
            }
            return domainEvents;
        }

        // SKIP
        // 需要對無懈可擊的出牌的人數減少一位
        if (isSkip(playType)) {
            reactionPlayers.remove(playerId);
        }
        // 查看是否剩餘無懈可擊人數未回答
        // Y -> 繼續等待其他人作答，不做任何事
        // N -> 結束這個 WardBehavior，根據 stack 中 WardBehavior 的數量：
        //      奇數：清掉所有的 WardBehavior ，並且不發動任何 behavior 效果。
        //      偶數：清掉所有的 WardBehavior ，發動 stack 中除了 WardBehavior 外第一個遇到的 behavior 效果，發動這個效果可以另外開一隻 doBehaviorAction 的 api。
        if (reactionPlayers.isEmpty()) {
            domainEvents.addAll(doBehaviorAction());
        } else {
            domainEvents.add(new SkipWardEvent(playerId, this.cardId));
        }

        return domainEvents;
    }


    @Override
    public List<DomainEvent> doBehaviorAction() {
        List<DomainEvent> domainEvents = new ArrayList<>();
        Stack<Behavior> topBehaviors = game.getTopBehavior();
        int wardBehaviorSize = 0;
        MutableTriple<String, String /*wardPlayerId*/, String> message = null;

        WardEvent wardEvent = null;

        for (int i = topBehaviors.size() - 1; i >= 0; i--) {
            Behavior behavior = topBehaviors.get(i);
            if (!(behavior instanceof WardBehavior)) {
                if (wardEvent != null) {
                    domainEvents.add(new WardEvent(wardEvent.getPlayerId(), behavior.getCardId(), wardEvent.getWardCardId(), wardEvent.getMessage()
                            + String.format("%s 的 %s", behavior.getBehaviorPlayer().getGeneralName(), behavior.getCard().getName())));
                }
                break;
            }
            behavior.setIsOneRound(true);
            if (behavior.getBehaviorPlayer() != null) { // 反之是系統的 behavior，不是真的有人出牌
                wardBehaviorSize++;
                // B 對 D 出了 XXXX
                // D 對 B 出了無懈可擊 (latest)
                // Ｄ的無懈可擊抵銷了B的無懈可擊
                if (wardEvent == null) {
                    Player wardPlayer = behavior.getBehaviorPlayer();
                    String unDoneMessage = String.format("%s 的無懈可擊抵銷了 ", wardPlayer.getGeneralName());
                    wardEvent = new WardEvent(wardPlayer.getId(), null, behavior.getCardId(), unDoneMessage);
                } else if (StringUtils.isBlank(wardEvent.getCardId())) {
                    domainEvents.add(new WardEvent(wardEvent.getPlayerId(), behavior.getCardId(), wardEvent.getWardCardId(), wardEvent.getMessage()
                            + String.format("%s 的 %s", behavior.getBehaviorPlayer().getGeneralName(), behavior.getCard().getName())));
                    wardEvent = null;
                }

            }
        }

        game.removeCompletedBehaviors();
        Behavior firstNotWardBehavior = null;
        Player activePlayer = null;
        if (topBehaviors.peek().isOneRound()) {
            firstNotWardBehavior = topBehaviors.pop();
            activePlayer = game.getCurrentRoundPlayer();
            System.out.println("activePlayer 6 "+activePlayer);
        } else {
            firstNotWardBehavior = topBehaviors.peek();
            if (firstNotWardBehavior.isTargetPlayerNeedToResponse() && !firstNotWardBehavior.isNeed2ndApiToUseEffect()) {
                System.out.println("firstNotWardBehavior.isTargetPlayerNeedToResponse() && !firstNotWardBehavior.isNeed2ndApiToUseEffect()");
                activePlayer = firstNotWardBehavior.getCurrentReactionPlayer();
                System.out.println("activePlayer 5 "+activePlayer);
            } else {
                activePlayer = game.getCurrentRoundPlayer();
                System.out.println("activePlayer 4 "+activePlayer);
            }
        }
        // 奇數：清掉所有的 WardBehavior ，並且不發動任何 behavior 效果。
        // 偶數：清掉所有的 WardBehavior ，發動 stack 中除了 WardBehavior 外第一個遇到的 behavior 效果，發動這個效果可以另外開一隻 doBehaviorAction 的 api。
        if (wardBehaviorSize % 2 == 0) {
            domainEvents.addAll(firstNotWardBehavior.doBehaviorAction()); // 相信這裡會幫我們設定好 activePlayer
            activePlayer = game.getCurrentRound().getActivePlayer();
            System.out.println("activePlayer 3 "+activePlayer);
        } else {
            // 如果上面是可以多回合的 Behavior 那上面就會走 peek 路線，這時候還存在 TopBehavior ，但當 WardBehavior 為奇數代表已經被抵銷，就該移除
            if (game.getTopBehavior().peek().equals(firstNotWardBehavior)) {
                game.removeTopBehavior();
            }
            activePlayer = game.getCurrentRoundPlayer();
            System.out.println("activePlayer 2 "+activePlayer);
        }
        System.out.println("wardBehaviorSize "+wardBehaviorSize);

        game.getCurrentRound().setStage(Stage.Normal);
        System.out.println("activePlayer 1 "+activePlayer);
        game.getCurrentRound().setActivePlayer(activePlayer);
        game.removeCompletedBehaviors();

        return domainEvents;
    }
}