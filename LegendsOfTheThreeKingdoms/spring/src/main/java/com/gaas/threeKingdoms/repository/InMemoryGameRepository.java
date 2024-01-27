package com.gaas.threeKingdoms.repository;


import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.outport.GameRepository;
import org.springframework.stereotype.Repository;

import java.util.HashMap;

@Repository
public class InMemoryGameRepository implements GameRepository {

    HashMap<String, Game> store = new HashMap<>();

    public Game save(Game game) {
        store.put(game.getGameId(), game);
        return game;
    }

    @Override
    public Game findById(String gameId) {
        return store.get(gameId);
    }

}
