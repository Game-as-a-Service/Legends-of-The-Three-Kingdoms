package org.gaas.usecase;

import lombok.RequiredArgsConstructor;
import org.gaas.GameRepository;
import org.gaas.domain.Game;
import org.gaas.domain.events.DomainEvent;


import javax.inject.Named;
import java.util.List;
@Named
@RequiredArgsConstructor
public class DiscardCardUseCase {

    private final GameRepository repository;

    public void execute(String gameId, List<String> cardIds, DiscardPresenter presenter) {
        Game game = repository.findGameById(gameId);
        List<DomainEvent> events = game.playerDiscardCard(cardIds);
        repository.save(game);
        presenter.renderEvents(events);
    }

    public  interface DiscardPresenter<T> {
        void renderEvents(List<DomainEvent> events);
        T present();
    }
}
