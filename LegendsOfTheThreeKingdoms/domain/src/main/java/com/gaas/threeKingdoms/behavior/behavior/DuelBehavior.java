package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.AskKillEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.DuelEvent;
import com.gaas.threeKingdoms.events.PlayCardEvent;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.round.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.gaas.threeKingdoms.behavior.behavior.WardBehavior.WARD_TRIGGER_PLAYER_ID;
import static com.gaas.threeKingdoms.handcard.PlayCard.isKillCard;
import static com.gaas.threeKingdoms.handcard.PlayCard.isSkip;

public class DuelBehavior extends Behavior {

    public DuelBehavior(Game game, Player behaviorPlayer, List<String> reactionPlayers, Player currentReactionPlayer, String cardId, String playType, HandCard card) {
        super(game, behaviorPlayer, reactionPlayers, currentReactionPlayer, cardId, playType, card, true, false, false);
    }

    @Override
    public List<DomainEvent> playerAction() {
        List<DomainEvent> events = new ArrayList<>();
        String currentReactionPlayerId = currentReactionPlayer.getId();
        playerPlayCard(behaviorPlayer, currentReactionPlayer, cardId);

        events.add(new PlayCardEvent(
                "出牌",
                behaviorPlayer.getId(),
                currentReactionPlayerId,
                cardId,
                playType));

        if (game.doesAnyPlayerHaveWard(behaviorPlayer.getId())) {
            game.getCurrentRound().setStage(Stage.Wait_Accept_Ward_Effect);
            setIsOneRound(false);

            Behavior wardBehavior = new WardBehavior(
                    game,
                    null,
                    game.whichPlayersHaveWard().stream().map(Player::getId).collect(Collectors.toList()),
                    null,
                    cardId,
                    PlayType.INACTIVE.getPlayType(),
                    card,
                    true
            );
            wardBehavior.putParam(WARD_TRIGGER_PLAYER_ID, behaviorPlayer.getId());

            game.updateTopBehavior(wardBehavior);
            events.addAll(wardBehavior.playerAction());
        } else {
            events.addAll(doBehaviorAction());
        }

        events.add(game.getGameStatusEvent("發動決鬥"));

        return events;
    }

    @Override
    protected List<DomainEvent> doResponseToPlayerAction(String playerId, String targetPlayerId, String cardId, String playType) {
        // 當前操作的玩家 playerId 可能是 reactionPlayers index 0 => 第一個要出殺的人 或  reactionPlayers index 1 => 也等於 behaviorPlayer
        String otherPlayerId = reactionPlayers.stream().filter(id -> !id.equals(playerId)).findFirst().get();

        if (isSkip(playType)) {
            // 當前反應者出閃，那就是等於 reactionPlayers index (playerId) 受到傷害， reactionPlayers 另一個為攻擊者
            Player damagePlayer = game.getPlayer(playerId);
            int originalHp = damagePlayer.getHP();
            List<DomainEvent> damagedEvent = game.getDamagedEvent(playerId, otherPlayerId, cardId, card, playType, originalHp, damagePlayer, game.getCurrentRound(), Optional.of(this));
            damagedEvent.add(game.getGameStatusEvent("扣血"));
            return damagedEvent;
        } else if (isKillCard(cardId)) {
            List<DomainEvent> events = new ArrayList<>();
            playerPlayCardNotUpdateActivePlayer(game.getPlayer(playerId), cardId);

            // 當前反應者出完殺後，就需要換另外一個人出
            currentReactionPlayer = game.getPlayer(otherPlayerId);

            game.getCurrentRound().setActivePlayer(currentReactionPlayer);
            events.add(new PlayCardEvent("出牌", playerId, otherPlayerId, cardId, playType));

            String gameMessage = "";
            if (!currentReactionPlayer.getHand().hasTypeInHand(Kill.class)) {
                int originalHp = currentReactionPlayer.getHP();
                List<DomainEvent> damagedEvents = game.getDamagedEvent(
                        otherPlayerId,
                        playerId,
                        "",
                        card,
                        PlayType.SKIP.getPlayType(),
                        originalHp,
                        currentReactionPlayer,
                        game.getCurrentRound(),
                        Optional.of(this));
                events.addAll(damagedEvents);
                isTargetPlayerNeedToResponse = false;
                isOneRound = true;
                System.out.println("1 currentReactionPlayer "+currentReactionPlayer.getId());
                gameMessage = "扣血";
            } else {
                AskKillEvent askKillEvent = new AskKillEvent(currentReactionPlayer.getId());
                events.add(askKillEvent);
                gameMessage = playerId + "已出殺";
            }
            events.add(game.getGameStatusEvent(gameMessage));
            return events;
        } else {
            //TODO:怕有其他效果或殺的其他case
        }
        return null;
    }

    @Override
    public List<DomainEvent> doBehaviorAction() {
        List<DomainEvent> events = new ArrayList<>();
        String currentReactionPlayerId = currentReactionPlayer.getId();
        events.add(new DuelEvent(behaviorPlayer.getId(), currentReactionPlayerId, cardId));
        // 判斷 currentReactionPlayer 是否有殺
        if (!currentReactionPlayer.getHand().hasTypeInHand(Kill.class)) {
            int originalHp = currentReactionPlayer.getHP();
            List<DomainEvent> damagedEvents = game.getDamagedEvent(
                    currentReactionPlayerId,
                    behaviorPlayer.getId(),
                    "", // 代替 A 出 skip
                    card,
                    PlayType.SKIP.getPlayType(),  // 代替 A 出 skip
                    originalHp,
                    currentReactionPlayer,
                    game.getCurrentRound(),
                    Optional.of(this));
            events.addAll(damagedEvents);
            isOneRound = true;
            isTargetPlayerNeedToResponse = false;
            System.out.println("2 currentReactionPlayer "+currentReactionPlayer.getId());
        } else {
            System.out.println("2 AskKillEvent currentReactionPlayer"+currentReactionPlayer.getId());

            events.add(new AskKillEvent(currentReactionPlayerId));
        }

        return events;
    }
}