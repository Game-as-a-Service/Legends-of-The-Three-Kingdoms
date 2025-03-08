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
public class ChooseCardFromBountifulHarvestUseCase {

    private final GameRepository gameRepository;

    public void execute(String gameId, ChooseCardFromBountifulHarvestUseCase.ChooseCardFromBountifulHarvestRequest request, ChooseCardFromBountifulHarvestUseCase.ChooseCardFromBountifulHarvestPresenter presenter) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found"));
        List<DomainEvent> events = game.playerChooseCardFromBountifulHarvest(request.playerId, request.cardId);
        gameRepository.save(game);
        presenter.renderEvents(events);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChooseCardFromBountifulHarvestRequest {
        private String playerId;
        private String cardId;
    }

    public interface ChooseCardFromBountifulHarvestPresenter<T> {
        void renderEvents(List<DomainEvent> events);

        T present();
    }

}
