package com.gaas.threeKingdoms.usecase;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.outport.GameRepository;
import jakarta.inject.Named;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Named
public class FindGameByIdUseCase {

    private final GameRepository repository;

    public void findGameById(String gameId, String playerId, FindGamePresenter presenter) {
        Game game = repository.findById(gameId);
        presenter.renderGame(game, playerId);
    }

    public interface FindGamePresenter<T> {
        void renderGame(Game game, String playerId);

        T present();
    }
}
