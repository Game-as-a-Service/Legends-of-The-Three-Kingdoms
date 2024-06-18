package com.gaas.threeKingdoms.usecase;


import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.DomainEvent;
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
public class ChooseHorseUseCase {

    private final GameRepository gameRepository;

    public void execute(String gameId, ChooseHorseUseCase.ChooseHorseRequest request, ChooseHorsePresenter presenter) {
        Game game = gameRepository.findById(gameId);
        List<DomainEvent> events = game.playerChooseHorseForQilinBow(request.playerId, request.cardId);
        gameRepository.save(game);
        presenter.renderEvents(events);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChooseHorseRequest {
        private String playerId;
        private String cardId;
    }

    public interface ChooseHorsePresenter<T> {
        void renderEvents(List<DomainEvent> events);

        T present();
    }
}
