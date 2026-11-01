package com.gaas.threeKingdoms.presenter;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.gaas.threeKingdoms.events.DomainEvent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
// 固定輸出順序：Jackson 2.18+ 對「子類別覆寫 getter」的欄位排序規則改了，不指定會讓 event 跑到 data 後面
@JsonPropertyOrder({"event", "data", "message"})
public class ViewModel<T> {
    protected String event; //List<Event>
    protected T data;
    protected String message;


    @SuppressWarnings("unchecked")
    public static <T extends DomainEvent> Optional<T> getEvent(List<DomainEvent> events, Class<T> type) {
        return events.stream()
                .filter(e -> type.isAssignableFrom(e.getClass()))
                .map(e -> (T) e)
                .findFirst();
    }

    @SuppressWarnings("unchecked")
    public static <T extends DomainEvent> Optional<T> getEvent(List<DomainEvent> events, Class<T> type, int index) {
        List<T> filteredEvents = events.stream()
                .filter(e -> type.isAssignableFrom(e.getClass()))
                .map(e -> (T) e)
                .toList();

        if (index >= 0 && index < filteredEvents.size()) {
            return Optional.of(filteredEvents.get(index));
        }

        return Optional.empty(); // 如果索引無效，返回空的 Optional
    }


    protected static <T extends DomainEvent> List<T> getEvents(List<DomainEvent> events, Class<T> type) {
        return events.stream()
                .filter(e -> type.isAssignableFrom(e.getClass()))
                .map(e -> (T) e)
                .collect(Collectors.toList());
    }
}
