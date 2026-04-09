package com.gaas.threeKingdoms.usecase;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.exception.NotFoundException;
import com.gaas.threeKingdoms.outport.GameRepository;
import jakarta.inject.Named;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Named
public class UseViperSpearKillUseCase {
    private final GameRepository gameRepository;

    public void execute(String gameId, UseViperSpearKillRequest request, UseViperSpearKillPresenter presenter) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found"));
        List<DomainEvent> events = game.playerUseViperSpearKill(
                request.playerId, request.targetPlayerId, request.discardCardIds);
        gameRepository.save(game);
        presenter.renderEvents(events);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UseViperSpearKillRequest {
        private String playerId;
        private String targetPlayerId;
        private List<String> discardCardIds; // exactly 2 cardIds
    }

    public interface UseViperSpearKillPresenter<T> {
        void renderEvents(List<DomainEvent> events);

        T present();
    }
}
