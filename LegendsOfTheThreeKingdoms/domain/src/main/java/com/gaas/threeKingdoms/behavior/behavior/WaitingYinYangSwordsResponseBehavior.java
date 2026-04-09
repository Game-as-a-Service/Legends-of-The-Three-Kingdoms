package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.round.Round;

import java.util.ArrayList;
import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.isSkip;

/**
 * 雌雄雙股劍效果等待回應
 * 目標選擇：棄一張手牌 (playType=active, cardId=要棄的牌) 或 讓攻擊者摸一張牌 (playType=skip)
 */
public class WaitingYinYangSwordsResponseBehavior extends Behavior {

    public WaitingYinYangSwordsResponseBehavior(Game game, Player behaviorPlayer, List<String> reactionPlayers,
                                                 Player currentReactionPlayer, String cardId, String playType, HandCard card) {
        super(game, behaviorPlayer, reactionPlayers, currentReactionPlayer, cardId, playType, card, true, false, false);
    }

    @Override
    protected List<DomainEvent> doResponseToPlayerAction(String playerId, String targetPlayerId, String cardId, String playType) {
        List<DomainEvent> events = new ArrayList<>();
        Player targetPlayer = game.getPlayer(playerId); // the one who needs to respond (被殺的人)
        Player attackerPlayer = behaviorPlayer; // the attacker (出殺的人)

        if (isSkip(playType)) {
            // Target lets attacker draw 1 card
            DomainEvent drawEvent = game.drawCardToPlayer(attackerPlayer, false, 1);
            events.add(drawEvent);
        } else {
            // Target discards a hand card
            HandCard discardedCard = targetPlayer.playCard(cardId);
            game.getGraveyard().add(discardedCard);
            events.add(new PlayCardEvent("雌雄雙股劍棄牌", playerId, targetPlayerId, cardId, playType));
        }

        // Now proceed: check if target has armor with special effect (EightDiagramTactic)
        Behavior killBehavior = game.peekTopBehaviorSecondElement().orElse(null);
        if (killBehavior != null && NormalActiveKillBehavior.isEquipmentHasSpecialEffect(targetPlayer)) {
            game.getCurrentRound().setStage(com.gaas.threeKingdoms.round.Stage.Wait_Equipment_Effect);
            events.add(new AskPlayEquipmentEffectEvent(targetPlayer.getId(), targetPlayer.getEquipment().getArmor(), List.of(targetPlayer.getId())));
        } else {
            events.add(new AskDodgeEvent(playerId));
        }

        events.add(game.getGameStatusEvent("雌雄雙股劍效果結束"));
        isOneRound = true;
        return events;
    }
}
