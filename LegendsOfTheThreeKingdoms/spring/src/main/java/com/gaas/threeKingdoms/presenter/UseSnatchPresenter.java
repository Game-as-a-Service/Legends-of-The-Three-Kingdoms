package com.gaas.threeKingdoms.presenter;

import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.presenter.common.GameDataViewModel;
import com.gaas.threeKingdoms.presenter.common.PlayerDataViewModel;
import com.gaas.threeKingdoms.presenter.common.RoundDataViewModel;
import com.gaas.threeKingdoms.presenter.mapper.DomainEventToViewModelMapper;
import com.gaas.threeKingdoms.usecase.UseSnatchUseCase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static com.gaas.threeKingdoms.presenter.ViewModel.getEvent;

public class UseSnatchPresenter implements UseSnatchUseCase.UseSnatchPresenter<List<UseSnatchPresenter.GameViewModel>> {

    private final DomainEventToViewModelMapper domainEventToViewModelMapper = new DomainEventToViewModelMapper();
    private List<UseSnatchPresenter.GameViewModel> viewModels = new ArrayList<>();
    private List<ViewModel<?>> effectViewModels = new ArrayList<>();

    @Override
    public void renderEvents(List<DomainEvent> events) {
        GameStatusEvent gameStatusEvent = getEvent(events, GameStatusEvent.class).orElseThrow();
        effectViewModels = domainEventToViewModelMapper.mapEventsToViewModels(events);

        List<PlayerEvent> playerEvents = gameStatusEvent.getSeats();
        RoundEvent roundEvent = gameStatusEvent.getRound();
        List<PlayerDataViewModel> playerDataViewModels = playerEvents.stream().map(PlayerDataViewModel::new).toList();

        // 將回合資訊放入 RoundDataViewModel ，後續會放到 GameDataViewModel
        RoundDataViewModel roundDataViewModel = new RoundDataViewModel(roundEvent);

        for (PlayerDataViewModel viewModel : playerDataViewModels) {

            // 此 use case 的 data 物件
            GameDataViewModel gameDataViewModel = new GameDataViewModel(
                    PlayerDataViewModel.hiddenOtherPlayerRoleInformation(
                            playerDataViewModels, viewModel.getId()), roundDataViewModel, gameStatusEvent.getGamePhase());

            viewModels.add(new UseSnatchPresenter.GameViewModel(
                    effectViewModels,
                    gameDataViewModel,
                    gameStatusEvent.getMessage(),
                    gameStatusEvent.getGameId(),
                    viewModel.getId()));
        }
    }

    @Override
    public List<UseSnatchPresenter.GameViewModel> present() {
        return viewModels;
    }


    public static class UseSnatchViewModel extends ViewModel<UseSnatchPresenter.UseSnatchDataViewModel> {
        public UseSnatchViewModel(UseSnatchPresenter.UseSnatchDataViewModel data, String message) {
            super(" UseSnatchEvent", data, message);
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UseSnatchDataViewModel {
        private String playerId;
        private String targetPlayerId;
        private String cardId;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GameViewModel extends GameProcessViewModel<GameDataViewModel> {
        private String gameId;
        private String playerId;

        public GameViewModel(List<ViewModel<?>> viewModels, GameDataViewModel data, String message, String gameId, String playerId) {
            super(viewModels, data, message);
            this.gameId = gameId;
            this.playerId = playerId;
        }
    }
}