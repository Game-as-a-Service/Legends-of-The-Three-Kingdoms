package com.gaas.threeKingdoms.skill.wei;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.behavior.NormalActiveKillBehavior;
import com.gaas.threeKingdoms.behavior.behavior.WaitingJianXiongResponseBehavior;
import com.gaas.threeKingdoms.events.AskJianXiongEffectEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.skill.context.DamageContext;
import com.gaas.threeKingdoms.skill.trigger.OnDamagedSkill;

import java.util.List;

/**
 * 曹操 (WEI001) 奸雄
 * 身份標準版：當你受到 1 點傷害後，你可以獲得造成此傷害的牌。
 *
 * v1 範圍：
 *   - 只接受 sourceCard instanceof Kill（普通殺、虛擬殺、火攻轉殺等）
 *   - 多點傷害整次只觸發 1 次
 *   - 受傷者瀕死或非存活狀態時不觸發
 *   - sourceCard 已不在棄牌堆（被其他效果先取走）時不觸發
 *
 * TODO: 後續擴充
 *   - 決鬥傷害 (sourceCard 是 Duel scroll)
 *   - AOE 傷害（南蠻入侵 / 萬箭齊發 — sourceCard 是 scroll）
 *   - 雷殺 / 火殺 等 special Kill
 *   - 多點傷害多次觸發
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
            return List.of();
        }
        if (!(sourceCard instanceof Kill)) {
            return List.of();
        }

        Player damaged = ctx.damagedPlayer();
        if (!damaged.isHPGreaterThanZero()) {
            return List.of();
        }
        if (!game.getGraveyard().contains(sourceCard.getId())) {
            return List.of();
        }

        // 守門：v1 只支援 NormalActiveKillBehavior（含子類 ViperSpearKill / HeavenlyDoubleHalberd）
        // 上的單體 Kill 傷害觸發。對 AOE polling behavior（BarbarianInvasion / ArrowBarrage）
        // 或 Duel 等 caller，雖然 v1 因 sourceCard 過濾而不會走到這裡，但加守門防止未來
        // 加新 OnDamagedSkill 時誤把 polling 中的 behavior pop 掉。
        if (!(game.peekTopBehavior() instanceof NormalActiveKillBehavior)) {
            return List.of();
        }

        // 把 setIsOneRound(true) 的 kill behavior 先彈出，讓奸雄 behavior 成為 stack 頂端
        game.removeCompletedBehaviors();

        WaitingJianXiongResponseBehavior waiting = new WaitingJianXiongResponseBehavior(game, damaged, sourceCard.getId());
        game.updateTopBehavior(waiting);
        game.getCurrentRound().setActivePlayer(damaged);

        return List.of(new AskJianXiongEffectEvent(damaged.getId(), sourceCard.getId()));
    }
}
