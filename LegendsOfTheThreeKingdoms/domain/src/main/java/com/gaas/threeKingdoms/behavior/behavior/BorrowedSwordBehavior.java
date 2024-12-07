package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.PlayCardEvent;
import com.gaas.threeKingdoms.events.WeaponUsurpationEvent;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.WeaponCard;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.round.Round;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.gaas.threeKingdoms.handcard.PlayCard.isKillCard;
import static com.gaas.threeKingdoms.handcard.PlayCard.isSkip;

public class BorrowedSwordBehavior extends Behavior {
    public BorrowedSwordBehavior(Game game, Player behaviorPlayer, List<String> reactionPlayers, Player currentReactionPlayer, String cardId, String playType, HandCard card) {
        super(game, behaviorPlayer, reactionPlayers, currentReactionPlayer, cardId, playType, card, true, false);
    }

    @Override
    public List<DomainEvent> playerAction() {
        String targetPlayerId = reactionPlayers.get(0);
        playerPlayCardNotUpdateActivePlayer(behaviorPlayer, cardId); // 初次打出借刀殺人，還沒選人
        List<DomainEvent> events = new ArrayList<>();
        events.add(new PlayCardEvent("出借刀殺人", behaviorPlayer.getId(), targetPlayerId, cardId, playType));
        events.add(game.getGameStatusEvent("出借刀殺人"));
        return events;
    }

    @Override
    protected List<DomainEvent> doResponseToPlayerAction(String playerId, String targetPlayerId, String cardId, String playType) {
        Player player = game.getPlayer(playerId);
        if (isKillCard(cardId)) {
            Behavior behavior = new NormalActiveKillBehavior(game, player, List.of(targetPlayerId), player, cardId, playType, player.getHand().getCard(cardId).get());
            isOneRound = true;
            game.updateTopBehavior(behavior);
            return behavior.playerAction();
        } else if (isSkip(playType)) {
            WeaponCard targetWeaponCard = player.getEquipmentWeaponCard();
            player.getEquipment().setWeapon(null);
            behaviorPlayer.getHand().addCardToHand(targetWeaponCard);
            isOneRound = true;
            List<DomainEvent> events = new ArrayList<>();
            events.add(new PlayCardEvent("不出牌", playerId, targetPlayerId, cardId, playType));
            events.add(new WeaponUsurpationEvent(playerId, behaviorPlayer.getId(), targetWeaponCard.getId()));
            Round currentRound = game.getCurrentRound();
            currentRound.setActivePlayer(currentRound.getCurrentRoundPlayer());
            events.add(game.getGameStatusEvent("跳過"));
            return events;
        }

        throw new IllegalStateException(String.format("Can't play this card:[%s]", cardId));
    }
}
