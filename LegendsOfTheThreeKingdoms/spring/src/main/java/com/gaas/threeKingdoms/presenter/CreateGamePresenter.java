package com.gaas.threeKingdoms.presenter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.usecase.StartGameUseCase;

import java.util.ArrayList;
import java.util.List;


public class CreateGamePresenter implements StartGameUseCase.CreateGamePresenter<List<CreateGamePresenter.CreateGameViewModel>> {

    private List<CreateGameViewModel> viewModels;

    public void renderGame(Game game) {
        viewModels = new ArrayList<>();

        game.getPlayers().forEach(player -> {
            if (Role.MONARCH.equals(player.getRoleCard().getRole())) {
                viewModels.add(new CreateGameViewModel(game.getGameId(), new CreateGameDataViewModel(hiddenRoleInformationByPlayer(game, player)), "請選擇武將", player.getId()));
            } else
                viewModels.add(new CreateGameViewModel(game.getGameId(), new CreateGameDataViewModel(hiddenRoleInformationByPlayer(game, player)), "請等待主公選擇武將", player.getId()));
        });
    }


    @Data
    @NoArgsConstructor
    public static class CreateGameViewModel extends ViewModel<CreateGameDataViewModel> {
        private String gameId;
        private String playerId;

        public CreateGameViewModel(String gameId, CreateGameDataViewModel data, String message, String playerId) {
            super("createGameEvent", data, message);
            this.gameId = gameId;
            this.playerId = playerId;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateGameDataViewModel {
        private List<SeatViewModel> seats;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatViewModel {
        public String id;
        public String roleId;
    }


    @Override
    public List<CreateGameViewModel> present() {
        return viewModels;
    }

    public static List<SeatViewModel> hiddenRoleInformationByPlayer(Game game, Player currentPlayer) {
        List<SeatViewModel> seatViewModels = game.getSeatingChart().getPlayers().stream()
                .map(CreateGamePresenter::domainToSeatViewModel)
                .toList();

        List<SeatViewModel> tempSeatViewModels = new ArrayList<>();

        for (SeatViewModel seatViewModel : seatViewModels) {
            SeatViewModel copyPlayer = new SeatViewModel(seatViewModel.getId(), seatViewModel.getRoleId());
            tempSeatViewModels.add(copyPlayer);
        }

        List<SeatViewModel> needToHiddenRoleCardPlayers = tempSeatViewModels.stream()
                .filter(player -> !"MONARCH".equals(player.getRoleId()))
                .filter(player -> !player.getId().equals(currentPlayer.getId()))
                .toList();

        needToHiddenRoleCardPlayers.forEach(player -> tempSeatViewModels.get(tempSeatViewModels.indexOf(player)).setRoleId(""));

        return tempSeatViewModels;
    }

    public static SeatViewModel domainToSeatViewModel(Player player) {
        return new SeatViewModel(player.getId(), player.getRoleCard().getRole().name());
    }

}

