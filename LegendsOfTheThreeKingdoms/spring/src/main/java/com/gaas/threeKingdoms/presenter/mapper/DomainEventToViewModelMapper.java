package com.gaas.threeKingdoms.presenter.mapper;

import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.presenter.*;
import com.gaas.threeKingdoms.presenter.common.PlayerDataViewModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DomainEventToViewModelMapper {

    private final Map<Class<? extends DomainEvent>, Function<DomainEvent, ViewModel<?>>> eventToViewModelMappers;

    public DomainEventToViewModelMapper() {
        eventToViewModelMappers = new HashMap<>();

        eventToViewModelMappers.put(PlayCardEvent.class, event -> {
            PlayCardEvent playCardEvent = (PlayCardEvent) event;
            return new PlayCardPresenter.PlayCardViewModel(
                    new PlayCardPresenter.PlayCardDataViewModel(
                            playCardEvent.getPlayerId(),
                            playCardEvent.getTargetPlayerId(),
                            playCardEvent.getCardId(),
                            playCardEvent.getPlayType()
                    ), playCardEvent.getMessage()
            );
        });

        eventToViewModelMappers.put(PlayerDamagedEvent.class, event -> {
            PlayerDamagedEvent playerDamagedEvent = (PlayerDamagedEvent) event;
            return new PlayCardPresenter.PlayerDamagedViewModel(
                    new PlayCardPresenter.PlayerDamagedDataViewModel(
                            playerDamagedEvent.getPlayerId(),
                            playerDamagedEvent.getFrom(),
                            playerDamagedEvent.getTo()
                    )
            );
        });

        eventToViewModelMappers.put(AskKillEvent.class, event -> {
            AskKillEvent askKillEvent = (AskKillEvent) event;
            return new PlayCardPresenter.AskKillViewModel(
                    new PlayCardPresenter.AskKillDataViewModel(
                            askKillEvent.getPlayerId()
                    )
            );
        });

        eventToViewModelMappers.put(FinishActionEvent.class, event -> {
            FinishActionEvent finishActionEvent = (FinishActionEvent) event;
            return new FinishActionPresenter.FinishActionViewModel(
                    new FinishActionPresenter.FinishActionDataViewModel(finishActionEvent.getPlayerId())
            );
        });

        eventToViewModelMappers.put(RoundStartEvent.class, event -> new RoundStartPresenter.RoundStartViewModel());

        eventToViewModelMappers.put(JudgementEvent.class, event -> new RoundStartPresenter.JudgementViewModel());

        eventToViewModelMappers.put(NotifyDiscardEvent.class, event -> {
            NotifyDiscardEvent notifyEvent = (NotifyDiscardEvent) event;
            FinishActionPresenter.NotifyDiscardDataViewModel dataViewModel = new FinishActionPresenter.NotifyDiscardDataViewModel(
                    notifyEvent.getDiscardCount(), notifyEvent.getDiscardPlayerId());
            return new FinishActionPresenter.NotifyDiscardViewModel(dataViewModel);
        });
        eventToViewModelMappers.put(ContentmentEvent.class, event -> {
            ContentmentEvent contentmentEvent = (ContentmentEvent) event;
            FinishActionPresenter.ContentmentDataViewModel dataViewModel = new FinishActionPresenter.ContentmentDataViewModel(
                    contentmentEvent.getPlayerId(), contentmentEvent.getDrawCardId(), contentmentEvent.isSuccess());
            return new FinishActionPresenter.ContentmentViewModel(dataViewModel);
        });
        eventToViewModelMappers.put(RoundEndEvent.class, event -> new FinishActionPresenter.RoundEndViewModel());

        eventToViewModelMappers.put(RemoveHorseEvent.class, event -> {
            RemoveHorseEvent removeHorseEvent = (RemoveHorseEvent) event;
            return new ChooseHorsePresenter.RemoveHorseViewModel(
                    new ChooseHorsePresenter.RemoveHorseDataViewModel(
                            removeHorseEvent.getPlayerId(),
                            removeHorseEvent.getMountCardId()
                    )
            );
        });

        eventToViewModelMappers.put(PlayerDyingEvent.class, event -> {
            PlayerDyingEvent playerDyingEvent = (PlayerDyingEvent) event;
            return new PlayCardPresenter.PlayerDyingViewModel(
                    new PlayCardPresenter.PlayerDyingDataViewModel(
                            playerDyingEvent.getPlayerId()
                    )
            );
        });

        eventToViewModelMappers.put(AskPeachEvent.class, event -> {
            AskPeachEvent askPeachEvent = (AskPeachEvent) event;
            return new PlayCardPresenter.AskPeachViewModel(
                    new PlayCardPresenter.AskPeachDataViewModel(
                            askPeachEvent.getPlayerId(), askPeachEvent.getDyingPlayerId()
                    )
            );
        });

        eventToViewModelMappers.put(SettlementEvent.class, event -> {
            SettlementEvent settlementEvent = (SettlementEvent) event;
            return new PlayCardPresenter.SettlementViewModel(
                    new PlayCardPresenter.SettlementDataViewModel(
                            settlementEvent.getPlayerId(), settlementEvent.getRole()
                    )
            );
        });

        eventToViewModelMappers.put(GameOverEvent.class, event -> {
            GameOverEvent gameOverEvent = (GameOverEvent) event;
            return new PlayCardPresenter.GameOverViewModel(
                    new PlayCardPresenter.GameOverDataViewModel(
                            gameOverEvent.getPlayers().stream()
                                    .map(PlayerDataViewModel::new)
                                    .toList(), gameOverEvent.getWinners())
            );
        });

        eventToViewModelMappers.put(PeachEvent.class, event -> {
            PeachEvent peachEvent = (PeachEvent) event;
            PlayCardPresenter.PeachDataViewModel dataViewModel = new PlayCardPresenter.PeachDataViewModel(
                    peachEvent.getPlayerId(),
                    peachEvent.getFrom(),
                    peachEvent.getTo()
            );
            return new PlayCardPresenter.PeachViewModel(dataViewModel);
        });

        eventToViewModelMappers.put(BorrowedSwordEvent.class, event -> {
            BorrowedSwordEvent borrowedSwordEvent = (BorrowedSwordEvent) event;
            UseBorrowedSwordEffectPresenter.BorrowedSwordDataViewModel dataViewModel =
                    new UseBorrowedSwordEffectPresenter.BorrowedSwordDataViewModel(
                            borrowedSwordEvent.getCardId(),
                            borrowedSwordEvent.getBorrowedPlayerId(),
                            borrowedSwordEvent.getAttackTargetPlayerId()
                    );
            return new UseBorrowedSwordEffectPresenter.BorrowedSwordViewModel(dataViewModel, borrowedSwordEvent.getMessage());
        });

        eventToViewModelMappers.put(WeaponUsurpationEvent.class, event -> {
            WeaponUsurpationEvent weaponUsurpationEvent = (WeaponUsurpationEvent) event;
            UseBorrowedSwordEffectPresenter.WeaponUsurpationDataViewModel dataViewModel =
                    new UseBorrowedSwordEffectPresenter.WeaponUsurpationDataViewModel(
                            weaponUsurpationEvent.getGivenWeaponPlayerId(),
                            weaponUsurpationEvent.getTakenWeaponPlayerId(),
                            weaponUsurpationEvent.getWeaponCardId()
                    );
            return new UseBorrowedSwordEffectPresenter.WeaponUsurpationViewModel(dataViewModel, weaponUsurpationEvent.getMessage());
        });

        eventToViewModelMappers.put(DismantleEvent.class, event -> {
            DismantleEvent dismantleEvent = (DismantleEvent) event;
            UseDismantlePresenter.UseDismantleDataViewModel dataViewModel = new UseDismantlePresenter.UseDismantleDataViewModel(
                    dismantleEvent.getPlayerId(),
                    dismantleEvent.getTargetPlayerId(),
                    dismantleEvent.getCardId()
            );
            return new UseDismantlePresenter.UseDismantleViewModel(dataViewModel, dismantleEvent.getMessage());
        });

        eventToViewModelMappers.put(EightDiagramTacticEffectEvent.class, event -> {
            EightDiagramTacticEffectEvent tacticEffectEvent = (EightDiagramTacticEffectEvent) event;
            UseEquipmentEffectPresenter.UseEquipmentEffectDataViewModel dataViewModel = new UseEquipmentEffectPresenter.UseEquipmentEffectDataViewModel(
                    tacticEffectEvent.getDrawCardId(), tacticEffectEvent.isSuccess()
            );
            return new UseEquipmentEffectPresenter.UseEquipmentEffectViewModel(dataViewModel);
        });

        eventToViewModelMappers.put(QilinBowCardEffectEvent.class, event -> {
            QilinBowCardEffectEvent qilinBowEvent = (QilinBowCardEffectEvent) event;
            UseEquipmentEffectPresenter.UseQilinBowCardEffectDataViewModel dataViewModel = new UseEquipmentEffectPresenter.UseQilinBowCardEffectDataViewModel(
                    qilinBowEvent.getMountCardId()
            );
            return new UseEquipmentEffectPresenter.UseQilinBowCardEffectViewModel(dataViewModel);
        });

        eventToViewModelMappers.put(AskChooseMountCardEvent.class, event -> {
            AskChooseMountCardEvent askChooseMountEvent = (AskChooseMountCardEvent) event;
            UseEquipmentEffectPresenter.AskChooseMountCardDataViewModel dataViewModel = new UseEquipmentEffectPresenter.AskChooseMountCardDataViewModel(
                    askChooseMountEvent.getChooseMountCardPlayerId(),
                    askChooseMountEvent.getTargetPlayerId(),
                    askChooseMountEvent.getMountsCardIds()
            );
            return new UseEquipmentEffectPresenter.AskChooseMountCardViewModel(dataViewModel);
        });

        eventToViewModelMappers.put(SkipEquipmentEffectEvent.class, event -> {
            SkipEquipmentEffectEvent skipEquipmentEvent = (SkipEquipmentEffectEvent) event;
            UseEquipmentEffectPresenter.SkipEquipmentEffectDataViewModel dataViewModel = new UseEquipmentEffectPresenter.SkipEquipmentEffectDataViewModel(
                    skipEquipmentEvent.getPlayerId(),
                    skipEquipmentEvent.getCardId()
            );
            return new UseEquipmentEffectPresenter.SkipEquipmentEffectViewModel(dataViewModel);
        });

        eventToViewModelMappers.put(AskPlayEquipmentEffectEvent.class, event -> {
            AskPlayEquipmentEffectEvent askPlayEquipmentEvent = (AskPlayEquipmentEffectEvent) event;
            PlayCardPresenter.AskPlayEquipmentEffectDataViewModel dataViewModel = new PlayCardPresenter.AskPlayEquipmentEffectDataViewModel(
                    askPlayEquipmentEvent.getPlayerId(),
                    askPlayEquipmentEvent.getEquipmentCard().getId(),
                    askPlayEquipmentEvent.getEquipmentCard().getName(),
                    askPlayEquipmentEvent.getTargetPlayerIds()
            );
            return new PlayCardPresenter.AskPlayEquipmentEffectViewModel(dataViewModel);
        });

        eventToViewModelMappers.put(PlayEquipmentCardEvent.class, event -> {
            PlayEquipmentCardEvent playEquipmentCardEvent = (PlayEquipmentCardEvent) event;
            PlayCardPresenter.PlayEquipmentCardDataViewModel dataViewModel = new PlayCardPresenter.PlayEquipmentCardDataViewModel(
                    playEquipmentCardEvent.getPlayerId(),
                    playEquipmentCardEvent.getCardId(),
                    playEquipmentCardEvent.getDeprecatedCardId()
            );
            return new PlayCardPresenter.PlayEquipmentCardViewModel(dataViewModel);
        });

        eventToViewModelMappers.put(DiscardEvent.class, event -> {
            DiscardEvent discardEvent = (DiscardEvent) event;
            return new DiscardPresenter.DiscardViewModel(discardEvent);
        });

    }

    public List<ViewModel<?>> mapEventsToViewModels(List<DomainEvent> events) {
        return events.stream()
                .map(this::mapEventToViewModel)
                .filter(Objects::nonNull) // 過濾掉無法處理的事件
                .collect(Collectors.toList());
    }

    private ViewModel<?> mapEventToViewModel(DomainEvent event) {
        // 根據事件類型找到對應的轉型器並執行
        Function<DomainEvent, ViewModel<?>> mapper = eventToViewModelMappers.get(event.getClass());
        if (mapper != null) {
            return mapper.apply(event);
        }
        return null; // 沒有對應轉型器的事件將被忽略
    }
}
