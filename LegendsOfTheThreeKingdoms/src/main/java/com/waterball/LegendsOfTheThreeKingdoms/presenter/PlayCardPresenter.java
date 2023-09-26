package com.waterball.LegendsOfTheThreeKingdoms.presenter;

import com.waterball.LegendsOfTheThreeKingdoms.domain.events.DomainEvent;
import com.waterball.LegendsOfTheThreeKingdoms.domain.events.PlayCardEvent;
import com.waterball.LegendsOfTheThreeKingdoms.domain.events.RoundEvent;
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

import static com.waterball.LegendsOfTheThreeKingdoms.presenter.ViewModel.getEvent;

public class PlayCardPresenter implements GameService.Presenter<List<PlayCardPresenter.GameViewModel>> {

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

        PlayCardEvent event = getEvent(events, PlayCardEvent.class).orElseThrow(RuntimeException::new);


        List<PlayerDataViewModel> playerDataViewModels = event.getSeats().stream().map(PlayerDataViewModel::new).toList();
        RoundEvent roundEvent = event.getRound();

        PlayCardDataViewModel playCardDataViewModel = new PlayCardDataViewModel(event.getPlayerId(), event.getTargetPlayerId(), event.getCardId(), event.getPlayType());
        PlayCardViewModel playCardViewModel = new PlayCardViewModel(playCardDataViewModel);


        // 將回合資訊放入 RoundDataViewModel ，後續會放到 GameDataViewModel
        RoundDataViewModel roundDataViewModel = new RoundDataViewModel(roundEvent);


        for (PlayerDataViewModel viewModel : playerDataViewModels) {
            // 此 use case 的 data 物件
            GameDataViewModel gameDataViewModel = new GameDataViewModel(
                    PlayerDataViewModel.hiddenOtherPlayerRoleInformation(
                            playerDataViewModels, viewModel.getId()), roundDataViewModel, event.getGamePhase());

            viewModels.add(new GameViewModel(
                            List.of(playCardViewModel),
                            gameDataViewModel,
                            event.getMessage(),
                            event.getGameId(),
                            viewModel.getId()));

        }
    }


    @Data
    public static class PlayCardViewModel extends ViewModel<PlayCardPresenter.PlayCardDataViewModel> {
        //private PlayCardPresenter.PlayCardDataViewModel data;

        public PlayCardViewModel() {
            super("PlayCardEvent", null, "出牌");
        }

        public PlayCardViewModel(PlayCardPresenter.PlayCardDataViewModel data) {
            super("PlayCardEvent", data, "出牌");
            //this.data = data;
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
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GameViewModel extends GameProcessViewModel<GameDataViewModel> {
        private String gameId;
        private String playerId;

        public GameViewModel(List<ViewModel> events, GameDataViewModel data, String message, String gameId, String playerId) {
            super(events, data, message);
            this.gameId = gameId;
            this.playerId = playerId;
        }
    }

}
