package com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.gamephase.GeneralDying;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.MinusMountsCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.PlusMountsCard;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.round.Round;
import com.gaas.threeKingdoms.round.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * 麒麟弓
 * */
public class QilinBowCard extends WeaponCard {

    public QilinBowCard(PlayCard playCard) {
        super(playCard, 5);
    }

    @Override
    public List<DomainEvent> equipmentEffect(Game game) {
        List<DomainEvent> events = new ArrayList<>();
        Round currentRound = game.getCurrentRound();
        Behavior behavior = game.peekTopBehavior();
        Player damagedPlayer = game.getPlayer(behavior.getReactionPlayers().get(0));
        String message = "";
        String removeMountCardId = "";

        if (damagedPlayer.onlyHasOneMount()) {
            // 只有一馬時，直接移除此馬
            removeMountCardId = damagedPlayer.removeOneMount();
            message = String.format("發動麒麟弓效果 移除 %s", removeMountCardId);
            HandCard card = behavior.getCard(); // Kill
            int originalHp = damagedPlayer.getHP();
            card.effect(damagedPlayer);
            PlayerDamagedEvent playerDamagedEvent = createPlayerDamagedEvent(originalHp, damagedPlayer);
            if (isPlayerStillAlive(damagedPlayer)) {
                currentRound.setActivePlayer(currentRound.getCurrentRoundPlayer());
                behavior.setIsOneRound(true);
                RoundEvent roundEvent = new RoundEvent(currentRound);
                List<PlayerEvent> playerEvents = game.getPlayers().stream().map(PlayerEvent::new).toList();
                QilinBowCardEffectEvent qilinBowCardEffectEvent = new QilinBowCardEffectEvent(message, true, removeMountCardId, game.getGameId(), playerEvents, roundEvent, game.getGamePhase().getPhaseName());
                events.add(playerDamagedEvent);
                events.add(qilinBowCardEffectEvent);
            } else {
                PlayerDyingEvent playerDyingEvent = createPlayerDyingEvent(damagedPlayer);
                AskPeachEvent askPeachEvent = createAskPeachEvent(damagedPlayer, damagedPlayer);
                game.enterPhase(new GeneralDying(game));
                currentRound.setDyingPlayer(damagedPlayer);
                currentRound.setActivePlayer(damagedPlayer);
                List<PlayerEvent> playerEvents = game.getPlayers().stream().map(PlayerEvent::new).toList();
                RoundEvent roundEvent = new RoundEvent(currentRound);
                QilinBowCardEffectEvent qilinBowCardEffectEvent = new QilinBowCardEffectEvent(message, true, removeMountCardId, game.getGameId(), playerEvents, roundEvent, game.getGamePhase().getPhaseName());
                behavior.setIsOneRound(false);
                events.add(qilinBowCardEffectEvent);
                events.add(playerDamagedEvent);
                events.add(playerDyingEvent);
                events.add(askPeachEvent);
            }
            return events;
        }

        DomainEvent askChooseMountEvent = askChooseMountEvent(damagedPlayer);

        Round round = game.getCurrentRound();
        round.setActivePlayer(behavior.getBehaviorPlayer());
        round.setStage(Stage.Wait_Accept_Equipment_Effect);

        return List.of(askChooseMountEvent);
    }

    private DomainEvent askChooseMountEvent(Player damagedPlayer) {
        String plusOneMountsCardId = damagedPlayer.getEquipmentPlusOneMountsCard().getId();
        String minusOneMountsCardId = damagedPlayer.getEquipmentMinusOneMountsCard().getId();
        return new AskChooseMountCardEvent("AskChooseMountCardEvent", "請選擇要被捨棄的馬", List.of(plusOneMountsCardId, minusOneMountsCardId));
    }

    private boolean isPlayerStillAlive(Player damagedPlayer) {
        return damagedPlayer.getHP() > 0;
    }

    private PlayerDamagedEvent createPlayerDamagedEvent(int originalHp, Player damagedPlayer) {
        return new PlayerDamagedEvent(damagedPlayer.getId(), originalHp, damagedPlayer.getHP());
    }

    private PlayerDyingEvent createPlayerDyingEvent(Player player) {
        return new PlayerDyingEvent(player.getId());
    }

    private AskPeachEvent createAskPeachEvent(Player player, Player dyingPlayer) {
        return new AskPeachEvent(player.getId(), dyingPlayer.getId());
    }

}


