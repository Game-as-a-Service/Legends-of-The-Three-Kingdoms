package com.waterball.LegendsOfTheThreeKingdoms.repository;

import com.waterball.LegendsOfTheThreeKingdoms.domain.Game;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class InMemoryGameRepository {

    HashMap<String, Game> store = new HashMap<>();

    public Game save(Game game) {
        store.put(game.getGameId(), game);
        return game;
    }

    public Game findGameById(String gameId) {
        return store.get(gameId);
    }
}
