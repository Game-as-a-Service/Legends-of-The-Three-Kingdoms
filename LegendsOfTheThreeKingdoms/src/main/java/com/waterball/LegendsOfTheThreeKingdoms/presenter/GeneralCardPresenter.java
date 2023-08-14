package com.waterball.LegendsOfTheThreeKingdoms.presenter;

import com.waterball.LegendsOfTheThreeKingdoms.controller.dto.PlayerViewModel;
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

    public void renderGame(GameDto game, PlayerDto chooseGeneralPlayer) {
        List<PlayerDto> players = game.getPlayers();
        boolean allPlayerHadChoseGeneral = players.stream().allMatch(player -> Objects.nonNull(player.getGeneralCard()));
        PlayerViewModel activePlayerViewModel = new PlayerViewModel(chooseGeneralPlayer);
        List<PlayerViewModel> playerViewModel = players.stream().map(PlayerViewModel::new).collect(Collectors.toList());
        String gamePhaseState = game.getGamePhaseState();

        // 主公選擇武將中: 其他人顯示 等待主公選擇武將 ->在更早之前更早之
        // 主公選擇完畢: 推送主公選擇的武將 > 換其他人選擇
        // 有人選擇武將（未全部選擇完）： 已選擇的人顯示 請等待其他人選擇完武將
        // 所有人選擇完武將： 推送所有人選擇的武將卡 =>進入下一個階段
        
        if (allPlayerHadChoseGeneral) {
            viewModel = new GeneralCardViewModel("generalCardEvent",game.getGameId(), activePlayerViewModel, playerViewModel, gamePhaseState,"所有人都已選擇完武將");
        } else {
            viewModel = new GeneralCardViewModel("generalCardEvent",game.getGameId(), activePlayerViewModel, playerViewModel, gamePhaseState,"請等待其他人選擇完武將");
        }
    }

    public GeneralCardViewModel present() {
        return viewModel;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeneralCardViewModel  {
        private String name;
        private String gameId;
        private PlayerViewModel activePlayer;
        private List<PlayerViewModel> players;
        private String gamePhase;
        private String message;

        public String getActivePlayerId(){
            return activePlayer.getId();
        }

    }
}

