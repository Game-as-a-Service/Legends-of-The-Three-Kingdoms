package com.gaas.threeKingdoms.e2e.scrollcard;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.RedRabbitHorse;
import com.gaas.threeKingdoms.handcard.scrollcard.PeachGarden;
import com.gaas.threeKingdoms.player.Equipment;
import com.gaas.threeKingdoms.player.HealthStatus;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.rolecard.Role;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.e2e.MockUtil.createPlayer;
import static com.gaas.threeKingdoms.e2e.MockUtil.initGame;
import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PeachGardenTest extends AbstractBaseIntegrationTest {

    public PeachGardenTest() {
        this.gameId = "my-id";
    }

    private String peachGardenCardId = "SHA027";

    @Test
    public void givenPlayerABCD_PlayerAHasPeachGarden_WhenPlayerAPlaysPeachGarden_ThenActivePlayerRemainsA_PlayerCHpIncrease() throws Exception {
//        Given
//        玩家 A B C D
//        A的回合
//        A hp=1 max=1
//        B hp=2 max=2
//        c hp=1 max=2
//        d hp=0 max=2
//        A有有桃園結義
        givenPlayerAHavePeachGarden();

        String currentPlayer = "player-a";
        String targetPlayerId = "player-c";
        String playedCardId = "BS8008";
        mockMvcUtil.playCard(gameId, currentPlayer, targetPlayerId, playedCardId, "active")
                .andExpect(status().isOk()).andReturn();
        websocketUtil.popAllPlayerMessage();
        mockMvcUtil.playCard(gameId, targetPlayerId, currentPlayer, "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();
        websocketUtil.popAllPlayerMessage();

//        When
//        A 出桃園結義
        currentPlayer = "player-a";
        playedCardId = peachGardenCardId;
        mockMvcUtil.playCard(gameId, currentPlayer, "", playedCardId, "active")
                .andExpect(status().isOk()).andReturn();

//        Then
//        active player 還是 A
//        A hp=1 max=1
//        B hp=2 max=2
//        c hp=2 max=2
//        d hp=0 max=2

        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/PeachGardenBehavior/player_a_play_peach_garden_for_%s.json";
        for (String testPlayerId : playerIds) {
            String testPlayerJson = "";
//            testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            testPlayerJson = websocketUtil.getValue(testPlayerId);
            testPlayerId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, testPlayerId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }

    }

    private void givenPlayerAHavePeachGarden() {
        Player playerA = createPlayer(
                "player-a",
                1,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new PeachGarden(SHA027)
        );
        Equipment equipmentA = new Equipment();
        equipmentA.setMinusOne(new RedRabbitHorse(EH5044));
        playerA.setEquipment(equipmentA);

        Player playerB = createPlayer("player-b",
                2,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MINISTER,
                new Peach(BH4030), new Dodge(BH2028)
        );
        Player playerC = createPlayer(
                "player-c",
                2,
                General.劉備,
                HealthStatus.ALIVE,
                Role.REBEL,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)
        );
        Player playerD = createPlayer(
                "player-d",
                0,
                General.劉備,
                HealthStatus.DEATH,
                Role.TRAITOR,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)
        );

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        repository.save(game);
    }


}
