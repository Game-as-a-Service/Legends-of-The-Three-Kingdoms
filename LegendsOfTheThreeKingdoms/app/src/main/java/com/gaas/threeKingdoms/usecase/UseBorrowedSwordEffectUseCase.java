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
public class UseBorrowedSwordEffectUseCase {
    private final GameRepository gameRepository;

    public void execute(String gameId, UseBorrowedSwordEffectUseCase.UseBorrowedSwordRequest request, UseBorrowedSwordEffectUseCase.UseBorrowedSwordPresenter presenter) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found"));
        List<DomainEvent> events = game.useBorrowedSwordEffect(request.currentPlayerId, request.borrowedPlayerId, request.attackTargetPlayerId);
        gameRepository.save(game);
        presenter.renderEvents(events);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UseBorrowedSwordRequest {
        private String currentPlayerId;
        private String borrowedPlayerId;
        private String attackTargetPlayerId;
    }

    public interface UseBorrowedSwordPresenter<T> {
        void renderEvents(List<DomainEvent> events);

        T present();
    }
}