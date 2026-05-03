package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.AskJianXiongEffectEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.JianXiongEffectEvent;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.round.Round;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 奸雄等待回應 — 受傷者可選擇 ACCEPT（取得造成傷害的牌）或 SKIP。
 * behaviorPlayer = 受傷者；sourceCardId 為造成傷害的牌的 ID（牌物件在 ACCEPT 時從 graveyard 取）。
 *
 * 注意：故意只儲存 cardId 而不存 HandCard 物件 — 因為 VirtualKill（如丈八蛇矛）
 * 不在 PlayCard.CARD_FACTORY_MAP 中，reload 時 PlayCard.findById 會回 null。
 * 所有實際的牌操作（移到手牌）都在 resolveChoice 時透過 graveyard.removeCard(cardId) 完成。
 */
public class WaitingJianXiongResponseBehavior extends Behavior {

    public WaitingJianXiongResponseBehavior(Game game, Player damagedPlayer, String sourceCardId) {
        super(game,
                damagedPlayer,
                List.of(damagedPlayer.getId()),
                damagedPlayer,
                sourceCardId,
                PlayType.SYSTEM_INTERNAL.getPlayType(),
                null,
                false,
                false,
                true);
    }

    public List<DomainEvent> resolveChoice(String respondingPlayerId, AskJianXiongEffectEvent.Choice choice) {
        if (!behaviorPlayer.getId().equals(respondingPlayerId)) {
            throw new IllegalStateException(
                    String.format("player %s is not the damaged player who should respond to JianXiong", respondingPlayerId));
        }

        List<DomainEvent> events = new ArrayList<>();
        Round round = game.getCurrentRound();
        round.setActivePlayer(round.getCurrentRoundPlayer());

        if (choice == AskJianXiongEffectEvent.Choice.ACCEPT) {
            Optional<HandCard> taken = game.getGraveyard().removeCard(cardId);
            if (taken.isEmpty()) {
                throw new IllegalStateException("Source card no longer in graveyard: " + cardId);
            }
            behaviorPlayer.getHand().addCardToHand(taken.get());
            events.add(new JianXiongEffectEvent(behaviorPlayer.getId(), cardId, true));
            events.add(game.getGameStatusEvent(behaviorPlayer.getId() + " 發動奸雄"));
        } else if (choice == AskJianXiongEffectEvent.Choice.SKIP) {
            events.add(new JianXiongEffectEvent(behaviorPlayer.getId(), cardId, false));
            events.add(game.getGameStatusEvent("放棄奸雄"));
        } else {
            throw new IllegalArgumentException("Invalid JianXiong choice: " + choice);
        }

        isOneRound = true;
        return events;
    }
}
