package com.waterball.LegendsOfTheThreeKingdoms.presenter;

public abstract class ViewModel<T> {
    protected String event;
    protected T data;
    protected String message;

    public ViewModel() {
    }

    public ViewModel(String event, T data, String message) {
        this.event = event;
        this.data = data;
        this.message = message;
    }

    public String getEvent() {
        return event;
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

}
