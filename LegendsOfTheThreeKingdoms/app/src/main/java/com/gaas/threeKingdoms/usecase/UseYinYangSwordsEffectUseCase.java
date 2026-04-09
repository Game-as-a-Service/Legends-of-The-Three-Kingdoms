package com.gaas.threeKingdoms.usecase;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.YinYangSwordsEffectEvent;
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
public class UseYinYangSwordsEffectUseCase {
    private final GameRepository gameRepository;

    public void execute(String gameId, UseYinYangSwordsEffectRequest request, UseYinYangSwordsEffectPresenter presenter) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found"));
        List<DomainEvent> events = game.playerUseYinYangSwordsEffect(
                request.playerId,
                YinYangSwordsEffectEvent.Choice.valueOf(request.choice),
                request.cardId
        );
        gameRepository.save(game);
        presenter.renderEvents(events);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UseYinYangSwordsEffectRequest {
        private String playerId;
        private String choice;  // "TARGET_DISCARDS" or "ATTACKER_DRAWS"
        private String cardId;  // only required when choice is TARGET_DISCARDS
    }

    public interface UseYinYangSwordsEffectPresenter<T> {
        void renderEvents(List<DomainEvent> events);

        T present();
    }
}
