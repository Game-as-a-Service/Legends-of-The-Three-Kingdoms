package com.gaas.threeKingdoms.presenter;

import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.presenter.common.GameDataViewModel;
import com.gaas.threeKingdoms.presenter.common.PlayerDataViewModel;
import com.gaas.threeKingdoms.presenter.common.RoundDataViewModel;
import lombok.*;
import com.gaas.threeKingdoms.usecase.OthersChoosePlayerGeneralUseCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RoundStartPresenter implements OthersChoosePlayerGeneralUseCase.RoundStartPresenter<List<RoundStartPresenter.PlayerTakeTurnViewModel>> {

    private List<PlayerTakeTurnViewModel> viewModels;


    public List<PlayerTakeTurnViewModel> present() {
        return viewModels;
    }

    public void renderEvents(List<DomainEvent> events) {
        if (!events.isEmpty()) {
            viewModels = new ArrayList<>();
            updateEventToPlayerTakeTurnViewModel(events);
        } else {
            viewModels = Collections.emptyList();
        }
    }

    private void updateEventToPlayerTakeTurnViewModel(List<DomainEvent> events) {
        // 取三個DomainEvent
        // TODO roundStartEvent, judgementEvent暫時沒用到
        RoundStartEvent roundStartEvent = ViewModel.getEvent(events, RoundStartEvent.class).orElseThrow(RuntimeException::new);
        JudgementEvent judgementEvent = ViewModel.getEvent(events, JudgementEvent.class).orElseThrow(RuntimeException::new);
        DrawCardEvent drawCardEvent = ViewModel.getEvent(events, DrawCardEvent.class).orElseThrow(RuntimeException::new);

        // 三種 event 的 view model
        RoundStartViewModel roundStartViewModel = new RoundStartViewModel();
        JudgementViewModel judgementViewModel = new JudgementViewModel();
        DrawCardViewModel drawCardViewModel = new DrawCardViewModel();
        DrawCardDataViewModel drawCardDataViewModel = new DrawCardDataViewModel(drawCardEvent.getSize(), drawCardEvent.getCardIds(), drawCardEvent.getDrawCardPlayerId());
        drawCardViewModel.setData(drawCardDataViewModel);

        // 取得 drawCardEvent 中的玩家全部資訊
        List<PlayerDataViewModel> playerDataViewModels = drawCardEvent.getSeats().stream().map(PlayerDataViewModel::new).toList();

        // 取得 drawCardEvent 中的回合資訊
        RoundEvent roundEvent = drawCardEvent.getRound();
        String currentRoundPlayerId = roundEvent.getCurrentRoundPlayer();

        // 將回合資訊放入 RoundDataViewModel ，後續會放到 GameDataViewModel
        RoundDataViewModel roundDataViewModel = new RoundDataViewModel(roundEvent);

        for (PlayerDataViewModel viewModel : playerDataViewModels) {

            // 此 use case 的 data 物件
            GameDataViewModel GameDataViewModel = new GameDataViewModel(
                    PlayerDataViewModel.hiddenOtherPlayerRoleInformation(
                            playerDataViewModels, viewModel.getId()), roundDataViewModel, drawCardEvent.getGamePhase()
            );

            // 非主公看不到此次 PlayerDrawCardEvent 的抽配 card ids
            DrawCardViewModel drawCardViewModelInHidden = hiddenOtherPlayerCardIds(drawCardDataViewModel, viewModel, currentRoundPlayerId);

            viewModels.add(new PlayerTakeTurnViewModel(List.of(roundStartViewModel, judgementViewModel, drawCardViewModelInHidden),
                    GameDataViewModel,
                    drawCardEvent.getMessage(),
                    drawCardEvent.getGameId(),
                    viewModel.getId())
            );
        }
    }

    public DrawCardViewModel hiddenOtherPlayerCardIds(DrawCardDataViewModel drawCardDataViewModel, PlayerDataViewModel targetPlayerDataViewModel, String currentRoundPlayerId) {
        List<String> cards = drawCardDataViewModel.getCards();
        List<String> hiddenCards = new ArrayList<>();
        if (PlayerDataViewModel.isCurrentRoundPlayer(targetPlayerDataViewModel, currentRoundPlayerId)) {
            hiddenCards.addAll(cards);
        }
        return new DrawCardViewModel(new DrawCardDataViewModel(drawCardDataViewModel.getSize(), hiddenCards, drawCardDataViewModel.getDrawCardPlayerId()));
    }

    @Setter
    @Getter
    public static class RoundStartViewModel extends ViewModel<Object> {
        public RoundStartViewModel() {
            super("RoundStartEvent", "", "回合已開始");
        }
    }


    @Setter
    @Getter
    public static class JudgementViewModel extends ViewModel<Object> {
        public JudgementViewModel() {
            super("JudgementEvent", "", "判定結束");
        }
    }


    @Data
    public static class DrawCardViewModel extends ViewModel<DrawCardDataViewModel> {
        private DrawCardDataViewModel data;

        public DrawCardViewModel() {
            super("DrawCardEvent", null, "玩家摸牌");
        }

        public DrawCardViewModel(DrawCardDataViewModel data) {
            super("DrawCardEvent", null, "玩家摸牌");
            this.data = data;
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DrawCardDataViewModel {
        private int size;
        private List<String> cards;
        private String drawCardPlayerId;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PlayerTakeTurnViewModel extends GameProcessViewModel<GameDataViewModel> {
        private String gameId;
        private String playerId;

        public PlayerTakeTurnViewModel(List<ViewModel> events, GameDataViewModel data, String message, String gameId, String playerId) {
            super(events, data, message);
            this.gameId = gameId;
            this.playerId = playerId;
        }
    }


}
