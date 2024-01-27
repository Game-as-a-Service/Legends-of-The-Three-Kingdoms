package com.gaas.threeKingdoms.usecase;

import com.gaas.threeKingdoms.outport.GameRepository;
import lombok.RequiredArgsConstructor;
import com.gaas.threeKingdoms.Game;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.inject.Named;

@Component
public class FindGameByIdUseCase {

    private final GameRepository repository;

    @Autowired
    public FindGameByIdUseCase(GameRepository repository) {
        this.repository = repository;
    }

    public void findGameById(String gameId, String playerId, FindGamePresenter presenter) {
        Game game = repository.findById(gameId);
        presenter.renderGame(game, playerId);
    }

    public interface FindGamePresenter<T> {
        void renderGame(Game game, String playerId);
        T present();
    }
}
