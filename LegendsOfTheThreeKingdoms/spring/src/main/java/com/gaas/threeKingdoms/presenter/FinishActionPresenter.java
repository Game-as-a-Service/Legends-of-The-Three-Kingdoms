package com.gaas.threeKingdoms.presenter;

import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.GameStatusEvent;
import com.gaas.threeKingdoms.events.PlayerEvent;
import com.gaas.threeKingdoms.events.RoundEvent;
import com.gaas.threeKingdoms.presenter.common.GameDataViewModel;
import com.gaas.threeKingdoms.presenter.common.PlayerDataViewModel;
import com.gaas.threeKingdoms.presenter.common.RoundDataViewModel;
import com.gaas.threeKingdoms.presenter.mapper.DomainEventToViewModelMapper;
import com.gaas.threeKingdoms.usecase.FinishActionUseCase;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.gaas.threeKingdoms.presenter.ViewModel.getEvent;


public class FinishActionPresenter implements FinishActionUseCase.FinishActionPresenter<List<FinishActionPresenter.GameViewModel>> {

    private final DomainEventToViewModelMapper domainEventToViewModelMapper = new DomainEventToViewModelMapper();
    List<ViewModel<?>> eventToViewModels = new ArrayList<>();
    private List<GameViewModel> viewModels = new ArrayList<>();

    public void renderEvents(List<DomainEvent> events) {
        eventToViewModels = domainEventToViewModelMapper.mapEventsToViewModels(events);
        updateFinishActionEventToViewModel(events);
    }

    @Override
    public List<GameViewModel> present() {
        return viewModels;
    }

    private void updateFinishActionEventToViewModel(List<DomainEvent> events) {
        GameStatusEvent gameStatusEvent = getEvent(events, GameStatusEvent.class).orElseThrow();

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

            List<ViewModel<?>> personalEventToViewModels = new ArrayList<>(eventToViewModels);

            personalEventToViewModels = personalEventToViewModels.stream().map(personalViewModel -> {
                if (personalViewModel instanceof RoundStartPresenter.DrawCardViewModel drawCardViewModel) {
                    personalViewModel = hiddenOtherPlayerCardIds(drawCardViewModel.getData(), viewModel, drawCardViewModel.getData().getDrawCardPlayerId());
                }
                return personalViewModel;
            }).collect(Collectors.toList());

            viewModels.add(new GameViewModel(personalEventToViewModels,
                    gameDataViewModel,
                    gameStatusEvent.getMessage(),
                    gameStatusEvent.getGameId(),
                    viewModel.getId()
            ));
        }
    }

    public RoundStartPresenter.DrawCardViewModel hiddenOtherPlayerCardIds(RoundStartPresenter.DrawCardDataViewModel drawCardDataViewModel, PlayerDataViewModel targetPlayerDataViewModel, String currentRoundPlayerId) {
        List<String> cards = drawCardDataViewModel.getCards();
        List<String> hiddenCards = new ArrayList<>();
        if (PlayerDataViewModel.isCurrentRoundPlayer(targetPlayerDataViewModel, currentRoundPlayerId) && drawCardDataViewModel.getDrawCardPlayerId().equals(currentRoundPlayerId)) {
            hiddenCards.addAll(cards);
        }
        return new RoundStartPresenter.DrawCardViewModel(new RoundStartPresenter.DrawCardDataViewModel(drawCardDataViewModel.getSize(), hiddenCards, drawCardDataViewModel.getDrawCardPlayerId()));
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

    @Setter
    @Getter
    public static class FinishActionViewModel extends ViewModel<FinishActionDataViewModel> {
        public FinishActionViewModel(FinishActionDataViewModel data) {
            super("FinishActionEvent", data, "結束出牌");
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FinishActionDataViewModel {
        private String playerId;
    }

    @Data
    public static class NotifyDiscardViewModel extends ViewModel<NotifyDiscardDataViewModel> {
        public NotifyDiscardViewModel(NotifyDiscardDataViewModel data) {
            super("NotifyDiscardEvent", data, "通知棄牌");
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NotifyDiscardDataViewModel {
        private int discardCount;
        private String discardPlayerId;
    }

    @Data
    public static class ContentmentViewModel extends ViewModel<ContentmentDataViewModel> {
        public ContentmentViewModel(ContentmentDataViewModel data) {
            super("ContentmentEvent", data, "發動樂不思蜀效果");
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ContentmentDataViewModel {
        private String playerId;
        private String drawCardId;
        private boolean isSuccess;
    }


    @Setter
    @Getter
    public static class RoundEndViewModel extends ViewModel<Object> {
        public RoundEndViewModel() {
            super("RoundEndEvent", "", "回合已結束");
        }
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
