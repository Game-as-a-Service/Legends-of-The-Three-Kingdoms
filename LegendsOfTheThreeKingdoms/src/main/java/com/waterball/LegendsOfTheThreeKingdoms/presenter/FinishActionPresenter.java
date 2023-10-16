package com.waterball.LegendsOfTheThreeKingdoms.presenter;

import com.waterball.LegendsOfTheThreeKingdoms.domain.events.*;
import com.waterball.LegendsOfTheThreeKingdoms.presenter.common.GameDataViewModel;
import com.waterball.LegendsOfTheThreeKingdoms.presenter.common.PlayerDataViewModel;
import com.waterball.LegendsOfTheThreeKingdoms.presenter.common.RoundDataViewModel;
import com.waterball.LegendsOfTheThreeKingdoms.service.GameService;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

import static com.waterball.LegendsOfTheThreeKingdoms.presenter.RoundStartPresenter.hiddenOtherPlayerCardIds;
import static com.waterball.LegendsOfTheThreeKingdoms.presenter.ViewModel.getEvent;

public class FinishActionPresenter implements GameService.Presenter<List<FinishActionPresenter.GameViewModel>> {
    List<ViewModel> eventToViewModels = new ArrayList<>();
    private List<FinishActionPresenter.GameViewModel> viewModels = new ArrayList<>();

    public void renderEvents(List<DomainEvent> events) {
        updateFinishActionEventToViewModel(events);
    }

    @Override
    public List<FinishActionPresenter.GameViewModel> present() {
        return viewModels;
    }

    private void updateFinishActionEventToViewModel(List<DomainEvent> events) {
        FinishActionEvent finishActionEvent = getEvent(events, FinishActionEvent.class).orElseThrow(RuntimeException::new);
        FinishActionViewModel finishActionViewModel = new FinishActionViewModel();
        NotifyDiscardEvent notifyDiscardEvent = getEvent(events, NotifyDiscardEvent.class).orElseThrow(RuntimeException::new);
        NotifyDiscardViewModel notifyDiscardViewModel = new NotifyDiscardViewModel(new NotifyDiscardDataViewModel(notifyDiscardEvent.getDiscardCount()));
        RoundEndEvent roundEndEvent = getEvent(events, RoundEndEvent.class).orElseThrow(RuntimeException::new);
        RoundEndViewModel roundEndViewModel = new RoundEndViewModel();

        if (notifyDiscardEvent.getDiscardCount() == 0) {
            RoundStartEvent roundStartEvent = getEvent(events, RoundStartEvent.class).orElseThrow(RuntimeException::new);
            JudgementEvent JudgementEvent = getEvent(events, JudgementEvent.class).orElseThrow(RuntimeException::new);
            DrawCardEvent drawCardEvent = getEvent(events, DrawCardEvent.class).orElseThrow(RuntimeException::new);

            RoundStartPresenter.RoundStartViewModel roundStartViewModel = new RoundStartPresenter.RoundStartViewModel();
            RoundStartPresenter.JudgementViewModel judgementViewModel = new RoundStartPresenter.JudgementViewModel();
            RoundStartPresenter.DrawCardViewModel drawCardViewModel = new RoundStartPresenter.DrawCardViewModel();
            RoundStartPresenter.DrawCardDataViewModel drawCardDataViewModel = new RoundStartPresenter.DrawCardDataViewModel(drawCardEvent.getSize(), drawCardEvent.getCardIds());
            drawCardViewModel.setData(drawCardDataViewModel);


            // 取得 drawCardEvent 中的玩家全部資訊
            List<PlayerDataViewModel> playerDataViewModels = drawCardEvent.getSeats().stream().map(PlayerDataViewModel::new).toList();

            // 取得 drawCardEvent 中的回合資訊
            RoundEvent roundEvent = drawCardEvent.getRound();

            // 將回合資訊放入 RoundDataViewModel ，後續會放到 GameDataViewModel
            RoundDataViewModel roundDataViewModel = new RoundDataViewModel(roundEvent);
            for (PlayerDataViewModel viewModel : playerDataViewModels) {

                // 此 use case 的 data 物件
                GameDataViewModel gameDataViewModel = new GameDataViewModel(
                        PlayerDataViewModel.hiddenOtherPlayerRoleInformation(
                                playerDataViewModels, viewModel.getId()), roundDataViewModel, drawCardEvent.getGamePhase()
                );

                // 非主公看不到此次 PlayerDrawCardEvent 的抽配 card ids
                RoundStartPresenter.DrawCardViewModel drawCardViewModelInHidden = hiddenOtherPlayerCardIds(drawCardDataViewModel, viewModel);
                viewModels.add(new FinishActionPresenter.GameViewModel(List.of(finishActionViewModel, notifyDiscardViewModel, roundEndViewModel, roundStartViewModel, judgementViewModel, drawCardViewModelInHidden),
                        gameDataViewModel,
                        drawCardEvent.getMessage(),
                        drawCardEvent.getGameId(),
                        viewModel.getId())
                );
            }
        } else {

        }
    }

    private void updateViewModels(PlayCardPresenter.PlayCardViewModel playCardViewModel, PlayCardPresenter.PlayerDamagedViewModel playerDamageEventViewModel) {
        if (playCardViewModel == null) throw new RuntimeException();
        eventToViewModels.add(playCardViewModel);
        if (playerDamageEventViewModel != null) eventToViewModels.add(playerDamageEventViewModel);
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
