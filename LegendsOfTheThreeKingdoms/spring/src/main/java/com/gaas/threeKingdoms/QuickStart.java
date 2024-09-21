package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.repository.TestGameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class QuickStart implements CommandLineRunner {

    @Autowired
    private TestGameRepository testGameRepository;

    public static void main(String[] args) {
        SpringApplication.run(QuickStart.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        TestGame game = new TestGame("YangJum4", "Strategy", 2021);
        testGameRepository.save(game);
        System.out.println("TestGame has been saved successfully!");
    }
}
