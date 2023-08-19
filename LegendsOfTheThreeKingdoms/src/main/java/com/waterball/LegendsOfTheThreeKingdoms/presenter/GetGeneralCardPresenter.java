package com.waterball.LegendsOfTheThreeKingdoms.presenter;

import com.waterball.LegendsOfTheThreeKingdoms.domain.generalcard.General;
import com.waterball.LegendsOfTheThreeKingdoms.domain.generalcard.GeneralCard;
import com.waterball.LegendsOfTheThreeKingdoms.service.GameService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class GetGeneralCardPresenter implements GameService.Presenter<GetGeneralCardPresenter.GetGeneralCardViewModel> {

    private GetGeneralCardViewModel viewModel;

    public void renderGame(List<GeneralCard> generalList, String gameId, String playerId) {
        viewModel = new GetGeneralCardViewModel(generalList, gameId, playerId);
    }


    public GetGeneralCardViewModel present() {
        return viewModel;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GetGeneralCardViewModel {
        private List<GeneralCard> generalList;
        private String gameId;
        private String playerId;
    }
}
