package com.gaas.threeKingdoms.presenter;


import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.presenter.common.GameDataViewModel;
import com.gaas.threeKingdoms.presenter.common.PlayerDataViewModel;
import com.gaas.threeKingdoms.presenter.common.RoundDataViewModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.gaas.threeKingdoms.usecase.PlayCardUseCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


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


        updateViewModels(playCardViewModel, playerDamageEventViewModel, playerDyingViewModel, askPeachViewModel, settlementViewModel, gameOverViewModel);

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


    private void updateViewModels(
            PlayCardViewModel playCardViewModel,
            PlayerDamagedViewModel playerDamageEventViewModel,
            PlayerDyingViewModel playerDyingViewModel,
            AskPeachViewModel askPeachViewModel,
            SettlementViewModel settlementViewModel,
            GameOverViewModel gameOverViewModel
    ) {
        if (playCardViewModel == null) throw new RuntimeException();
        eventToViewModels.add(playCardViewModel);
        if (playerDamageEventViewModel != null) eventToViewModels.add(playerDamageEventViewModel);
        if (playerDyingViewModel != null) eventToViewModels.add(playerDyingViewModel);
        if (askPeachViewModel != null) eventToViewModels.add(askPeachViewModel);
        if (settlementViewModel != null) eventToViewModels.add(settlementViewModel);
        if (gameOverViewModel != null) eventToViewModels.add(gameOverViewModel);
    }

    private PlayerDamagedViewModel getPlayerDamageEventViewModel(List<DomainEvent> events) {
        PlayerDamagedEvent playerDamagedEvent = getEvent(events, PlayerDamagedEvent.class).orElse(null);
        if (playerDamagedEvent != null) {
            PlayerDamagedDataViewModel playerDamagedDataViewModel = new PlayerDamagedDataViewModel(playerDamagedEvent.getPlayerId(), playerDamagedEvent.getFrom(), playerDamagedEvent.getTo());
            return new PlayerDamagedViewModel(playerDamagedDataViewModel);
        }
        return null;
    }

    private PlayerDyingViewModel getPlayerDyingEventViewModel(List<DomainEvent> events) {
        PlayerDyingEvent playerDyingEvent = getEvent(events, PlayerDyingEvent.class).orElse(null);
        if (playerDyingEvent != null) {
            PlayerDyingDataViewModel playerDyingDataViewModel = new PlayerDyingDataViewModel(playerDyingEvent.getPlayerId());
            return new PlayerDyingViewModel(playerDyingDataViewModel);
        }
        return null;
    }


    private AskPeachViewModel getAskPeachViewModel(List<DomainEvent> events) {
        AskPeachEvent askPeachEvent = getEvent(events, AskPeachEvent.class).orElse(null);
        if (askPeachEvent != null) {
            AskPeachDataViewModel askPeachDataViewModel = new AskPeachDataViewModel(askPeachEvent.getPlayerId());
            return new AskPeachViewModel(askPeachDataViewModel);
        }
        return null;
    }

    private SettlementViewModel getSettlementViewModel(List<DomainEvent> events) {
        SettlementEvent settlementEvent = getEvent(events, SettlementEvent.class).orElse(null);
        if (settlementEvent != null) {
            SettlementDataViewModel settlementDataViewModel = new SettlementDataViewModel(settlementEvent.getPlayerId(), settlementEvent.getRole());
            return new SettlementViewModel(settlementDataViewModel);
        }
        return null;
    }

    private GameOverViewModel getGameOverViewModel(List<DomainEvent> events) {
        GameOverEvent gameOverEvent = getEvent(events, GameOverEvent.class).orElse(null);
        if (gameOverEvent != null) {
            GameOverDataViewModel gameOverDataViewModel = new GameOverDataViewModel(gameOverEvent.getPlayers().stream()
                    .map(PlayerDataViewModel::new)
                    .toList(), gameOverEvent.getWinners());
            return new GameOverViewModel(gameOverDataViewModel);
        }
        return null;
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
