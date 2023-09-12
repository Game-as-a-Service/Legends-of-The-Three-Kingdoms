package com.waterball.LegendsOfTheThreeKingdoms.presenter;

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
}
