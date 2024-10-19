package com.gaas.threeKingdoms.repository.data;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.gamephase.*;
import com.gaas.threeKingdoms.player.Player;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.*;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameData {

    @Id  // 將 gameId 作為 MongoDB 的主鍵
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
        Game game = new Game();
        game.setGameId(this.gameId);
        game.setPlayers(this.players.stream().map(PlayerData::toDomain).collect(Collectors.toList()));
        game.setDeck(this.deck.toDomain());
        game.setGeneralCardDeck(this.generalCardDeck == null ? null : generalCardDeck.toDomain());
        game.setGraveyard(this.graveyard.toDomain());
        game.setSeatingChart(this.seatingChart.toDomain());
        game.setCurrentRound(round == null ? null : this.round.toDomain());
        game.setGamePhase(enterGamePhase(this.gamePhase, game));
        game.setWinners(this.winners != null ? this.winners.stream().map(PlayerData::toDomain).collect(Collectors.toList()) : new ArrayList<>());

        Stack<Behavior> topBehaviors = new Stack<>();

        for (BehaviorData behaviorData : this.topBehaviors) {
            topBehaviors.push(behaviorData.toDomain(game));
        }
        game.setTopBehavior(topBehaviors);

        return game;
    }

    public static GameData fromDomain(Game game) {
        List<Player> winner = Optional.ofNullable(game.getWinners()).orElseGet(Collections::emptyList);
        List<PlayerData> winnerData = winner.stream().map(PlayerData::fromDomain).collect(Collectors.toList());

        return GameData.builder()
                .gameId(game.getGameId())
                .players(game.getPlayers().stream().map(PlayerData::fromDomain).collect(Collectors.toList()))
                .generalCardDeck(GeneralCardDeckData.fromDomain(game.getGeneralCardDeck()))
                .deck(DeckData.fromDomain(game.getDeck()))
                .graveyard(GraveyardData.fromDomain(game.getGraveyard()))
                .seatingChart(SeatingChartData.fromDomain(game.getSeatingChart()))
                .round(RoundData.fromDomain(game.getCurrentRound()))
                .gamePhase(game.getGamePhase().getPhaseName())
                .winners(winnerData)
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
