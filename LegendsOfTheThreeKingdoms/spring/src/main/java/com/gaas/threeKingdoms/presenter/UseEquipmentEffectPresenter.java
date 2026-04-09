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
            super("UseEquipmentEffectEvent", data, String.format("發動效果 %s", data.isSuccess ? "成功" : "失敗"));
        }
    }

    public static class AskChooseMountCardViewModel extends ViewModel<AskChooseMountCardDataViewModel> {
        public AskChooseMountCardViewModel(AskChooseMountCardDataViewModel data) {
            super("AskChooseMountCardEvent", data, "選擇要移除哪匹馬");
        }
    }

    public static class UseQilinBowCardEffectViewModel extends ViewModel<UseQilinBowCardEffectDataViewModel> {
        public UseQilinBowCardEffectViewModel(UseQilinBowCardEffectDataViewModel data) {
            super("UseQilinBowCardEffectEvent", data, "發動效果麒麟弓效果");
        }
    }

    public static class BlackPommelEffectViewModel extends ViewModel<BlackPommelEffectDataViewModel> {
        public BlackPommelEffectViewModel(BlackPommelEffectDataViewModel data) {
            super("BlackPommelEffectEvent", data, "青釭劍發動，殺無視防具");
        }
    }

    public static class AskYinYangSwordsEffectViewModel extends ViewModel<AskYinYangSwordsEffectDataViewModel> {
        public AskYinYangSwordsEffectViewModel(AskYinYangSwordsEffectDataViewModel data) {
            super("AskYinYangSwordsEffectEvent", data, "雌雄雙股劍效果：請選擇棄一張手牌或讓攻擊者摸牌");
        }
    }

    public static class YinYangSwordsEffectViewModel extends ViewModel<YinYangSwordsEffectDataViewModel> {
        public YinYangSwordsEffectViewModel(YinYangSwordsEffectDataViewModel data) {
            super("YinYangSwordsEffectEvent", data, "雌雄雙股劍效果發動");
        }
    }

    public static class AskGreenDragonCrescentBladeEffectViewModel extends ViewModel<AskGreenDragonCrescentBladeEffectDataViewModel> {
        public AskGreenDragonCrescentBladeEffectViewModel(AskGreenDragonCrescentBladeEffectDataViewModel data) {
            super("AskGreenDragonCrescentBladeEffectEvent", data, "青龍偃月刀效果：是否要再出一張殺");
        }
    }

    public static class GreenDragonCrescentBladeTriggerViewModel extends ViewModel<GreenDragonCrescentBladeTriggerDataViewModel> {
        public GreenDragonCrescentBladeTriggerViewModel(GreenDragonCrescentBladeTriggerDataViewModel data) {
            super("GreenDragonCrescentBladeTriggerEvent", data, "青龍偃月刀發動");
        }
    }

    public static class AskStonePiercingAxeEffectViewModel extends ViewModel<AskStonePiercingAxeEffectDataViewModel> {
        public AskStonePiercingAxeEffectViewModel(AskStonePiercingAxeEffectDataViewModel data) {
            super("AskStonePiercingAxeEffectEvent", data, "貫石斧效果：是否棄兩張牌強制命中");
        }
    }

    public static class StonePiercingAxeTriggerViewModel extends ViewModel<StonePiercingAxeTriggerDataViewModel> {
        public StonePiercingAxeTriggerViewModel(StonePiercingAxeTriggerDataViewModel data) {
            super("StonePiercingAxeTriggerEvent", data, "貫石斧發動");
        }
    }

    public static class ViperSpearKillTriggerViewModel extends ViewModel<ViperSpearKillTriggerDataViewModel> {
        public ViperSpearKillTriggerViewModel(ViperSpearKillTriggerDataViewModel data) {
            super("ViperSpearKillTriggerEvent", data, "丈八蛇矛發動");
        }
    }

    public static class SkipEquipmentEffectViewModel extends ViewModel<SkipEquipmentEffectDataViewModel> {
        public SkipEquipmentEffectViewModel(SkipEquipmentEffectDataViewModel data) {
            super("SkipEquipmentEffectEvent", data, "跳過裝備效果");
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
    public static class BlackPommelEffectDataViewModel {
        private String attackerPlayerId;
        private String targetPlayerId;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AskYinYangSwordsEffectDataViewModel {
        private String attackerPlayerId;
        private String targetPlayerId;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class YinYangSwordsEffectDataViewModel {
        private String attackerPlayerId;
        private String targetPlayerId;
        private String choice;
        private String discardedCardId;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AskGreenDragonCrescentBladeEffectDataViewModel {
        private String attackerPlayerId;
        private String targetPlayerId;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GreenDragonCrescentBladeTriggerDataViewModel {
        private String attackerPlayerId;
        private String targetPlayerId;
        private String killCardId;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AskStonePiercingAxeEffectDataViewModel {
        private String attackerPlayerId;
        private String targetPlayerId;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StonePiercingAxeTriggerDataViewModel {
        private String attackerPlayerId;
        private String targetPlayerId;
        private List<String> discardedCardIds;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ViperSpearKillTriggerDataViewModel {
        private String attackerPlayerId;
        private String targetPlayerId;
        private List<String> discardedCardIds;
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
