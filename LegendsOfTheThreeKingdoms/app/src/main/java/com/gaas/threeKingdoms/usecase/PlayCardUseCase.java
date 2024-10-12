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
public class PlayCardUseCase {

    private final GameRepository gameRepository;


    public void execute(String gameId, PlayCardRequest request, PlayCardPresenter presenter) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found"));
        List<DomainEvent> events = game.playerPlayCard(request.playerId, request.cardId, request.targetPlayerId, request.playType);
        gameRepository.save(game);
        presenter.renderEvents(events);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlayCardRequest {
        private String playerId;
        private String targetPlayerId;
        private String cardId;
        private String playType;
    }

    public interface PlayCardPresenter<T> {
        void renderEvents(List<DomainEvent> events);

        T present();
    }

}
