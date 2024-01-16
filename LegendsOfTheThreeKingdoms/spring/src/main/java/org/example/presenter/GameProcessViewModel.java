package org.example.presenter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class GameProcessViewModel<T> {
    protected List<ViewModel> events;
    protected T data;
    protected String message;

    public void addViewModelToEvents(ViewModel viewModel) {
        events.add(viewModel);
    }

    public void addViewModelToEvents(int index, ViewModel viewModel) {
        events.add(index, viewModel);
    }
}
