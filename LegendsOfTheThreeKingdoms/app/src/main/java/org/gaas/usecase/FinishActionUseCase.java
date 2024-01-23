package org.gaas.usecase;

import lombok.RequiredArgsConstructor;
import org.gaas.GameRepository;
import org.gaas.domain.Game;
import org.gaas.domain.events.DomainEvent;

import javax.inject.Named;
import java.util.List;

@Named
@RequiredArgsConstructor
public class FinishActionUseCase {

    private final GameRepository gameRepository;

    public void execute(String gameId, String playerId, FinishActionPresenter presenter) {
        Game game = gameRepository.findGameById(gameId);
        List<DomainEvent> events = game.finishAction(playerId);
        gameRepository.save(game);
        presenter.renderEvents(events);
    }


    public interface FinishActionPresenter<T> {
        void renderEvents(List<DomainEvent> events);

        T present();
    }

}
