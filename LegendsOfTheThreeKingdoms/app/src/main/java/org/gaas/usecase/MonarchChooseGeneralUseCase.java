package org.gaas.usecase;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.gaas.domain.Game;
import org.gaas.domain.events.DomainEvent;
import org.gaas.GameRepository;

import javax.inject.Named;
import java.util.List;

@Named
@RequiredArgsConstructor
public class MonarchChooseGeneralUseCase {
    private final GameRepository repository;

    public void execute(String gameId, MonarchChooseGeneralRequest request, MonarchChooseGeneralCardPresenter presenter) {
        Game game = repository.findGameById(gameId);
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
