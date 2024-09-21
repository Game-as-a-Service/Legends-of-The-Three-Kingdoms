package com.gaas.threeKingdoms.repository;

import com.gaas.threeKingdoms.TestGame;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestGameRepository extends MongoRepository<TestGame, String> {

}