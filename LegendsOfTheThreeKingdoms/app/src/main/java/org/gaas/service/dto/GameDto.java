package org.gaas.service.dto;

import org.gaas.domain.rolecard.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameDto {
    private String gameId;
    private List<PlayerDto> players;
    private String gamePhaseState;
}