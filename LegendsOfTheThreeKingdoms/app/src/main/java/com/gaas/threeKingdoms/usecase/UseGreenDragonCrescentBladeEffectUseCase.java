package com.gaas.threeKingdoms.usecase;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.AskGreenDragonCrescentBladeEffectEvent;
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
public class UseGreenDragonCrescentBladeEffectUseCase {
    private final GameRepository gameRepository;

    public void execute(String gameId, UseGreenDragonCrescentBladeEffectRequest request, UseGreenDragonCrescentBladeEffectPresenter presenter) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found"));
        List<DomainEvent> events = game.playerUseGreenDragonCrescentBladeEffect(
                request.playerId,
                AskGreenDragonCrescentBladeEffectEvent.Choice.valueOf(request.choice),
                request.killCardId
        );
        gameRepository.save(game);
        presenter.renderEvents(events);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UseGreenDragonCrescentBladeEffectRequest {
        private String playerId;
        private String choice;    // "KILL" or "SKIP"
        private String killCardId; // only required when choice is KILL
    }

    public interface UseGreenDragonCrescentBladeEffectPresenter<T> {
        void renderEvents(List<DomainEvent> events);

        T present();
    }
}
