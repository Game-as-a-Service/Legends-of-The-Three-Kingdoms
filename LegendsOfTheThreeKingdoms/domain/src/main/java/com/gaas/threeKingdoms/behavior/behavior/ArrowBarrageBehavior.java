package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.AskDodgeEvent;
import com.gaas.threeKingdoms.events.AskPlayEquipmentEffectEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.PlayCardEvent;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.round.Round;
import com.gaas.threeKingdoms.round.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.gaas.threeKingdoms.behavior.behavior.NormalActiveKillBehavior.isEquipmentHasSpecialEffect;
import static com.gaas.threeKingdoms.handcard.PlayCard.*;

public class ArrowBarrageBehavior extends Behavior {
    public ArrowBarrageBehavior(Game game, Player player, List<String> reactivePlayers, Player currentReactionPlayer, String cardId, String playType, HandCard card) {
        super(game, player, reactivePlayers, currentReactionPlayer, cardId, playType, card, true, false, false);
    }

    @Override
    public boolean isOneRoundDefault() {
        return false;
    }

    @Override
    public List<DomainEvent> playerAction() {
        List<DomainEvent> events = new ArrayList<>();
        String currentReactionPlayerId = currentReactionPlayer.getId();
        Player targetPlayer = game.getPlayer(currentReactionPlayerId);

        playerPlayCard(behaviorPlayer, currentReactionPlayer, cardId);

        Round currentRound = game.getCurrentRound();

        events.add(new PlayCardEvent(
                "出牌",
                behaviorPlayer.getId(),
                "",
                cardId,
                playType));
        if (isEquipmentHasSpecialEffect(targetPlayer)) {
            currentRound.setStage(Stage.Wait_Equipment_Effect);
            DomainEvent askPlayEquipmentEffectEvent = new AskPlayEquipmentEffectEvent(targetPlayer.getId(), targetPlayer.getEquipment().getArmor(), List.of(targetPlayer.getId()));
            events.add(askPlayEquipmentEffectEvent);
        } else {
            events.add(new AskDodgeEvent(currentReactionPlayerId));
        }
        events.add(game.getGameStatusEvent("發動萬箭齊發"));

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
                AskDodgeEvent askDodgeEvent = new AskDodgeEvent(currentReactionPlayer.getId());
                events.add(askDodgeEvent);
                game.getCurrentRound().setActivePlayer(currentReactionPlayer);
                events.add(game.getGameStatusEvent("扣血但還活著"));
                isOneRound = false;

                // 最後一個人
                if (reactionPlayers.get(reactionPlayers.size() - 1).equals(playerId)) {
                    isOneRound = true;
                    events.remove(askDodgeEvent);
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
            game.getCurrentRound().setActivePlayer(currentReactionPlayer);
            events.add(game.getGameStatusEvent(playerId + "出閃"));
            AskDodgeEvent askDodgeEvent = new AskDodgeEvent(currentReactionPlayer.getId());
            events.add(new PlayCardEvent("出牌", playerId, targetPlayerId, cardId, playType));
            events.add(askDodgeEvent);
            // 最後一個人，結束此behavior，askDodgeEvent不再出現
            if (reactionPlayers.get(reactionPlayers.size() - 1).equals(playerId)) {
                isOneRound = true;
                events.remove(askDodgeEvent);
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
