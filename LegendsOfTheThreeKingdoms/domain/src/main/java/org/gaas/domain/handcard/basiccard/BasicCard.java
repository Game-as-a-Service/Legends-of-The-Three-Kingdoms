package org.gaas.domain.handcard.basiccard;

import org.gaas.domain.handcard.HandCard;
import org.gaas.domain.handcard.PlayCard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;


public abstract class BasicCard extends HandCard {
    public BasicCard(PlayCard playCard) {
        super(playCard);
    }
}
