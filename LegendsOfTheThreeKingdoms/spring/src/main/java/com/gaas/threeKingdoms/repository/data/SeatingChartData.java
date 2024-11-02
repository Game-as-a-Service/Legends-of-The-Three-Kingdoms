package com.gaas.threeKingdoms.repository.data;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.SeatingChart;
import com.gaas.threeKingdoms.player.Player;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatingChartData {

    private ArrayList<String> playerList;

    public static SeatingChartData fromDomain(SeatingChart seatingChart) {
        if (seatingChart.getPlayers() == null) {
            throw new IllegalArgumentException("PlayerList cannot be null");
        }

        return SeatingChartData.builder()
                .playerList(seatingChart.getPlayers().stream()
                        .map(Player::getId)
                        .collect(Collectors.toCollection(ArrayList::new)))
                .build();
    }

    public SeatingChart toDomain(Game game) {
        if (playerList == null) {
            throw new IllegalArgumentException("PlayerDataList cannot be null");
        }

        return new SeatingChart(this.playerList.stream()
                .map(game::getPlayer)
                .collect(Collectors.toCollection(ArrayList::new)));
    }
}
