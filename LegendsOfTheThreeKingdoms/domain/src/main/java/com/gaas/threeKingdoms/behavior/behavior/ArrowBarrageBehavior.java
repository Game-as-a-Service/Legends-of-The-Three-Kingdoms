package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.AskDodgeEvent;
import com.gaas.threeKingdoms.events.AskPlayEquipmentEffectEvent;
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

import static com.gaas.threeKingdoms.behavior.behavior.NormalActiveKillBehavior.isEquipmentHasSpecialEffect;
import static com.gaas.threeKingdoms.behavior.behavior.WardBehavior.WARD_TARGET_PLAYER_IDS;
import static com.gaas.threeKingdoms.behavior.behavior.WardBehavior.WARD_TRIGGER_PLAYER_ID;
import static com.gaas.threeKingdoms.handcard.PlayCard.*;

public class ArrowBarrageBehavior extends Behavior {
    private boolean pollingStarted = false;

    public ArrowBarrageBehavior(Game game, Player player, List<String> reactivePlayers, Player currentReactionPlayer, String cardId, String playType, HandCard card) {
        super(game, player, reactivePlayers, currentReactionPlayer, cardId, playType, card, true, false, false);
    }

    public boolean isPollingStarted() {
        return pollingStarted;
    }

    public void setPollingStarted(boolean pollingStarted) {
        this.pollingStarted = pollingStarted;
    }

    @Override
    public List<DomainEvent> playerAction() {
        List<DomainEvent> events = new ArrayList<>();
        playerPlayCard(behaviorPlayer, currentReactionPlayer, cardId);

        events.add(new PlayCardEvent("出牌", behaviorPlayer.getId(), "", cardId, playType));

        if (game.doesAnyPlayerHaveWard(behaviorPlayer.getId())) {
            // Phase 1: Ward 可取消整個萬箭齊發
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

        events.add(game.getGameStatusEvent("發動萬箭齊發"));
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
            // Phase 2 Ward even → 問當前玩家出閃（需檢查裝備）
            Player targetPlayer = game.getPlayer(currentReactionPlayer.getId());
            if (isEquipmentHasSpecialEffect(targetPlayer)) {
                game.getCurrentRound().setStage(Stage.Wait_Equipment_Effect);
                events.add(new AskPlayEquipmentEffectEvent(targetPlayer.getId(), targetPlayer.getEquipment().getArmor(), List.of(targetPlayer.getId())));
            } else {
                events.add(new AskDodgeEvent(currentReactionPlayer.getId()));
            }
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
            Player targetPlayer = game.getPlayer(currentReactionPlayer.getId());
            if (isEquipmentHasSpecialEffect(targetPlayer)) {
                game.getCurrentRound().setStage(Stage.Wait_Equipment_Effect);
                events.add(new AskPlayEquipmentEffectEvent(targetPlayer.getId(), targetPlayer.getEquipment().getArmor(), List.of(targetPlayer.getId())));
            } else {
                events.add(new AskDodgeEvent(currentReactionPlayer.getId()));
            }
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
                boolean isLastPlayer = reactionPlayers.get(reactionPlayers.size() - 1).equals(playerId);
                if (isLastPlayer) {
                    isOneRound = true;
                    game.getCurrentRound().setActivePlayer(game.getCurrentRoundPlayer());
                } else {
                    isOneRound = false;
                    game.getCurrentRound().setActivePlayer(currentReactionPlayer);
                }
                events.add(game.getGameStatusEvent("扣血但還活著"));
                if (!isLastPlayer) {
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
        } else if (isDodgeCard(cardId)) {
            playerPlayCardNotUpdateActivePlayer(game.getPlayer(playerId), cardId);
            List<DomainEvent> events = new ArrayList<>();
            currentReactionPlayer = game.getNextPlayer(currentReactionPlayer);
            boolean isLastPlayer = reactionPlayers.get(reactionPlayers.size() - 1).equals(playerId);
            if (isLastPlayer) {
                isOneRound = true;
                game.getCurrentRound().setActivePlayer(game.getCurrentRoundPlayer());
            } else {
                game.getCurrentRound().setActivePlayer(currentReactionPlayer);
            }
            events.add(game.getGameStatusEvent(playerId + "出閃"));
            events.add(new PlayCardEvent("出牌", playerId, targetPlayerId, cardId, playType));
            if (!isLastPlayer) {
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
