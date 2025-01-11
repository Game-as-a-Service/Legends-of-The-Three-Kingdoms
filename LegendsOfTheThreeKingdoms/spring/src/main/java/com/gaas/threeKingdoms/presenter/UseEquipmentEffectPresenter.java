package com.gaas.threeKingdoms.presenter;

import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.presenter.common.GameDataViewModel;
import com.gaas.threeKingdoms.presenter.common.PlayerDataViewModel;
import com.gaas.threeKingdoms.presenter.common.RoundDataViewModel;
import com.gaas.threeKingdoms.presenter.mapper.DomainEventToViewModelMapper;
import com.gaas.threeKingdoms.usecase.UseEquipmentUseCase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

import static com.gaas.threeKingdoms.presenter.PlayCardPresenter.*;
import static com.gaas.threeKingdoms.presenter.PlayCardPresenter.getGameOverViewModel;
import static com.gaas.threeKingdoms.presenter.ViewModel.getEvent;

public class UseEquipmentEffectPresenter implements UseEquipmentUseCase.UseEquipmentPresenter<List<UseEquipmentEffectPresenter.GameViewModel>> {

    private List<ViewModel<?>> eventToViewModels = new ArrayList<>();
    private List<GameViewModel> viewModels = new ArrayList<>();
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

            viewModels.add(new GameViewModel(
                    eventToViewModels,
                    gameDataViewModel,
                    gameStatusEvent.getMessage(),
                    gameStatusEvent.getGameId(),
                    viewModel.getId()));
        }
    }

    @Override
    public List<UseEquipmentEffectPresenter.GameViewModel> present() {
        return viewModels;
    }


    public static class UseEquipmentEffectViewModel extends ViewModel<UseEquipmentEffectDataViewModel> {
        public UseEquipmentEffectViewModel(UseEquipmentEffectDataViewModel data) {
            super("UseEquipmentEffectViewModel", data, String.format("發動效果 %s", data.isSuccess ? "成功" : "失敗"));
        }
    }

    public static class AskChooseMountCardViewModel extends ViewModel<AskChooseMountCardDataViewModel> {
        public AskChooseMountCardViewModel(AskChooseMountCardDataViewModel data) {
            super("AskChooseMountCardEvent", data, "選擇要移除哪匹馬");
        }
    }

    public static class UseQilinBowCardEffectViewModel extends ViewModel<UseQilinBowCardEffectDataViewModel> {
        public UseQilinBowCardEffectViewModel(UseQilinBowCardEffectDataViewModel data) {
            super("UseQilinBowCardEffectViewModel", data, "發動效果麒麟弓效果");
        }
    }

    public static class SkipEquipmentEffectViewModel extends ViewModel<SkipEquipmentEffectDataViewModel> {
        public SkipEquipmentEffectViewModel(SkipEquipmentEffectDataViewModel data) {
            super("SkipEquipmentEffectViewModel", data, "跳過裝備效果");
        }
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SkipEquipmentEffectDataViewModel {
        private String playerId;
        private String cardId;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UseQilinBowCardEffectDataViewModel {
        private String mountCardId;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AskChooseMountCardDataViewModel {
        private String chooseMountCardPlayerId;
        private String targetPlayerId;
        private List<String> mountCardIds;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UseEquipmentEffectDataViewModel {
        private String drawCardId;
        private boolean isSuccess;
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
