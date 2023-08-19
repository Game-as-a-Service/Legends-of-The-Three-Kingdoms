package com.waterball.LegendsOfTheThreeKingdoms.presenter;

import com.waterball.LegendsOfTheThreeKingdoms.domain.Game;
import com.waterball.LegendsOfTheThreeKingdoms.domain.rolecard.Role;
import com.waterball.LegendsOfTheThreeKingdoms.service.GameService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


public class CreateGamePresenter implements GameService.Presenter<List<CreateGamePresenter.CreateGameViewModel>> {

    private List<CreateGameViewModel> viewModels;

    public void renderGame(Game game) {
        viewModels = new ArrayList<>();
        game.getPlayers().forEach(player -> {
            if (Role.MONARCH.equals(player.getRoleCard().getRole())) {
                viewModels.add(new CreateGameViewModel(player.getId(), "createGameEvent", game.getGameId(), "請選擇武將"));
            } else
                viewModels.add(new CreateGameViewModel(player.getId(), "createGameEvent", game.getGameId(), "請等待主公選擇武將"));
        });
    }

    public List<CreateGameViewModel> present() {
        return viewModels;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateGameViewModel {
        private String playerId;
        private String name;
        private String gameId;
        private String message;
    }
}
