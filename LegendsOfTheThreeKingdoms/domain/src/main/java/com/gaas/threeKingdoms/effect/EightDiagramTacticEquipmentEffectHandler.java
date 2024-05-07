package com.gaas.threeKingdoms.effect;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.Round;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.EffectEvent;
import com.gaas.threeKingdoms.events.RoundEvent;
import com.gaas.threeKingdoms.handcard.EquipmentPlayType;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.ArmorCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.EightDiagramTactic;
import com.gaas.threeKingdoms.player.Player;

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
        if (skipEquipmentEffect(playType)) {
            game.peekTopBehavior().setIsOneRound(false);
            return new ArrayList<>();
        }
        Player player = getPlayer(playerId);
        ArmorCard armorCard = player.getEquipment().getArmor();

        List<DomainEvent> domainEvents = armorCard.equipmentEffect(game);

        boolean isEightDiagramTacticEffectSuccess = domainEvents.stream()
                .map(EffectEvent.class::cast)
                .allMatch(EffectEvent::isSuccess);

        game.peekTopBehavior().setIsOneRound(isEightDiagramTacticEffectSuccess);
        return domainEvents;
    }

    private boolean skipEquipmentEffect(EquipmentPlayType equipmentPlayType) {
        return equipmentPlayType.equals(EquipmentPlayType.SKIP);
    }

}