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
public class OthersChoosePlayerGeneralUseCase {

    private final GameRepository repository;

    public void execute(String gameId, MonarchChooseGeneralUseCase.MonarchChooseGeneralRequest request, InitialEndPresenter initialEndPresenter, RoundStartPresenter roundStartPresenter, SelectionStatusPresenter selectionStatusPresenter) {
        Game game = repository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found"));
        List<DomainEvent> events = game.othersChoosePlayerGeneral(request.getPlayerId(), request.getGeneralId());
        repository.save(game);
        initialEndPresenter.renderEvents(events);
        roundStartPresenter.renderEvents(events);
        selectionStatusPresenter.renderEvents(events);
    }


    public interface InitialEndPresenter<T> {
        void renderEvents(List<DomainEvent> events);

        T present();
    }

    public interface RoundStartPresenter<T> {
        void renderEvents(List<DomainEvent> events);

        T present();
    }

    /** 選將進度廣播（issue #237） */
    public interface SelectionStatusPresenter<T> {
        void renderEvents(List<DomainEvent> events);

        T present();
    }


}
