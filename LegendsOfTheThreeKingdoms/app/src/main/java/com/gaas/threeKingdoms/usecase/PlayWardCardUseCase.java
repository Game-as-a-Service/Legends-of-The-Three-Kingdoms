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
public class PlayWardCardUseCase {

    private final GameRepository gameRepository;

    public void execute(String gameId, PlayWardCardUseCase.PlayWardCardRequest request, PlayWardCardPresenter presenter) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found"));
        List<DomainEvent> events = game.playWardCard(request.playerId, request.cardId, request.playType);
        gameRepository.save(game);
        presenter.renderEvents(events);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlayWardCardRequest {
        private String playerId;
        private String cardId;
        private String playType;
    }

    public interface PlayWardCardPresenter<T> {
        void renderEvents(List<DomainEvent> events);

        T present();
    }

}
