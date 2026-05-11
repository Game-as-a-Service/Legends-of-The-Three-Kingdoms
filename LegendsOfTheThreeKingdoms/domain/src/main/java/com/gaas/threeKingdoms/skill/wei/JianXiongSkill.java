package com.gaas.threeKingdoms.skill.wei;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.behavior.JianXiongCompatibleTopBehavior;
import com.gaas.threeKingdoms.behavior.behavior.DyingAskPeachBehavior;
import com.gaas.threeKingdoms.behavior.behavior.ViperSpearKillBehavior;
import com.gaas.threeKingdoms.behavior.behavior.WaitingJianXiongResponseBehavior;
import com.gaas.threeKingdoms.events.AskJianXiongEffectEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.basiccard.VirtualKill;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.skill.context.DamageContext;
import com.gaas.threeKingdoms.skill.trigger.OnDamagedSkill;

import java.util.List;

/**
 * 曹操 (WEI001) 奸雄
 * 身份標準版：當你受到 1 點傷害後，你可以獲得造成此傷害的牌。
 *
 * 觸發範圍：
 *   - sourceCard != null（武將技直接傷害如 剛烈 / 離間 / 雷擊 sourceCard 為 null，不觸發）
 *   - 受傷者仍存活（HP > 0；瀕死流程結束後若被救回會 replay）
 *   - top behavior 實作 {@link JianXiongCompatibleTopBehavior}（或為空 stack）
 *
 * 取牌規則（FAQ）：
 *   - 一般殺 / 武器觸發殺 / 決鬥 / 閃電判定 / 南蠻入侵 / 萬箭齊發 → 獲得 sourceCard 本身
 *   - 丈八蛇矛攻擊 → 獲得攻擊者棄掉的兩張手牌（VirtualKill 不算）
 *   - 致命傷被救回 → DyingAskPeachBehavior revival 分支 replay；ViperSpear 致命走兩張棄牌特例
 *   - 多點傷害整次只觸發 1 次
 *
 * AOE polling 整合機制：caller {@link JianXiongCompatibleTopBehavior#isPollingCaller()}
 * 為 true 時（BarbarianInvasion / ArrowBarrage），caller 端會偵測 WaitingJianXiongResponseBehavior
 * 在 stack 頂並把 polling-advance 註冊為 onResolved callback，避免 activePlayer 衝突。
 */
public class JianXiongSkill implements OnDamagedSkill {

    public static final String GENERAL_ID = General.曹操.getGeneralId();
    public static final String SKILL_NAME = "奸雄";

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
        HandCard sourceCard = ctx.sourceCard();
        if (sourceCard == null) {
            // 武將技直接傷害（剛烈 / 離間 / 雷擊 等）— 規則：不獲得任何牌
            return List.of();
        }

        Player damaged = ctx.damagedPlayer();
        if (!damaged.isHPGreaterThanZero()) {
            return List.of();
        }

        // 守門：top 必須是 JianXiongCompatibleTopBehavior 或空 stack（Lightning 無 Ward 時 stack 空）。
        // 各 behavior 自己聲明能否 host JianXiong；新增 behavior 不需回頭改本檔。
        Behavior top = game.isTopBehaviorEmpty() ? null : game.peekTopBehavior();
        if (top != null && !(top instanceof JianXiongCompatibleTopBehavior)) {
            return List.of();
        }

        // 決定要拿哪些牌：
        //   - 丈八蛇矛 alive (VirtualKill + ViperSpearKillBehavior) → 兩張棄牌
        //   - 丈八蛇矛 致命 + revive (VirtualKill + DyingAskPeachBehavior) → 兩張棄牌（從 pending 取）
        //   - 其他（普通殺 / 錦囊 / 一般武器觸發殺）→ sourceCard 本身
        List<String> takeIds;
        if (sourceCard instanceof VirtualKill) {
            if (top instanceof ViperSpearKillBehavior viper) {
                takeIds = viper.getDiscardedCardIds();
            } else if (top instanceof DyingAskPeachBehavior dying
                    && dying.getPendingViperSpearDiscardCardIds() != null) {
                takeIds = dying.getPendingViperSpearDiscardCardIds();
            } else {
                return List.of();
            }
            // 全有或全無：兩張都要還在墓地（中途被別的效果拿走時整體跳過）
            if (takeIds.isEmpty() || !takeIds.stream().allMatch(id -> game.getGraveyard().contains(id))) {
                return List.of();
            }
        } else if (game.getGraveyard().contains(sourceCard.getId())) {
            takeIds = List.of(sourceCard.getId());
        } else {
            return List.of();
        }

        // polling caller（BI / AB）需保留底層 behavior 等 callback resume；其他 caller 可直接
        // pop（getDamagedEvent line 645 已標 isOneRound=true）。由 marker 上的方法決定。
        boolean isPollingCaller = top instanceof JianXiongCompatibleTopBehavior c && c.isPollingCaller();
        if (!isPollingCaller) {
            game.removeCompletedBehaviors();
        }

        WaitingJianXiongResponseBehavior waiting = new WaitingJianXiongResponseBehavior(game, damaged, takeIds);
        game.updateTopBehavior(waiting);
        game.getCurrentRound().setActivePlayer(damaged);

        return List.of(new AskJianXiongEffectEvent(damaged.getId(), takeIds));
    }
}
