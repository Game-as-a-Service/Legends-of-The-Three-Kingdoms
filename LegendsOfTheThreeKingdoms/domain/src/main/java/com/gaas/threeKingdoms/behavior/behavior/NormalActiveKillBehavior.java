package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.AskActivateYinYangSwordsEvent;
import com.gaas.threeKingdoms.events.AskDodgeEvent;
import com.gaas.threeKingdoms.events.AskGreenDragonCrescentBladeEffectEvent;
import com.gaas.threeKingdoms.events.AskPlayEquipmentEffectEvent;
import com.gaas.threeKingdoms.events.AskStonePiercingAxeEffectEvent;
import com.gaas.threeKingdoms.events.BlackPommelEffectEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.PlayCardEvent;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.equipmentcard.EquipmentCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.BlackPommelCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.GreenDragonCrescentBladeCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.QilinBowCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.StonePiercingAxeCard;
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

        // 雌雄雙股劍效果：異性出殺時，先問攻擊者是否要發動
        if (shouldTriggerYinYangSwords(behaviorPlayer, targetPlayer)) {
            isOneRound = false;
            currentRound.setActivePlayer(behaviorPlayer);
            game.updateTopBehavior(new WaitingYinYangSwordsActivationBehavior(
                    game, behaviorPlayer, Collections.singletonList(targetPlayerId),
                    behaviorPlayer, cardId, PlayType.ACTIVE.getPlayType(), card));
            events.add(new AskActivateYinYangSwordsEvent(behaviorPlayer.getId(), targetPlayerId));
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
            damagedPlayer.playCard(cardId);
            PlayCardEvent playCardEvent = new PlayCardEvent("出牌", playerId, targetPlayerId, cardId, playType);

            // 青龍偃月刀效果：攻擊者裝備青龍偃月刀時，可再出一張殺
            if (behaviorPlayer.getEquipmentWeaponCard() instanceof GreenDragonCrescentBladeCard) {
                isOneRound = false;
                currentRound.setActivePlayer(behaviorPlayer);
                // 使用 this.cardId/this.card（殺的），不是 parameter cardId（閃的）
                game.updateTopBehavior(new WaitingGreenDragonCrescentBladeResponseBehavior(
                        game, behaviorPlayer, List.of(playerId),
                        behaviorPlayer, this.cardId, PlayType.ACTIVE.getPlayType(), this.card));
                AskGreenDragonCrescentBladeEffectEvent askEvent = new AskGreenDragonCrescentBladeEffectEvent(
                        behaviorPlayer.getId(), playerId);
                return List.of(playCardEvent, askEvent, game.getGameStatusEvent("出牌"));
            }

            // 貫石斧效果：攻擊者裝備貫石斧且可棄牌 ≥2 張時，可棄兩牌強制命中
            if (behaviorPlayer.getEquipmentWeaponCard() instanceof StonePiercingAxeCard
                    && getDiscardableCardCount(behaviorPlayer) >= 2) {
                isOneRound = false;
                currentRound.setActivePlayer(behaviorPlayer);
                // 使用 this.cardId/this.card（殺的），不是 parameter cardId（閃的）
                game.updateTopBehavior(new WaitingStonePiercingAxeResponseBehavior(
                        game, behaviorPlayer, List.of(playerId),
                        behaviorPlayer, this.cardId, PlayType.ACTIVE.getPlayType(), this.card));
                AskStonePiercingAxeEffectEvent askEvent = new AskStonePiercingAxeEffectEvent(
                        behaviorPlayer.getId(), playerId);
                return List.of(playCardEvent, askEvent, game.getGameStatusEvent("出牌"));
            }

            currentRound.setActivePlayer(currentRound.getCurrentRoundPlayer());
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

    private int getDiscardableCardCount(Player player) {
        return player.getHand().getCards().size() + player.getEquipment().getAllEquipmentCards().size();
    }

}
