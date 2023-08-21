package com.waterball.LegendsOfTheThreeKingdoms.presenter;

import com.waterball.LegendsOfTheThreeKingdoms.controller.dto.PlayerViewModel;
import com.waterball.LegendsOfTheThreeKingdoms.domain.generalcard.GeneralCard;
import com.waterball.LegendsOfTheThreeKingdoms.service.GameService;
import com.waterball.LegendsOfTheThreeKingdoms.service.dto.GameDto;
import com.waterball.LegendsOfTheThreeKingdoms.service.dto.PlayerDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class MonarchGeneralCardPresenter implements GameService.Presenter<MonarchGeneralCardPresenter.MonarchGeneralCardViewModel> {

    private MonarchGeneralCardViewModel viewModel;

    public void renderGame(GameDto game, PlayerDto chooseGeneralPlayer) {
        List<PlayerDto> players = game.getPlayers();
        PlayerViewModel monarchViewModel = new PlayerViewModel(chooseGeneralPlayer);
        List<String> playerIdList = players.stream().map(PlayerDto::getId).collect(Collectors.toList());
        String gamePhaseState = game.getGamePhaseState();
        GeneralCard generalCard = chooseGeneralPlayer.getGeneralCard();

        // 主公選擇完畢: 推送主公選擇的武將

        viewModel = new MonarchGeneralCardViewModel("MonarchGeneralCardEvent", game.getGameId(), monarchViewModel, playerIdList, gamePhaseState, String.format("主公 %s 已選擇 %s", chooseGeneralPlayer.getId(), generalCard.getGeneralName()), generalCard);
    }

    public MonarchGeneralCardViewModel present() {
        return viewModel;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonarchGeneralCardViewModel {
        private String name;
        private String gameId;
        private PlayerViewModel monarchPlayer;
        private List<String> playerIdList;
        private String gamePhase;
        private String message;
        private GeneralCard monarchGeneralCard;
    }
}

