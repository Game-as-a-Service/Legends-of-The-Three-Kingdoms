package com.waterball.LegendsOfTheThreeKingdoms.domain;

import com.waterball.LegendsOfTheThreeKingdoms.domain.player.Player;

import java.util.List;

public class SeatingChart {

    private List<Player> players;
    private int head;

    public SeatingChart(List<Player> players) {
        this.players = players;
    }

    // Calculate the distance between two players
    public int calculateDistance(Player player, Player targetPlayer) {
        int seat1 = players.indexOf(player);
        int seat2 = players.indexOf(targetPlayer);

        int totalSeats = players.size();
        int distanceClockwise = (seat2 - seat1 + totalSeats) % totalSeats;
        int distanceCounterClockwise = (seat1 - seat2 + totalSeats) % totalSeats;
        // Return the shorter distance of clockwise and counterclockwise
        return Math.min(distanceClockwise, distanceCounterClockwise);
    }



}
