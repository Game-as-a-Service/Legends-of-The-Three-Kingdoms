package com.gaas.threeKingdoms.skill.trigger;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.skill.Skill;

import java.util.List;
import java.util.Optional;

/**
 * 在 AskDodgeEvent 即將被 emit 時介入的技能 trigger（例：護駕）。
 *
 * 回傳語意：
 *   - Optional.empty()：不介入，caller 自行 emit 原本的 AskDodgeEvent
 *   - Optional.of(events)：已 push 替代 behavior 至 stack 並組好替代 ask 事件，caller 直接把
 *     events addAll 進回傳事件列表，跳過原本的 AskDodgeEvent
 */
public interface BeforeAskDodgeSkill extends Skill {
    Optional<List<DomainEvent>> beforeAskDodge(Game game, Player damaged, Behavior parentBehavior);
}
