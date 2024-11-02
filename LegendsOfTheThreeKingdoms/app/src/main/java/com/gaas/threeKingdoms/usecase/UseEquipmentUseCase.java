package com.gaas.threeKingdoms.usecase;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.exception.NotFoundException;
import com.gaas.threeKingdoms.handcard.EquipmentPlayType;
import com.gaas.threeKingdoms.outport.GameRepository;
import jakarta.inject.Named;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Named
public class UseEquipmentUseCase {
    private final GameRepository gameRepository;

    public void execute(String gameId, UseEquipmentRequest request, UseEquipmentPresenter presenter) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found"));
        List<DomainEvent> events = game.playerUseEquipment(request.playerId, request.cardId, request.targetPlayerId, request.playType);
        gameRepository.save(game);
        presenter.renderEvents(events);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UseEquipmentRequest {
        private String playerId;
        private String targetPlayerId;
        private String cardId;
        private EquipmentPlayType playType;
    }

    public interface UseEquipmentPresenter<T> {
        void renderEvents(List<DomainEvent> events);

        T present();
    }
}
