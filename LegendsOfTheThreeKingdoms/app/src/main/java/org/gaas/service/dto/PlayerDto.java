package org.gaas.service.dto;

import org.gaas.domain.generalcard.GeneralCard;
import org.gaas.domain.rolecard.RoleCard;
import org.gaas.domain.player.Hand;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlayerDto {
    private String id;
    private RoleCard roleCard;
    private GeneralCard generalCard;
    private Hand hand;
}
