package com.gaas.threeKingdoms.skill.wei;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.behavior.behavior.ArrowBarrageBehavior;
import com.gaas.threeKingdoms.behavior.behavior.BarbarianInvasionBehavior;
import com.gaas.threeKingdoms.behavior.behavior.DuelBehavior;
import com.gaas.threeKingdoms.behavior.behavior.DyingAskPeachBehavior;
import com.gaas.threeKingdoms.behavior.behavior.LightningJudgementBehavior;
import com.gaas.threeKingdoms.behavior.behavior.NormalActiveKillBehavior;
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
 *   - 受傷者仍存活（HP > 0；瀕死或死亡不觸發）
 *   - top behavior 屬於白名單：NormalActiveKill / Duel / LightningJudgement /
 *     BarbarianInvasion / ArrowBarrage / 空 stack
 *
 * 取牌規則（FAQ）：
 *   - 一般殺 / 武器觸發殺 / 決鬥 / 閃電判定 / 南蠻入侵 / 萬箭齊發 → 獲得 sourceCard 本身
 *   - 丈八蛇矛攻擊 → 獲得攻擊者棄掉的兩張手牌（VirtualKill 不算）
 *   - 多點傷害整次只觸發 1 次
 *
 * AOE polling 整合機制：BarbarianInvasion / ArrowBarrage 在 doResponseToPlayerAction
 * 會偵測 WaitingJianXiongResponseBehavior 在 stack 頂，並把 polling-advance 註冊為
 * onResolved callback。等 JianXiong 解決後再 resume，避免 activePlayer 衝突。
 *
 * TODO（不在 v1）：
 *   - 致命傷被救回後仍可發動（dying flow 重構）
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

        // 守門：白名單 + 空 stack（Lightning 無 Ward 時 stack 空）
        //   - NormalActiveKillBehavior（含子類 ViperSpearKill / HeavenlyDoubleHalberd）：殺 / 武器觸發殺
        //   - DuelBehavior：決鬥輸給對手（Path A skip 與 Path B swap 都安全）
        //   - LightningJudgementBehavior：閃電判定打中（Ward 路徑）
        //   - BarbarianInvasionBehavior / ArrowBarrageBehavior：AOE polling
        //     （caller 偵測 WaitingJX 並把 polling-advance defer 為 callback，避免 activePlayer 衝突）
        //   - DyingAskPeachBehavior：致命傷被救回後 replay（FAQ）— DyingBehavior 在 revival
        //     branch 已 set isOneRound=true 才呼叫 SkillEngine，安全 pop
        //   - 空 stack：閃電判定無 Ward 路徑
        Behavior top = game.isTopBehaviorEmpty() ? null : game.peekTopBehavior();
        boolean topIsAllowed = top == null
                || top instanceof NormalActiveKillBehavior
                || top instanceof DuelBehavior
                || top instanceof LightningJudgementBehavior
                || top instanceof BarbarianInvasionBehavior
                || top instanceof ArrowBarrageBehavior
                || top instanceof DyingAskPeachBehavior;
        if (!topIsAllowed) {
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

        // 對 polling caller（BI / AB）保留底層 behavior — 等 WaitingJX 解決後 callback 會
        // resume polling。對其他 caller（Kill / Duel / Lightning）把 line 645 標的 isOneRound=true
        // 先 pop 掉，讓 WaitingJX 成為 stack 頂端。
        boolean isPollingCaller = top instanceof BarbarianInvasionBehavior
                || top instanceof ArrowBarrageBehavior;
        if (!isPollingCaller) {
            game.removeCompletedBehaviors();
        }

        WaitingJianXiongResponseBehavior waiting = new WaitingJianXiongResponseBehavior(game, damaged, takeIds);
        game.updateTopBehavior(waiting);
        game.getCurrentRound().setActivePlayer(damaged);

        return List.of(new AskJianXiongEffectEvent(damaged.getId(), takeIds));
    }
}
