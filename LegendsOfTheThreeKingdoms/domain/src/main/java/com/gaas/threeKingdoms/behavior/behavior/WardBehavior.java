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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import static com.gaas.threeKingdoms.handcard.PlayCard.isActive;
import static com.gaas.threeKingdoms.handcard.PlayCard.isSkip;

public class WardBehavior extends Behavior {
    public WardBehavior(Game game, Player behaviorPlayer, List<String> reactionPlayers, Player currentReactionPlayer, String cardId, String playType, HandCard card, boolean isTargetPlayerNeedToResponse) {
        super(game, behaviorPlayer, reactionPlayers, currentReactionPlayer, cardId, playType, card, isTargetPlayerNeedToResponse, false);
    }

    @Override
    public List<DomainEvent> playerAction() {
        List<DomainEvent> events = new ArrayList<>();
        List<String> playerIds = new ArrayList<>();
        game.getPlayers().forEach(player -> {
                    if (player.getHand().getCards().stream().anyMatch(card -> card instanceof Ward)) {
                        playerIds.add(player.getId());
                    }
                }
        );
        events.add(new WaitForWardEvent(new HashSet<>(playerIds)));
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

            WardEvent wardEvent = new WardEvent(playerId, this.cardId, cardId);

            domainEvents.add(playCardEvent);
            domainEvents.add(wardEvent);

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
            game.updateTopBehavior(wardBehavior);

            if (game.doesAnyPlayerHaveWard()) {
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
            domainEvents.add(new SkipWardEvent(playerId, cardId));
        }

        return domainEvents;
    }


    @Override
    public List<DomainEvent> doBehaviorAction() {
        List<DomainEvent> domainEvents = new ArrayList<>();
        Stack<Behavior> topBehaviors = game.getTopBehavior();
        int wardBehaviorSize = 0;
        for (int i = topBehaviors.size() - 1; i >= 0; i--) {
            Behavior behavior = topBehaviors.get(i);
            if (!(behavior instanceof WardBehavior)) {
                break;
            }
            behavior.setIsOneRound(true);
            if (behavior.getBehaviorPlayer() != null) {
                wardBehaviorSize++;
            }
        }
        game.removeCompletedBehaviors();
        Behavior firstNotWardBehavior = topBehaviors.pop();
        if (wardBehaviorSize % 2 == 0) {
            domainEvents.addAll(firstNotWardBehavior.doBehaviorAction());
        }
        game.getCurrentRound().setStage(Stage.Normal);
        return domainEvents;
    }
}