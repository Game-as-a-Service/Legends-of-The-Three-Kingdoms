package org.example.presenter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.gaas.domain.Game;
import org.gaas.domain.player.Player;
import org.gaas.usecase.FindGameByIdUseCase;

import java.util.List;
import java.util.NoSuchElementException;


public class FindGamePresenter implements FindGameByIdUseCase.FindGamePresenter<FindGamePresenter.FindGameViewModel> {

    private FindGameViewModel viewModel;

    public void renderGame(Game game, String playerId) {
        Player currentPlayer = game.getPlayers()
                .stream()
                .filter(player -> playerId.equals(player.getId()))
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
        viewModel = new FindGameViewModel(game.getGameId(), new FindGameDataViewModel(CreateGamePresenter.hiddenRoleInformationByPlayer(game, currentPlayer)), "", playerId);
    }

    @Override
    public FindGameViewModel present() {
        return viewModel;
    }


    @Data
    @NoArgsConstructor
    public static class FindGameViewModel extends ViewModel<FindGameDataViewModel> {
        private String gameId;
        private String playerId;

        public FindGameViewModel(String gameId, FindGameDataViewModel data,String message, String playerId) {
            super("findGameEvent",data,message);
            this.gameId = gameId;
            this.playerId = playerId;
        }
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FindGameDataViewModel {
        private List<CreateGamePresenter.SeatViewModel> seats;
    }


}
