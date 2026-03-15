package com.gaas.threeKingdoms.presenter;

import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.presenter.common.GameDataViewModel;
import com.gaas.threeKingdoms.presenter.common.PlayerDataViewModel;
import com.gaas.threeKingdoms.presenter.common.RoundDataViewModel;
import com.gaas.threeKingdoms.presenter.mapper.DomainEventToViewModelMapper;
import com.gaas.threeKingdoms.usecase.UseBorrowedSwordEffectUseCase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.gaas.threeKingdoms.presenter.ViewModel.getEvent;

public class UseBorrowedSwordEffectPresenter implements UseBorrowedSwordEffectUseCase.UseBorrowedSwordPresenter<List<UseBorrowedSwordEffectPresenter.GameViewModel>> {

    private List<UseBorrowedSwordEffectPresenter.GameViewModel> viewModels = new ArrayList<>();
    private final DomainEventToViewModelMapper domainEventToViewModelMapper = new DomainEventToViewModelMapper();

    @Override
    public void renderEvents(List<DomainEvent> events) {
        List<ViewModel<?>> effectViewModels = domainEventToViewModelMapper.mapEventsToViewModels(events);

        GameStatusEvent gameStatusEvent = getEvent(events, GameStatusEvent.class).orElseThrow();
        List<PlayerEvent> playerEvents = gameStatusEvent.getSeats();
        RoundEvent roundEvent = gameStatusEvent.getRound();
        List<PlayerDataViewModel> playerDataViewModels = playerEvents.stream().map(PlayerDataViewModel::new).toList();

        // 將回合資訊放入 RoundDataViewModel ，後續會放到 GameDataViewModel
        RoundDataViewModel roundDataViewModel = new RoundDataViewModel(roundEvent);

        for (PlayerDataViewModel viewModel : playerDataViewModels) {

            // 此 use case 的 data 物件
            GameDataViewModel gameDataViewModel = new GameDataViewModel(
                    PlayerDataViewModel.hiddenOtherPlayerRoleInformation(
                            playerDataViewModels, viewModel.getId()), roundDataViewModel, gameStatusEvent.getGamePhase());

            List<ViewModel<?>> personalEventToViewModels = new ArrayList<>(effectViewModels);

            personalEventToViewModels = personalEventToViewModels.stream().map(personalViewModel -> {
                if (personalViewModel instanceof PlayCardPresenter.WaitForWardViewModel waitForWardViewModel) {
                    WaitForWardEvent waitForWardEvent = getEvent(events, WaitForWardEvent.class).orElseThrow(RuntimeException::new);
                    if (waitForWardEvent.getPlayerIds().contains(viewModel.getId())) {
                        return (ViewModel<?>) new PlayCardPresenter.AskPlayWardViewModel(waitForWardViewModel.getData());
                    }
                }
                return personalViewModel;
            }).collect(Collectors.toList());

            viewModels.add(new UseBorrowedSwordEffectPresenter.GameViewModel(
                    personalEventToViewModels,
                    gameDataViewModel,
                    gameStatusEvent.getMessage(),
                    gameStatusEvent.getGameId(),
                    viewModel.getId()));
        }
    }

    public static class WeaponUsurpationViewModel extends ViewModel<UseBorrowedSwordEffectPresenter.WeaponUsurpationDataViewModel> {
        public WeaponUsurpationViewModel(UseBorrowedSwordEffectPresenter.WeaponUsurpationDataViewModel data, String message) {
            super("WeaponUsurpationEvent", data, message);
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WeaponUsurpationDataViewModel {
        private String givenWeaponPlayerId;
        private String takenWeaponPlayerId;
        private String weaponCardId;
    }


    public static class BorrowedSwordViewModel extends ViewModel<UseBorrowedSwordEffectPresenter.BorrowedSwordDataViewModel> {
        public BorrowedSwordViewModel(UseBorrowedSwordEffectPresenter.BorrowedSwordDataViewModel data, String message) {
            super("BorrowedSwordEvent", data, message);
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BorrowedSwordDataViewModel {
        private String cardId;
        private String borrowedPlayerId;
        private String attackTargetPlayerId;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GameViewModel extends GameProcessViewModel<GameDataViewModel> {
        private String gameId;
        private String playerId;

        public GameViewModel(List<ViewModel<?>> viewModels, GameDataViewModel data, String message, String gameId, String playerId) {
            super(viewModels, data, message);
            this.gameId = gameId;
            this.playerId = playerId;
        }
    }

    @Override
    public List<UseBorrowedSwordEffectPresenter.GameViewModel> present() {
        return viewModels;
    }

}
