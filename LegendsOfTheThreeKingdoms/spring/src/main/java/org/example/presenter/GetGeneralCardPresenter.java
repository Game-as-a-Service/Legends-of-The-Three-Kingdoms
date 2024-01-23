package org.example.presenter;

import org.gaas.domain.events.GetMonarchGeneralCardsEvent;
import org.gaas.domain.generalcard.GeneralCard;
import org.gaas.usecase.StartGameUseCase;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

public class GetGeneralCardPresenter implements StartGameUseCase.GetGeneralCardPresenter<GetGeneralCardPresenter.GetGeneralCardViewModel> {

    private GetGeneralCardViewModel viewModel;

    public void renderGame(GetMonarchGeneralCardsEvent event, String gameId, String monarchPlayerId) {
        viewModel = new GetGeneralCardViewModel(event.getGeneralCardsList().stream()
                .map(GeneralCard::getGeneralID)
                .collect(Collectors.toList()), "可選擇的武將", gameId, monarchPlayerId);
    }

    public GetGeneralCardViewModel present() {
        return viewModel;
    }


    @Data
    @NoArgsConstructor
    public static class GetGeneralCardViewModel extends ViewModel<List<String>> {
        private String gameId;
        private String playerId;

        public GetGeneralCardViewModel(List<String> data, String message, String gameId, String playerId) {
            super("getGeneralCardEvent", data, message);
            this.gameId = gameId;
            this.playerId = playerId;
        }

    }

}
