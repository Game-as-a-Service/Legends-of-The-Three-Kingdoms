package com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.behavior.behavior.WaitingQilinBowResponseBehavior;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.gamephase.GeneralDying;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.round.Round;
import com.gaas.threeKingdoms.round.Stage;
import com.gaas.threeKingdoms.skill.registry.SkillEngine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 麒麟弓
 * */
public class QilinBowCard extends WeaponCard {

    public QilinBowCard(PlayCard playCard) {
        super(playCard, 4);
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
            // 移除NormalActiveKill
//            game.removeTopBehavior();
            // 只有一馬時，直接移除此馬
            HandCard removedMount = damagedPlayer.removeOneMount();
            removeMountCardId = removedMount.getId();
            game.getGraveyard().add(removedMount);
            message = String.format("發動麒麟弓效果 移除 %s", removeMountCardId);
            // 梟姬：失去裝備 → 摸牌。麒麟弓是「先移除馬、再造成傷害」，摸牌事件也要排在傷害事件之前，
            // 且必須在 getDamagedEvent / getGameStatusEvent 之前呼叫（兩者都會快照當下的玩家狀態）。
            List<DomainEvent> loseMountDrawEvents = SkillEngine.afterLoseEquipment(game, damagedPlayer, 1);
            HandCard card = behavior.getCard(); // Kill
            int originalHp = damagedPlayer.getHP();
            List<DomainEvent> damageEvents = game.getDamagedEvent(behavior.getBehaviorPlayer().getId(), damagedPlayer.getId(), behavior.getCardId(), card, PlayType.SYSTEM_INTERNAL.getPlayType(), originalHp, damagedPlayer, currentRound, Optional.of(behavior));
            if (isPlayerStillAlive(damagedPlayer)) {
                currentRound.setActivePlayer(currentRound.getCurrentRoundPlayer());
                behavior.setIsOneRound(true);
                QilinBowCardEffectEvent qilinBowCardEffectEvent = new QilinBowCardEffectEvent(message, true, removeMountCardId);
                GameStatusEvent gameStatusEvent = game.getGameStatusEvent(message);
                events.add(qilinBowCardEffectEvent);
                events.addAll(loseMountDrawEvents);
                events.addAll(damageEvents);
                events.add(gameStatusEvent);
            } else {
                game.enterPhase(new GeneralDying(game));
                currentRound.setDyingPlayer(damagedPlayer);
                currentRound.setActivePlayer(damagedPlayer);
                QilinBowCardEffectEvent qilinBowCardEffectEvent = new QilinBowCardEffectEvent(message, true, removeMountCardId);
                behavior.setIsOneRound(false);
                GameStatusEvent gameStatusEvent = game.getGameStatusEvent(message);
                events.add(qilinBowCardEffectEvent);
                events.addAll(loseMountDrawEvents);
                events.addAll(damageEvents);
                events.add(gameStatusEvent);
            }
            return events;
        }

        DomainEvent askChooseMountEvent = askChooseMountEvent(damagedPlayer, behavior.getBehaviorPlayer());
        Round round = game.getCurrentRound();
        round.setActivePlayer(behavior.getBehaviorPlayer());
        round.setStage(Stage.Wait_Accept_Equipment_Effect);
        GameStatusEvent gameStatusEvent = game.getGameStatusEvent("請選擇要被捨棄的馬");
        return List.of(askChooseMountEvent, gameStatusEvent);
    }

    private DomainEvent askChooseMountEvent(Player damagedPlayer, Player chooseMountCardPlayer) {
        String plusOneMountsCardId = damagedPlayer.getEquipmentPlusOneMountsCard().getId();
        String minusOneMountsCardId = damagedPlayer.getEquipmentMinusOneMountsCard().getId();
        return new AskChooseMountCardEvent("AskChooseMountCardEvent", "請選擇要被捨棄的馬", List.of(plusOneMountsCardId, minusOneMountsCardId), chooseMountCardPlayer.getId(), damagedPlayer.getId());
    }

    private boolean isPlayerStillAlive(Player damagedPlayer) {
        return damagedPlayer.getHP() > 0;
    }
}


