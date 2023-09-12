package com.waterball.LegendsOfTheThreeKingdoms.presenter;

import com.waterball.LegendsOfTheThreeKingdoms.domain.events.DomainEvent;
import com.waterball.LegendsOfTheThreeKingdoms.service.GameService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class RoundStartPresenter implements GameService.Presenter<List<RoundStartPresenter.RoundStartViewModel>> {

    private List<RoundStartPresenter.RoundStartViewModel> viewModels;

    public List<RoundStartPresenter.RoundStartViewModel> present() {
        return viewModels;
    }

    public void renderEvents(List<DomainEvent> events){

    }

//    @Data
//    @NoArgsConstructor
//    public static class RoundStartViewModel extends ViewModel<RoundStartPresenter.InitialViewModel> {
//        private String gameId;
//        private String playerId;
//
//        public RoundStartViewModel(String gameId, RoundStartPresenter.RoundStartDataViewModel data, String message, String playerId) {
//            super("RoundStartViewModel", data, message);
//            this.gameId = gameId;
//            this.playerId = playerId;
//        }
//    }

    @Data
    @NoArgsConstructor
    public static class RoundStartViewModel extends GameProcessViewModel<RoundStartPresenter.RoundStartDataViewModel> {
        private String gameId;
        private String playerId;

        public RoundStartViewModel(List<ViewModel> events, RoundStartDataViewModel data, String message, String gameId, String playerId) {
            super(events, data, message);
            this.gameId = gameId;
            this.playerId = playerId;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoundStartDataViewModel {
        private List<InitialEndPresenter.PlayerDataViewModel> seats;
        private InitialEndPresenter.RoundDataViewModel round;
        private String gamePhase;
    }
}
