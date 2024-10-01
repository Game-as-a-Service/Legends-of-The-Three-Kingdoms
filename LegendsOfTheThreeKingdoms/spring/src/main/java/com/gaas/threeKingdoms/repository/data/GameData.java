package com.gaas.threeKingdoms.repository.data;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.gamephase.*;
import com.gaas.threeKingdoms.generalcard.GeneralCardDeck;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.PlayCard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameData {

    private String gameId;
    private List<PlayerData> players;
    private GeneralCardDeckData generalCardDeck;
    private DeckData deck;
    private GraveyardData graveyard;
    private SeatingChartData seatingChart;
    private RoundData round;
    private String gamePhase;
    private List<PlayerData> winners;
    private List<BehaviorData> topBehaviors;

    public Game toDomain() {
        Game game = null;

        game = Game.builder()
                .gameId(this.gameId)
                .players(this.players.stream().map(PlayerData::toDomain).collect(Collectors.toList()))
                .deck(this.deck.toDomain())
                .graveyard(this.graveyard.toDomain())
                .seatingChart(this.seatingChart.toDomain())
                .currentRound(this.round.toDomain())
                .gamePhase(enterGamePhase(this.gamePhase, game))
                .winners(this.winners.stream().map(PlayerData::toDomain).collect(Collectors.toList()))
                .build();

        Stack<Behavior> topBehaviors = new Stack<>();
        for (int i = this.topBehaviors.size() - 1; i >= 0; i--) {
            topBehaviors.push(this.topBehaviors.get(i).toDomain(game));
        }
        game.setTopBehavior(topBehaviors);

        return game;
    }

    public static GameData fromDomain(Game game) {
        return GameData.builder()
                .gameId(game.getGameId())
                .players(game.getPlayers().stream().map(PlayerData::fromDomain).collect(Collectors.toList()))
                .generalCardDeck(GeneralCardDeckData.fromDomain(game.getGeneralCardDeck()))
                .deck(DeckData.fromDomain(game.getDeck()))
                .graveyard(GraveyardData.fromDomain(game.getGraveyard()))
                .seatingChart(SeatingChartData.fromDomain(game.getSeatingChart()))
                .round(RoundData.fromDomain(game.getCurrentRound()))
                .gamePhase(game.getGamePhase().getPhaseName())
                .winners(game.getWinners().stream().map(PlayerData::fromDomain).collect(Collectors.toList()))
                .topBehaviors(game.getTopBehavior().stream().map(BehaviorData::fromDomain).collect(Collectors.toCollection(ArrayList::new)))
                .build();
    }

    public static GamePhase enterGamePhase(String gamePhase, Game game) {
        return switch (gamePhase) {
            case "Normal" -> new Normal(game);
            case "Initial" -> new Initial(game);
            case "GeneralDying" -> new GeneralDying(game);
            case "GameOver" -> new GameOver(game);
            default -> null;
        };
    }

}
