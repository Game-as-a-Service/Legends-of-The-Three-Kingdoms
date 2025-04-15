package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.scrollcard.BarbarianInvasion;
import com.gaas.threeKingdoms.player.*;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.rolecard.RoleCard;
import com.gaas.threeKingdoms.round.Round;

import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.SS7007;

//@Document
public class TestGame {
    private String title;
    private String genre;
    private int releaseYear;
    private HandCard card;

    // Constructor
    public TestGame(String title, String genre, int releaseYear) {
        card = new BarbarianInvasion(SS7007);
        this.title = title;
        this.genre = genre;
        this.releaseYear = releaseYear;
  }


    private Player createPlayer(String id, int hp, General general, HealthStatus healthStatus, Role role, HandCard... cards) {
        Player player = PlayerBuilder.construct()
                .withId(id)
                .withBloodCard(new BloodCard(hp))
                .withGeneralCard(new GeneralCard(general))
                .withHealthStatus(healthStatus)
                .withRoleCard(new RoleCard(role))
                .withEquipment(new Equipment())
                .withHand(new Hand())
                .build();

        player.getHand().addCardToHand(Arrays.asList(cards));
        return player;
    }

    private static Game initGame(String gameId, List<Player> players, Player currentRoundPlayer) {
//        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = new Game(gameId, players);
        game.setCurrentRound(new Round(currentRoundPlayer));
        game.enterPhase(new Normal(game));
        return game;
    }
}
