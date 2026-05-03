package com.gaas.threeKingdoms.skill.trigger;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.skill.Skill;
import com.gaas.threeKingdoms.skill.context.DamageContext;

import java.util.List;

public interface OnDamagedSkill extends Skill {
    /**
     * 玩家受到傷害後觸發。回傳要 append 到 events 的 DomainEvent 列表。
     * 若需要互動（ASK），由 skill 自行 push WaitingXxxBehavior 到 game.behaviorStack。
     */
    List<DomainEvent> onDamaged(Game game, DamageContext ctx);
}
