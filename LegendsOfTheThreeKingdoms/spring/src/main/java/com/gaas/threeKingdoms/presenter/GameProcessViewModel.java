package com.gaas.threeKingdoms.presenter;

import com.gaas.threeKingdoms.presenter.common.PrivateSkillCardMasker;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class GameProcessViewModel<T> {
    protected List<ViewModel<?>> events;
    protected T data;
    protected String message;

    /**
     * per-player 推播用：依 viewer 遮蔽只有發動者能看的技能牌（如觀星觀看的牌堆頂）。
     */
    protected GameProcessViewModel(List<ViewModel<?>> events, T data, String message, String viewerPlayerId) {
        this(PrivateSkillCardMasker.maskFor(events, viewerPlayerId), data, message);
    }

    public void addViewModelToEvents(ViewModel viewModel) {
        events.add(viewModel);
    }

    public void addViewModelToEvents(int index, ViewModel viewModel) {
        events.add(index, viewModel);
    }
}
