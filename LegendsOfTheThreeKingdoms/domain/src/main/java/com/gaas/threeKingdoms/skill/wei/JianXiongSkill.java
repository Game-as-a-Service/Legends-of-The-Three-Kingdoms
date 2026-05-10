package com.gaas.threeKingdoms.skill.wei;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
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
 *   - top behavior 為 NormalActiveKillBehavior（含子類 ViperSpearKill / HeavenlyDoubleHalberdKill）
 *
 * 取牌規則（FAQ）：
 *   - 一般殺 / 錦囊（閃電 / 南蠻 / 萬箭 / 決鬥）/ 武器觸發殺 → 獲得 sourceCard 本身
 *   - 丈八蛇矛攻擊 → 獲得攻擊者棄掉的兩張手牌（VirtualKill 不算）
 *   - 多點傷害整次只觸發 1 次
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

        // 守門：v1 只支援 NormalActiveKillBehavior（含子類 ViperSpearKill / HeavenlyDoubleHalberd）
        // 上的單體 Kill 傷害觸發。對 AOE polling behavior（BarbarianInvasion / ArrowBarrage）
        // 或 Duel 等 caller，因 polling 中不會走到這裡，加守門防止未來
        // 加新 OnDamagedSkill 時誤把 polling 中的 behavior pop 掉。
        Behavior top = game.peekTopBehavior();
        if (!(top instanceof NormalActiveKillBehavior)) {
            return List.of();
        }

        // 決定要拿哪些牌：
        //   - 丈八蛇矛 (VirtualKill + ViperSpearKillBehavior) → 兩張棄牌（FAQ 特例）
        //   - 其他（普通殺 / 錦囊 / 一般武器觸發殺）→ sourceCard 本身
        List<String> takeIds;
        if (sourceCard instanceof VirtualKill && top instanceof ViperSpearKillBehavior viper) {
            takeIds = viper.getDiscardedCardIds();
            // 全有或全無：兩張都要還在墓地（中途被別的效果拿走時整體跳過）
            if (takeIds.isEmpty() || !takeIds.stream().allMatch(id -> game.getGraveyard().contains(id))) {
                return List.of();
            }
        } else if (game.getGraveyard().contains(sourceCard.getId())) {
            takeIds = List.of(sourceCard.getId());
        } else {
            return List.of();
        }

        // 把 setIsOneRound(true) 的 kill behavior 先彈出，讓奸雄 behavior 成為 stack 頂端
        game.removeCompletedBehaviors();

        WaitingJianXiongResponseBehavior waiting = new WaitingJianXiongResponseBehavior(game, damaged, takeIds);
        game.updateTopBehavior(waiting);
        game.getCurrentRound().setActivePlayer(damaged);

        return List.of(new AskJianXiongEffectEvent(damaged.getId(), takeIds));
    }
}
