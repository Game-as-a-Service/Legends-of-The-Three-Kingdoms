package com.waterball.LegendsOfTheThreeKingdoms.presenter;

import com.waterball.LegendsOfTheThreeKingdoms.domain.events.*;
import com.waterball.LegendsOfTheThreeKingdoms.domain.rolecard.Role;
import com.waterball.LegendsOfTheThreeKingdoms.service.GameService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.waterball.LegendsOfTheThreeKingdoms.presenter.ViewModel.getEvent;

public class InitialEndPresenter implements GameService.Presenter<List<InitialEndPresenter.InitialEndViewModel>> {

    private List<InitialEndViewModel> viewModels;


    public void renderEvents(List<DomainEvent> events) {
        if (events.size() > 0) {
            viewModels = new ArrayList<>();
            InitialEndEvent event = getEvent(events, InitialEndEvent.class).orElseThrow(RuntimeException::new);
            List<PlayerDataViewModel> playerDataViewModels = event.getSeats().stream().map(playerEvent -> new PlayerDataViewModel(playerEvent.getId(), playerEvent.getGeneralId(), playerEvent.getRoleId(), playerEvent.getHp(), playerEvent.getHand(), playerEvent.getEquipments(), playerEvent.getEquipments())).collect(Collectors.toList());

            RoundEvent roundEvent = event.getRound();
            RoundDataViewModel roundDataViewModel = new RoundDataViewModel(roundEvent.getRoundPhase(), roundEvent.getCurrentRoundPlayer(), roundEvent.getActivePlayer(), roundEvent.getDyingPlayer(), roundEvent.isShowKill());

            for (PlayerDataViewModel viewModel : playerDataViewModels) {
                InitialEndDataViewModel initialEndDataViewModel = new InitialEndDataViewModel(hiddenRoleInformationByPlayer(playerDataViewModels, viewModel.getId()), roundDataViewModel, event.getGamePhase());
                viewModels.add(new InitialEndViewModel(event.getGameId(), initialEndDataViewModel, "", viewModel.getId()));
            }
        }
    }


    public List<InitialEndViewModel> present() {
        return viewModels;
    }

    @Data
    @NoArgsConstructor
    public static class InitialEndViewModel extends ViewModel<InitialEndDataViewModel> {
        private String gameId;
        private String playerId;

        public InitialEndViewModel(String gameId, InitialEndDataViewModel data, String message, String playerId) {
            super("initialEndViewModel", data, message);
            this.gameId = gameId;
            this.playerId = playerId;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InitialEndDataViewModel {
        private List<PlayerDataViewModel> seats;
        private RoundDataViewModel round;
        private String gamePhase;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlayerDataViewModel {
        private String id;
        private String generalId;
        private String roleId;
        private int hp;
        private HandEvent hand;
        private List<String> equipments;
        private List<String> delayScrolls;

        public static PlayerDataViewModel deepCopy(PlayerDataViewModel p) {
            return new PlayerDataViewModel(p.getId(), p.getGeneralId(), p.getRoleId(), p.getHp(), HandEvent.deepCopy(p.getHand()), new ArrayList<>(p.getEquipments()), new ArrayList<>(p.getDelayScrolls()));
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoundDataViewModel {
        private String roundPhase;
        private String currentRoundPlayer;
        private String activePlayer;
        private String dyingPlayer;
        private boolean isShowKill;
    }

    private List<PlayerDataViewModel> hiddenRoleInformationByPlayer(List<PlayerDataViewModel> viewModels, String playerId) {
        List<PlayerDataViewModel> playerDataViewModels = new ArrayList<>();

        for (PlayerDataViewModel viewModel : viewModels){
            playerDataViewModels.add(PlayerDataViewModel.deepCopy(viewModel));
        }

        for (int i = 0; i < playerDataViewModels.size(); i++) {
            PlayerDataViewModel viewModel = playerDataViewModels.get(i);
            if (!viewModel.getId().equals(playerId)) {
                if (!viewModel.getRoleId().equals(Role.MONARCH.getRole())){
                    viewModel.setRoleId("");
                    viewModel.setHand(new HandEvent(0, new ArrayList<>()));
                } else {
                    viewModel.setHand(new HandEvent(0,new ArrayList<>()));
                }
            }
        }
        return playerDataViewModels;
    }
    
}
