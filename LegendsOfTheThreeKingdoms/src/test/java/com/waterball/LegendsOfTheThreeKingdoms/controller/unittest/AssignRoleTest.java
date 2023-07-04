package com.waterball.LegendsOfTheThreeKingdoms.controller.unittest;

import com.waterball.LegendsOfTheThreeKingdoms.domain.Game;
import com.waterball.LegendsOfTheThreeKingdoms.domain.builders.Players;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.Player;
import com.waterball.LegendsOfTheThreeKingdoms.domain.rolecard.RoleCard;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

public class AssignRoleTest {

    @Test
    void givenFourPlayers_WhenAssignRole_ThenOneMonarchOneMinisterOneRebelOneTraitors() {
        //Given
        var game = new Game();
        List<Player> players = asList(
                Players.defaultPlayer("player-a"),
                Players.defaultPlayer("player-b"),
                Players.defaultPlayer("player-c"),
                Players.defaultPlayer("player-d")
        );
        game.setPlayers(players);

        // when
        game.assignRoles();

        // then
        Set<RoleCard> roleCards = Arrays.stream(RoleCard.ROLES.get(4)).collect(Collectors.toSet());
        Assertions.assertTrue(
                game.getPlayers().stream().map(Player::getRoleCard)
                        .collect(Collectors.toSet())
                        .equals(roleCards));
    }

    @Test
    void givenThreePlayers_WhenAssignRoles_ThenThrowException() {
        //Given
        var game = new Game();
        List<Player> players = asList(
                Players.defaultPlayer("player-a"),
                Players.defaultPlayer("player-b"),
                Players.defaultPlayer("player-c")
        );
        game.setPlayers(players);

        // when
        Assertions.assertThrows(RuntimeException.class,
                game::assignRoles);
    }

    @Test
    void givenFourPlayers_WhenAssignRole_EveryOneShouldHaveOne() {
        //Given
        var game = new Game();
        List<Player> players = asList(
                Players.defaultPlayer("player-a"),
                Players.defaultPlayer("player-b"),
                Players.defaultPlayer("player-c"),
                Players.defaultPlayer("player-d")
        );
        game.setPlayers(players);

        // when
        game.assignRoles();

        // then
        Assertions.assertTrue(game.getPlayer("player-a").getRoleCard() != null);
        Assertions.assertTrue(game.getPlayer("player-b").getRoleCard() != null);
        Assertions.assertTrue(game.getPlayer("player-c").getRoleCard() != null);
        Assertions.assertTrue(game.getPlayer("player-d").getRoleCard() != null);
    }

}
