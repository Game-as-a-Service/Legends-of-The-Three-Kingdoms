package com.gaas.threeKingdoms.presenter;


import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.presenter.common.GameDataViewModel;
import com.gaas.threeKingdoms.presenter.common.PlayerDataViewModel;
import com.gaas.threeKingdoms.presenter.common.RoundDataViewModel;
import com.gaas.threeKingdoms.presenter.mapper.DomainEventToViewModelMapper;
import com.gaas.threeKingdoms.usecase.PlayCardUseCase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

import static com.gaas.threeKingdoms.presenter.FinishActionPresenter.hiddenOtherPlayerCardIds;
import static com.gaas.threeKingdoms.presenter.ViewModel.getEvent;


public class PlayCardPresenter implements PlayCardUseCase.PlayCardPresenter<List<PlayCardPresenter.GameViewModel>> {
    List<ViewModel<?>> eventToViewModels = new ArrayList<>();
    private List<GameViewModel> viewModels;
    private final DomainEventToViewModelMapper domainEventToViewModelMapper = new DomainEventToViewModelMapper();

    public void renderEvents(List<DomainEvent> events) {
        if (!events.isEmpty()) {
            eventToViewModels = domainEventToViewModelMapper.mapEventsToViewModels(events);
            updatePlayCardEventToViewModel(events);
        } else {
            viewModels = Collections.emptyList();
        }
    }

    @Override
    public List<GameViewModel> present() {
        return viewModels;
    }

    private void updatePlayCardEventToViewModel(List<DomainEvent> events) {
        viewModels = new ArrayList<>();
        PlayCardEvent playCardEvent = getEvent(events, PlayCardEvent.class).orElseThrow(RuntimeException::new);
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
                }
                return personalViewModel;
            }).collect(Collectors.toList());

            viewModels.add(new GameViewModel(
                    personalEventToViewModels,
                    gameDataViewModel,
                    playCardEvent.getMessage(),
                    gameStatusEvent.getGameId(),
                    viewModel.getId()));
        }
    }

    public static GameOverViewModel getGameOverViewModel(List<DomainEvent> events) {
        return getEvent(events, GameOverEvent.class)
                .map(gameOverEvent -> {
                    GameOverDataViewModel gameOverDataViewModel = new GameOverDataViewModel(gameOverEvent.getPlayers().stream()
                            .map(PlayerDataViewModel::new)
                            .toList(), gameOverEvent.getWinners());
                    return new GameOverViewModel(gameOverDataViewModel);
                })
                .orElse(null);
    }

    @Data
    public static class PlayCardViewModel extends ViewModel<PlayCardDataViewModel> {
        //private PlayCardPresenter.PlayCardDataViewModel data;

        public PlayCardViewModel() {
            super("PlayCardEvent", null, "出牌");
        }

        public PlayCardViewModel(PlayCardDataViewModel data, String eventMessage) {
            super("PlayCardEvent", data, eventMessage);
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PlayCardDataViewModel {
        private String playerId;
        private String targetPlayerId;
        private String cardId;
        private String playType;
    }


    @Data
    public static class PlayerDamagedViewModel extends ViewModel<PlayerDamagedDataViewModel> {
        public PlayerDamagedViewModel(PlayerDamagedDataViewModel data) {
            super("PlayerDamagedEvent", data, "扣血");
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PlayerDamagedDataViewModel {
        private String playerId;
        private int from;
        private int to;
    }

    @Data
    public static class PlayerDyingViewModel extends ViewModel<PlayerDyingDataViewModel> {
        public PlayerDyingViewModel(PlayerDyingDataViewModel data) {
            super("PlayerDyingEvent", data, "玩家已瀕臨死亡");
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PlayerDyingDataViewModel {
        private String playerId;
    }

    @Data
    public static class AskPeachViewModel extends ViewModel<AskPeachDataViewModel> {
        public AskPeachViewModel(AskPeachDataViewModel data) {
            super("AskPeachEvent", data, "要求玩家出桃");
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AskPeachDataViewModel {
        private String playerId;
        private String dyingPlayerId;
    }

    @Data
    public static class SettlementViewModel extends ViewModel<SettlementDataViewModel> {
        public SettlementViewModel(SettlementDataViewModel data) {
            super("SettlementEvent", data, "結算");
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SettlementDataViewModel {
        private String playerId;
        private String role;
    }

    @Data
    public static class GameOverViewModel extends ViewModel<GameOverDataViewModel> {
        public GameOverViewModel(GameOverDataViewModel data) {
            super("GameOverEvent", data, "遊戲結束");
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GameOverDataViewModel {
        private List<PlayerDataViewModel> players;
        private List<String> winners;
    }

    @Data
    public static class PeachViewModel extends ViewModel<PeachDataViewModel> {
        public PeachViewModel(PeachDataViewModel data) {
            super("PeachEvent", data, "玩家出桃");
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PeachDataViewModel {
        private String playerId;
        private int from;
        private int to;
    }

    @Data
    public static class SomethingForNothingViewModel extends ViewModel<SomethingForNothingDataViewModel> {
        public SomethingForNothingViewModel(SomethingForNothingDataViewModel data) {
            super("SomethingForNothingEvent", data, "玩家出無懈可擊");
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SomethingForNothingDataViewModel {
        private String playerId;
    }


    @Data
    public static class PlayEquipmentCardViewModel extends ViewModel<PlayEquipmentCardDataViewModel> {
        public PlayEquipmentCardViewModel(PlayEquipmentCardDataViewModel data) {
            super("PlayEquipmentEvent", data, "玩家出裝備卡");
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PlayEquipmentCardDataViewModel {
        private String playerId;
        private String cardId;
        private String deprecatedCardId;
    }


    @Data
    public static class AskPlayEquipmentEffectViewModel extends ViewModel<AskPlayEquipmentEffectDataViewModel> {
        public AskPlayEquipmentEffectViewModel(AskPlayEquipmentEffectDataViewModel data) {
            super("AskPlayEquipmentEffectEvent", data, String.format("請問要否要發動裝備卡%s的效果", data.getEquipmentCardName()));
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AskPlayEquipmentEffectDataViewModel {
        private String playerId;
        private String equipmentCardId;
        private String equipmentCardName;
        private List<String> targetPlayerIds;
    }

    @Data
    public static class AskKillViewModel extends ViewModel<AskKillDataViewModel> {
        public AskKillViewModel(AskKillDataViewModel data) {
            super("AskKillEvent", data, String.format("請問 %s 要否要出殺", data.getPlayerId()));
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AskKillDataViewModel {
        private String playerId;
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
