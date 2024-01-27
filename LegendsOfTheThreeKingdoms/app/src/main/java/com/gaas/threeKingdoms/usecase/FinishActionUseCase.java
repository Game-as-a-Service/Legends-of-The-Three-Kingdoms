package com.gaas.threeKingdoms.usecase;

import lombok.RequiredArgsConstructor;
import com.gaas.threeKingdoms.outport.GameRepository;
import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.DomainEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.inject.Named;
import java.util.List;

@Component
public class FinishActionUseCase {

    private final GameRepository gameRepository;

    @Autowired
    public FinishActionUseCase(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

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
