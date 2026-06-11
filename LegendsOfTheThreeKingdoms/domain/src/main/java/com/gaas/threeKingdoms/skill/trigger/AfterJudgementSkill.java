package com.gaas.threeKingdoms.skill.trigger;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.skill.Skill;

import java.util.List;

/**
 * 自己的判定牌生效後觸發（例：天妒 — 收判定牌入手）。
 * judgementCard 此時已在墓地（drawCardForCardEffect 的行為）。
 */
public interface AfterJudgementSkill extends Skill {
    List<DomainEvent> afterJudgement(Game game, Player judgementOwner, HandCard judgementCard);
}
