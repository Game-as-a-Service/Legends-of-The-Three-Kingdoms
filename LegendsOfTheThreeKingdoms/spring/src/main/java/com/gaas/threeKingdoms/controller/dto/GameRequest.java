package com.gaas.threeKingdoms.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.gaas.threeKingdoms.usecase.StartGameUseCase;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameRequest implements Serializable {
    private String gameId;
    private List<String> players;


    public StartGameUseCase.CreateGameRequest toUseCaseRequest() {
        return new StartGameUseCase.CreateGameRequest(this.gameId,this.getPlayers());
    }
}
