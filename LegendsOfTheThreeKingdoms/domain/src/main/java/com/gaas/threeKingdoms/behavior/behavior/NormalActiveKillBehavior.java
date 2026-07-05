package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.behavior.HuJiaCompatibleAskDodgeBehavior;
import com.gaas.threeKingdoms.events.AskActivateYinYangSwordsEvent;
import com.gaas.threeKingdoms.events.AskDodgeEvent;
import com.gaas.threeKingdoms.events.AskGreenDragonCrescentBladeEffectEvent;
import com.gaas.threeKingdoms.events.AskPlayEquipmentEffectEvent;
import com.gaas.threeKingdoms.events.AskStonePiercingAxeEffectEvent;
import com.gaas.threeKingdoms.events.BlackPommelEffectEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.PlayCardEvent;
import com.gaas.threeKingdoms.skill.registry.SkillEngine;
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


public class NormalActiveKillBehavior extends Behavior
        implements com.gaas.threeKingdoms.behavior.JianXiongCompatibleTopBehavior,
                   HuJiaCompatibleAskDodgeBehavior {

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
            emitAskDodgeOrHuJia(events, targetPlayer);
        }
        events.add(game.getGameStatusEvent("出牌"));
        return events;
    }

    /**
     * AskDodge 前先讓 SkillEngine 介入：
     * 1. 鐵騎（攻擊者側）：判定生效 → 目標不能出閃，直接結算傷害
     * 2. 護駕（目標側）：主公曹操 → 改問 Wei 武將代閃
     */
    private void emitAskDodgeOrHuJia(List<DomainEvent> events, Player targetPlayer) {
        boolean[] tieQiSuccess = new boolean[1];
        events.addAll(SkillEngine.tieQiJudgementEvents(game, behaviorPlayer, targetPlayer, tieQiSuccess));
        if (tieQiSuccess[0]) {
            int originalHp = targetPlayer.getHP();
            events.addAll(game.getDamagedEvent(targetPlayer.getId(), behaviorPlayer.getId(),
                    this.cardId, this.card, PlayType.SYSTEM_INTERNAL.getPlayType(),
                    originalHp, targetPlayer, game.getCurrentRound(), Optional.of(this)));
            return;
        }
        Optional<List<DomainEvent>> intercepted = SkillEngine.beforeAskDodge(game, targetPlayer, this);
        if (intercepted.isPresent()) {
            events.addAll(intercepted.get());
        } else {
            events.add(new AskDodgeEvent(targetPlayer.getId()));
        }
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
            emitAskDodgeOrHuJia(events, targetPlayer);
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
            // 用 helper 確保閃進墓地（與 ArrowBarrage / HDH / Duel 等行為一致；
            // 既有 bare playCard(cardId) 寫法只移出手牌、不進墓地，導致取墓地牌的技能行為分歧）
            playerPlayCardNotUpdateActivePlayer(damagedPlayer, cardId);
            return handleDodgeChain(playerId, playerId, cardId, playType);
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

    @Override
    public List<DomainEvent> acceptDodgeFromHuJia(String dodgedPlayerId, String weiPlayerId, String dodgeCardId) {
        // 注意：dodge cardId 已在 WaitingHuJiaResponseBehavior 中由 Wei 棄入墓地
        return handleDodgeChain(dodgedPlayerId, weiPlayerId, dodgeCardId, PlayType.ACTIVE.getPlayType());
    }

    /**
     * 共用的 dodge 後處理鏈：emit PlayCardEvent + 處理 GDCB / SPA 鏈、否則結束 behavior。
     * <p>
     * 由兩條路徑共享：
     * <ul>
     *   <li>正常 dodge response（{@code doResponseToPlayerAction} 內，dodgedPlayerId == cardSourcePlayerId）— 出閃者即被詢問者</li>
     *   <li>護駕代閃（{@code acceptDodgeFromHuJia}，cardSourcePlayerId 為 Wei 武將）— 出閃者非被詢問者</li>
     * </ul>
     * 抽出此共用 helper 是為了避免兩條路徑在 GDCB/SPA 鏈邏輯上分歧。
     *
     * @param dodgedPlayerId 被詢問出閃的玩家 ID（曹操在 HuJia 路徑下）
     * @param cardSourcePlayerId 實際打出閃的玩家 ID（HuJia 路徑下為 Wei 武將；正常路徑下與 dodgedPlayerId 相同）
     * @param dodgeCardId 打出的閃 cardId
     * @param playType {@link PlayType#ACTIVE} 字串值
     */
    private List<DomainEvent> handleDodgeChain(String dodgedPlayerId, String cardSourcePlayerId,
                                                String dodgeCardId, String playType) {
        Round currentRound = game.getCurrentRound();
        PlayCardEvent playCardEvent = new PlayCardEvent(
                "出牌", cardSourcePlayerId, behaviorPlayer.getId(), dodgeCardId, playType);

        // 青龍偃月刀效果：攻擊者裝備青龍偃月刀時，可再出一張殺
        if (behaviorPlayer.getEquipmentWeaponCard() instanceof GreenDragonCrescentBladeCard) {
            isOneRound = false;
            currentRound.setActivePlayer(behaviorPlayer);
            // 使用 this.cardId/this.card（殺的），不是 parameter dodgeCardId（閃的）
            game.updateTopBehavior(new WaitingGreenDragonCrescentBladeResponseBehavior(
                    game, behaviorPlayer, List.of(dodgedPlayerId),
                    behaviorPlayer, this.cardId, PlayType.ACTIVE.getPlayType(), this.card));
            return List.of(playCardEvent,
                    new AskGreenDragonCrescentBladeEffectEvent(behaviorPlayer.getId(), dodgedPlayerId),
                    game.getGameStatusEvent("出牌"));
        }

        // 貫石斧效果：攻擊者裝備貫石斧且可棄牌 ≥2 張時，可棄兩牌強制命中
        if (behaviorPlayer.getEquipmentWeaponCard() instanceof StonePiercingAxeCard
                && getDiscardableCardCount(behaviorPlayer) >= 2) {
            isOneRound = false;
            currentRound.setActivePlayer(behaviorPlayer);
            // 使用 this.cardId/this.card（殺的），不是 parameter dodgeCardId（閃的）
            game.updateTopBehavior(new WaitingStonePiercingAxeResponseBehavior(
                    game, behaviorPlayer, List.of(dodgedPlayerId),
                    behaviorPlayer, this.cardId, PlayType.ACTIVE.getPlayType(), this.card));
            return List.of(playCardEvent,
                    new AskStonePiercingAxeEffectEvent(behaviorPlayer.getId(), dodgedPlayerId),
                    game.getGameStatusEvent("出牌"));
        }

        currentRound.setActivePlayer(currentRound.getCurrentRoundPlayer());
        isOneRound = true;
        return List.of(playCardEvent, game.getGameStatusEvent("出牌"));
    }

    /**
     * 流離：把這張殺的目標改為 newTarget，並對新目標重新走「防具詢問或問閃」流程
     * （新目標的護駕/鐵騎/流離等 hook 照常套用 — 流離可被連鎖轉移）。
     */
    public List<DomainEvent> redirectTo(Player newTarget) {
        reactionPlayers.clear();
        reactionPlayers.add(newTarget.getId());
        currentReactionPlayer = newTarget;
        game.getCurrentRound().setActivePlayer(newTarget);

        List<DomainEvent> events = new ArrayList<>();
        if (isEquipmentHasSpecialEffect(newTarget) && !isAttackerHasBlackPommel(behaviorPlayer)) {
            game.getCurrentRound().setStage(Stage.Wait_Equipment_Effect);
            events.add(new AskPlayEquipmentEffectEvent(newTarget.getId(),
                    newTarget.getEquipment().getArmor(), List.of(newTarget.getId())));
        } else {
            emitAskDodgeOrHuJia(events, newTarget);
        }
        return events;
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
