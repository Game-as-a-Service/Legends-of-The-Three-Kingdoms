package com.gaas.threeKingdoms.presenter;

import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.presenter.common.GameDataViewModel;
import com.gaas.threeKingdoms.presenter.common.PlayerDataViewModel;
import com.gaas.threeKingdoms.presenter.common.RoundDataViewModel;
import com.gaas.threeKingdoms.presenter.mapper.DomainEventToViewModelMapper;
import com.gaas.threeKingdoms.usecase.UseSkillEffectUseCase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static com.gaas.threeKingdoms.presenter.ViewModel.getEvent;

public class UseSkillEffectPresenter implements UseSkillEffectUseCase.UseSkillEffectPresenter<List<UseSkillEffectPresenter.GameViewModel>> {

    private List<GameViewModel> viewModels = new ArrayList<>();
    private final DomainEventToViewModelMapper domainEventToViewModelMapper = new DomainEventToViewModelMapper();

    @Override
    public void renderEvents(List<DomainEvent> events) {
        List<ViewModel<?>> effectViewModels = domainEventToViewModelMapper.mapEventsToViewModels(events);

        // 取最後一個 GameStatusEvent = 技能 resolve + 後續推進（判定 resume / 輪詢 resume）後的最終狀態；
        // 第一個可能是推進前的快照（activePlayer 仍為被詢問者）
        GameStatusEvent gameStatusEvent = lastGameStatusEvent(events);
        List<PlayerEvent> playerEvents = gameStatusEvent.getSeats();
        RoundEvent roundEvent = gameStatusEvent.getRound();
        List<PlayerDataViewModel> playerDataViewModels = playerEvents.stream().map(PlayerDataViewModel::new).toList();

        RoundDataViewModel roundDataViewModel = new RoundDataViewModel(roundEvent);

        for (PlayerDataViewModel viewModel : playerDataViewModels) {
            GameDataViewModel gameDataViewModel = new GameDataViewModel(
                    PlayerDataViewModel.hiddenOtherPlayerRoleInformation(
                            playerDataViewModels, viewModel.getId()), roundDataViewModel, gameStatusEvent.getGamePhase());

            // issue #214：技能結算可能推進到無懈可擊詢問（如奸雄/激將 ACCEPT 後南蠻輪詢下一家）
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

    public static class AskSkillEffectViewModel extends ViewModel<AskSkillEffectDataViewModel> {
        public AskSkillEffectViewModel(AskSkillEffectDataViewModel data) {
            super("AskSkillEffectEvent", data, "詢問是否發動武將技");
        }
    }

    public static class SkillEffectViewModel extends ViewModel<SkillEffectDataViewModel> {
        public SkillEffectViewModel(SkillEffectDataViewModel data) {
            super("SkillEffectEvent", data, "武將技發動結果");
        }

        /** 帶 domain event 具體結算訊息（如「剛烈判定紅桃，未生效」，issue #235）。 */
        public SkillEffectViewModel(SkillEffectDataViewModel data, String message) {
            super("SkillEffectEvent", data, message);
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AskSkillEffectDataViewModel {
        private String skillName;
        private String playerId;
        private List<String> dataCardIds;
        private String dataPlayerId;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SkillEffectDataViewModel {
        private String skillName;
        private String playerId;
        private boolean accepted;
        private List<String> dataCardIds;
        private String dataPlayerId;
    }
    private static GameStatusEvent lastGameStatusEvent(List<DomainEvent> events) {
        for (int i = events.size() - 1; i >= 0; i--) {
            if (events.get(i) instanceof GameStatusEvent status) {
                return status;
            }
        }
        throw new IllegalStateException("No GameStatusEvent in events");
    }
}
