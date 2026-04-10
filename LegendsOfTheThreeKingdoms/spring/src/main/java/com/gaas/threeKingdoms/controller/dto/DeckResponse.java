package com.gaas.threeKingdoms.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DeckResponse {
    private String gameId;
    private int deckSize;
    private List<String> cardIds;
}
