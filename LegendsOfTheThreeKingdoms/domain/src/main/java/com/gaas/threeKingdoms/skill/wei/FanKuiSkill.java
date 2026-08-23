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
 * ACCEPT 取牌規則（對齊順手牽羊 useSnatchEffect 的選牌語意）：
 *   - request.cardIds[0] 為數字 → 來源手牌的 0-based index（手牌為隱藏資訊，等同盲選）
 *   - request.cardIds[0] 為傷害來源的裝備牌 id → 取該裝備
 *   - 未指定 → 取來源第一張手牌（index 0）；來源無手牌時退而取第一件裝備
 *   - 來源無手牌無裝備 → 觸發時即不詢問
 *   - 前端可由 GameStatusEvent seats 取得來源手牌張數（index 範圍 0..N-1）
 *
 * 觸發：top behavior 為 JianXiongCompatibleTopBehavior（含 AOE polling caller，PR #221）或空 stack。
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
        if (top != null && !(top instanceof JianXiongCompatibleTopBehavior)) {
            return List.of(); // 非相容 host 不觸發
        }
        // polling caller（南蠻/萬箭）需保留底層 behavior，resolve 後 resume 輪詢（mirror 奸雄 #209 樣板）
        boolean isPollingCaller = top instanceof JianXiongCompatibleTopBehavior compatible
                && compatible.isPollingCaller();
        if (!isPollingCaller) {
            game.removeCompletedBehaviors();
        }
        WaitingSkillEffectBehavior waiting = new WaitingSkillEffectBehavior(game, damaged, SKILL_NAME);
        waiting.putParam(PARAM_ATTACKER_ID, attacker.getId());
        if (isPollingCaller) {
            waiting.putParam(WaitingSkillEffectBehavior.PARAM_RESUME_POLLING, "true");
        }
        game.updateTopBehavior(waiting);
        game.getCurrentRound().setActivePlayer(damaged);

        // 展示可取的裝備（只列實際存在的裝備 id；手牌隱藏不展示內容）
        return List.of(new AskSkillEffectEvent(SKILL_NAME, damaged.getId(),
                equipmentIds(attacker), attacker.getId()));
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
            String pick = (cardIds != null && !cardIds.isEmpty()) ? cardIds.get(0) : null;
            if (pick != null && equipmentIds(attacker).contains(pick)) {
                takenCardId = takeEquipment(attacker, simaYi, pick);
            } else if (pick != null && pick.matches("\\d+")) {
                // 手牌 index（0-based，同順手牽羊 targetCardIndex）
                int index = Integer.parseInt(pick);
                if (index >= attacker.getHandSize()) {
                    throw new IllegalArgumentException("Hand card index over size");
                }
                takenCardId = takeHandCard(attacker, simaYi, index);
            } else if (pick != null) {
                throw new IllegalArgumentException("Invalid FanKui pick: " + pick
                        + "（須為來源手牌 index 或來源裝備 id）");
            } else if (attacker.getHandSize() > 0) {
                takenCardId = takeHandCard(attacker, simaYi, 0);
            } else if (attacker.getEquipment().hasAnyEquipment()) {
                takenCardId = takeEquipment(attacker, simaYi, equipmentIds(attacker).get(0));
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

    /** 來源實際存在的裝備 id（getAllEquipmentCardIds 會以 "" 佔位空欄，不可直接拿來選/取）。 */
    private static List<String> equipmentIds(Player player) {
        return player.getEquipment().getAllEquipmentCards().stream()
                .map(HandCard::getId)
                .toList();
    }

    private String takeHandCard(Player from, Player to, int index) {
        HandCard taken = from.getHand().getCards().get(index);
        from.playCard(taken.getId());
        to.getHand().addCardToHand(taken);
        return taken.getId();
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
