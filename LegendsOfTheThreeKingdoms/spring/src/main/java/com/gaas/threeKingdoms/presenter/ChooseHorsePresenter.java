package com.gaas.threeKingdoms.presenter;

import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.presenter.common.GameDataViewModel;
import com.gaas.threeKingdoms.presenter.common.PlayerDataViewModel;
import com.gaas.threeKingdoms.presenter.common.RoundDataViewModel;
import com.gaas.threeKingdoms.presenter.mapper.DomainEventToViewModelMapper;
import com.gaas.threeKingdoms.usecase.ChooseHorseUseCase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.gaas.threeKingdoms.presenter.PlayCardPresenter.*;
import static com.gaas.threeKingdoms.presenter.ViewModel.getEvent;

public class ChooseHorsePresenter implements ChooseHorseUseCase.ChooseHorsePresenter<List<ChooseHorsePresenter.GameViewModel>>{

    private final DomainEventToViewModelMapper domainEventToViewModelMapper = new DomainEventToViewModelMapper();
    private List<GameViewModel> viewModels = new ArrayList<>();
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

            viewModels.add(new ChooseHorsePresenter.GameViewModel(
                    effectViewModels,
                    gameDataViewModel,
                    gameStatusEvent.getMessage(),
                    gameStatusEvent.getGameId(),
                    viewModel.getId()));
        }

    }


    @Override
    public List<ChooseHorsePresenter.GameViewModel> present() {
        return viewModels;
    }

    private void updateViewModels(ViewModel<?>... viewModels) {
        Arrays.stream(viewModels)
                .filter(Objects::nonNull)
                .forEach(effectViewModels::add);
    }

    public static class RemoveHorseViewModel extends ViewModel<RemoveHorseDataViewModel> {
        public RemoveHorseViewModel(RemoveHorseDataViewModel data) {
            super("RemoveHorseViewModel", data, "移除馬匹");
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RemoveHorseDataViewModel {
        private String playerId;
        private String mountCardId;
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
