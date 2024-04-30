package com.gaas.threeKingdoms.effect;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.EffectEvent;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.ArmorCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.EightDiagramTactic;
import com.gaas.threeKingdoms.player.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class EightDiagramTacticEffectHandler extends EffectHandler {

    public EightDiagramTacticEffectHandler(EffectHandler next, Game game) {
        super(next, game);
    }

    @Override
    protected boolean match(String playerId, String cardId, String targetPlayerId, PlayType playType) {
        Player player = getPlayer(playerId);
        Optional<HandCard> card = Optional.ofNullable(player.getEquipmentArmorCard());
        boolean isEightDiagramTactic = card.filter(armorCard -> armorCard instanceof EightDiagramTactic && cardId.equals(armorCard.getId())).isPresent();
        boolean isEquipmentPlayType = playType.equals(PlayType.EQUIPMENT_ACTIVE) || playType.equals(PlayType.EQUIPMENT_SKIP);
        return (isEightDiagramTactic && isEquipmentPlayType);
    }

    @Override
    protected List<DomainEvent> doHandle(String playerId, String cardId, String targetPlayerId, PlayType playType) {
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

    private boolean skipEquipmentEffect(PlayType playType) {
        return PlayType.EQUIPMENT_SKIP == playType;
    }

}
