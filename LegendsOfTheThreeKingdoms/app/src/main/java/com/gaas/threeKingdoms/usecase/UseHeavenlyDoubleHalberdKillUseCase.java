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
public class UseHeavenlyDoubleHalberdKillUseCase {
    private final GameRepository gameRepository;

    public void execute(String gameId, UseHeavenlyDoubleHalberdKillRequest request, UseHeavenlyDoubleHalberdKillPresenter presenter) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found"));
        List<DomainEvent> events = game.playerUseHeavenlyDoubleHalberdKill(
                request.playerId,
                request.cardId,
                request.targetPlayerIds);
        gameRepository.save(game);
        presenter.renderEvents(events);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UseHeavenlyDoubleHalberdKillRequest {
        private String playerId;
        private String cardId;
        /** 全部目標 id；size 1~3，index 0 為主要目標 */
        private List<String> targetPlayerIds;
    }

    public interface UseHeavenlyDoubleHalberdKillPresenter<T> {
        void renderEvents(List<DomainEvent> events);

        T present();
    }
}
