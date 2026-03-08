package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.AskKillEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.PlayCardEvent;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.round.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.gaas.threeKingdoms.behavior.behavior.WardBehavior.WARD_TARGET_PLAYER_IDS;
import static com.gaas.threeKingdoms.behavior.behavior.WardBehavior.WARD_TRIGGER_PLAYER_ID;
import static com.gaas.threeKingdoms.handcard.PlayCard.isKillCard;
import static com.gaas.threeKingdoms.handcard.PlayCard.isSkip;

public class BarbarianInvasionBehavior extends Behavior {
    private boolean pollingStarted = false;

    public BarbarianInvasionBehavior(Game game, Player behaviorPlayer, List<String> reactionPlayers, Player currentReactionPlayer, String cardId, String playType, HandCard card) {
        super(game, behaviorPlayer, reactionPlayers, currentReactionPlayer, cardId, playType, card, true, false, false);
    }

    @Override
    public List<DomainEvent> playerAction() {
        List<DomainEvent> events = new ArrayList<>();
        String currentReactionPlayerId = currentReactionPlayer.getId();
        playerPlayCard(behaviorPlayer, currentReactionPlayer, cardId);

        events.add(new PlayCardEvent("出牌", behaviorPlayer.getId(), "", cardId, playType));

        if (game.doesAnyPlayerHaveWard(behaviorPlayer.getId())) {
            // Phase 1: Ward 可取消整個南蠻入侵
            game.getCurrentRound().setStage(Stage.Wait_Accept_Ward_Effect);
            setIsOneRound(false);

            Behavior wardBehavior = new WardBehavior(
                    game, null,
                    game.whichPlayersHaveWard(behaviorPlayer.getId()).stream()
                            .map(Player::getId).collect(Collectors.toList()),
                    null, cardId, PlayType.INACTIVE.getPlayType(), card, true
            );
            wardBehavior.putParam(WARD_TRIGGER_PLAYER_ID, behaviorPlayer.getId());
            wardBehavior.putParam(WARD_TARGET_PLAYER_IDS, new ArrayList<>(reactionPlayers));
            game.updateTopBehavior(wardBehavior);

            events.addAll(wardBehavior.playerAction());
        } else {
            // 沒人有 Ward → 直接開始輪詢
            pollingStarted = true;
            events.addAll(askNextPlayerOrWard());
        }

        events.add(game.getGameStatusEvent("發動南蠻入侵"));
        return events;
    }

    @Override
    public List<DomainEvent> doBehaviorAction() {
        List<DomainEvent> events = new ArrayList<>();
        if (!pollingStarted) {
            // Phase 1 Ward even → 開始輪詢第一個玩家
            pollingStarted = true;
            events.addAll(askNextPlayerOrWard());
        } else {
            // Phase 2 Ward even → 直接問當前玩家出殺
            events.add(new AskKillEvent(currentReactionPlayer.getId()));
            game.getCurrentRound().setActivePlayer(currentReactionPlayer);
        }
        return events;
    }

    @Override
    public List<DomainEvent> doWardCancelledAction() {
        if (!pollingStarted) {
            return null; // Phase 1: 標準移除
        }
        // Phase 2: 跳過這個玩家，繼續輪詢
        List<DomainEvent> events = new ArrayList<>();
        String currentId = currentReactionPlayer.getId();
        boolean isLastPlayer = reactionPlayers.get(reactionPlayers.size() - 1).equals(currentId);

        if (isLastPlayer) {
            isOneRound = true;
            game.getCurrentRound().setStage(Stage.Normal);
            game.getCurrentRound().setActivePlayer(game.getCurrentRoundPlayer());
            return events;
        }

        currentReactionPlayer = game.getNextPlayer(currentReactionPlayer);
        events.addAll(askNextPlayerOrWard());
        return events;
    }

    private List<DomainEvent> askNextPlayerOrWard() {
        List<DomainEvent> events = new ArrayList<>();
        if (game.doesAnyPlayerHaveWard(behaviorPlayer.getId())) {
            game.getCurrentRound().setStage(Stage.Wait_Accept_Ward_Effect);
            setIsOneRound(false);

            Behavior wardBehavior = new WardBehavior(
                    game, null,
                    game.whichPlayersHaveWard(behaviorPlayer.getId()).stream()
                            .map(Player::getId).collect(Collectors.toList()),
                    null, cardId, PlayType.INACTIVE.getPlayType(), card, true
            );
            wardBehavior.putParam(WARD_TRIGGER_PLAYER_ID, behaviorPlayer.getId());
            wardBehavior.putParam(WARD_TARGET_PLAYER_IDS, List.of(currentReactionPlayer.getId()));
            game.updateTopBehavior(wardBehavior);

            events.addAll(wardBehavior.playerAction());
        } else {
            game.getCurrentRound().setStage(Stage.Normal);
            events.add(new AskKillEvent(currentReactionPlayer.getId()));
            game.getCurrentRound().setActivePlayer(currentReactionPlayer);
        }
        return events;
    }

    @Override
    protected List<DomainEvent> doResponseToPlayerAction(String playerId, String targetPlayerId, String cardId, String playType) {

        if (isSkip(playType)) {
            int originalHp = currentReactionPlayer.getHP();
            List<DomainEvent> damagedEvent = game.getDamagedEvent(playerId, targetPlayerId, cardId, card, playType, originalHp, currentReactionPlayer, game.getCurrentRound(), Optional.of(this));
            // Remove the current player to next player
            currentReactionPlayer = game.getNextPlayer(currentReactionPlayer);

            List<DomainEvent> events = new ArrayList<>(damagedEvent);
            if (!game.getGamePhase().getPhaseName().equals("GeneralDying")) { // 如果受到傷害且沒死亡
                events.add(game.getGameStatusEvent("扣血但還活著"));
                isOneRound = false;

                // 最後一個人
                if (reactionPlayers.get(reactionPlayers.size() - 1).equals(playerId)) {
                    isOneRound = true;
                    game.getCurrentRound().setActivePlayer(game.getCurrentRoundPlayer());
                } else {
                    events.addAll(askNextPlayerOrWard());
                }
            } else {
                events.add(game.getGameStatusEvent("扣血已瀕臨死亡"));

                // 最後一個人
                if (reactionPlayers.get(reactionPlayers.size() - 1).equals(playerId)) {
                    isOneRound = true;
                }
            }

            return events;
        } else if (isKillCard(cardId)) {
            playerPlayCardNotUpdateActivePlayer(game.getPlayer(playerId), cardId);
            List<DomainEvent> events = new ArrayList<>();
            currentReactionPlayer = game.getNextPlayer(currentReactionPlayer);
            events.add(game.getGameStatusEvent(playerId + "出殺"));
            events.add(new PlayCardEvent("出牌", playerId, targetPlayerId, cardId, playType));
            // 最後一個人，結束此behavior
            if (reactionPlayers.get(reactionPlayers.size() - 1).equals(playerId)) {
                isOneRound = true;
                game.getCurrentRound().setActivePlayer(game.getCurrentRoundPlayer());
            } else {
                events.addAll(askNextPlayerOrWard());
            }
            return events;
        } else {
            //TODO:怕有其他效果或殺的其他case
        }
        return null;
    }

    public boolean isInReactionPlayers(String playerId) {
        return reactionPlayers.contains(playerId);
    }

}
