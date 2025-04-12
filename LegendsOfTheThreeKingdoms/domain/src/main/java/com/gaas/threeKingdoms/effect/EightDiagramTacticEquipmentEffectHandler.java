package com.gaas.threeKingdoms.effect;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.behavior.behavior.ArrowBarrageBehavior;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.handcard.EquipmentPlayType;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.ArmorCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.EightDiagramTactic;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.round.Round;
import com.gaas.threeKingdoms.round.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EightDiagramTacticEquipmentEffectHandler extends EquipmentEffectHandler {

    public EightDiagramTacticEquipmentEffectHandler(EquipmentEffectHandler next, Game game) {
        super(next, game);
    }

    @Override
    protected boolean match(String playerId, String cardId, String targetPlayerId, EquipmentPlayType playType) {
        Player player = getPlayer(playerId);
        Optional<HandCard> card = Optional.ofNullable(player.getEquipmentArmorCard());
        return card.filter(armorCard -> armorCard instanceof EightDiagramTactic && cardId.equals(armorCard.getId())).isPresent();
    }

    @Override
    protected List<DomainEvent> doHandle(String playerId, String cardId, String targetPlayerId, EquipmentPlayType playType) {
        Round currentRound = game.getCurrentRound();
        Stage currentStage = currentRound.getStage();
        if (!currentStage.equals(Stage.Wait_Equipment_Effect)) {
            throw new IllegalStateException(String.format("CurrentRound stage not Wait_Equipment_Effect but [%s]", currentStage));
        }
        currentRound.setStage(Stage.Normal);
        if (skipEquipmentEffect(playType)) {
            game.peekTopBehavior().setIsOneRound(false);
            GameStatusEvent gameStatusEvent = game.getGameStatusEvent("跳過八卦陣效果");
            SkipEquipmentEffectEvent skipEquipmentEffectEvent = new SkipEquipmentEffectEvent(playerId, cardId);
            return new ArrayList<>(List.of(gameStatusEvent, skipEquipmentEffectEvent, new AskDodgeEvent(playerId)));
        }
        Player player = getPlayer(playerId);
        ArmorCard armorCard = player.getEquipment().getArmor();

        List<DomainEvent> domainEvents = armorCard.equipmentEffect(game);

        boolean isEightDiagramTacticEffectSuccess = domainEvents.stream()
                .filter(event -> event instanceof EffectEvent)
                .map(EffectEvent.class::cast)
                .allMatch(EffectEvent::isSuccess);

        Behavior topBehavior = game.peekTopBehavior();
        boolean isOneRoundBehavior = topBehavior.judgeWhetherRemoveTopBehavior();
        topBehavior.setIsOneRound(isOneRoundBehavior);
        if (isEightDiagramTacticEffectSuccess) {
            game.removeCompletedBehaviors();
            addAskDodgeEventIfCurrentBehaviorIsArrowBarrageBehavior(domainEvents);
        } else {
            domainEvents.add(new AskDodgeEvent(playerId));
        }

        return domainEvents;
    }

    private boolean skipEquipmentEffect(EquipmentPlayType equipmentPlayType) {
        return equipmentPlayType.equals(EquipmentPlayType.SKIP);
    }

    private void addAskDodgeEventIfCurrentBehaviorIsArrowBarrageBehavior(List<DomainEvent> events) {
        Behavior topBehavior = game.peekTopBehavior();
        if (topBehavior instanceof ArrowBarrageBehavior arrowBarrageBehavior) {
            Player currentReactionPlayer = arrowBarrageBehavior.getCurrentReactionPlayer();
            currentReactionPlayer =  game.getNextPlayer(currentReactionPlayer);
            Player arrowBarrageCurrentReactionPlayer = currentReactionPlayer;
            if (arrowBarrageBehavior.isInReactionPlayers(arrowBarrageCurrentReactionPlayer.getId())) {
                events.add(new AskDodgeEvent(arrowBarrageCurrentReactionPlayer.getId()));
            }
            game.getCurrentRound().setActivePlayer(arrowBarrageCurrentReactionPlayer);
        }
    }




}
