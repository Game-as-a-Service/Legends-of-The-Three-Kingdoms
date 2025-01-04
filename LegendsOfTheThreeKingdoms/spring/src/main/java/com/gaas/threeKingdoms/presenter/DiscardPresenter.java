package com.gaas.threeKingdoms.presenter;

import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.presenter.common.GameDataViewModel;
import com.gaas.threeKingdoms.presenter.common.PlayerDataViewModel;
import com.gaas.threeKingdoms.presenter.common.RoundDataViewModel;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.presenter.mapper.DomainEventToViewModelMapper;
import lombok.*;
import com.gaas.threeKingdoms.usecase.DiscardCardUseCase;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.gaas.threeKingdoms.presenter.ViewModel.getEvent;

public class DiscardPresenter implements DiscardCardUseCase.DiscardPresenter<List<DiscardPresenter.GameViewModel>> {

    private final DomainEventToViewModelMapper domainEventToViewModelMapper = new DomainEventToViewModelMapper();
    List<ViewModel<?>> eventToViewModels = new ArrayList<>();
    private List<GameViewModel> viewModels = new ArrayList<>();

    public void renderEvents(List<DomainEvent> events) {
        eventToViewModels = domainEventToViewModelMapper.mapEventsToViewModels(events);
        GameStatusEvent gameStatusEvent = getEvent(events, GameStatusEvent.class).orElseThrow();

        Optional<RoundStartPresenter.DrawCardViewModel> drawCardViewModelTemplate = ViewModel.getEvent(events, DrawCardEvent.class)
                .map(drawCardEvent -> {
                    RoundStartPresenter.DrawCardDataViewModel drawCardDataViewModel = new RoundStartPresenter.DrawCardDataViewModel(drawCardEvent.getSize(), drawCardEvent.getCardIds(), drawCardEvent.getDrawCardPlayerId());
                    RoundStartPresenter.DrawCardViewModel drawCardViewModel = new RoundStartPresenter.DrawCardViewModel();
                    drawCardViewModel.setData(drawCardDataViewModel);
                    return drawCardViewModel;
                });

        List<PlayerEvent> playerEvents = gameStatusEvent.getSeats();
        RoundEvent roundEvent = gameStatusEvent.getRound();
        List<PlayerDataViewModel> playerDataViewModels = playerEvents.stream().map(PlayerDataViewModel::new).toList();



//        DiscardEvent discardEvent = ViewModel.getEvent(events, DiscardEvent.class).orElseThrow(RuntimeException::new);
//        DiscardViewModel discardViewModel = new DiscardViewModel(discardEvent);
//        RoundEndEvent roundEndEvent = ViewModel.getEvent(events, RoundEndEvent.class).orElseThrow(RuntimeException::new);
//        FinishActionPresenter.RoundEndViewModel roundEndViewModel = new FinishActionPresenter.RoundEndViewModel();
//        RoundStartEvent roundStartEvent = ViewModel.getEvent(events, RoundStartEvent.class).orElseThrow(RuntimeException::new);
//        JudgementEvent JudgementEvent = ViewModel.getEvent(events, JudgementEvent.class).orElseThrow(RuntimeException::new);
//        DrawCardEvent drawCardEvent = ViewModel.getEvent(events, DrawCardEvent.class).orElseThrow(RuntimeException::new);
//
//        RoundStartPresenter.RoundStartViewModel roundStartViewModel = new RoundStartPresenter.RoundStartViewModel();
//        RoundStartPresenter.JudgementViewModel judgementViewModel = new RoundStartPresenter.JudgementViewModel();
//        RoundStartPresenter.DrawCardViewModel drawCardViewModel = new RoundStartPresenter.DrawCardViewModel();
//        RoundStartPresenter.DrawCardDataViewModel drawCardDataViewModel = new RoundStartPresenter.DrawCardDataViewModel(drawCardEvent.getSize(), drawCardEvent.getCardIds(), drawCardEvent.getDrawCardPlayerId());
//        drawCardViewModel.setData(drawCardDataViewModel);


        // 取得 drawCardEvent 中的玩家全部資訊
//        List<PlayerDataViewModel> playerDataViewModels = drawCardEvent.getSeats().stream().map(PlayerDataViewModel::new).toList();

        // 取得 drawCardEvent 中的回合資訊
//        RoundEvent roundEvent = drawCardEvent.getRound();

        // 將回合資訊放入 RoundDataViewModel ，後續會放到 GameDataViewModel
        RoundDataViewModel roundDataViewModel = new RoundDataViewModel(roundEvent);
//        String currentRoundPlayerId = roundEvent.getCurrentRoundPlayer();
        for (PlayerDataViewModel viewModel : playerDataViewModels) {

            // 此 use case 的 data 物件
//            GameDataViewModel gameDataViewModel = createGameDataViewModel(viewModel, playerDataViewModels, roundDataViewModel, drawCardEvent.getGamePhase());

            GameDataViewModel gameDataViewModel = new GameDataViewModel(
                    PlayerDataViewModel.hiddenOtherPlayerRoleInformation(
                            playerDataViewModels, viewModel.getId()), roundDataViewModel, gameStatusEvent.getGamePhase());


            List<ViewModel<?>> personalEventToViewModels = new ArrayList<>(eventToViewModels);

            drawCardViewModelTemplate.ifPresent(drawCardViewModeltmpl -> {
                personalEventToViewModels.add(hiddenOtherPlayerCardIds(drawCardViewModeltmpl.getData(), viewModel, roundEvent.getCurrentRoundPlayer()));
            });

            // 非主公看不到此次 PlayerDrawCardEvent 的抽配 card ids
//            RoundStartPresenter.DrawCardViewModel drawCardViewModelInHidden = hiddenOtherPlayerCardIds(drawCardDataViewModel, viewModel, currentRoundPlayerId);
            viewModels.add(new GameViewModel(personalEventToViewModels,
                    gameDataViewModel,
                    gameStatusEvent.getMessage(),
                    gameStatusEvent.getGameId(),
                    viewModel.getId())
            );
        }

    }

    @Override
    public List<GameViewModel> present() {
        return viewModels;
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
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DiscardDataViewModel {
        private List<String> cardIds;
        private String discardPlayerId;
    }

    @Setter
    @Getter
    public static class DiscardViewModel extends ViewModel<DiscardDataViewModel> {
        public DiscardViewModel(DiscardEvent event) {
            super(event.getName(), new DiscardDataViewModel(event.getDiscardCards().stream().map(HandCard::getId).toList(), event.getDiscardPlayerId()), event.getMessage());
        }
    }

    private static GameDataViewModel createGameDataViewModel(PlayerDataViewModel viewModel, List<PlayerDataViewModel> playerDataViewModels, RoundDataViewModel roundDataViewModel, String gamePhase) {
        return new GameDataViewModel(
                PlayerDataViewModel.hiddenOtherPlayerRoleInformation(playerDataViewModels, viewModel.getId()), roundDataViewModel, gamePhase);
    }

    public RoundStartPresenter.DrawCardViewModel hiddenOtherPlayerCardIds(RoundStartPresenter.DrawCardDataViewModel drawCardDataViewModel, PlayerDataViewModel targetPlayerDataViewModel, String currentRoundPlayerId) {
        List<String> cards = drawCardDataViewModel.getCards();
        List<String> hiddenCards = new ArrayList<>();
        if (PlayerDataViewModel.isCurrentRoundPlayer(targetPlayerDataViewModel, currentRoundPlayerId)) {
            hiddenCards.addAll(cards);
        }
        return new RoundStartPresenter.DrawCardViewModel(new RoundStartPresenter.DrawCardDataViewModel(drawCardDataViewModel.getSize(), hiddenCards, drawCardDataViewModel.getDrawCardPlayerId()));
    }
}
