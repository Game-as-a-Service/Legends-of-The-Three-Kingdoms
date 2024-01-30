package com.gaas.threeKingdoms.presenter;

import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.InitialEndEvent;
import com.gaas.threeKingdoms.events.RoundEvent;
import com.gaas.threeKingdoms.presenter.common.GameDataViewModel;
import com.gaas.threeKingdoms.presenter.common.PlayerDataViewModel;
import com.gaas.threeKingdoms.presenter.common.RoundDataViewModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import com.gaas.threeKingdoms.usecase.OthersChoosePlayerGeneralUseCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
@Data
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
        InitialEndEvent event = ViewModel.getEvent(events, InitialEndEvent.class).orElseThrow(RuntimeException::new);
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

    @EqualsAndHashCode(callSuper = true)
    @Data
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
