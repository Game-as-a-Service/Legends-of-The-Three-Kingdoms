package com.gaas.threeKingdoms.presenter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.usecase.FindGameByIdUseCase;

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
        viewModel = new FindGameViewModel(game.getGameId(),
                new FindGameDataViewModel(
                        CreateGamePresenter.hiddenRoleInformationByPlayer(game, currentPlayer),
                        buildSelectionStatus(game)),
                "", playerId);
    }

    /** 選將階段（Initial）重整/重連補進度（issue #237）；其餘階段為 null。 */
    private static GeneralSelectionStatusPresenter.GeneralSelectionStatusDataViewModel buildSelectionStatus(Game game) {
        if (!"Initial".equals(game.getGamePhase().getPhaseName())) {
            return null;
        }
        List<String> selected = game.getPlayers().stream()
                .filter(p -> p.getGeneralCard() != null).map(Player::getId).toList();
        List<String> pending = game.getPlayers().stream()
                .filter(p -> p.getGeneralCard() == null).map(Player::getId).toList();
        return new GeneralSelectionStatusPresenter.GeneralSelectionStatusDataViewModel(
                selected, pending, selected.size(), game.getPlayers().size(), pending.isEmpty());
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
        // 選將階段的進度快照（issue #237）；非 Initial 階段為 null（additive 欄位）
        private GeneralSelectionStatusPresenter.GeneralSelectionStatusDataViewModel selectionStatus;
    }


}
