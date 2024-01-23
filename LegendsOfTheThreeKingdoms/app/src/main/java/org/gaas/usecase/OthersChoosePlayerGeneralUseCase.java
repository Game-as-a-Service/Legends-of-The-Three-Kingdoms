package org.gaas.usecase;

import lombok.RequiredArgsConstructor;
import org.gaas.domain.Game;
import org.gaas.domain.events.DomainEvent;
import org.gaas.GameRepository;

import javax.inject.Named;
import java.util.List;

@Named
@RequiredArgsConstructor
public class OthersChoosePlayerGeneralUseCase {

    private GameRepository repository;

    public void execute(String gameId, MonarchChooseGeneralUseCase.MonarchChooseGeneralRequest request, InitialEndPresenter initialEndPresenter, RoundStartPresenter roundStartPresenter) {
        Game game = repository.findGameById(gameId);
        List<DomainEvent> events = game.othersChoosePlayerGeneral(request.getPlayerId(), request.getGeneralId());
        repository.save(game);
        initialEndPresenter.renderEvents(events);
        roundStartPresenter.renderEvents(events);
    }


    public interface InitialEndPresenter<T> {
        void renderEvents(List<DomainEvent> events);
        T present();
    }

    public interface RoundStartPresenter<T> {
        void renderEvents(List<DomainEvent> events);
        T present();
    }


}
