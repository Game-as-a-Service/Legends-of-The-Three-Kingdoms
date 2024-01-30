package com.gaas.threeKingdoms.usecase;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.outport.GameRepository;
import jakarta.inject.Named;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Named
public class MonarchChooseGeneralUseCase {

    private final GameRepository repository;


    public void execute(String gameId, MonarchChooseGeneralRequest request, MonarchChooseGeneralCardPresenter presenter) {
        Game game = repository.findById(gameId);
        List<DomainEvent> events = game.monarchChoosePlayerGeneral(request.getPlayerId(), request.getGeneralId());
        repository.save(game);
        presenter.renderEvents(events);
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonarchChooseGeneralRequest {
        private String playerId;
        private String generalId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OthersChooseGeneralRequest {
        private String playerId;
        private String generalId;
    }

    public interface MonarchChooseGeneralCardPresenter<T> {
        void renderEvents(List<DomainEvent> events);

        T present();
    }
}
