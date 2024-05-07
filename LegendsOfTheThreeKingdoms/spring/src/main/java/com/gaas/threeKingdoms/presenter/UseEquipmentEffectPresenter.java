package com.gaas.threeKingdoms.presenter;

import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.presenter.common.GameDataViewModel;
import com.gaas.threeKingdoms.presenter.common.PlayerDataViewModel;
import com.gaas.threeKingdoms.presenter.common.RoundDataViewModel;
import com.gaas.threeKingdoms.usecase.UseEquipmentUseCase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static com.gaas.threeKingdoms.presenter.ViewModel.getEvent;

public class UseEquipmentEffectPresenter implements UseEquipmentUseCase.UseEquipmentPresenter<List<UseEquipmentEffectPresenter.GameViewModel>> {

    private List<GameViewModel> viewModels = new ArrayList<>();
    private List<ViewModel> effectViewModels = new ArrayList<>();

    @Override
    public void renderEvents(List<DomainEvent> events) {
        EightDiagramTacticEffectEvent effectEvent = getEvent(events, EightDiagramTacticEffectEvent.class).orElseThrow(RuntimeException::new);
        UseEquipmentEffectDataViewModel useEquipmentEffectDataViewModel = new UseEquipmentEffectDataViewModel(effectEvent.getDrawCardId(), effectEvent.isSuccess());
        UseEquipmentEffectViewModel useEquipmentEffectViewModel = new UseEquipmentEffectViewModel(useEquipmentEffectDataViewModel);
        effectViewModels.add(useEquipmentEffectViewModel);

        List<PlayerEvent> playerEvents = effectEvent.getSeats();
        RoundEvent roundEvent = effectEvent.getRound();
        List<PlayerDataViewModel> playerDataViewModels = playerEvents.stream().map(PlayerDataViewModel::new).toList();

        // 將回合資訊放入 RoundDataViewModel ，後續會放到 GameDataViewModel
        RoundDataViewModel roundDataViewModel = new RoundDataViewModel(roundEvent);

        for (PlayerDataViewModel viewModel : playerDataViewModels) {

            // 此 use case 的 data 物件
            GameDataViewModel gameDataViewModel = new GameDataViewModel(
                    PlayerDataViewModel.hiddenOtherPlayerRoleInformation(
                            playerDataViewModels, viewModel.getId()), roundDataViewModel, effectEvent.getGamePhase());

            viewModels.add(new UseEquipmentEffectPresenter.GameViewModel(
                    effectViewModels,
                    gameDataViewModel,
                    effectEvent.getMessage(),
                    effectEvent.getGameId(),
                    viewModel.getId()));
        }
    }


    @Override
    public List<UseEquipmentEffectPresenter.GameViewModel> present() {
        return viewModels;
    }


    public static class UseEquipmentEffectViewModel extends ViewModel<UseEquipmentEffectDataViewModel> {
        public UseEquipmentEffectViewModel(UseEquipmentEffectDataViewModel data) {
            super("UseEquipmentEffectViewModel", data,"發動效果");
        }
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

        public GameViewModel(List<ViewModel> viewModels, GameDataViewModel data, String message, String gameId, String playerId) {
            super(viewModels, data, message);
            this.gameId = gameId;
            this.playerId = playerId;
        }
    }
}
