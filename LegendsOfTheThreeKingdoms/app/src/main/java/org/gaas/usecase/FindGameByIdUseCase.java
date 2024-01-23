package org.gaas.usecase;

import lombok.RequiredArgsConstructor;
import org.gaas.domain.Game;
import org.gaas.GameRepository;
import javax.inject.Named;

@Named
@RequiredArgsConstructor
public class FindGameByIdUseCase {

    private GameRepository repository;

    public void findGameById(String gameId, String playerId, FindGamePresenter presenter) {
        Game game = repository.findGameById(gameId);
        presenter.renderGame(game, playerId);
    }

    public interface FindGamePresenter<T> {
        void renderGame(Game game, String playerId);
        T present();
    }
}
