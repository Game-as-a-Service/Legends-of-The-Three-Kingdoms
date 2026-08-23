package com.gaas.threeKingdoms.controller.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class SetGeneralCardDeckRequest {
    private List<String> generalIds;
}
