package com.waterball.LegendsOfTheThreeKingdoms.presenter;

import com.waterball.LegendsOfTheThreeKingdoms.domain.events.GetMonarchGeneralCardsEvent;
import com.waterball.LegendsOfTheThreeKingdoms.domain.generalcard.General;
import com.waterball.LegendsOfTheThreeKingdoms.domain.generalcard.GeneralCard;
import com.waterball.LegendsOfTheThreeKingdoms.service.GameService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

public class GetGeneralCardPresenter implements GameService.Presenter<GetGeneralCardPresenter.GetGeneralCardViewModel> {

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
