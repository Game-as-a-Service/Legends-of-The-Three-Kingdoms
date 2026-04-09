package com.gaas.threeKingdoms.e2e.equipment;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.e2e.JsonFileWriterUtil;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.YinYangSwordsCard;
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

public class YinYangSwordsTest extends AbstractBaseIntegrationTest {

    @Test
    public void testPlayerAEquipYinYangSwords() throws Exception {
        // Given A 手牌有雌雄雙股劍
        givenPlayerAHasYinYangSwordsInHand();

        // When A 出雌雄雙股劍
        mockMvcUtil.playCard(gameId, "player-a", "player-a", "ES2002", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        // Then 驗證每個 player 收到的 websocket 訊息
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/EquipmentTest/YinYangSwords/player_a_equip_yin_yang_swords_for_%s.json";
        for (String testPlayerId : playerIds) {
            String testPlayerJson = websocketUtil.getValue(testPlayerId);
//            String testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            testPlayerId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, testPlayerId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }
    }

    @Test
    public void testPlayerAKillsOppositeGenderAndTargetChoosesDiscard() throws Exception {
        // Given A (劉備 MALE, 已裝備雌雄雙股劍) 對 B (甄姬 FEMALE) 出殺
        givenPlayerAEquippedYinYangSwordsAndPlayerBIsFemale();

        // When A 出殺
        mockMvcUtil.playCard(gameId, "player-a", "player-b", "BS8008", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        websocketUtil.popAllPlayerMessage();

        // B 選擇棄一張手牌 (BH3029 Peach)
        mockMvcUtil.useYinYangSwordsEffect(gameId, "player-b", "TARGET_DISCARDS", "BH3029")
                .andExpect(status().isOk()).andReturn();

        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/EquipmentTest/YinYangSwords/target_discards_for_%s.json";
        for (String testPlayerId : playerIds) {
            String testPlayerJson = websocketUtil.getValue(testPlayerId);
//            String testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            testPlayerId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, testPlayerId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }
    }

    @Test
    public void testPlayerAKillsOppositeGenderAndTargetChoosesAttackerDraws() throws Exception {
        // Given A (劉備 MALE, 已裝備雌雄雙股劍) 對 B (甄姬 FEMALE) 出殺
        givenPlayerAEquippedYinYangSwordsAndPlayerBIsFemale();

        // When A 出殺
        mockMvcUtil.playCard(gameId, "player-a", "player-b", "BS8008", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        websocketUtil.popAllPlayerMessage();

        // B 選擇讓 A 摸一張牌
        mockMvcUtil.useYinYangSwordsEffect(gameId, "player-b", "ATTACKER_DRAWS", "")
                .andExpect(status().isOk()).andReturn();

        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/EquipmentTest/YinYangSwords/attacker_draws_for_%s.json";
        for (String testPlayerId : playerIds) {
            String testPlayerJson = websocketUtil.getValue(testPlayerId);
//            String testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            testPlayerId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, testPlayerId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }
    }

    @Test
    public void testPlayerAKillsSameGenderAndYinYangSwordsNotTriggered() throws Exception {
        // Given A (劉備 MALE, 已裝備雌雄雙股劍) 對 C (劉備 MALE) 出殺 → 不觸發雌雄雙股劍
        givenPlayerAEquippedYinYangSwordsAndPlayerBIsFemale();

        // When A 出殺對 C (同性)
        mockMvcUtil.playCard(gameId, "player-a", "player-c", "BS8008", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/EquipmentTest/YinYangSwords/same_gender_no_trigger_for_%s.json";
        for (String testPlayerId : playerIds) {
            String testPlayerJson = websocketUtil.getValue(testPlayerId);
//            String testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            testPlayerId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, testPlayerId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }
    }

    private void givenPlayerAHasYinYangSwordsInHand() {
        Player playerA = createPlayer("player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH,
                new YinYangSwordsCard(ES2002));
        Player playerB = createPlayer("player-b", 4, General.甄姬, HealthStatus.ALIVE, Role.TRAITOR);
        Player playerC = createPlayer("player-c", 4, General.劉備, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.劉備, HealthStatus.ALIVE, Role.MINISTER);

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Kill(BS9009)));
        game.setDeck(deck);
        repository.save(game);
    }

    private void givenPlayerAEquippedYinYangSwordsAndPlayerBIsFemale() {
        Player playerA = createPlayer("player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH, new Kill(BS8008));
        playerA.getEquipment().setWeapon(new YinYangSwordsCard(ES2002));

        Player playerB = createPlayer("player-b", 4, General.甄姬, HealthStatus.ALIVE, Role.TRAITOR,
                new Dodge(BH2028), new Peach(BH3029));
        Player playerC = createPlayer("player-c", 4, General.劉備, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.劉備, HealthStatus.ALIVE, Role.MINISTER);

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Kill(BS9009)));
        game.setDeck(deck);
        repository.save(game);
    }
}
