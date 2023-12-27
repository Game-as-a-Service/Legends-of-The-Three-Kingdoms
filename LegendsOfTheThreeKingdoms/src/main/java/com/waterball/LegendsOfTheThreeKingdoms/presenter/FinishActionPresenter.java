package com.waterball.LegendsOfTheThreeKingdoms.presenter;

import com.waterball.LegendsOfTheThreeKingdoms.domain.Round;
import com.waterball.LegendsOfTheThreeKingdoms.domain.events.*;
import com.waterball.LegendsOfTheThreeKingdoms.presenter.common.GameDataViewModel;
import com.waterball.LegendsOfTheThreeKingdoms.presenter.common.PlayerDataViewModel;
import com.waterball.LegendsOfTheThreeKingdoms.presenter.common.RoundDataViewModel;
import com.waterball.LegendsOfTheThreeKingdoms.service.GameService;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

import static com.waterball.LegendsOfTheThreeKingdoms.presenter.ViewModel.getEvent;

public class FinishActionPresenter implements GameService.Presenter<List<FinishActionPresenter.GameViewModel>> {
    List<ViewModel> eventToViewModels = new ArrayList<>();
    private List<GameViewModel> viewModels = new ArrayList<>();

    public void renderEvents(List<DomainEvent> events) {
        updateFinishActionEventToViewModel(events);
    }

    @Override
    public List<GameViewModel> present() {
        return viewModels;
    }

    private void updateFinishActionEventToViewModel(List<DomainEvent> events) {
        FinishActionEvent finishActionEvent = getEvent(events, FinishActionEvent.class).orElseThrow(RuntimeException::new);
        FinishActionViewModel finishActionViewModel = new FinishActionViewModel();
        NotifyDiscardEvent notifyDiscardEvent = getEvent(events, NotifyDiscardEvent.class).orElseThrow(RuntimeException::new);
        NotifyDiscardViewModel notifyDiscardViewModel = new NotifyDiscardViewModel(new NotifyDiscardDataViewModel(notifyDiscardEvent.getDiscardCount(), notifyDiscardEvent.getDiscardPlayerId()));


        if (playerNeedToDiscard(notifyDiscardEvent)) {
            // 取得 notifyDiscardEvent 中的玩家全部資訊
            List<PlayerDataViewModel> playerDataViewModels = notifyDiscardEvent.getSeats().stream().map(PlayerDataViewModel::new).toList();

            // 取得 notifyDiscardEvent 中的回合資訊
            RoundEvent roundEvent = notifyDiscardEvent.getRound();

            // 將回合資訊放入 RoundDataViewModel ，後續會放到 GameDataViewModel
            RoundDataViewModel roundDataViewModel = new RoundDataViewModel(roundEvent);

            for (PlayerDataViewModel viewModel : playerDataViewModels) {

                // 此 use case 的 data 物件
                GameDataViewModel gameDataViewModel = createGameDataViewModel(viewModel, playerDataViewModels, roundDataViewModel, notifyDiscardEvent.getGamePhase());

                // 非主公看不到此次 PlayerDrawCardEvent 的抽配 card ids
                viewModels.add(new GameViewModel(List.of(finishActionViewModel, notifyDiscardViewModel),
                        gameDataViewModel,
                        notifyDiscardEvent.getMessage(),
                        notifyDiscardEvent.getGameId(),
                        viewModel.getId())
                );
            }
        } else {
            // 不用棄牌，直接進入下一回合
            RoundEndEvent roundEndEvent = getEvent(events, RoundEndEvent.class).orElseThrow(RuntimeException::new);
            RoundEndViewModel roundEndViewModel = new RoundEndViewModel();
            RoundStartEvent roundStartEvent = getEvent(events, RoundStartEvent.class).orElseThrow(RuntimeException::new);
            JudgementEvent JudgementEvent = getEvent(events, JudgementEvent.class).orElseThrow(RuntimeException::new);
            DrawCardEvent drawCardEvent = getEvent(events, DrawCardEvent.class).orElseThrow(RuntimeException::new);

            RoundStartPresenter.RoundStartViewModel roundStartViewModel = new RoundStartPresenter.RoundStartViewModel();
            RoundStartPresenter.JudgementViewModel judgementViewModel = new RoundStartPresenter.JudgementViewModel();
            RoundStartPresenter.DrawCardViewModel drawCardViewModel = new RoundStartPresenter.DrawCardViewModel();
            RoundStartPresenter.DrawCardDataViewModel drawCardDataViewModel = new RoundStartPresenter.DrawCardDataViewModel(drawCardEvent.getSize(), drawCardEvent.getCardIds(), drawCardEvent.getDrawCardPlayerId());
            drawCardViewModel.setData(drawCardDataViewModel);


            // 取得 drawCardEvent 中的玩家全部資訊
            List<PlayerDataViewModel> playerDataViewModels = drawCardEvent.getSeats().stream().map(PlayerDataViewModel::new).toList();

            // 取得 drawCardEvent 中的回合資訊
            RoundEvent roundEvent = drawCardEvent.getRound();

            // 將回合資訊放入 RoundDataViewModel ，後續會放到 GameDataViewModel
            RoundDataViewModel roundDataViewModel = new RoundDataViewModel(roundEvent);
            String currentRoundPlayerId = roundEvent.getCurrentRoundPlayer();
            for (PlayerDataViewModel viewModel : playerDataViewModels) {

                // 此 use case 的 data 物件
                GameDataViewModel gameDataViewModel = createGameDataViewModel(viewModel, playerDataViewModels, roundDataViewModel, drawCardEvent.getGamePhase());

                // 非主公看不到此次 PlayerDrawCardEvent 的抽配 card ids
                RoundStartPresenter.DrawCardViewModel drawCardViewModelInHidden = hiddenOtherPlayerCardIds(drawCardDataViewModel, viewModel, currentRoundPlayerId);
                viewModels.add(new GameViewModel(List.of(finishActionViewModel, notifyDiscardViewModel, roundEndViewModel, roundStartViewModel, judgementViewModel, drawCardViewModelInHidden),
                        gameDataViewModel,
                        drawCardEvent.getMessage(),
                        drawCardEvent.getGameId(),
                        viewModel.getId())
                );
            }
        }
    }

    private static GameDataViewModel createGameDataViewModel(PlayerDataViewModel viewModel, List<PlayerDataViewModel> playerDataViewModels, RoundDataViewModel roundDataViewModel, String gamePhase) {
        return new GameDataViewModel(
                PlayerDataViewModel.hiddenOtherPlayerRoleInformation(playerDataViewModels, viewModel.getId()), roundDataViewModel, gamePhase);
    }

    private boolean playerNeedToDiscard(NotifyDiscardEvent notifyDiscardEvent) {
        return notifyDiscardEvent.getDiscardCount() > 0;
    }


    private void updateViewModels(PlayCardPresenter.PlayCardViewModel playCardViewModel, PlayCardPresenter.PlayerDamagedViewModel playerDamageEventViewModel) {
        if (playCardViewModel == null) throw new RuntimeException();
        eventToViewModels.add(playCardViewModel);
        if (playerDamageEventViewModel != null) eventToViewModels.add(playerDamageEventViewModel);
    }

    public RoundStartPresenter.DrawCardViewModel hiddenOtherPlayerCardIds(RoundStartPresenter.DrawCardDataViewModel drawCardDataViewModel, PlayerDataViewModel targetPlayerDataViewModel, String currentRoundPlayerId) {
        List<String> cards = drawCardDataViewModel.getCards();
        List<String> hiddenCards = new ArrayList<>();
        if (PlayerDataViewModel.isCurrentRoundPlayer(targetPlayerDataViewModel, currentRoundPlayerId)) {
            hiddenCards.addAll(cards);
        }
        return new RoundStartPresenter.DrawCardViewModel(new RoundStartPresenter.DrawCardDataViewModel(drawCardDataViewModel.getSize(), hiddenCards, drawCardDataViewModel.getDrawCardPlayerId()));
    }

    private PlayCardPresenter.PlayerDamagedViewModel getPlayerDamageEventViewModel(List<DomainEvent> events) {
        PlayerDamagedEvent playerDamagedEvent = getEvent(events, PlayerDamagedEvent.class).orElse(null);
        if (playerDamagedEvent != null) {
            PlayCardPresenter.PlayerDamagedDataViewModel playerDamagedDataViewModel = new PlayCardPresenter.PlayerDamagedDataViewModel(playerDamagedEvent.getPlayerId(), playerDamagedEvent.getFrom(), playerDamagedEvent.getTo());
            return new PlayCardPresenter.PlayerDamagedViewModel(playerDamagedDataViewModel);
        }
        return null;
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
    public static class FinishActionViewModel extends ViewModel<Object> {
        public FinishActionViewModel() {
            super("FinishActionEvent", "", "結束出牌");
        }
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

        public GameViewModel(List<ViewModel> viewModels, GameDataViewModel data, String message, String gameId, String playerId) {
            super(viewModels, data, message);
            this.gameId = gameId;
            this.playerId = playerId;
        }


    }
}
