package com.gaas.threeKingdoms.usecase;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.outport.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.inject.Named;
import java.util.List;

@Component
public class MonarchChooseGeneralUseCase {

    private final GameRepository repository;

    @Autowired
    public MonarchChooseGeneralUseCase(GameRepository repository) {
        this.repository = repository;
    }

    public void execute(String gameId, MonarchChooseGeneralRequest request, MonarchChooseGeneralCardPresenter presenter) {
        Game game = repository.findById(gameId);
        List<DomainEvent> events = game.monarchChoosePlayerGeneral(request.getPlayerId(), request.getGeneralId());
        repository.save(game);
        presenter.renderEvents(events);
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonarchChooseGeneralRequest {
        private String playerId;
        private String generalId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OthersChooseGeneralRequest {
        private String playerId;
        private String generalId;
    }

    public interface MonarchChooseGeneralCardPresenter<T> {
        void renderEvents(List<DomainEvent> events);
        T present();
    }
}
