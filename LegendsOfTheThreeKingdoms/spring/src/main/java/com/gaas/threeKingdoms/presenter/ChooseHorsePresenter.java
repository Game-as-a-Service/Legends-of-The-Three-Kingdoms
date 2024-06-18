package com.gaas.threeKingdoms.presenter;

import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.GameStatusEvent;
import com.gaas.threeKingdoms.presenter.common.GameDataViewModel;
import com.gaas.threeKingdoms.usecase.ChooseHorseUseCase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.gaas.threeKingdoms.presenter.ViewModel.getEvent;

public class ChooseHorsePresenter implements ChooseHorseUseCase.ChooseHorsePresenter<List<ChooseHorsePresenter.GameViewModel>>{

    private List<GameViewModel> viewModels = new ArrayList<>();
    private List<ViewModel> effectViewModels = new ArrayList<>();

    @Override
    public void renderEvents(List<DomainEvent> events) {
        GameStatusEvent gameStatusEvent = getEvent(events, GameStatusEvent.class).orElseThrow();
//        ChooseHorseEffectViewModel chooseHorseEffectViewModel = getChooseHorseEffectEvent(events);
//        updateViewModels(gameStatusEvent);

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


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GameViewModel extends GameProcessViewModel<GameDataViewModel> {
        private String gameId;
        private String playerId;

        public GameViewModel(List<ViewModel> viewModels, GameDataViewModel data, String message, String gameId, String playerId) {
            super(viewModels, data, message);
            this.gameId = gameId;
            this.playerId = playerId;
        }
    }
}
