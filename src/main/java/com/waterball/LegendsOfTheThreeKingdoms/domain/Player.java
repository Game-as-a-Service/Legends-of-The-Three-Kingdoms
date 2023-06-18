package com.waterball.LegendsOfTheThreeKingdoms.domain;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Player {
    private String id;
    private RoleCard role;
    private GeneralCard generalCard;
}
