package org.example.presenter;

import org.gaas.domain.events.*;
import org.gaas.domain.handcard.HandCard;
import org.example.presenter.common.GameDataViewModel;
import org.example.presenter.common.PlayerDataViewModel;
import org.example.presenter.common.RoundDataViewModel;
import com.waterball.LegendsOfTheThreeKingdoms.service.GameService;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

import static org.example.presenter.ViewModel.getEvent;

public class DiscardPresenter implements GameService.Presenter<List<DiscardPresenter.GameViewModel>> {

    private List<GameViewModel> viewModels = new ArrayList<>();

    public void renderEvents(List<DomainEvent> events) {
        DiscardEvent discardEvent = getEvent(events, DiscardEvent.class).orElseThrow(RuntimeException::new);
        DiscardViewModel discardViewModel = new DiscardViewModel(discardEvent);
        RoundEndEvent roundEndEvent = getEvent(events, RoundEndEvent.class).orElseThrow(RuntimeException::new);
        FinishActionPresenter.RoundEndViewModel roundEndViewModel = new FinishActionPresenter.RoundEndViewModel();
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
            viewModels.add(new GameViewModel(List.of(discardViewModel, roundEndViewModel, roundStartViewModel, judgementViewModel, drawCardViewModelInHidden),
                    gameDataViewModel,
                    drawCardEvent.getMessage(),
                    drawCardEvent.getGameId(),
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

        public GameViewModel(List<ViewModel> viewModels, GameDataViewModel data, String message, String gameId, String playerId) {
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
