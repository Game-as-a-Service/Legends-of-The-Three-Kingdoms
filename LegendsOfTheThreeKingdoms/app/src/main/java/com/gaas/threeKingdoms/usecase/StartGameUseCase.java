package com.gaas.threeKingdoms.usecase;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.GetMonarchGeneralCardsEvent;
import com.gaas.threeKingdoms.outport.GameRepository;
import com.gaas.threeKingdoms.player.Player;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class StartGameUseCase {

    private final GameRepository gameRepository;

    public void execute(CreateGameRequest createGameRequest, CreateGamePresenter createGamePresenter, GetGeneralCardPresenter getMonarchGeneralCardPresenter) {
        // 創建遊戲
        List<Player> players = createGameRequest.getPlayers().stream().map(playerId -> {
            Player player = new Player();
            player.setId(playerId);
            return player;
        }).collect(Collectors.toList());

        // 改
        Game game = new Game(createGameRequest.getGameId(), players);
        game.assignRoles();

        // 存
        gameRepository.save(game);

        // 推
        createGamePresenter.renderGame(game);

        // 主公抽可選擇的武將牌
        GetMonarchGeneralCardsEvent event = game.getMonarchCanChooseGeneralCards();

        // 推播給主公
        getMonarchGeneralCardPresenter.renderGame(event, game.getGameId(), game.getMonarchPlayerId());
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateGameRequest {
        private String gameId;
        private List<String> players;
    }


    public interface CreateGamePresenter<T> {
        void renderGame(Game game);

        T present();
    }

    public interface GetGeneralCardPresenter<T> {
        void renderGame(GetMonarchGeneralCardsEvent event, String gameId, String monarchPlayerId);

        T present();
    }

}
