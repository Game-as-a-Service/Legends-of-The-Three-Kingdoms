package com.waterball.LegendsOfTheThreeKingdoms.presenter;

import com.waterball.LegendsOfTheThreeKingdoms.domain.rolecard.Role;
import com.waterball.LegendsOfTheThreeKingdoms.service.GameService;
import com.waterball.LegendsOfTheThreeKingdoms.service.dto.GameDto;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



public class CreateGamePresenter implements GameService.Presenter<List<CreateGamePresenter.CreateGameViewModel>> {

    private List<CreateGameViewModel> viewModels;

    public void renderGame(GameDto game) {
        viewModels = new ArrayList<>();
        game.getPlayers().forEach(playerDto -> {
            if (Role.MONARCH.equals(playerDto.getRoleCard().getRole())){
                viewModels.add(new CreateGameViewModel(playerDto.getId(),"createGameEvent",game.getGameId(),"請選擇武將"));
            } else
                viewModels.add(new CreateGameViewModel(playerDto.getId(),"createGameEvent",game.getGameId(),"請等待主公選擇武將"));
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
        //TODO 加 List<String> generalIds
    }
}
