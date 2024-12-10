package com.gaas.threeKingdoms.presenter;

import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.presenter.common.GameDataViewModel;
import com.gaas.threeKingdoms.presenter.common.PlayerDataViewModel;
import com.gaas.threeKingdoms.presenter.common.RoundDataViewModel;
import com.gaas.threeKingdoms.usecase.UseBorrowedSwordEffectUseCase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.gaas.threeKingdoms.presenter.PlayCardPresenter.getAskKillViewModel;
import static com.gaas.threeKingdoms.presenter.ViewModel.getEvent;

public class UseBorrowedSwordEffectPresenter implements UseBorrowedSwordEffectUseCase.UseBorrowedSwordPresenter<List<UseBorrowedSwordEffectPresenter.GameViewModel>> {

    private List<UseBorrowedSwordEffectPresenter.GameViewModel> viewModels = new ArrayList<>();
    private List<ViewModel> effectViewModels = new ArrayList<>();

    @Override
    public void renderEvents(List<DomainEvent> events) {
        GameStatusEvent gameStatusEvent = getEvent(events, GameStatusEvent.class).orElseThrow();
        UseBorrowedSwordEffectPresenter.WeaponUsurpationViewModel weaponUsurpationViewModel = getWeaponUsurpationEventViewModel(events);
        UseBorrowedSwordEffectPresenter.BorrowedSwordViewModel borrowedSwordViewModel = getBorrowedSwordViewModel(events);
        PlayCardPresenter.PlayCardViewModel playCardViewModel = getPlayCardEventViewModel(events);

        updateViewModels(
                playCardViewModel,
                weaponUsurpationViewModel,
                borrowedSwordViewModel
        );

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

            viewModels.add(new UseBorrowedSwordEffectPresenter.GameViewModel(
                    effectViewModels,
                    gameDataViewModel,
                    gameStatusEvent.getMessage(),
                    gameStatusEvent.getGameId(),
                    viewModel.getId()));
        }
    }

    private void updateViewModels(ViewModel<?>... viewModels) {
        Arrays.stream(viewModels)
                .filter(Objects::nonNull)
                .forEach(effectViewModels::add);
    }



    public static BorrowedSwordViewModel getBorrowedSwordViewModel(List<DomainEvent> events) {
        return getEvent(events, BorrowedSwordEvent.class)
                .map(event -> {
                    BorrowedSwordDataViewModel borrowedSwordDataViewModel = new BorrowedSwordDataViewModel(event.getCardId(), event.getBorrowedPlayerId(), event.getAttackTargetPlayerId());
                    return new BorrowedSwordViewModel(borrowedSwordDataViewModel, event.getMessage());
                })
                .orElse(null);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PlayCardDataViewModel {
        private String playerId;
        private String targetPlayerId;
        private String cardId;
        private String playType;
    }

    private PlayCardPresenter.PlayCardViewModel getPlayCardEventViewModel(List<DomainEvent> events) {
        return getEvent(events, PlayCardEvent.class)
                .map(event -> {
                    PlayCardPresenter.PlayCardDataViewModel playCardDataViewModel = new PlayCardPresenter.PlayCardDataViewModel(event.getPlayerId(), event.getTargetPlayerId(), event.getCardId(), event.getPlayType());
                    return new PlayCardPresenter.PlayCardViewModel(playCardDataViewModel, event.getMessage());
                })
                .orElse(null);
    }

    public static UseBorrowedSwordEffectPresenter.WeaponUsurpationViewModel getWeaponUsurpationEventViewModel(List<DomainEvent> events) {
        return getEvent(events, WeaponUsurpationEvent.class)
                .map(event -> {
                    UseBorrowedSwordEffectPresenter.WeaponUsurpationDataViewModel weaponUsurpationDataViewModel = new UseBorrowedSwordEffectPresenter.WeaponUsurpationDataViewModel(event.getGivenWeaponPlayerId(), event.getTakenWeaponPlayerId(), event.getWeaponCardId());
                    return new UseBorrowedSwordEffectPresenter.WeaponUsurpationViewModel(weaponUsurpationDataViewModel, event.getMessage());
                })
                .orElse(null);
    }

    public static class WeaponUsurpationViewModel extends ViewModel<UseBorrowedSwordEffectPresenter.WeaponUsurpationDataViewModel> {
        public WeaponUsurpationViewModel(UseBorrowedSwordEffectPresenter.WeaponUsurpationDataViewModel data, String message) {
            super("WeaponUsurpationViewModel", data, message);
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

        public GameViewModel(List<ViewModel> viewModels, GameDataViewModel data, String message, String gameId, String playerId) {
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
