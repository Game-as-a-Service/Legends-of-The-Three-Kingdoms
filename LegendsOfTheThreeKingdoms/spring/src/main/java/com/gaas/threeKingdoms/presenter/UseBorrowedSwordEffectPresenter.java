package com.gaas.threeKingdoms.presenter;

import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.presenter.common.GameDataViewModel;
import com.gaas.threeKingdoms.presenter.common.PlayerDataViewModel;
import com.gaas.threeKingdoms.presenter.common.RoundDataViewModel;
import com.gaas.threeKingdoms.usecase.UseBorrowedSwordEffectUseCase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.gaas.threeKingdoms.presenter.ViewModel.getEvent;

//public class UseBorrowedSwordEffectPresenter implements UseBorrowedSwordEffectUseCase.UseBorrowedSwordPresenter<List<UseBorrowedSwordEffectPresenter.GameViewModel>> {
//
//    private List<UseBorrowedSwordEffectPresenter.GameViewModel> viewModels = new ArrayList<>();
//    private List<ViewModel> effectViewModels = new ArrayList<>();
//
//    @Override
//    public void renderEvents(List<DomainEvent> events) {
//        GameStatusEvent gameStatusEvent = getEvent(events, GameStatusEvent.class).orElseThrow();
//        UseEquipmentEffectPresenter.UseEquipmentEffectViewModel useEquipmentEffectViewModel = getEightDiagramTacticEffectEvent(events);
//        UseEquipmentEffectPresenter.UseQilinBowCardEffectViewModel useQilinBowCardEffectViewModel = getQilinBowCardEffectEvent(events);
//        PlayCardPresenter.PlayerDamagedViewModel playerDamageEventViewModel = getPlayerDamageEventViewModel(events);
//        UseEquipmentEffectPresenter.AskChooseMountCardViewModel askChooseMountCardViewModel = getAskChooseMountCardEventViewModel(events);
//        UseEquipmentEffectPresenter.SkipEquipmentEffectViewModel skipEquipmentEffectViewModel = getSkipEquipmentEffectViewModel(events);
//
//        updateViewModels(
//                useEquipmentEffectViewModel,
//                useQilinBowCardEffectViewModel,
//                playerDamageEventViewModel,
//                askChooseMountCardViewModel,
//                skipEquipmentEffectViewModel
//        );
//
//        List<PlayerEvent> playerEvents = gameStatusEvent.getSeats();
//        RoundEvent roundEvent = gameStatusEvent.getRound();
//        List<PlayerDataViewModel> playerDataViewModels = playerEvents.stream().map(PlayerDataViewModel::new).toList();
//
//        // 將回合資訊放入 RoundDataViewModel ，後續會放到 GameDataViewModel
//        RoundDataViewModel roundDataViewModel = new RoundDataViewModel(roundEvent);
//
//        for (PlayerDataViewModel viewModel : playerDataViewModels) {
//
//            // 此 use case 的 data 物件
//            GameDataViewModel gameDataViewModel = new GameDataViewModel(
//                    PlayerDataViewModel.hiddenOtherPlayerRoleInformation(
//                            playerDataViewModels, viewModel.getId()), roundDataViewModel, gameStatusEvent.getGamePhase());
//
//            viewModels.add(new UseBorrowedSwordEffectPresenter.GameViewModel(
//                    effectViewModels,
//                    gameDataViewModel,
//                    gameStatusEvent.getMessage(),
//                    gameStatusEvent.getGameId(),
//                    viewModel.getId()));
//        }
//    }
//
//    private void updateViewModels(ViewModel<?>... viewModels) {
//        Arrays.stream(viewModels)
//                .filter(Objects::nonNull)
//                .forEach(effectViewModels::add);
//    }
//
//    private UseEquipmentEffectPresenter.UseEquipmentEffectViewModel getEightDiagramTacticEffectEvent(List<DomainEvent> events) {
//        return getEvent(events, EightDiagramTacticEffectEvent.class)
//                .map(event -> {
//                    UseEquipmentEffectPresenter.UseEquipmentEffectDataViewModel useEquipmentEffectDataViewModel = new UseEquipmentEffectPresenter.UseEquipmentEffectDataViewModel(event.getDrawCardId(), event.isSuccess());
//                    return new UseEquipmentEffectPresenter.UseEquipmentEffectViewModel(useEquipmentEffectDataViewModel);
//                })
//                .orElse(null);
//    }
//
//    private UseEquipmentEffectPresenter.UseQilinBowCardEffectViewModel getQilinBowCardEffectEvent(List<DomainEvent> events) {
//        return getEvent(events, QilinBowCardEffectEvent.class)
//                .map(event -> {
//                    UseEquipmentEffectPresenter.UseQilinBowCardEffectDataViewModel useQilinBowCardEffectDataViewModel = new UseEquipmentEffectPresenter.UseQilinBowCardEffectDataViewModel(event.getMountCardId());
//                    return new UseEquipmentEffectPresenter.UseQilinBowCardEffectViewModel(useQilinBowCardEffectDataViewModel);
//                })
//                .orElse(null);
//    }
//
//
//    private PlayCardPresenter.PlayerDamagedViewModel getPlayerDamageEventViewModel(List<DomainEvent> events) {
//        return getEvent(events, PlayerDamagedEvent.class)
//                .map(event -> {
//                    PlayCardPresenter.PlayerDamagedDataViewModel playerDamagedDataViewModel = new PlayCardPresenter.PlayerDamagedDataViewModel(event.getPlayerId(), event.getFrom(), event.getTo());
//                    return new PlayCardPresenter.PlayerDamagedViewModel(playerDamagedDataViewModel);
//                })
//                .orElse(null);
//    }
//
//    private UseEquipmentEffectPresenter.AskChooseMountCardViewModel getAskChooseMountCardEventViewModel(List<DomainEvent> events) {
//        return getEvent(events, AskChooseMountCardEvent.class)
//                .map(event -> {
//                    UseEquipmentEffectPresenter.AskChooseMountCardDataViewModel askChooseMountCardDataViewModel = new UseEquipmentEffectPresenter.AskChooseMountCardDataViewModel(event.getChooseMountCardPlayerId(), event.getTargetPlayerId(), event.getMountsCardIds());
//                    return new UseEquipmentEffectPresenter.AskChooseMountCardViewModel(askChooseMountCardDataViewModel);
//                })
//                .orElse(null);
//    }
//
//    private UseEquipmentEffectPresenter.SkipEquipmentEffectViewModel getSkipEquipmentEffectViewModel(List<DomainEvent> events) {
//        return getEvent(events, SkipEquipmentEffectEvent.class)
//                .map(event -> {
//                    UseEquipmentEffectPresenter.SkipEquipmentEffectDataViewModel skipEquipmentEffectDataViewModel = new UseEquipmentEffectPresenter.SkipEquipmentEffectDataViewModel(event.getPlayerId(), event.getCardId());
//                    return new UseEquipmentEffectPresenter.SkipEquipmentEffectViewModel(skipEquipmentEffectDataViewModel);
//                })
//                .orElse(null);
//    }
//
//    @Override
//    public List<UseBorrowedSwordEffectPresenter.GameViewModel> present() {
//        return viewModels;
//    }
//
//
//    public static class UseEquipmentEffectViewModel extends ViewModel<UseEquipmentEffectPresenter.UseEquipmentEffectDataViewModel> {
//        public UseEquipmentEffectViewModel(UseEquipmentEffectPresenter.UseEquipmentEffectDataViewModel data) {
//            super("UseEquipmentEffectViewModel", data, String.format("發動效果 %s", data.isSuccess ? "成功" : "失敗"));
//        }
//    }
//
//    public static class AskChooseMountCardViewModel extends ViewModel<UseEquipmentEffectPresenter.AskChooseMountCardDataViewModel> {
//        public AskChooseMountCardViewModel(UseEquipmentEffectPresenter.AskChooseMountCardDataViewModel data) {
//            super("AskChooseMountCardEvent", data, "選擇要移除哪匹馬");
//        }
//    }
//
//    public static class UseQilinBowCardEffectViewModel extends ViewModel<UseEquipmentEffectPresenter.UseQilinBowCardEffectDataViewModel> {
//        public UseQilinBowCardEffectViewModel(UseEquipmentEffectPresenter.UseQilinBowCardEffectDataViewModel data) {
//            super("UseQilinBowCardEffectViewModel", data, "發動效果麒麟弓效果");
//        }
//    }
//
//    public static class SkipEquipmentEffectViewModel extends ViewModel<UseEquipmentEffectPresenter.SkipEquipmentEffectDataViewModel> {
//        public SkipEquipmentEffectViewModel(UseEquipmentEffectPresenter.SkipEquipmentEffectDataViewModel data) {
//            super("SkipEquipmentEffectViewModel", data, "跳過裝備效果");
//        }
//    }
//
//
//    @Data
//    @AllArgsConstructor
//    @NoArgsConstructor
//    public static class SkipEquipmentEffectDataViewModel {
//        private String playerId;
//        private String cardId;
//    }
//
//    @Data
//    @AllArgsConstructor
//    @NoArgsConstructor
//    public static class UseQilinBowCardEffectDataViewModel {
//        private String mountCardId;
//    }
//
//    @Data
//    @AllArgsConstructor
//    @NoArgsConstructor
//    public static class AskChooseMountCardDataViewModel {
//        private String chooseMountCardPlayerId;
//        private String targetPlayerId;
//        private List<String> mountCardIds;
//    }
//
//    @Data
//    @AllArgsConstructor
//    @NoArgsConstructor
//    public static class UseEquipmentEffectDataViewModel {
//        private String drawCardId;
//        private boolean isSuccess;
//    }
//
//    @Data
//    @AllArgsConstructor
//    @NoArgsConstructor
//    public static class GameViewModel extends GameProcessViewModel<GameDataViewModel> {
//        private String gameId;
//        private String playerId;
//
//        public GameViewModel(List<ViewModel> viewModels, GameDataViewModel data, String message, String gameId, String playerId) {
//            super(viewModels, data, message);
//            this.gameId = gameId;
//            this.playerId = playerId;
//        }
//    }
//}
