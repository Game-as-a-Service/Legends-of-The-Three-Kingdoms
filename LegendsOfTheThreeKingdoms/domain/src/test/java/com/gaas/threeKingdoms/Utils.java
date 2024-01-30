package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.events.DomainEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Utils {

    public static <T> boolean compareArrayLists(List<T> list1, List<T> list2) {
        if (list1.size() != list2.size()) {
            return false;
        }

        List<T> copy = new ArrayList<>(list2);

        for (T element : list1) {
            int index = copy.indexOf(element);
            if (index != -1) {
                copy.remove(index);
            } else {
                return false;
            }
        }
        return copy.isEmpty();
    }

    @SuppressWarnings("unchecked")
    public static <T extends DomainEvent> Optional<T> getEvent(List<DomainEvent> events, Class<T> type) {
        return events.stream()
                .filter(e -> type.isAssignableFrom(e.getClass()))
                .map(e -> (T) e).findFirst();
    }
}
