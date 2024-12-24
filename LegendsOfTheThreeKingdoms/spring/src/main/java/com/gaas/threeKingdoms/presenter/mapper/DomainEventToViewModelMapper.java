package com.gaas.threeKingdoms.presenter.mapper;

import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.presenter.FinishActionPresenter;
import com.gaas.threeKingdoms.presenter.PlayCardPresenter;
import com.gaas.threeKingdoms.presenter.RoundStartPresenter;
import com.gaas.threeKingdoms.presenter.ViewModel;
import com.gaas.threeKingdoms.presenter.common.RoundDataViewModel;
import org.springframework.stereotype.Component;

import java.util.*;
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

//        eventToViewModelMappers.put(DrawCardEvent.class, event -> {
//            DrawCardEvent drawCardEvent = (DrawCardEvent) event;
//            RoundEvent roundEvent = drawCardEvent.getRound();
//            RoundDataViewModel roundDataViewModel = new RoundDataViewModel(roundEvent);
//            RoundStartPresenter.DrawCardDataViewModel dataViewModel = new RoundStartPresenter.DrawCardDataViewModel(
//                    drawCardEvent.getSize(),
//                    drawCardEvent.getCardIds(),
//                    drawCardEvent.getDrawCardPlayerId()
//            );
//            RoundStartPresenter.DrawCardViewModel drawCardViewModel = new RoundStartPresenter.DrawCardViewModel();
//            drawCardViewModel.setData(dataViewModel);
//            return drawCardViewModel;
//        });


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
