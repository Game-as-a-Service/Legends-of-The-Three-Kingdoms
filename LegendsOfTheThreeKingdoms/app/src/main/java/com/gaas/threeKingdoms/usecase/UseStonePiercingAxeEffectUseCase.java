package com.gaas.threeKingdoms.usecase;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.AskStonePiercingAxeEffectEvent;
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
public class UseStonePiercingAxeEffectUseCase {
    private final GameRepository gameRepository;

    public void execute(String gameId, UseStonePiercingAxeEffectRequest request, UseStonePiercingAxeEffectPresenter presenter) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found"));
        List<DomainEvent> events = game.playerUseStonePiercingAxeEffect(
                request.playerId,
                AskStonePiercingAxeEffectEvent.Choice.valueOf(request.choice),
                request.discardCardIds
        );
        gameRepository.save(game);
        presenter.renderEvents(events);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UseStonePiercingAxeEffectRequest {
        private String playerId;
        private String choice;              // "DISCARD_TWO" or "SKIP"
        private List<String> discardCardIds; // exactly 2 cardIds when choice is DISCARD_TWO
    }

    public interface UseStonePiercingAxeEffectPresenter<T> {
        void renderEvents(List<DomainEvent> events);

        T present();
    }
}
