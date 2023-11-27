package com.waterball.LegendsOfTheThreeKingdoms.presenter;

import com.waterball.LegendsOfTheThreeKingdoms.domain.events.*;
import com.waterball.LegendsOfTheThreeKingdoms.presenter.common.GameDataViewModel;
import com.waterball.LegendsOfTheThreeKingdoms.presenter.common.PlayerDataViewModel;
import com.waterball.LegendsOfTheThreeKingdoms.presenter.common.RoundDataViewModel;
import com.waterball.LegendsOfTheThreeKingdoms.service.GameService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.waterball.LegendsOfTheThreeKingdoms.presenter.ViewModel.getEvent;

public class PlayCardPresenter implements GameService.Presenter<List<PlayCardPresenter.GameViewModel>> {
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


        updateViewModels(playCardViewModel, playerDamageEventViewModel, playerDyingViewModel, askPeachViewModel);

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


    private void updateViewModels(PlayCardViewModel playCardViewModel,
                                  PlayerDamagedViewModel playerDamageEventViewModel,
                                  PlayerDyingViewModel playerDyingViewModel,
                                  AskPeachViewModel askPeachViewModel) {
        if (playCardViewModel == null) throw new RuntimeException();
        eventToViewModels.add(playCardViewModel);
        if (playerDamageEventViewModel != null) eventToViewModels.add(playerDamageEventViewModel);
        if (playerDyingViewModel != null) eventToViewModels.add(playerDyingViewModel);
        if (askPeachViewModel != null) eventToViewModels.add(askPeachViewModel);
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

    @Data
    public static class PlayCardViewModel extends ViewModel<PlayCardPresenter.PlayCardDataViewModel> {
        //private PlayCardPresenter.PlayCardDataViewModel data;

        public PlayCardViewModel() {
            super("PlayCardEvent", null, "出牌");
        }

        public PlayCardViewModel(PlayCardPresenter.PlayCardDataViewModel data, String eventMessage) {
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
    public static class PlayerDamagedViewModel extends ViewModel<PlayCardPresenter.PlayerDamagedDataViewModel> {
        public PlayerDamagedViewModel(PlayCardPresenter.PlayerDamagedDataViewModel data) {
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
    public static class PlayerDyingViewModel extends ViewModel<PlayCardPresenter.PlayerDyingDataViewModel> {
        public PlayerDyingViewModel(PlayCardPresenter.PlayerDyingDataViewModel data) {
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
    public static class AskPeachViewModel extends ViewModel<PlayCardPresenter.AskPeachDataViewModel> {
        public AskPeachViewModel(PlayCardPresenter.AskPeachDataViewModel data) {
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
