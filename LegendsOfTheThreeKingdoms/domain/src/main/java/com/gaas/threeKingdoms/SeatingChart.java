package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.MountsCard;
import com.gaas.threeKingdoms.player.Player;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SeatingChart {

    private List<Player> players;

    public SeatingChart(List<Player> players) {
        this.players = players;
    }

    public List<Player> getPlayers(){
        return players;
    }
    // Calculate the distance between two players
    public int calculateDistance(Player player, Player targetPlayer) {
        int seat1 = players.indexOf(player);
        int seat2 = players.indexOf(targetPlayer);

        int totalSeats = players.size();
        int minusOneMountCount = getMountsCount(player);
        int distanceClockwise = (seat2 - seat1 + totalSeats) % totalSeats;
        int distanceCounterClockwise = (seat1 - seat2 + totalSeats) % totalSeats;
        // Return the shorter distance of clockwise and counterclockwise
        return Math.min(distanceClockwise, distanceCounterClockwise) + minusOneMountCount;
    }

    private int getMountsCount(Player player) {
        if (player.getEquipment() == null) return 0;
        MountsCard minusOne = player.getEquipment().getMinusOne();
        if (minusOne == null){
            return 0;
        } else {
            return -1;
        }
    }

    public Player getNextPlayer(Player player) {
        int seat = (players.indexOf(player) + 1) % players.size();
        return players.get(seat);
    }

    public Player getPrePlayer(Player player) {
        int seat = (players.indexOf(player) - 1 + players.size()) % players.size();
        return players.get(seat);
    }
}
