package com.gaas.threeKingdoms.skill.wei;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.behavior.JianXiongCompatibleTopBehavior;
import com.gaas.threeKingdoms.behavior.behavior.WaitingSkillEffectBehavior;
import com.gaas.threeKingdoms.events.AskSkillEffectEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.SkillEffectEvent;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.skill.context.DamageContext;
import com.gaas.threeKingdoms.skill.trigger.ChoiceResolvableSkill;
import com.gaas.threeKingdoms.skill.trigger.OnDamagedSkill;

import java.util.ArrayList;
import java.util.List;

/**
 * 司馬懿 (WEI002) 反饋 — 當你受到傷害時，可獲得傷害來源的一張牌（手牌或裝備）（issue #163）。
 *
 * ACCEPT 取牌規則：
 *   - request.cardIds[0] 指定傷害來源的裝備牌 id → 取該裝備
 *   - 未指定 → 取來源第一張手牌（手牌為隱藏資訊，等同隨機）
 *   - 來源無手牌無裝備 → 觸發時即不詢問
 *
 * v1 範圍：top behavior 為 JianXiongCompatibleTopBehavior 且非 polling caller（或空 stack）
 * 時觸發；AOE polling 中的 defer-resume 整合為 follow-up。
 */
public class FanKuiSkill implements OnDamagedSkill, ChoiceResolvableSkill {

    public static final String GENERAL_ID = General.司馬懿.getGeneralId();
    public static final String SKILL_NAME = "反饋";
    public static final String PARAM_ATTACKER_ID = "FANKUI_ATTACKER_ID";

    @Override
    public String getGeneralId() {
        return GENERAL_ID;
    }

    @Override
    public String getSkillName() {
        return SKILL_NAME;
    }

    @Override
    public List<DomainEvent> onDamaged(Game game, DamageContext ctx) {
        Player damaged = ctx.damagedPlayer();
        Player attacker = ctx.sourcePlayer();
        if (attacker == null || attacker.equals(damaged)) {
            return List.of(); // 無來源（閃電自傷等）不觸發
        }
        if (!damaged.isHPGreaterThanZero()) {
            return List.of(); // 瀕死流程不觸發（v1）
        }
        if (attacker.getHandSize() == 0 && !attacker.getEquipment().hasAnyEquipment()) {
            return List.of(); // 來源無牌可拿
        }
        Behavior top = game.isTopBehaviorEmpty() ? null : game.peekTopBehavior();
        if (top != null && !(top instanceof JianXiongCompatibleTopBehavior compatible
                && !compatible.isPollingCaller())) {
            return List.of(); // AOE polling 整合 follow-up
        }

        game.removeCompletedBehaviors();
        WaitingSkillEffectBehavior waiting = new WaitingSkillEffectBehavior(game, damaged, SKILL_NAME);
        waiting.putParam(PARAM_ATTACKER_ID, attacker.getId());
        game.updateTopBehavior(waiting);
        game.getCurrentRound().setActivePlayer(damaged);

        // 展示可取的裝備（手牌隱藏不展示內容）
        return List.of(new AskSkillEffectEvent(SKILL_NAME, damaged.getId(),
                attacker.getEquipment().getAllEquipmentCardIds(), attacker.getId()));
    }

    @Override
    public List<DomainEvent> resolveChoice(Game game, WaitingSkillEffectBehavior waiting,
                                           String choice, List<String> cardIds, String targetPlayerId) {
        Player simaYi = waiting.getBehaviorPlayer();
        String attackerId = (String) waiting.getParam(PARAM_ATTACKER_ID);
        Player attacker = game.getPlayer(attackerId);
        List<DomainEvent> events = new ArrayList<>();
        game.getCurrentRound().setActivePlayer(game.getCurrentRound().getCurrentRoundPlayer());

        if ("ACCEPT".equals(choice)) {
            String takenCardId;
            if (cardIds != null && !cardIds.isEmpty()
                    && attacker.getEquipment().getAllEquipmentCardIds().contains(cardIds.get(0))) {
                takenCardId = takeEquipment(attacker, simaYi, cardIds.get(0));
            } else if (attacker.getHandSize() > 0) {
                HandCard taken = attacker.getHand().getCards().get(0);
                takenCardId = taken.getId();
                attacker.playCard(takenCardId);
                simaYi.getHand().addCardToHand(taken);
            } else if (attacker.getEquipment().hasAnyEquipment()) {
                takenCardId = takeEquipment(attacker, simaYi,
                        attacker.getEquipment().getAllEquipmentCardIds().get(0));
            } else {
                throw new IllegalStateException("attacker has no card to take");
            }
            events.add(new SkillEffectEvent(SKILL_NAME, simaYi.getId(), true,
                    List.of(takenCardId), attackerId));
            events.add(game.getGameStatusEvent(simaYi.getId() + " 發動反饋"));
        } else if ("SKIP".equals(choice)) {
            events.add(new SkillEffectEvent(SKILL_NAME, simaYi.getId(), false, List.of(), attackerId));
            events.add(game.getGameStatusEvent("放棄反饋"));
        } else {
            throw new IllegalArgumentException("Invalid FanKui choice: " + choice);
        }
        return events;
    }

    private String takeEquipment(Player from, Player to, String equipmentId) {
        HandCard equipment = from.getEquipment().getAllEquipmentCards().stream()
                .filter(c -> c.getId().equals(equipmentId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("equipment not found: " + equipmentId));
        from.getEquipment().removeEquipment(equipmentId);
        to.getHand().addCardToHand(equipment);
        return equipmentId;
    }
}
