package com.gaas.threeKingdoms.config;

import com.gaas.threeKingdoms.outport.GameRepository;
import com.gaas.threeKingdoms.usecase.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {
    @Bean
    public StartGameUseCase startGameUseCase(GameRepository gameRepository) {
        return new StartGameUseCase(gameRepository);
    }

    @Bean
    public PlayCardUseCase playCardUseCase(GameRepository gameRepository) {
        return new PlayCardUseCase(gameRepository);
    }

    @Bean
    public DiscardCardUseCase discardCardUseCase(GameRepository gameRepository) {
        return new DiscardCardUseCase(gameRepository);
    }

    @Bean
    public FinishActionUseCase finishActionUseCase(GameRepository gameRepository) {
        return new FinishActionUseCase(gameRepository);
    }

    @Bean
    public FindGameByIdUseCase findGameByIdUseCase(GameRepository gameRepository) {
        return new FindGameByIdUseCase(gameRepository);
    }

    @Bean
    public MonarchChooseGeneralUseCase monarchChooseGeneralUseCase(GameRepository gameRepository) {
        return new MonarchChooseGeneralUseCase(gameRepository);
    }

    @Bean
    public OthersChoosePlayerGeneralUseCase othersChoosePlayerGeneralUseCase(GameRepository gameRepository) {
        return new OthersChoosePlayerGeneralUseCase(gameRepository);
    }
}
