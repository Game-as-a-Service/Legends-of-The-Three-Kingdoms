package com.gaas.threeKingdoms.presenter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 連線狀態廣播 ViewModel（issue #239）— 純 spring 層，無對應 domain event。
 */
public class PlayerConnectionStatusPresenter {

    @Data
    @NoArgsConstructor
    public static class PlayerConnectionStatusViewModel extends ViewModel<PlayerConnectionStatusDataViewModel> {
        private String gameId;

        public PlayerConnectionStatusViewModel(String gameId, PlayerConnectionStatusDataViewModel data, String message) {
            super("PlayerConnectionStatusEvent", data, message);
            this.gameId = gameId;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlayerConnectionStatusDataViewModel {
        private List<String> connectedPlayerIds;
        private int connectedCount;
        private int totalCount;
    }
}
