package com.gaas.threeKingdoms.usecase;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.AskActivateYinYangSwordsEvent;
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
public class ActivateYinYangSwordsUseCase {
    private final GameRepository gameRepository;

    public void execute(String gameId, ActivateYinYangSwordsRequest request, ActivateYinYangSwordsPresenter presenter) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found"));
        AskActivateYinYangSwordsEvent.Choice choice =
                AskActivateYinYangSwordsEvent.Choice.valueOf(request.choice);
        List<DomainEvent> events = game.playerActivateYinYangSwords(request.playerId, choice);
        gameRepository.save(game);
        presenter.renderEvents(events);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivateYinYangSwordsRequest {
        private String playerId;
        private String choice; // "ACTIVATE" or "SKIP"
    }

    public interface ActivateYinYangSwordsPresenter<T> {
        void renderEvents(List<DomainEvent> events);
        T present();
    }
}
