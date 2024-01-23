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
public class PlayCardUseCase {

    private final GameRepository gameRepository;

    public void execute(String gameId, PlayCardRequest request, PlayCardPresenter presenter) {
        Game game = gameRepository.findGameById(gameId);
        List<DomainEvent> events = game.playerPlayCard(request.playerId, request.cardId, request.targetPlayerId, request.playType);
        gameRepository.save(game);
        presenter.renderEvents(events);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlayCardRequest {
        private String playerId;
        private String targetPlayerId;
        private String cardId;
        private String playType;
    }

    public interface PlayCardPresenter<T> {
        void renderEvents(List<DomainEvent> events);
        T present();
    }

}
