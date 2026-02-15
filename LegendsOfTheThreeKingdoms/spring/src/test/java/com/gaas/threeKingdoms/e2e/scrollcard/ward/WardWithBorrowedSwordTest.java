package com.gaas.threeKingdoms.e2e.scrollcard.ward;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.e2e.JsonFileWriterUtil;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.RepeatingCrossbowCard;
import com.gaas.threeKingdoms.handcard.scrollcard.BorrowedSword;
import com.gaas.threeKingdoms.handcard.scrollcard.Dismantle;
import com.gaas.threeKingdoms.handcard.scrollcard.Ward;
import com.gaas.threeKingdoms.player.Equipment;
import com.gaas.threeKingdoms.player.HealthStatus;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.rolecard.Role;
import org.junit.jupiter.api.DisplayName;
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

public class WardWithBorrowedSwordTest extends AbstractBaseIntegrationTest {

    @Test
    @DisplayName("""
            Given
            有 玩家 A 劉備 B 張飛 C 關羽 D 呂布
            A 有借刀殺人
            B 有武器、殺、無懈可擊

            A 出借刀殺人，指定 B 殺 C
            B 收到 WaitForWardEvent

            When
            B 出無懈可擊

            Then
            A B C D 收到 event B 的無懈可擊 抵銷了 A 的借刀殺人
            """)
    public void givenPlayerAHasBorrowedSwordAndPlayerBHasWard_WhenPlayerBPlaysWard_ThenWardCancelsBorrowedSword() throws Exception {
        givenPlayerAHasBorrowedSwordV1();

        // A 出借刀殺人
        mockMvcUtil.playCard(gameId, "player-a", "player-b", "SCK065", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // A 指定 B 殺 C → 觸發 WaitForWardEvent
        mockMvcUtil.useBorrowedSwordEffect(gameId, "player-a", "player-b", "player-c")
                .andExpect(status().isOk()).andReturn();

        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Ward/BorrowedSword/player_a_use_borrowed_sword_effect_for_%s.json";
        for (String testPlayerId : playerIds) {
            String testPlayerJson = "";
//            testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            testPlayerJson = websocketUtil.getValue(testPlayerId);
            testPlayerId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, testPlayerId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }

        // B 出無懈可擊
        mockMvcUtil.playWardCard(gameId, "player-b", "SSJ011", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Ward/BorrowedSword/player_b_play_ward_for_%s.json";
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

    @Test
    @DisplayName("""
            Given
            有 玩家 A 劉備 B 張飛 C 關羽 D 呂布
            A 有借刀殺人
            B 有武器、殺、無懈可擊
            C 有無懈可擊

            A 出借刀殺人，指定 B 殺 C
            B 出無懈可擊

            When
            C 出無懈可擊

            Then
            A B C D 收到 C 的無懈可擊抵銷了 B 的無懈可擊
            A B C D 收到借刀殺人發動 的 event (BorrowedSwordEvent)
            """)
    public void givenPlayerBAndCHaveWard_WhenPlayerBAndCPlayWard_ThenBorrowedSwordTakesEffect() throws Exception {
        givenPlayerAHasBorrowedSwordV2();

        // A 出借刀殺人
        mockMvcUtil.playCard(gameId, "player-a", "player-b", "SCK065", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // A 指定 B 殺 C → 觸發 WaitForWardEvent
        mockMvcUtil.useBorrowedSwordEffect(gameId, "player-a", "player-b", "player-c")
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");

        // B 出無懈可擊
        mockMvcUtil.playWardCard(gameId, "player-b", "SSJ011", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Ward/BorrowedSword/player_b_play_ward_for_%s_v2.json";
        for (String testPlayerId : playerIds) {
            String testPlayerJson = "";
//            testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            testPlayerJson = websocketUtil.getValue(testPlayerId);
            testPlayerId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, testPlayerId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }

        // C 出無懈可擊
        mockMvcUtil.playWardCard(gameId, "player-c", "SSJ011", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Ward/BorrowedSword/player_c_play_ward_for_%s_v2.json";
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

    @Test
    @DisplayName("""
            Given
            有 玩家 A 劉備 B 張飛 C 關羽 D 呂布
            A 有借刀殺人
            B 有武器、殺、無懈可擊
            C 有無懈可擊

            A 出借刀殺人，指定 B 殺 C
            B skip 無懈可擊

            When
            C skip 無懈可擊

            Then
            A B C D 收到借刀殺人發動 的 event (BorrowedSwordEvent)
            """)
    public void givenPlayerBAndCHaveWard_WhenPlayerBAndCSkipWard_ThenBorrowedSwordTakesEffect() throws Exception {
        givenPlayerAHasBorrowedSwordV2();

        // A 出借刀殺人
        mockMvcUtil.playCard(gameId, "player-a", "player-b", "SCK065", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // A 指定 B 殺 C → 觸發 WaitForWardEvent
        mockMvcUtil.useBorrowedSwordEffect(gameId, "player-a", "player-b", "player-c")
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");

        // B skip 無懈可擊
        mockMvcUtil.playWardCard(gameId, "player-b", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // C skip 無懈可擊
        mockMvcUtil.playWardCard(gameId, "player-c", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();

        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Ward/BorrowedSword/player_c_skip_ward_for_%s_v3.json";
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

    @Test
    @DisplayName("""
            Given
            有 玩家 A 劉備 B 張飛 C 關羽 D 呂布
            A 有借刀殺人、無懈可擊
            B 有武器、殺、無懈可擊
            C 有無懈可擊

            A 出借刀殺人，指定 B 殺 C
            B 出無懈可擊
            A 出無懈可擊

            When
            C skip 無懈可擊

            Then
            A B C D 收到 event >> A 的無懈可擊 抵銷了 B 的無懈可擊
            A B C D 收到借刀殺人發動 的 event (BorrowedSwordEvent)
            """)
    public void givenABCHaveWard_WhenBPlaysWardAndAPlaysWardAndCSkips_ThenBorrowedSwordTakesEffect() throws Exception {
        givenPlayerAHasBorrowedSwordV4();

        // A 出借刀殺人
        mockMvcUtil.playCard(gameId, "player-a", "player-b", "SCK065", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // A 指定 B 殺 C → 觸發 WaitForWardEvent
        mockMvcUtil.useBorrowedSwordEffect(gameId, "player-a", "player-b", "player-c")
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");

        // B 出無懈可擊
        mockMvcUtil.playWardCard(gameId, "player-b", "SSJ011", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Ward/BorrowedSword/player_b_play_ward_for_%s_v4.json";
        for (String testPlayerId : playerIds) {
            String testPlayerJson = "";
//            testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            testPlayerJson = websocketUtil.getValue(testPlayerId);
            testPlayerId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, testPlayerId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }

        // A 出無懈可擊
        mockMvcUtil.playWardCard(gameId, "player-a", "SSJ011", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Ward/BorrowedSword/player_a_play_ward_for_%s_v4.json";
        for (String testPlayerId : playerIds) {
            String testPlayerJson = "";
//            testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            testPlayerJson = websocketUtil.getValue(testPlayerId);
            testPlayerId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, testPlayerId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }

        // C skip 無懈可擊
        mockMvcUtil.playWardCard(gameId, "player-c", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();

        filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Ward/BorrowedSword/player_c_skip_ward_for_%s_v4.json";
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

    @Test
    @DisplayName("""
            Given
            有 玩家 A 劉備 B 張飛 C 關羽 D 呂布
            A 有借刀殺人、無懈可擊
            B 有武器、殺、無懈可擊
            C 有無懈可擊

            A 出借刀殺人，指定 B 殺 C
            C 出無懈可擊
            A 出無懈可擊

            When
            B 出無懈可擊

            Then
            A B C D 收到 B 的無懈可擊抵銷了 A 的無懈可擊
            A B C D 收到 C 的無懈可擊抵銷了 A 的借刀殺人
            """)
    public void givenABCHaveWard_WhenCPlaysWardAndAPlaysWardAndBPlaysWard_ThenWardCancelsBorrowedSword() throws Exception {
        givenPlayerAHasBorrowedSwordV4();

        // A 出借刀殺人
        mockMvcUtil.playCard(gameId, "player-a", "player-b", "SCK065", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // A 指定 B 殺 C → 觸發 WaitForWardEvent
        mockMvcUtil.useBorrowedSwordEffect(gameId, "player-a", "player-b", "player-c")
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");

        // C 出無懈可擊
        mockMvcUtil.playWardCard(gameId, "player-c", "SSJ011", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Ward/BorrowedSword/player_c_play_ward_for_%s_v5.json";
        for (String testPlayerId : playerIds) {
            String testPlayerJson = "";
//            testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            testPlayerJson = websocketUtil.getValue(testPlayerId);
            testPlayerId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, testPlayerId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }

        // A 出無懈可擊
        mockMvcUtil.playWardCard(gameId, "player-a", "SSJ011", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Ward/BorrowedSword/player_a_play_ward_for_%s_v5.json";
        for (String testPlayerId : playerIds) {
            String testPlayerJson = "";
//            testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            testPlayerJson = websocketUtil.getValue(testPlayerId);
            testPlayerId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, testPlayerId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }

        // B 出無懈可擊
        mockMvcUtil.playWardCard(gameId, "player-b", "SSJ011", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Ward/BorrowedSword/player_b_play_ward_for_%s_v5.json";
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

    // V1: A 有借刀殺人, B 有武器+殺+無懈可擊 (只有 B 有 Ward)
    private void givenPlayerAHasBorrowedSwordV1() {
        Player playerA = createPlayer(
                "player-a",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039), new BorrowedSword(SCK065)
        );
        Player playerB = createPlayer("player-b",
                4,
                General.張飛,
                HealthStatus.ALIVE,
                Role.MINISTER,
                new Kill(BS8008), new Peach(BH3029), new Peach(BH2028), new Peach(BH2028), new Ward(SSJ011)
        );
        Player playerC = createPlayer(
                "player-c",
                4,
                General.關羽,
                HealthStatus.ALIVE,
                Role.REBEL,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)
        );
        Player playerD = createPlayer(
                "player-d",
                4,
                General.呂布,
                HealthStatus.ALIVE,
                Role.TRAITOR,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)
        );

        Equipment equipmentB = new Equipment();
        equipmentB.setWeapon(new RepeatingCrossbowCard(ECA066));
        playerB.setEquipment(equipmentB);

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Dodge(BDJ089), new Peach(BH3029), new Dodge(BH2028)));
        game.setDeck(deck);
        repository.save(game);
    }

    // V2: A 有借刀殺人, B 有武器+殺+無懈可擊, C 有無懈可擊
    private void givenPlayerAHasBorrowedSwordV2() {
        Player playerA = createPlayer(
                "player-a",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039), new BorrowedSword(SCK065)
        );
        Player playerB = createPlayer("player-b",
                4,
                General.張飛,
                HealthStatus.ALIVE,
                Role.MINISTER,
                new Kill(BS8008), new Peach(BH3029), new Peach(BH2028), new Peach(BH2028), new Ward(SSJ011)
        );
        Player playerC = createPlayer(
                "player-c",
                4,
                General.關羽,
                HealthStatus.ALIVE,
                Role.REBEL,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039), new Ward(SSJ011)
        );
        Player playerD = createPlayer(
                "player-d",
                4,
                General.呂布,
                HealthStatus.ALIVE,
                Role.TRAITOR,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)
        );

        Equipment equipmentB = new Equipment();
        equipmentB.setWeapon(new RepeatingCrossbowCard(ECA066));
        playerB.setEquipment(equipmentB);

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Dodge(BDJ089), new Peach(BH3029), new Dodge(BH2028)));
        game.setDeck(deck);
        repository.save(game);
    }

    // V4: A 有借刀殺人+無懈可擊, B 有武器+殺+無懈可擊, C 有無懈可擊
    private void givenPlayerAHasBorrowedSwordV4() {
        Player playerA = createPlayer(
                "player-a",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new BorrowedSword(SCK065), new Ward(SSJ011)
        );
        Player playerB = createPlayer("player-b",
                4,
                General.張飛,
                HealthStatus.ALIVE,
                Role.MINISTER,
                new Kill(BS8008), new Peach(BH3029), new Peach(BH2028), new Peach(BH2028), new Ward(SSJ011)
        );
        Player playerC = createPlayer(
                "player-c",
                4,
                General.關羽,
                HealthStatus.ALIVE,
                Role.REBEL,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039), new Ward(SSJ011)
        );
        Player playerD = createPlayer(
                "player-d",
                4,
                General.呂布,
                HealthStatus.ALIVE,
                Role.TRAITOR,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)
        );

        Equipment equipmentB = new Equipment();
        equipmentB.setWeapon(new RepeatingCrossbowCard(ECA066));
        playerB.setEquipment(equipmentB);

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Dodge(BDJ089), new Peach(BH3029), new Dodge(BH2028)));
        game.setDeck(deck);
        repository.save(game);
    }

    private void popAllPlayerMessage() {
        websocketUtil.getValue("player-a");
        websocketUtil.getValue("player-b");
        websocketUtil.getValue("player-c");
        websocketUtil.getValue("player-d");
    }
}
