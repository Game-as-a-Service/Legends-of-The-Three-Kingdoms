package org.example.repository;


import org.springframework.stereotype.Component;

import java.util.HashMap;
import org.gaas.domain.Game;

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
