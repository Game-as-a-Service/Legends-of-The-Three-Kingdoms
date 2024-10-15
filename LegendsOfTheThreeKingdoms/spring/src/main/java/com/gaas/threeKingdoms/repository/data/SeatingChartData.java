package com.gaas.threeKingdoms.repository.data;

import com.gaas.threeKingdoms.SeatingChart;
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

    private ArrayList<PlayerData> playerDataList;

    public static SeatingChartData fromDomain(SeatingChart seatingChart) {
        if (seatingChart.getPlayers() == null) {
            throw new IllegalArgumentException("PlayerList cannot be null");
        }

        return SeatingChartData.builder()
                .playerDataList(seatingChart.getPlayers().stream()
                        .map(PlayerData::fromDomain)
                        .collect(Collectors.toCollection(ArrayList::new)))
                .build();
    }

    public SeatingChart toDomain() {
        if (playerDataList == null) {
            throw new IllegalArgumentException("PlayerDataList cannot be null");
        }

        return new SeatingChart(this.playerDataList.stream()
                .map(PlayerData::toDomain)
                .collect(Collectors.toCollection(ArrayList::new)));
    }
}
