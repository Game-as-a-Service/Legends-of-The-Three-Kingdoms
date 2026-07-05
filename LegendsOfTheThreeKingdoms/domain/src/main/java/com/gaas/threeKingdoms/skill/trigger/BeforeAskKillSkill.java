package com.gaas.threeKingdoms.skill.trigger;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.skill.Skill;

import java.util.List;
import java.util.Optional;

/**
 * 在 AskKillEvent 即將被 emit 時介入的技能 trigger（例：激將）。
 * 語意同 {@link BeforeAskDodgeSkill}：empty = 不介入；present = 已 push 替代 behavior + 替代事件。
 */
public interface BeforeAskKillSkill extends Skill {
    Optional<List<DomainEvent>> beforeAskKill(Game game, Player asked, Behavior parentBehavior);
}
