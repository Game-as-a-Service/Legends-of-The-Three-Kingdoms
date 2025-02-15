package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.AskDodgeEvent;
import com.gaas.threeKingdoms.events.AskPlayEquipmentEffectEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.PlayCardEvent;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.equipmentcard.EquipmentCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.QilinBowCard;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.round.Round;
import com.gaas.threeKingdoms.round.Stage;

import java.util.ArrayList;
import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.isDodgeCard;
import static com.gaas.threeKingdoms.handcard.PlayCard.isSkip;


public class NormalActiveKillBehavior extends Behavior {

    public NormalActiveKillBehavior(Game game, Player behaviorPlayer, List<String> reactionPlayers, Player currentReactionPlayer, String cardId, String playType, HandCard card) {
        super(game, behaviorPlayer, reactionPlayers, currentReactionPlayer, cardId, playType, card, true, false);
    }

    @Override
    public List<DomainEvent> playerAction() {
        String targetPlayerId = reactionPlayers.get(0);
        Player targetPlayer = game.getPlayer(targetPlayerId);

        playerPlayCard(behaviorPlayer, game.getPlayer(targetPlayerId), cardId);

        Round currentRound = game.getCurrentRound();

        List<DomainEvent> events = new ArrayList<>();
        events.add(new PlayCardEvent("出牌", behaviorPlayer.getId(), targetPlayerId, cardId, playType));
        if (isEquipmentHasSpecialEffect(targetPlayer)) {
            currentRound.setStage(Stage.Wait_Equipment_Effect);
            DomainEvent askPlayEquipmentEffectEvent = new AskPlayEquipmentEffectEvent(targetPlayer.getId(), targetPlayer.getEquipment().getArmor(), List.of(targetPlayer.getId()));
            events.add(askPlayEquipmentEffectEvent);
        } else {
            events.add(new AskDodgeEvent(targetPlayerId));
        }
        events.add(game.getGameStatusEvent("出牌"));
        return events;
    }

    @Override
    public List<DomainEvent> doResponseToPlayerAction(String playerId, String targetPlayerId, String cardId, String playType) {
        Player damagedPlayer = game.getPlayer(playerId);
        int originalHp = damagedPlayer.getHP();

        if (isSkip(playType)) {
            Round currentRound = game.getCurrentRound();
            // 麒麟弓要先發動效果，待麒麟弓效果發動後再扣血
            if (isAskPlayerUseQilinBow(behaviorPlayer, damagedPlayer)) {
                isOneRound = false;
                currentRound.setActivePlayer(behaviorPlayer);
                currentRound.setStage(Stage.Wait_Equipment_Effect);
                EquipmentCard equipmentCard = behaviorPlayer.getEquipmentWeaponCard();
                AskPlayEquipmentEffectEvent askPlayEquipmentEffectEvent = new AskPlayEquipmentEffectEvent(behaviorPlayer.getId(), equipmentCard, List.of(playerId));
                PlayCardEvent playCardEvent = new PlayCardEvent("不出牌", playerId, targetPlayerId, cardId, playType);
                return List.of(playCardEvent, askPlayEquipmentEffectEvent, game.getGameStatusEvent("出牌"));
            }

            List<DomainEvent> events = game.getDamagedEvent(playerId, targetPlayerId, cardId, card, playType, originalHp, damagedPlayer, currentRound, this);
            String message = game.getGamePhase().getPhaseName().equals("GeneralDying") ? "扣血已瀕臨死亡" : "扣血但還活著";
            events.add(game.getGameStatusEvent(message));
            isOneRound = true;
            return events;
        } else if (isDodgeCard(cardId)) {
            Round currentRound = game.getCurrentRound();
            currentRound.setActivePlayer(currentRound.getCurrentRoundPlayer());
            damagedPlayer.playCard(cardId);
            PlayCardEvent playCardEvent = new PlayCardEvent("出牌", playerId, targetPlayerId, cardId, playType);
            isOneRound = true;
            return List.of(playCardEvent, game.getGameStatusEvent("出牌"));
        } else if (isQilinBowSuccess(playType)) {
            Round currentRound = game.getCurrentRound();
            List<DomainEvent> events = game.getDamagedEvent(playerId, targetPlayerId, cardId, card, playType, originalHp, damagedPlayer, currentRound, this);
            //playerDyingEvent
            String message = game.getGamePhase().getPhaseName().equals("GeneralDying") ? "扣血已瀕臨死亡" : "扣血但還活著";
            events.add(game.getGameStatusEvent(message));
            isOneRound = true;
            return events;
        } else {
            //TODO:怕有其他效果或殺的其他case
            return new ArrayList<>();
        }
    }

    private boolean isQilinBowSuccess(String playType) {
        return  PlayType.QilinBow.getPlayType().equals(playType);
    }

    private boolean isAskPlayerUseQilinBow(Player attackPlayer, Player damagedPlayer) {
        return attackPlayer.getEquipmentWeaponCard() instanceof QilinBowCard && damagedPlayer.hasMountsCard();
    }

    private boolean isEquipmentHasSpecialEffect(Player targetPlayer) {
        return targetPlayer.getEquipment().hasSpecialEffect();
    }

}
