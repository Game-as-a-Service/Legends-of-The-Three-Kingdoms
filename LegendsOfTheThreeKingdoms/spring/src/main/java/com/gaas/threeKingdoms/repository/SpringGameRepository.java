package com.gaas.threeKingdoms.repository;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.outport.GameRepository;
import com.gaas.threeKingdoms.repository.dao.GameDAO;
import com.gaas.threeKingdoms.repository.data.GameData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SpringGameRepository implements GameRepository {

    private final GameDAO gameDAO;

    @Override
    public Game save(Game game) {
        GameData data = GameData.fromDomain(game);
        GameData savedData = gameDAO.save(data);
        return savedData.toDomain();
    }

    @Override
    public Optional<Game> findById(String gameId) {
        return gameDAO.findById(gameId)
                .map(GameData::toDomain);
    }
}
