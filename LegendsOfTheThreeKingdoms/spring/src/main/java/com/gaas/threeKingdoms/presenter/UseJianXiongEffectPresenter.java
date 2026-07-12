package com.gaas.threeKingdoms.presenter;

import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.presenter.common.GameDataViewModel;
import com.gaas.threeKingdoms.presenter.common.PlayerDataViewModel;
import com.gaas.threeKingdoms.presenter.common.RoundDataViewModel;
import com.gaas.threeKingdoms.presenter.mapper.DomainEventToViewModelMapper;
import com.gaas.threeKingdoms.usecase.UseJianXiongEffectUseCase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static com.gaas.threeKingdoms.presenter.ViewModel.getEvent;

public class UseJianXiongEffectPresenter implements UseJianXiongEffectUseCase.UseJianXiongEffectPresenter<List<UseJianXiongEffectPresenter.GameViewModel>> {

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

            // issue #214：奸雄解決後南蠻/萬箭輪詢推進，可能產生對下一家的無懈可擊詢問
            viewModels.add(new GameViewModel(
                    PlayCardPresenter.personalizeWardViewModels(effectViewModels, events, viewModel.getId()),
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

    public static class AskJianXiongEffectViewModel extends ViewModel<AskJianXiongEffectDataViewModel> {
        public AskJianXiongEffectViewModel(AskJianXiongEffectDataViewModel data) {
            super("AskJianXiongEffectEvent", data, "奸雄：是否獲得造成傷害的牌");
        }
    }

    public static class JianXiongEffectViewModel extends ViewModel<JianXiongEffectDataViewModel> {
        public JianXiongEffectViewModel(JianXiongEffectDataViewModel data) {
            super("JianXiongEffectEvent", data, "奸雄發動");
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AskJianXiongEffectDataViewModel {
        private String playerId;
        private java.util.List<String> sourceCardIds;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class JianXiongEffectDataViewModel {
        private String playerId;
        private java.util.List<String> sourceCardIds;
        private boolean taken;
    }
}
