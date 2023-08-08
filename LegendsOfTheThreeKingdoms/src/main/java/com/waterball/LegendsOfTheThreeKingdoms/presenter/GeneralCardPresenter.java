package com.waterball.LegendsOfTheThreeKingdoms.presenter;

import com.waterball.LegendsOfTheThreeKingdoms.controller.WebSocketController;
import com.waterball.LegendsOfTheThreeKingdoms.controller.dto.PlayerResponse;
import com.waterball.LegendsOfTheThreeKingdoms.service.GameService;
import com.waterball.LegendsOfTheThreeKingdoms.service.dto.GameDto;
import com.waterball.LegendsOfTheThreeKingdoms.service.dto.PlayerDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class GeneralCardPresenter implements GameService.Presenter<GeneralCardPresenter.GeneralCardViewModel> {

    private GeneralCardViewModel viewModel;


    public void renderGame(GameDto game){
        List<PlayerDto> players = game.getPlayers();
        boolean allPlayerHadChoseGeneral = players.stream().allMatch(player -> Objects.nonNull(player.getGeneralCard()));
        List<PlayerResponse> playerResponses = players.stream().map(PlayerResponse::new).collect(Collectors.toList());

        if (allPlayerHadChoseGeneral) {
            viewModel = new GeneralCardViewModel(playerResponses ,"所有人都已選擇完武將");
        } else{
            viewModel = new GeneralCardViewModel(playerResponses,"請等待其他人選擇完武將");
        }
    }

    public GeneralCardViewModel present() {
        return viewModel;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeneralCardViewModel {
        private List<PlayerResponse> players;
        private String message;
    }
}

