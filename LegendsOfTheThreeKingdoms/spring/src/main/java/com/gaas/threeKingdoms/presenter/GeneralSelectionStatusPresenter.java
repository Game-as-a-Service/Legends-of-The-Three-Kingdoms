package com.gaas.threeKingdoms.presenter;

import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.GeneralSelectionStatusEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import static com.gaas.threeKingdoms.presenter.ViewModel.getEvent;

/**
 * 選將進度廣播 presenter（issue #237）— 每次有人選完武將（含主公）廣播全員。
 * 事件不存在時 present() 回 null（broadcast 端跳過）。
 */
public class GeneralSelectionStatusPresenter
        implements com.gaas.threeKingdoms.usecase.OthersChoosePlayerGeneralUseCase.SelectionStatusPresenter<GeneralSelectionStatusPresenter.GeneralSelectionStatusViewModel> {

    private GeneralSelectionStatusViewModel viewModel;

    @Override
    public void renderEvents(List<DomainEvent> events) {
        viewModel = getEvent(events, GeneralSelectionStatusEvent.class)
                .map(e -> new GeneralSelectionStatusViewModel(
                        e.getGameId(), e.getPlayerIds(),
                        new GeneralSelectionStatusDataViewModel(
                                e.getSelectedPlayerIds(), e.getPendingPlayerIds(),
                                e.getSelectedPlayerIds().size(), e.getPlayerIds().size(),
                                e.isAllSelected()),
                        e.getMessage()))
                .orElse(null);
    }

    @Override
    public GeneralSelectionStatusViewModel present() {
        return viewModel;
    }

    @Data
    @NoArgsConstructor
    public static class GeneralSelectionStatusViewModel extends ViewModel<GeneralSelectionStatusDataViewModel> {
        private String gameId;
        private List<String> playerIds;

        public GeneralSelectionStatusViewModel(String gameId, List<String> playerIds,
                                               GeneralSelectionStatusDataViewModel data, String message) {
            super("GeneralSelectionStatusEvent", data, message);
            this.gameId = gameId;
            this.playerIds = playerIds;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeneralSelectionStatusDataViewModel {
        private List<String> selectedPlayerIds;
        private List<String> pendingPlayerIds;
        private int selectedCount;
        private int totalCount;
        private boolean allSelected;
    }
}
