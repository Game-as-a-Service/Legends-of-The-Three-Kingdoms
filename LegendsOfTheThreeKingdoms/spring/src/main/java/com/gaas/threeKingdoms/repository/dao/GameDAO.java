package com.gaas.threeKingdoms.repository.dao;

import com.gaas.threeKingdoms.repository.data.GameData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameDAO extends MongoRepository<GameData, String> {
}
