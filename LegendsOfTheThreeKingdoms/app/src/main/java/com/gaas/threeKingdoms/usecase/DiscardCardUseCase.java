package com.gaas.threeKingdoms.usecase;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.exception.NotFoundException;
import com.gaas.threeKingdoms.outport.GameRepository;
import jakarta.inject.Named;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Named
public class DiscardCardUseCase {

    private final GameRepository repository;

    public void execute(String gameId, List<String> cardIds, DiscardPresenter presenter) {
        Game game = repository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found"));
        List<DomainEvent> events = game.playerDiscardCard(cardIds);
        repository.save(game);
        presenter.renderEvents(events);
    }

    public interface DiscardPresenter<T> {
        void renderEvents(List<DomainEvent> events);

        T present();
    }
}
