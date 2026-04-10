package com.gaas.threeKingdoms.effect;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.behavior.behavior.ArrowBarrageBehavior;
import com.gaas.threeKingdoms.behavior.behavior.HeavenlyDoubleHalberdKillBehavior;
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
        topBehavior.setIsOneRound(isOneRoundBehavior && isEightDiagramTacticEffectSuccess);
        if (isEightDiagramTacticEffectSuccess) {
            addAskDodgeEventIfCurrentBehaviorIsArrowBarrageBehavior(domainEvents);
            addAskDodgeEventIfCurrentBehaviorIsHeavenlyDoubleHalberdKillBehavior(domainEvents, playerId);
        } else {
            domainEvents.add(new AskDodgeEvent(playerId));
        }
        GameStatusEvent gameStatusEvent = game.getGameStatusEvent("發動八卦陣效果");
        domainEvents.add(gameStatusEvent);
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

    /**
     * 方天畫戟：當前目標用八卦陣成功抵擋後，依「目標列表順序」推進到下一位目標並詢問出閃/防具效果。
     * 若當前目標已是最後一位，則將 behavior 標記為結束。
     */
    private void addAskDodgeEventIfCurrentBehaviorIsHeavenlyDoubleHalberdKillBehavior(List<DomainEvent> events, String playerId) {
        Behavior topBehavior = game.peekTopBehavior();
        if (topBehavior instanceof HeavenlyDoubleHalberdKillBehavior halberdBehavior) {
            boolean isLast = halberdBehavior.isLastReactionPlayer(playerId);
            if (isLast) {
                halberdBehavior.setIsOneRound(true);
                game.getCurrentRound().setActivePlayer(game.getCurrentRound().getCurrentRoundPlayer());
            } else {
                halberdBehavior.setIsOneRound(false);
                halberdBehavior.advanceToNextTarget();
                halberdBehavior.askCurrentTargetDodgeOrEquipmentEffect(events);
            }
        }
    }




}
