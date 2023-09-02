package com.waterball.LegendsOfTheThreeKingdoms.presenter;

import com.waterball.LegendsOfTheThreeKingdoms.domain.events.DomainEvent;
import com.waterball.LegendsOfTheThreeKingdoms.domain.events.GetGeneralCardByOthersEvent;
import com.waterball.LegendsOfTheThreeKingdoms.domain.events.MonarchChooseGeneralCardEvent;
import com.waterball.LegendsOfTheThreeKingdoms.service.GameService;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.waterball.LegendsOfTheThreeKingdoms.presenter.ViewModel.getEvent;
import static com.waterball.LegendsOfTheThreeKingdoms.presenter.ViewModel.getEvents;

@RequiredArgsConstructor
public class MonarchChooseGeneralCardPresenter implements GameService.Presenter<MonarchChooseGeneralCardPresenter.MonarchChooseGeneralCardViewModel> {

    private MonarchChooseGeneralCardViewModel viewModel;
    private List<GetGeneralCardByOthersViewModel> generalCardByOtherViewModels;


    public void renderEvents(List<DomainEvent> events) {
        Optional<MonarchChooseGeneralCardEvent> monarchChooseGeneralCardEvent = getEvent(events, MonarchChooseGeneralCardEvent.class);
        viewModel = monarchChooseGeneralCardEvent
                .map(e -> new MonarchChooseGeneralCardViewModel(e.getGameId(), e.getPlayerIds(), "MonarchGeneralChosenEvent", new MonarchChooseGeneralDataViewModel(e.getGeneralCard().getGeneralID()), e.getMessage()))
                .orElse(null);
        String gameId = viewModel.gameId;

        List<GetGeneralCardByOthersEvent> getGeneralCardByOthersEvent = getEvents(events, GetGeneralCardByOthersEvent.class);

        generalCardByOtherViewModels = getGeneralCardByOthersEvent
                .stream()
                .map(e -> new GetGeneralCardByOthersViewModel(gameId, e.getPlayerId(), "getGeneralCardEventByOthers", e.getGeneralCardsList().stream().map(generalCard -> generalCard.getGeneralID()).toList(), e.getMessage()))
                .toList();
    }

    @Override
    public MonarchChooseGeneralCardViewModel present() {
        return viewModel;
    }

    public List<GetGeneralCardByOthersViewModel> presentGeneralCardByOthers(){
        return generalCardByOtherViewModels;
    }


    @Data
    @NoArgsConstructor
    public static class MonarchChooseGeneralCardViewModel extends ViewModel<MonarchChooseGeneralDataViewModel> {
        private String gameId;
        private List<String> playerIds;

        public MonarchChooseGeneralCardViewModel(String gameId, List<String> playerIds, String event, MonarchChooseGeneralDataViewModel data, String message) {
            super(event, data, message);
            this.gameId = gameId;
            this.playerIds = playerIds;
        }

    }

    @Data
    @NoArgsConstructor
    public static class GetGeneralCardByOthersViewModel extends ViewModel<List<String>> {
        private String gameId;
        private String playerId;

        public GetGeneralCardByOthersViewModel(String gameId, String playerId, String event, List<String> data, String message) {
            super(event, data, message);
            this.gameId = gameId;
            this.playerId = playerId;
        }

        public int size() {
            return data.size();
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MonarchChooseGeneralDataViewModel {
        String monarchGeneralCard;
    }

}

