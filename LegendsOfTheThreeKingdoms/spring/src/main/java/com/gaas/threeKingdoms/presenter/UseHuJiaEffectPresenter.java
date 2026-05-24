package com.gaas.threeKingdoms.presenter;

import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.presenter.common.GameDataViewModel;
import com.gaas.threeKingdoms.presenter.common.PlayerDataViewModel;
import com.gaas.threeKingdoms.presenter.common.RoundDataViewModel;
import com.gaas.threeKingdoms.presenter.mapper.DomainEventToViewModelMapper;
import com.gaas.threeKingdoms.usecase.UseHuJiaEffectUseCase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static com.gaas.threeKingdoms.presenter.ViewModel.getEvent;

public class UseHuJiaEffectPresenter implements UseHuJiaEffectUseCase.UseHuJiaEffectPresenter<List<UseHuJiaEffectPresenter.GameViewModel>> {

    private List<GameViewModel> viewModels = new ArrayList<>();
    private final DomainEventToViewModelMapper domainEventToViewModelMapper = new DomainEventToViewModelMapper();

    @Override
    public void renderEvents(List<DomainEvent> events) {
        List<ViewModel<?>> effectViewModels = domainEventToViewModelMapper.mapEventsToViewModels(events);

        GameStatusEvent gameStatusEvent = getEvent(events, GameStatusEvent.class).orElseThrow();
        List<PlayerEvent> playerEvents = gameStatusEvent.getSeats();
        RoundEvent roundEvent = gameStatusEvent.getRound();
        List<PlayerDataViewModel> playerDataViewModels = playerEvents.stream().map(PlayerDataViewModel::new).toList();

        RoundDataViewModel roundDataViewModel = new RoundDataViewModel(roundEvent);

        for (PlayerDataViewModel viewModel : playerDataViewModels) {
            GameDataViewModel gameDataViewModel = new GameDataViewModel(
                    PlayerDataViewModel.hiddenOtherPlayerRoleInformation(
                            playerDataViewModels, viewModel.getId()), roundDataViewModel, gameStatusEvent.getGamePhase());

            viewModels.add(new GameViewModel(
                    new ArrayList<>(effectViewModels),
                    gameDataViewModel,
                    gameStatusEvent.getMessage(),
                    gameStatusEvent.getGameId(),
                    viewModel.getId()));
        }
    }

    @Override
    public List<GameViewModel> present() {
        return viewModels;
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

    public static class AskHuJiaEffectViewModel extends ViewModel<AskHuJiaEffectDataViewModel> {
        public AskHuJiaEffectViewModel(AskHuJiaEffectDataViewModel data) {
            super("AskHuJiaEffectEvent", data, "護駕：是否代替主公出閃");
        }
    }

    public static class HuJiaEffectViewModel extends ViewModel<HuJiaEffectDataViewModel> {
        public HuJiaEffectViewModel(HuJiaEffectDataViewModel data) {
            super("HuJiaEffectEvent", data, "護駕回應");
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AskHuJiaEffectDataViewModel {
        private String playerId;
        private String caoCaoPlayerId;
        private List<String> dodgeCardIdsInHand;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class HuJiaEffectDataViewModel {
        private String playerId;
        private String caoCaoPlayerId;
        private boolean accepted;
        private String dodgeCardId;
    }
}
