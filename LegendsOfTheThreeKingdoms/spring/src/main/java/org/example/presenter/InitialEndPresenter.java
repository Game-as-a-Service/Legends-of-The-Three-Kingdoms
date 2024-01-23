package org.example.presenter;

import org.gaas.domain.events.*;
import org.example.presenter.common.GameDataViewModel;
import org.example.presenter.common.RoundDataViewModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.example.presenter.common.PlayerDataViewModel;
import org.gaas.usecase.OthersChoosePlayerGeneralUseCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.example.presenter.ViewModel.getEvent;

public class InitialEndPresenter implements OthersChoosePlayerGeneralUseCase.InitialEndPresenter<List<InitialEndPresenter.InitialEndViewModel>> {

    private List<InitialEndViewModel> initialEndViewModels;

    public void renderEvents(List<DomainEvent> events) {
        if (!events.isEmpty()) {
            updateInitialEventToViewModel(events);
        } else {
            initialEndViewModels = Collections.emptyList();
        }
    }

    public void updateInitialEventToViewModel(List<DomainEvent> events) {
        initialEndViewModels = new ArrayList<>();
        InitialEndEvent event = getEvent(events, InitialEndEvent.class).orElseThrow(RuntimeException::new);
        List<PlayerDataViewModel> playerDataViewModels = event.getSeats().stream().map(PlayerDataViewModel::new).collect(Collectors.toList());

        RoundEvent roundEvent = event.getRound();
        RoundDataViewModel roundDataViewModel = new RoundDataViewModel(roundEvent);

        for (PlayerDataViewModel viewModel : playerDataViewModels) {
            GameDataViewModel gameDataViewModel = new GameDataViewModel(
                    PlayerDataViewModel.hiddenOtherPlayerRoleInformation(playerDataViewModels, viewModel.getId()),
                    roundDataViewModel,
                    event.getGamePhase());
            initialEndViewModels.add(new InitialEndViewModel(event.getGameId(), gameDataViewModel, "", viewModel.getId()));
        }
    }


    public List<InitialEndViewModel> present() {
        return initialEndViewModels;
    }

    @Data
    @NoArgsConstructor
    public static class InitialEndViewModel extends ViewModel<GameDataViewModel> {
        private String gameId;
        private String playerId;

        public InitialEndViewModel(String gameId, GameDataViewModel data, String message, String playerId) {
            super("initialEndViewModel", data, message);
            this.gameId = gameId;
            this.playerId = playerId;
        }
    }


}
