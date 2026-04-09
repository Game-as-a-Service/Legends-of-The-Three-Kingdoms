package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.AskDodgeEvent;
import com.gaas.threeKingdoms.events.AskPlayEquipmentEffectEvent;
import com.gaas.threeKingdoms.events.BlackPommelEffectEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.PlayCardEvent;
import com.gaas.threeKingdoms.events.AskYinYangSwordsEffectEvent;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.equipmentcard.EquipmentCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.BlackPommelCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.QilinBowCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.YinYangSwordsCard;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.round.Round;
import com.gaas.threeKingdoms.round.Stage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.gaas.threeKingdoms.handcard.PlayCard.isDodgeCard;
import static com.gaas.threeKingdoms.handcard.PlayCard.isSkip;


public class NormalActiveKillBehavior extends Behavior {

    public NormalActiveKillBehavior(Game game, Player behaviorPlayer, List<String> reactionPlayers, Player currentReactionPlayer, String cardId, String playType, HandCard card) {
        super(game, behaviorPlayer, reactionPlayers, currentReactionPlayer, cardId, playType, card, true, false, false);
    }

    @Override
    public List<DomainEvent> playerAction() {
        String targetPlayerId = reactionPlayers.get(0);
        Player targetPlayer = game.getPlayer(targetPlayerId);

        playerPlayCard(behaviorPlayer, game.getPlayer(targetPlayerId), cardId);

        Round currentRound = game.getCurrentRound();

        List<DomainEvent> events = new ArrayList<>();
        events.add(new PlayCardEvent("出牌", behaviorPlayer.getId(), targetPlayerId, cardId, playType));

        // 雌雄雙股劍效果：異性出殺時觸發
        if (shouldTriggerYinYangSwords(behaviorPlayer, targetPlayer)) {
            if (targetPlayer.getHandSize() == 0) {
                // 目標沒手牌，自動讓攻擊者摸一張牌
                DomainEvent drawEvent = game.drawCardToPlayer(behaviorPlayer, false, 1);
                events.add(drawEvent);
                // 繼續到閃/八卦陣
                addAskDodgeOrEquipmentEffect(events, targetPlayer, currentRound);
            } else {
                // 目標有手牌，詢問棄牌或讓攻擊者摸牌
                isOneRound = false;
                currentRound.setActivePlayer(targetPlayer);
                game.updateTopBehavior(new WaitingYinYangSwordsResponseBehavior(
                        game, behaviorPlayer, Collections.singletonList(targetPlayerId),
                        targetPlayer, cardId, PlayType.ACTIVE.getPlayType(), card));
                events.add(new AskYinYangSwordsEffectEvent(behaviorPlayer.getId(), targetPlayerId));
            }
        } else if (isEquipmentHasSpecialEffect(targetPlayer) && !isAttackerHasBlackPommel(behaviorPlayer)) {
            currentRound.setStage(Stage.Wait_Equipment_Effect);
            DomainEvent askPlayEquipmentEffectEvent = new AskPlayEquipmentEffectEvent(targetPlayer.getId(), targetPlayer.getEquipment().getArmor(), List.of(targetPlayer.getId()));
            events.add(askPlayEquipmentEffectEvent);
        } else {
            // 青釭劍發動：攻擊者有青釭劍且目標有防具時，發出效果事件
            if (isAttackerHasBlackPommel(behaviorPlayer) && isEquipmentHasSpecialEffect(targetPlayer)) {
                events.add(new BlackPommelEffectEvent(behaviorPlayer.getId(), targetPlayerId));
            }
            events.add(new AskDodgeEvent(targetPlayerId));
        }
        events.add(game.getGameStatusEvent("出牌"));
        return events;
    }

    private boolean shouldTriggerYinYangSwords(Player attacker, Player target) {
        return attacker.getEquipmentWeaponCard() instanceof YinYangSwordsCard
                && attacker.getGender() != target.getGender();
    }

    private void addAskDodgeOrEquipmentEffect(List<DomainEvent> events, Player targetPlayer, Round currentRound) {
        if (isEquipmentHasSpecialEffect(targetPlayer)) {
            currentRound.setStage(Stage.Wait_Equipment_Effect);
            events.add(new AskPlayEquipmentEffectEvent(targetPlayer.getId(), targetPlayer.getEquipment().getArmor(), List.of(targetPlayer.getId())));
        } else {
            events.add(new AskDodgeEvent(targetPlayer.getId()));
        }
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

            List<DomainEvent> events = game.getDamagedEvent(playerId, targetPlayerId, cardId, card, playType, originalHp, damagedPlayer, currentRound, Optional.of(this));
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
            List<DomainEvent> events = game.getDamagedEvent(playerId, targetPlayerId, cardId, card, playType, originalHp, damagedPlayer, currentRound, Optional.of(this));
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
        return  PlayType.SYSTEM_INTERNAL.getPlayType().equals(playType);
    }

    private boolean isAskPlayerUseQilinBow(Player attackPlayer, Player damagedPlayer) {
        return attackPlayer.getEquipmentWeaponCard() instanceof QilinBowCard && damagedPlayer.hasMountsCard();
    }

    static public boolean isEquipmentHasSpecialEffect(Player targetPlayer) {
        return targetPlayer.getEquipment().hasSpecialEffect();
    }

    private boolean isAttackerHasBlackPommel(Player attackPlayer) {
        return attackPlayer.getEquipmentWeaponCard() instanceof BlackPommelCard;
    }

}
