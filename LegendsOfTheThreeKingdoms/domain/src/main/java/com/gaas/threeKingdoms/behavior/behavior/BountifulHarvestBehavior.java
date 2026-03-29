package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.BountifulHarvestChooseCardEvent;
import com.gaas.threeKingdoms.events.BountifulHarvestEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.PlayCardEvent;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.round.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.gaas.threeKingdoms.behavior.behavior.WardBehavior.WARD_TARGET_PLAYER_IDS;
import static com.gaas.threeKingdoms.behavior.behavior.WardBehavior.WARD_TRIGGER_PLAYER_ID;

public class BountifulHarvestBehavior extends Behavior {

    public static final String BOUNTIFUL_HARVEST_CARDS = "BOUNTIFUL_HARVEST_CARDS";
    private boolean pollingStarted = false;

    public BountifulHarvestBehavior(Game game, Player player, List<String> reactivePlayers, Player currentReactionPlayer, String cardId, String playType, HandCard card) {
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

        events.add(new PlayCardEvent(
                "出牌",
                behaviorPlayer.getId(),
                "",
                cardId,
                playType));

        if (game.doesAnyPlayerHaveWard(behaviorPlayer.getId())) {
            // Phase 1: Ward 可取消整個五穀豐登
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

        events.add(game.getGameStatusEvent("發動五穀豐登"));
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
            // Phase 2 Ward even → 直接讓當前玩家選牌
            List<String> drawCardIds = (List<String>) params.get(BOUNTIFUL_HARVEST_CARDS);
            events.add(new BountifulHarvestEvent("輪到 " + currentReactionPlayer.getGeneralName() + " 選牌", currentReactionPlayer.getId(), drawCardIds));
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
            List<String> drawCardIds = (List<String>) params.get(BOUNTIFUL_HARVEST_CARDS);
            events.add(new BountifulHarvestEvent("輪到 " + currentReactionPlayer.getGeneralName() + " 選牌", currentReactionPlayer.getId(), drawCardIds));
            game.getCurrentRound().setActivePlayer(currentReactionPlayer);
        }
        return events;
    }

    @Override
    protected List<DomainEvent> doResponseToPlayerAction(String playerId, String targetPlayerId, String cardId, String playType) {
        List<String> drawCardIds = (List<String>) params.get(BountifulHarvestBehavior.BOUNTIFUL_HARVEST_CARDS);
        drawCardIds.remove(cardId);
        currentReactionPlayer.getHand().addCardToHand(PlayCard.findById(cardId));
        List<DomainEvent> events = new ArrayList<>();
        events.add(new BountifulHarvestChooseCardEvent(currentReactionPlayer, cardId));
        currentReactionPlayer = game.getNextPlayer(currentReactionPlayer);
        game.getCurrentRound().setActivePlayer(currentReactionPlayer);

        if (isLastReactionPlayers(playerId)) {
            isOneRound = true;
        } else {
            events.addAll(askNextPlayerOrWard());
        }
        events.add(game.getGameStatusEvent("五穀豐登選牌"));
        return events;
    }

    public boolean isLastReactionPlayers(String playerId) {
        return reactionPlayers.get(reactionPlayers.size() - 1).equals(playerId);
    }

}
