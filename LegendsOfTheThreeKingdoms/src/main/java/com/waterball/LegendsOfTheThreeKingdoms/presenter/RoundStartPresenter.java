package com.waterball.LegendsOfTheThreeKingdoms.presenter;

import com.waterball.LegendsOfTheThreeKingdoms.domain.events.*;
import com.waterball.LegendsOfTheThreeKingdoms.domain.rolecard.Role;
import com.waterball.LegendsOfTheThreeKingdoms.service.GameService;
import lombok.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.waterball.LegendsOfTheThreeKingdoms.presenter.ViewModel.getEvent;

public class RoundStartPresenter implements GameService.Presenter<List<RoundStartPresenter.PlayerTakeTurnViewModel>> {

    private List<PlayerTakeTurnViewModel> viewModels;


    public List<PlayerTakeTurnViewModel> present() {
        return viewModels;
    }

    public void renderEvents(List<DomainEvent> events) {
        if (!events.isEmpty()) {
            viewModels = new ArrayList<>();
            updateEventToPlayerTakeTurnViewModel(events);
        } else {
            viewModels = Collections.emptyList();
        }
    }

    private void updateEventToPlayerTakeTurnViewModel(List<DomainEvent> events) {
        // 取三個DomainEvent
        // TODO roundStartEvent, judgementEvent暫時沒用到
        RoundStartEvent roundStartEvent = getEvent(events, RoundStartEvent.class).orElseThrow(RuntimeException::new);
        JudgementEvent judgementEvent = getEvent(events, JudgementEvent.class).orElseThrow(RuntimeException::new);
        DrawCardEvent drawCardEvent = getEvent(events, DrawCardEvent.class).orElseThrow(RuntimeException::new);

        RoundStartViewModel roundStartViewModel = new RoundStartViewModel();
        JudgementViewModel judgementViewModel = new JudgementViewModel();
        DrawCardDataViewModel drewCardDataViewModel = new DrawCardDataViewModel(drawCardEvent.getSize(), drawCardEvent.getCardIds());

        DrawCardViewModel drawCardViewModel = new DrawCardViewModel();
        drawCardViewModel.setData(drewCardDataViewModel);

        // 取得drawCardEvent中的玩家資訊
        List<RoundStartPresenter.PlayerDataViewModel> playerDataViewModels = drawCardEvent.getSeats().stream().map(playerEvent -> new PlayerDataViewModel(playerEvent.getId(), playerEvent.getGeneralId(), playerEvent.getRoleId(), playerEvent.getHp(), playerEvent.getHand(), playerEvent.getEquipments(), playerEvent.getEquipments())).toList();

        // 取得drawCardEvent中的回合資訊
        RoundEvent roundEvent = drawCardEvent.getRound();

        // 將回合資訊放入RoundDataViewModel
        RoundDataViewModel roundDataViewModel = new RoundDataViewModel(roundEvent.getRoundPhase(), roundEvent.getCurrentRoundPlayer(), roundEvent.getActivePlayer(), roundEvent.getDyingPlayer(), roundEvent.isShowKill());

        for (PlayerDataViewModel viewModel : playerDataViewModels) {
            PlayerTakeTurnDataViewModel drawCardDataViewModel = new PlayerTakeTurnDataViewModel(hiddenRoleInformationByPlayer(playerDataViewModels, viewModel.getId()), roundDataViewModel, drawCardEvent.getGamePhase());
            viewModels.add(new PlayerTakeTurnViewModel(List.of(roundStartViewModel, judgementViewModel, drawCardViewModel), drawCardDataViewModel, drawCardEvent.getMessage(), drawCardEvent.getGameId(), viewModel.getId()));
        }
    }

    @Setter
    @Getter
    public static class RoundStartViewModel extends ViewModel<Object> {
        public RoundStartViewModel() {
            super("RoundStartEvent", "", "回合已開始");
        }
    }


    @Setter
    @Getter
    public static class JudgementViewModel extends ViewModel<Object> {
        public JudgementViewModel() {
            super("JudgementEvent", "", "判定結束");
        }
    }


    public static class JudgementDataViewModel {

    }

    @Data
    @AllArgsConstructor
    public static class DrawCardViewModel extends ViewModel<DrawCardDataViewModel> {
        private DrawCardDataViewModel data;

        public DrawCardViewModel() {
            super("DrawCardEvent", null, "玩家摸牌");
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DrawCardDataViewModel {
        private int size;
        private List<String> cards;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PlayerTakeTurnViewModel extends GameProcessViewModel<PlayerTakeTurnDataViewModel> {
        private String gameId;
        private String playerId;

        public PlayerTakeTurnViewModel(List<ViewModel> events, PlayerTakeTurnDataViewModel data, String message, String gameId, String playerId) {
            super(events, data, message);
            this.gameId = gameId;
            this.playerId = playerId;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlayerTakeTurnDataViewModel {
        private List<RoundStartPresenter.PlayerDataViewModel> seats;
        private RoundStartPresenter.RoundDataViewModel round;
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

        public static RoundStartPresenter.PlayerDataViewModel deepCopy(RoundStartPresenter.PlayerDataViewModel p) {
            return new RoundStartPresenter.PlayerDataViewModel(p.getId(), p.getGeneralId(), p.getRoleId(), p.getHp(), HandEvent.deepCopy(p.getHand()), new ArrayList<>(p.getEquipments()), new ArrayList<>(p.getDelayScrolls()));
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RoundDataViewModel {
        private String roundPhase;
        private String currentRoundPlayer;
        private String activePlayer;
        private String dyingPlayer;
        private boolean isShowKill;
    }

    private List<RoundStartPresenter.PlayerDataViewModel> hiddenRoleInformationByPlayer(List<RoundStartPresenter.PlayerDataViewModel> viewModels, String playerId) {
        List<RoundStartPresenter.PlayerDataViewModel> playerDataViewModels = new ArrayList<>();

        for (RoundStartPresenter.PlayerDataViewModel viewModel : viewModels) {
            playerDataViewModels.add(RoundStartPresenter.PlayerDataViewModel.deepCopy(viewModel));
        }

        for (int i = 0; i < playerDataViewModels.size(); i++) {
            RoundStartPresenter.PlayerDataViewModel viewModel = playerDataViewModels.get(i);
            int size = viewModel.getHand().getSize();
            if (isNotCurrentPlayer(playerId, viewModel)) {
                if (isNotMonarch(viewModel)) {
                    viewModel.setRoleId("");
                }
                viewModel.setHand(new HandEvent(size, new ArrayList<>()));
            }
        }
        return playerDataViewModels;
    }

    private static boolean isNotCurrentPlayer(String playerId, RoundStartPresenter.PlayerDataViewModel viewModel) {
        return !viewModel.getId().equals(playerId);
    }

    private static boolean isNotMonarch(RoundStartPresenter.PlayerDataViewModel viewModel) {
        return !viewModel.getRoleId().equals(Role.MONARCH.getRole());
    }
}
