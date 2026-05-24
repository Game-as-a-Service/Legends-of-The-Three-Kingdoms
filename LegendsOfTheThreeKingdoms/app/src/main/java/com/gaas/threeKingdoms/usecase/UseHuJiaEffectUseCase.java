package com.gaas.threeKingdoms.usecase;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.AskHuJiaEffectEvent;
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
public class UseHuJiaEffectUseCase {
    private final GameRepository gameRepository;

    public void execute(String gameId, UseHuJiaEffectRequest request, UseHuJiaEffectPresenter presenter) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found"));
        List<DomainEvent> events = game.playerUseHuJiaEffect(
                request.playerId,
                AskHuJiaEffectEvent.Choice.valueOf(request.choice),
                request.cardId
        );
        gameRepository.save(game);
        presenter.renderEvents(events);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UseHuJiaEffectRequest {
        private String playerId;
        private String choice;   // "ACCEPT" or "DECLINE"
        private String cardId;   // 必填 when ACCEPT；DECLINE 時可為 null
    }

    public interface UseHuJiaEffectPresenter<T> {
        void renderEvents(List<DomainEvent> events);

        T present();
    }
}
