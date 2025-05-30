package com.gaas.threeKingdoms.presenter;

import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.GameStatusEvent;
import com.gaas.threeKingdoms.events.RoundEvent;
import com.gaas.threeKingdoms.events.WaitForWardEvent;
import com.gaas.threeKingdoms.presenter.common.GameDataViewModel;
import com.gaas.threeKingdoms.presenter.common.PlayerDataViewModel;
import com.gaas.threeKingdoms.presenter.common.RoundDataViewModel;
import com.gaas.threeKingdoms.presenter.mapper.DomainEventToViewModelMapper;
import com.gaas.threeKingdoms.usecase.PlayWardCardUseCase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.gaas.threeKingdoms.presenter.FinishActionPresenter.hiddenOtherPlayerCardIds;
import static com.gaas.threeKingdoms.presenter.ViewModel.getEvent;

public class PlayWardCardPresenter implements PlayWardCardUseCase.PlayWardCardPresenter<List<PlayWardCardPresenter.GameViewModel>> {

    private List<ViewModel<?>> eventToViewModels = new ArrayList<>();
    private List<PlayWardCardPresenter.GameViewModel> viewModels = new ArrayList<>();
    private final DomainEventToViewModelMapper domainEventToViewModelMapper = new DomainEventToViewModelMapper();

    @Override
    public void renderEvents(List<DomainEvent> events) {
        eventToViewModels = domainEventToViewModelMapper.mapEventsToViewModels(events);

        GameStatusEvent gameStatusEvent = getEvent(events, GameStatusEvent.class).orElseThrow(RuntimeException::new);

        List<PlayerDataViewModel> playerDataViewModels = gameStatusEvent.getSeats().stream().map(PlayerDataViewModel::new).toList();
        RoundEvent roundEvent = gameStatusEvent.getRound();

        // 將回合資訊放入 RoundDataViewModel ，後續會放到 GameDataViewModel
        RoundDataViewModel roundDataViewModel = new RoundDataViewModel(roundEvent);

        for (PlayerDataViewModel viewModel : playerDataViewModels) {

            // 此 use case 的 data 物件
            GameDataViewModel gameDataViewModel = new GameDataViewModel(
                    PlayerDataViewModel.hiddenOtherPlayerRoleInformation(
                            playerDataViewModels, viewModel.getId()), roundDataViewModel, gameStatusEvent.getGamePhase());

            List<ViewModel<?>> personalEventToViewModels = new ArrayList<>(eventToViewModels);

            personalEventToViewModels = personalEventToViewModels.stream().map(personalViewModel -> {
                if (personalViewModel instanceof RoundStartPresenter.DrawCardViewModel drawCardViewModel) {
                    personalViewModel = hiddenOtherPlayerCardIds(drawCardViewModel.getData(), viewModel, drawCardViewModel.getData().getDrawCardPlayerId());
                } else if (personalViewModel instanceof PlayCardPresenter.WaitForWardViewModel waitForWardViewModel) {
                    WaitForWardEvent waitForWardEvent = getEvent(events, WaitForWardEvent.class).orElseThrow(RuntimeException::new);
                    if (waitForWardEvent.getPlayerIds().contains(viewModel.getId())) {
                        personalViewModel = new PlayCardPresenter.AskPlayWardViewModel(waitForWardViewModel.getData());
                    }
                }
                return personalViewModel;
            }).collect(Collectors.toList());

            viewModels.add(new PlayWardCardPresenter.GameViewModel(
                    personalEventToViewModels,
                    gameDataViewModel,
                    gameStatusEvent.getMessage(),
                    gameStatusEvent.getGameId(),
                    viewModel.getId()));
        }
    }

    @Override
    public List<PlayWardCardPresenter.GameViewModel> present() {
        return viewModels;
    }


    @Data
    public static class PlayWardCardViewModel extends ViewModel<PlayWardCardPresenter.PlayWardCardDataViewModel> {
        public PlayWardCardViewModel(PlayWardCardPresenter.PlayWardCardDataViewModel data, String eventMessage) {
            super("PlayWardCardEvent", data, eventMessage);
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PlayWardCardDataViewModel {
        private String playerId;
        private String cardId;
        private String wardCardId;
    }

    @Data
    public static class SkipWardCardViewModel extends ViewModel<PlayWardCardPresenter.SkipWardCardDataViewModel> {
        public SkipWardCardViewModel(PlayWardCardPresenter.SkipWardCardDataViewModel data, String eventMessage) {
            super("SkipWardEvent", data, eventMessage);
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SkipWardCardDataViewModel {
        private String playerId;
        private String wardCardId;
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

