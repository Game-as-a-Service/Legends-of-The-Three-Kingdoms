package com.gaas.threeKingdoms.presenter;


import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.presenter.common.GameDataViewModel;
import com.gaas.threeKingdoms.presenter.common.PlayerDataViewModel;
import com.gaas.threeKingdoms.presenter.common.RoundDataViewModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import com.gaas.threeKingdoms.usecase.PlayCardUseCase;

import java.util.*;


import static com.gaas.threeKingdoms.presenter.ViewModel.getEvent;


public class PlayCardPresenter implements PlayCardUseCase.PlayCardPresenter<List<PlayCardPresenter.GameViewModel>> {
    List<ViewModel> eventToViewModels = new ArrayList<>();
    private List<GameViewModel> viewModels;

    public void renderEvents(List<DomainEvent> events) {
        if (!events.isEmpty()) {
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

        List<PlayerDataViewModel> playerDataViewModels = playCardEvent.getSeats().stream().map(PlayerDataViewModel::new).toList();
        RoundEvent roundEvent = playCardEvent.getRound();

        PlayCardDataViewModel playCardDataViewModel = new PlayCardDataViewModel(playCardEvent.getPlayerId(), playCardEvent.getTargetPlayerId(), playCardEvent.getCardId(), playCardEvent.getPlayType());
        PlayCardViewModel playCardViewModel = new PlayCardViewModel(playCardDataViewModel, playCardEvent.getMessage());
        PlayerDamagedViewModel playerDamageEventViewModel = getPlayerDamageEventViewModel(events);
        PlayerDyingViewModel playerDyingViewModel = getPlayerDyingEventViewModel(events);
        AskPeachViewModel askPeachViewModel = getAskPeachViewModel(events);
        SettlementViewModel settlementViewModel = getSettlementViewModel(events);
        GameOverViewModel gameOverViewModel = getGameOverViewModel(events);
        PeachViewModel peachViewModel = getPeachViewModel(events);

        updateViewModels(playCardViewModel, playerDamageEventViewModel, playerDyingViewModel, askPeachViewModel, peachViewModel, settlementViewModel, gameOverViewModel);

        // 將回合資訊放入 RoundDataViewModel ，後續會放到 GameDataViewModel
        RoundDataViewModel roundDataViewModel = new RoundDataViewModel(roundEvent);


        for (PlayerDataViewModel viewModel : playerDataViewModels) {
            // 此 use case 的 data 物件
            GameDataViewModel gameDataViewModel = new GameDataViewModel(
                    PlayerDataViewModel.hiddenOtherPlayerRoleInformation(
                            playerDataViewModels, viewModel.getId()), roundDataViewModel, playCardEvent.getGamePhase());

            viewModels.add(new GameViewModel(
                    eventToViewModels,
                    gameDataViewModel,
                    playCardEvent.getMessage(),
                    playCardEvent.getGameId(),
                    viewModel.getId()));

        }
    }

   private void updateViewModels(ViewModel<?>... viewModels) {
       Arrays.stream(viewModels)
               .filter(Objects::nonNull)
               .forEach(eventToViewModels::add);
    }


    private PlayerDamagedViewModel getPlayerDamageEventViewModel(List<DomainEvent> events) {
        return getEvent(events, PlayerDamagedEvent.class)
                .map(event -> {
                    PlayerDamagedDataViewModel playerDamagedDataViewModel = new PlayerDamagedDataViewModel(event.getPlayerId(), event.getFrom(), event.getTo());
                    return new PlayerDamagedViewModel(playerDamagedDataViewModel);
                })
                .orElse(null);
    }

    private PlayerDyingViewModel getPlayerDyingEventViewModel(List<DomainEvent> events) {
        return getEvent(events,PlayerDyingEvent.class)
                .map(event -> {
                    PlayerDyingDataViewModel playerDyingDataViewModel = new PlayerDyingDataViewModel(event.getPlayerId());
                    return new PlayerDyingViewModel(playerDyingDataViewModel);
                })
                .orElse(null);
    }


    private AskPeachViewModel getAskPeachViewModel(List<DomainEvent> events) {
        return getEvent(events,AskPeachEvent.class)
                .map(event -> {
                    AskPeachDataViewModel askPeachDataViewModel = new AskPeachDataViewModel(event.getPlayerId());
                    return new AskPeachViewModel(askPeachDataViewModel);
                })
                .orElse(null);
    }

    private SettlementViewModel getSettlementViewModel(List<DomainEvent> events) {
        return getEvent(events,SettlementEvent.class)
                .map(event -> {
                    SettlementDataViewModel settlementDataViewModel = new SettlementDataViewModel(event.getPlayerId(), event.getRole());
                    return new SettlementViewModel(settlementDataViewModel);
                })
                .orElse(null);
    }



    private GameOverViewModel getGameOverViewModel(List<DomainEvent> events) {
        return getEvent(events, GameOverEvent.class)
                .map(gameOverEvent -> {
                    GameOverDataViewModel gameOverDataViewModel = new GameOverDataViewModel(gameOverEvent.getPlayers().stream()
                            .map(PlayerDataViewModel::new)
                            .toList(), gameOverEvent.getWinners());
                    return new GameOverViewModel(gameOverDataViewModel);
                })
                .orElse(null);
    }

    private PeachViewModel getPeachViewModel(List<DomainEvent> events) {
        return getEvent(events,PeachEvent.class)
                .map(event -> {
                    PeachDataViewModel peachDataViewModel = new PeachDataViewModel(event.getPlayerId(), event.getFrom(), event.getTo());
                    return new PeachViewModel(peachDataViewModel);
                })
                .orElse(null);
    }


    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class PlayCardViewModel extends ViewModel<PlayCardDataViewModel> {

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


    @EqualsAndHashCode(callSuper = true)
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

    @EqualsAndHashCode(callSuper = true)
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

    @EqualsAndHashCode(callSuper = true)
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
    }

    @EqualsAndHashCode(callSuper = true)
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

    @EqualsAndHashCode(callSuper = true)
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

    @EqualsAndHashCode(callSuper = true)
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

    @EqualsAndHashCode(callSuper = true)
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
