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
public class UseSnatchUseCase {

    private final GameRepository gameRepository;

    public void execute(String gameId, UseSnatchUseCase.UseSnatchRequest request, UseSnatchUseCase.UseSnatchPresenter presenter) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found"));
        List<DomainEvent> events = game.useSnatchEffect(request.currentPlayerId, request.targetPlayerId, request.cardId, request.targetCardIndex);
        gameRepository.save(game);
        presenter.renderEvents(events);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UseSnatchRequest {
        private String currentPlayerId;
        private String targetPlayerId;
        private String cardId;
        private Integer targetCardIndex;
    }

    public interface UseSnatchPresenter<T> {
        void renderEvents(List<DomainEvent> events);

        T present();
    }
}
