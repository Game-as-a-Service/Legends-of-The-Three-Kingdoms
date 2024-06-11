package com.gaas.threeKingdoms.presenter;

import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.presenter.common.GameDataViewModel;
import com.gaas.threeKingdoms.presenter.common.PlayerDataViewModel;
import com.gaas.threeKingdoms.presenter.common.RoundDataViewModel;
import com.gaas.threeKingdoms.usecase.UseEquipmentUseCase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

import static com.gaas.threeKingdoms.presenter.ViewModel.getEvent;

public class UseEquipmentEffectPresenter implements UseEquipmentUseCase.UseEquipmentPresenter<List<UseEquipmentEffectPresenter.GameViewModel>> {

    private List<GameViewModel> viewModels = new ArrayList<>();
    private List<ViewModel> effectViewModels = new ArrayList<>();

    @Override
    public void renderEvents(List<DomainEvent> events) {
        GameStatusEvent gameStatusEvent = getEvent(events, GameStatusEvent.class).orElseThrow();
        UseEquipmentEffectViewModel useEquipmentEffectViewModel = getEightDiagramTacticEffectEvent(events);
        UseQilinBowCardEffectViewModel useQilinBowCardEffectViewModel = getQilinBowCardEffectEvent(events);
        PlayCardPresenter.PlayerDamagedViewModel playerDamageEventViewModel = getPlayerDamageEventViewModel(events);

        updateViewModels(
                useEquipmentEffectViewModel,
                useQilinBowCardEffectViewModel,
                playerDamageEventViewModel
        );

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

            viewModels.add(new UseEquipmentEffectPresenter.GameViewModel(
                    effectViewModels,
                    gameDataViewModel,
                    gameStatusEvent.getMessage(),
                    gameStatusEvent.getGameId(),
                    viewModel.getId()));
        }
    }

    private void updateViewModels(ViewModel<?>... viewModels) {
        Arrays.stream(viewModels)
                .filter(Objects::nonNull)
                .forEach(effectViewModels::add);
    }

    private UseEquipmentEffectViewModel getEightDiagramTacticEffectEvent(List<DomainEvent> events) {
        return getEvent(events, EightDiagramTacticEffectEvent.class)
                .map(event -> {
                    UseEquipmentEffectPresenter.UseEquipmentEffectDataViewModel useEquipmentEffectDataViewModel = new UseEquipmentEffectDataViewModel(event.getDrawCardId(), event.isSuccess());
                    return new UseEquipmentEffectViewModel(useEquipmentEffectDataViewModel);
                })
                .orElse(null);
    }

    private UseQilinBowCardEffectViewModel getQilinBowCardEffectEvent(List<DomainEvent> events) {
        return getEvent(events, QilinBowCardEffectEvent.class)
                .map(event -> {
                    UseEquipmentEffectPresenter.UseQilinBowCardEffectDataViewModel useQilinBowCardEffectDataViewModel = new UseEquipmentEffectPresenter.UseQilinBowCardEffectDataViewModel(event.getMountCardId());
                    return new UseEquipmentEffectPresenter.UseQilinBowCardEffectViewModel(useQilinBowCardEffectDataViewModel);
                })
                .orElse(null);
    }


    private PlayCardPresenter.PlayerDamagedViewModel getPlayerDamageEventViewModel(List<DomainEvent> events) {
        return getEvent(events, PlayerDamagedEvent.class)
                .map(event -> {
                    PlayCardPresenter.PlayerDamagedDataViewModel playerDamagedDataViewModel = new PlayCardPresenter.PlayerDamagedDataViewModel(event.getPlayerId(), event.getFrom(), event.getTo());
                    return new PlayCardPresenter.PlayerDamagedViewModel(playerDamagedDataViewModel);
                })
                .orElse(null);
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

    public static class UseQilinBowCardEffectViewModel extends ViewModel<UseQilinBowCardEffectDataViewModel> {
        public UseQilinBowCardEffectViewModel(UseQilinBowCardEffectDataViewModel data) {
            super("UseQilinBowCardEffectViewModel", data, "發動效果麒麟弓效果");
        }
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
