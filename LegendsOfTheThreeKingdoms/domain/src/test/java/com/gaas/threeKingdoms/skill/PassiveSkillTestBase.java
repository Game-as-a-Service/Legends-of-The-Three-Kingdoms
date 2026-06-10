package com.gaas.threeKingdoms.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.player.*;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.rolecard.RoleCard;
import com.gaas.threeKingdoms.round.Round;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * Batch 1 被動技測試共用 setup：4 人場 a(回合主) → b → c → d。
 */
public abstract class PassiveSkillTestBase {

    protected Player buildPlayer(String id, General general, Role role) {
        return PlayerBuilder.construct()
                .withId(id)
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(general))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(role))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();
    }

    /** 4 人場：a = generalA（MONARCH、回合主）；b/c/d 可指定武將。 */
    protected Game createGame(General generalA, General generalB, General generalC, General generalD) {
        Game game = new Game();
        game.initDeck();
        Player a = buildPlayer("player-a", generalA, Role.MONARCH);
        Player b = buildPlayer("player-b", generalB, Role.MINISTER);
        Player c = buildPlayer("player-c", generalC, Role.REBEL);
        Player d = buildPlayer("player-d", generalD, Role.TRAITOR);
        game.setPlayers(asList(a, b, c, d));
        game.setCurrentRound(new Round(a));
        game.enterPhase(new Normal(game));
        return game;
    }

    protected List<Player> players(Game game) {
        return game.getPlayers();
    }
}
