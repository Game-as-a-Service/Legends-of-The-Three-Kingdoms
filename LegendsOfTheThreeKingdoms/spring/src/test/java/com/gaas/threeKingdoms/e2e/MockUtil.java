package com.gaas.threeKingdoms.e2e;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.round.Round;
import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.player.*;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.rolecard.RoleCard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MockUtil {
     public static Player createPlayer(String id, int hp, General general, HealthStatus healthStatus, Role role, HandCard... cards) {
        Player player = PlayerBuilder.construct()
                .withId(id)
                .withBloodCard(new BloodCard(hp))
                .withGeneralCard(new GeneralCard(general))
                .withHealthStatus(healthStatus)
                .withRoleCard(new RoleCard(role))
                .withEquipment(new Equipment())
                .withHand(new Hand())
                .withDelayScrollCards(new ArrayList<>())
                .build();

        player.getHand().addCardToHand(Arrays.asList(cards));
        return player;
    }

    public static Game initGame(String gameId, List<Player> players, Player currentRoundPlayer) {
        Game game = new Game(gameId, players);
        game.setCurrentRound(new Round(currentRoundPlayer));
        game.enterPhase(new Normal(game));
        Deck deck = new Deck();
        deck.init();
        game.setDeck(deck);
        return game;
    }
}
