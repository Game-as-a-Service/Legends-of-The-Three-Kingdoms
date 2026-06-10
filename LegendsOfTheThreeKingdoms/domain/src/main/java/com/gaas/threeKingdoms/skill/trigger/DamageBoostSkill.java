package com.gaas.threeKingdoms.skill.trigger;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.skill.Skill;

/**
 * 增加自己造成傷害的鎖定技（例：裸衣 — 自己回合內殺與決鬥傷害 +1）。
 */
public interface DamageBoostSkill extends Skill {
    int extraDamage(Game game, Player attacker, HandCard sourceCard);
}
