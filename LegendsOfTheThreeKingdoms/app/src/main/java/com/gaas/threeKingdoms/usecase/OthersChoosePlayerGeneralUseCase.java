package com.gaas.threeKingdoms.usecase;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.outport.GameRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class OthersChoosePlayerGeneralUseCase {

    private final GameRepository repository;

    public void execute(String gameId, MonarchChooseGeneralUseCase.MonarchChooseGeneralRequest request, InitialEndPresenter initialEndPresenter, RoundStartPresenter roundStartPresenter) {
        Game game = repository.findById(gameId);
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
