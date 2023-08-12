package com.waterball.LegendsOfTheThreeKingdoms.presenter;

import com.waterball.LegendsOfTheThreeKingdoms.domain.rolecard.Role;
import com.waterball.LegendsOfTheThreeKingdoms.service.GameService;
import com.waterball.LegendsOfTheThreeKingdoms.service.dto.GameDto;
import com.waterball.LegendsOfTheThreeKingdoms.service.dto.PlayerDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



public class MonarchGeneralCardPresenter implements GameService.Presenter<MonarchGeneralCardPresenter.MonarchGeneralViewModel> {

    private MonarchGeneralCardPresenter.MonarchGeneralViewModel viewModel;

    public void renderGame(GameDto game, PlayerDto chooseGeneralPlayer) {
        if (Role.MONARCH.equals(chooseGeneralPlayer.getRoleCard().getRole())){
            viewModel = new MonarchGeneralViewModel("請選擇武將");
        }
        viewModel = new MonarchGeneralViewModel("等待主公選擇武將");
    }

    public MonarchGeneralCardPresenter.MonarchGeneralViewModel present() {
        return viewModel;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonarchGeneralViewModel implements ViewModel{
        private String message;
    }
}
