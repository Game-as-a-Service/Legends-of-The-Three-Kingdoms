package com.gaas.threeKingdoms.presenter;

import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.presenter.common.GameDataViewModel;
import com.gaas.threeKingdoms.presenter.common.PlayerDataViewModel;
import com.gaas.threeKingdoms.presenter.common.RoundDataViewModel;
import com.gaas.threeKingdoms.presenter.mapper.DomainEventToViewModelMapper;
import com.gaas.threeKingdoms.usecase.UseDismantleUseCase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.gaas.threeKingdoms.presenter.FinishActionPresenter.hiddenOtherPlayerCardIds;
import static com.gaas.threeKingdoms.presenter.ViewModel.getEvent;

public class UseDismantlePresenter implements UseDismantleUseCase.UseDismantlePresenter<List<UseDismantlePresenter.GameViewModel>> {
    private final DomainEventToViewModelMapper domainEventToViewModelMapper = new DomainEventToViewModelMapper();
    private List<UseDismantlePresenter.GameViewModel> viewModels = new ArrayList<>();
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

            List<ViewModel<?>> personalEventToViewModels = new ArrayList<>(effectViewModels);

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


            viewModels.add(new UseDismantlePresenter.GameViewModel(
                    personalEventToViewModels,
                    gameDataViewModel,
                    gameStatusEvent.getMessage(),
                    gameStatusEvent.getGameId(),
                    viewModel.getId()));
        }
    }

    private UseDismantlePresenter.UseDismantleViewModel getDismantleEvent(List<DomainEvent> events) {
        return getEvent(events, DismantleEvent.class)
                .map(event -> {
                    UseDismantlePresenter.UseDismantleDataViewModel useDismantleDataViewModel = new UseDismantlePresenter.UseDismantleDataViewModel(event.getPlayerId(), event.getTargetPlayerId(), event.getCardId());
                    return new UseDismantlePresenter.UseDismantleViewModel(useDismantleDataViewModel, event.getMessage());
                })
                .orElse(null);
    }

    @Override
    public List<UseDismantlePresenter.GameViewModel> present() {
        return viewModels;
    }


    public static class UseDismantleViewModel extends ViewModel<UseDismantlePresenter.UseDismantleDataViewModel> {
        public UseDismantleViewModel(UseDismantlePresenter.UseDismantleDataViewModel data, String message) {
            super("UseDismantleEvent", data, message);
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UseDismantleDataViewModel {
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