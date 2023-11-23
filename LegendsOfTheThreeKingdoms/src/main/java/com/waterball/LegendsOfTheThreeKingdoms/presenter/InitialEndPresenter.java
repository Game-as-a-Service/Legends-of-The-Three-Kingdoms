package com.waterball.LegendsOfTheThreeKingdoms.presenter;

import com.waterball.LegendsOfTheThreeKingdoms.domain.events.*;
import com.waterball.LegendsOfTheThreeKingdoms.presenter.common.GameDataViewModel;
import com.waterball.LegendsOfTheThreeKingdoms.presenter.common.RoundDataViewModel;
import com.waterball.LegendsOfTheThreeKingdoms.service.GameService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.waterball.LegendsOfTheThreeKingdoms.presenter.common.PlayerDataViewModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.waterball.LegendsOfTheThreeKingdoms.presenter.ViewModel.getEvent;

public class InitialEndPresenter implements GameService.Presenter<List<InitialEndPresenter.InitialEndViewModel>> {

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
