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
        return SeatingChartData.builder()
                .playerDataList(seatingChart.getPlayers() != null ?
                        seatingChart.getPlayers().stream()
                                .map(PlayerData::fromDomain)
                                .collect(Collectors.toCollection(ArrayList::new))
                        : new ArrayList<>())
                .build();
    }

    public SeatingChart toDomain() {
        return new SeatingChart(this.playerDataList != null ?
                this.playerDataList.stream()
                        .map(PlayerData::toDomain)
                        .collect(Collectors.toCollection(ArrayList::new))
                : new ArrayList<>());
    }
}
