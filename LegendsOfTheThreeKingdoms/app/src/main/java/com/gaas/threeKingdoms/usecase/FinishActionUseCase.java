package com.gaas.threeKingdoms.usecase;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.outport.GameRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class FinishActionUseCase {

    private final GameRepository gameRepository;


    public void execute(String gameId, String playerId, FinishActionPresenter presenter) {
        Game game = gameRepository.findById(gameId);
        List<DomainEvent> events = game.finishAction(playerId);
        gameRepository.save(game);
        presenter.renderEvents(events);
    }


    public interface FinishActionPresenter<T> {
        void renderEvents(List<DomainEvent> events);

        T present();
    }

}
