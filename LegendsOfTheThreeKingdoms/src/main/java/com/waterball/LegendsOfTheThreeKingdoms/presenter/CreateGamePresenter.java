package com.waterball.LegendsOfTheThreeKingdoms.presenter;

import com.waterball.LegendsOfTheThreeKingdoms.domain.Game;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.Player;
import com.waterball.LegendsOfTheThreeKingdoms.domain.rolecard.Role;
import com.waterball.LegendsOfTheThreeKingdoms.domain.rolecard.RoleCard;
import com.waterball.LegendsOfTheThreeKingdoms.service.GameService;
import com.waterball.LegendsOfTheThreeKingdoms.service.dto.PlayerDto;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class CreateGamePresenter implements GameService.Presenter<List<CreateGamePresenter.CreateGameViewModel>> {

    private List<CreateGameViewModel> viewModels;

    public void renderGame(Game game) {
        viewModels = new ArrayList<>();

        game.getPlayers().forEach(player -> {
            if (Role.MONARCH.equals(player.getRoleCard().getRole())) {
                viewModels.add(new CreateGameViewModel(game.getGameId(), new CreateGameDataViewModel(dtoToViewModel(game, domainToSeatViewModel(player))), "請選擇武將", player.getId()));
            } else
                viewModels.add(new CreateGameViewModel(game.getGameId(), new CreateGameDataViewModel(dtoToViewModel(game, domainToSeatViewModel(player))), "請等待主公選擇武將", player.getId()));
        });
    }

    public static class CreateGameViewModel extends ViewModel<CreateGameDataViewModel> {
        private String gameId;
        private String playerId;

        public CreateGameViewModel(String gameId,CreateGameDataViewModel data, String message,String playerId) {
            super("createGameEvent", data, message);
            this.gameId = gameId;
            this.playerId = playerId;
        }
        public String getGameId(){
            return gameId;
        }
        public String getPlayerId(){
            return playerId;
        }
        public void setGameId(String gameId) {
            this.gameId = gameId;
        }
        public void setPlayerId(String playerId) {
            this.playerId = playerId;
        }
    }


    public static class CreateGameDataViewModel {
        private List<SeatViewModel> seats;

        public CreateGameDataViewModel(){}

        public CreateGameDataViewModel(List<SeatViewModel> seats) {
            this.seats = seats;
        }
        public List<SeatViewModel> getSeats() {
            return seats;
        }
        public void setSeats(List<SeatViewModel> seats) {
            this.seats = seats;
        }
    }

    public static class SeatViewModel {

        public String id;
        public String role;

        public SeatViewModel () {}

        public SeatViewModel(String id, String role) {
            this.id = id;
            this.role = role;
        }

        public String getId() {
            return id;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SeatViewModel that = (SeatViewModel) o;
            return Objects.equals(id, that.id) && Objects.equals(role, that.role);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, role);
        }
    }


    @Override
    public List<CreateGameViewModel> present() {
        return viewModels;
    }

    private List<SeatViewModel> dtoToViewModel(Game game, SeatViewModel comparePlayer) {
        List<SeatViewModel> seatViewModels = game.getSeatingChart().getPlayers().stream()
                .map(player -> domainToSeatViewModel(player))
                .collect(Collectors.toList());

        List<SeatViewModel> tempSeatViewModels = new ArrayList<>();

        for (SeatViewModel seatViewModel : seatViewModels) {
            SeatViewModel copyPlayer = new SeatViewModel(seatViewModel.getId(), seatViewModel.getRole());
            tempSeatViewModels.add(copyPlayer);
        }

        List<SeatViewModel> needToHiddenRoleCardPlayers = tempSeatViewModels.stream()
                .filter(player -> !"MONARCH".equals(player.getRole()))
                .filter(player -> !player.getId().equals(comparePlayer.getId()))
                .collect(Collectors.toList());

        needToHiddenRoleCardPlayers.forEach(player -> tempSeatViewModels.get(tempSeatViewModels.indexOf(player)).setRole(""));

        return tempSeatViewModels;
    }

    private SeatViewModel domainToSeatViewModel(Player player) {
        return new SeatViewModel(player.getId(), player.getRoleCard().getRole().name());
    }


}

