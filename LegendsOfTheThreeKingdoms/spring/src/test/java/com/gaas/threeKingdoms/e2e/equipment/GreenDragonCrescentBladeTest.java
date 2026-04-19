package com.gaas.threeKingdoms.e2e.equipment;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.e2e.JsonFileWriterUtil;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.EquipmentPlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.EightDiagramTactic;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.RedRabbitHorse;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.GreenDragonCrescentBladeCard;
import com.gaas.threeKingdoms.handcard.scrollcard.Dismantle;
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

public class GreenDragonCrescentBladeTest extends AbstractBaseIntegrationTest {

    // @Override protected boolean shouldRegenerateFixtures() { return true; }

    @Test
    public void testEquipGreenDragonCrescentBlade() throws Exception {
        // Given A 手牌有青龍偃月刀
        Player playerA = createPlayer("player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH,
                new GreenDragonCrescentBladeCard(ES5005));
        Player playerB = createPlayer("player-b", 4, General.劉備, HealthStatus.ALIVE, Role.TRAITOR);
        Player playerC = createPlayer("player-c", 4, General.劉備, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.劉備, HealthStatus.ALIVE, Role.MINISTER);
        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Kill(BS9009)));
        game.setDeck(deck);
        repository.save(game);

        // When A 出青龍偃月刀
        mockMvcUtil.playCard(gameId, "player-a", "player-a", "ES5005", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        // Then 驗證 4 個 player 的 WebSocket JSON
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/EquipmentTest/GreenDragonCrescentBlade/player_a_equip_gdcb_for_%s.json";
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
    public void testDodgeTriggersAskEffect_AndAttackerActivatesKill() throws Exception {
        // Given A 已裝備青龍偃月刀，A 出殺 BS8008 → B 出閃
        givenPlayerAEquippedGreenDragonCrescentBlade();

        mockMvcUtil.playCard(gameId, "player-a", "player-b", "BS8008", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        websocketUtil.popAllPlayerMessage();
        mockMvcUtil.playCard(gameId, "player-b", "player-a", "BH2028", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        websocketUtil.popAllPlayerMessage();

        // When A 選 KILL，再出 BS9009
        mockMvcUtil.useGreenDragonCrescentBladeEffect(gameId, "player-a", "KILL", "BS9009")
                .andExpect(status().isOk()).andReturn();

        // Then 驗證 4 個 player 的 WebSocket JSON
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/EquipmentTest/GreenDragonCrescentBlade/gdcb_kill_triggered_for_%s.json";
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
    public void testDodgeTriggersAskEffect_AndAttackerSkips() throws Exception {
        // Given A 已裝備青龍偃月刀
        givenPlayerAEquippedGreenDragonCrescentBlade();

        mockMvcUtil.playCard(gameId, "player-a", "player-b", "BS8008", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        websocketUtil.popAllPlayerMessage();
        mockMvcUtil.playCard(gameId, "player-b", "player-a", "BH2028", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        websocketUtil.popAllPlayerMessage();

        // When A 選 SKIP 不發動
        mockMvcUtil.useGreenDragonCrescentBladeEffect(gameId, "player-a", "SKIP", "")
                .andExpect(status().isOk()).andReturn();

        // Then 驗證 4 個 player 的 WebSocket JSON
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/EquipmentTest/GreenDragonCrescentBlade/gdcb_skip_for_%s.json";
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
    public void testEightDiagramTacticSuccess_TriggersGreenDragonCrescentBladeAsk() throws Exception {
        // Given A 裝備青龍偃月刀 + B 裝備八卦陣，deck 頂是紅色卡（八卦陣成功）
        Player playerA = createPlayer("player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH,
                new Kill(BS8008), new Kill(BS9009));
        playerA.getEquipment().setWeapon(new GreenDragonCrescentBladeCard(ES5005));

        Player playerB = createPlayer("player-b", 4, General.劉備, HealthStatus.ALIVE, Role.TRAITOR);
        playerB.getEquipment().setArmor(new EightDiagramTactic(ES2015));

        Player playerC = createPlayer("player-c", 4, General.劉備, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.劉備, HealthStatus.ALIVE, Role.MINISTER);

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        Deck deck = new Deck();
        deck.add(List.of(new RedRabbitHorse(BH3029)));
        game.setDeck(deck);
        repository.save(game);

        // When A 出殺 → B 發動八卦陣（抽到紅色成功）
        mockMvcUtil.playCard(gameId, "player-a", "player-b", "BS8008", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        websocketUtil.popAllPlayerMessage();
        mockMvcUtil.useEquipment(gameId, "player-b", "player-b", "ES2015", EquipmentPlayType.ACTIVE)
                .andExpect(status().isOk()).andReturn();

        // Then A 應收到 AskGreenDragonCrescentBladeEffectEvent
        assertAllPlayerJson("src/test/resources/TestJsonFile/EquipmentTest/GreenDragonCrescentBlade/gdcb_ask_after_eight_diagram_success_for_%s.json");
    }

    @Test
    public void testEightDiagramTacticSuccess_AttackerChoosesKill_TargetAskedEquipmentAgain() throws Exception {
        // Given A 裝備青龍偃月刀、B 裝備八卦陣，八卦陣成功
        givenPlayerAEquippedGDCBAndPlayerBHasEightDiagramTactic_WithRedDeck();

        // A 出殺 → B 發動八卦陣成功 → A 收到 AskGDCB
        mockMvcUtil.playCard(gameId, "player-a", "player-b", "BS8008", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        websocketUtil.popAllPlayerMessage();
        mockMvcUtil.useEquipment(gameId, "player-b", "player-b", "ES2015", EquipmentPlayType.ACTIVE)
                .andExpect(status().isOk()).andReturn();
        websocketUtil.popAllPlayerMessage();

        // When A 選 KILL 再出 BS9009
        mockMvcUtil.useGreenDragonCrescentBladeEffect(gameId, "player-a", "KILL", "BS9009")
                .andExpect(status().isOk()).andReturn();

        // Then B 應被再問八卦陣（AskPlayEquipmentEffectEvent）
        assertAllPlayerJson("src/test/resources/TestJsonFile/EquipmentTest/GreenDragonCrescentBlade/gdcb_after_eight_diagram_kill_retriggered_for_%s.json");
    }

    @Test
    public void testEightDiagramTacticSuccess_AttackerChoosesSkip_KillCancelled() throws Exception {
        // Given A 裝備青龍偃月刀、B 裝備八卦陣，八卦陣成功
        givenPlayerAEquippedGDCBAndPlayerBHasEightDiagramTactic_WithRedDeck();

        // A 出殺 → B 發動八卦陣成功 → A 收到 AskGDCB
        mockMvcUtil.playCard(gameId, "player-a", "player-b", "BS8008", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        websocketUtil.popAllPlayerMessage();
        mockMvcUtil.useEquipment(gameId, "player-b", "player-b", "ES2015", EquipmentPlayType.ACTIVE)
                .andExpect(status().isOk()).andReturn();
        websocketUtil.popAllPlayerMessage();

        // When A 選 SKIP 不發動
        mockMvcUtil.useGreenDragonCrescentBladeEffect(gameId, "player-a", "SKIP", "")
                .andExpect(status().isOk()).andReturn();

        // Then 殺被抵銷，回合換 A（current round player）
        assertAllPlayerJson("src/test/resources/TestJsonFile/EquipmentTest/GreenDragonCrescentBlade/gdcb_after_eight_diagram_skip_for_%s.json");
    }

    @Test
    public void testEightDiagramTacticSuccess_AttackerChoosesKill_FullFlowToDamage() throws Exception {
        // Given A 裝備青龍偃月刀、B 裝備八卦陣
        // Flow: 1st 八卦陣 成功 → A 選 KILL → B 選 SKIP 八卦陣 → B 沒閃 → B 扣血
        givenPlayerAEquippedGDCBAndPlayerBHasEightDiagramTactic_WithRedDeck();

        // A 出殺 → 1st 八卦陣 success（抽紅） → A 收到 AskGDCB
        mockMvcUtil.playCard(gameId, "player-a", "player-b", "BS8008", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        websocketUtil.popAllPlayerMessage();
        mockMvcUtil.useEquipment(gameId, "player-b", "player-b", "ES2015", EquipmentPlayType.ACTIVE)
                .andExpect(status().isOk()).andReturn();
        websocketUtil.popAllPlayerMessage();

        // A 選 KILL 再出 BS9009 → B 被再問八卦陣
        mockMvcUtil.useGreenDragonCrescentBladeEffect(gameId, "player-a", "KILL", "BS9009")
                .andExpect(status().isOk()).andReturn();
        websocketUtil.popAllPlayerMessage();

        // B 選 SKIP 不發動八卦陣 → 被問出閃
        mockMvcUtil.useEquipment(gameId, "player-b", "player-b", "ES2015", EquipmentPlayType.SKIP)
                .andExpect(status().isOk()).andReturn();
        websocketUtil.popAllPlayerMessage();

        // B 沒閃，SKIP → 應扣血
        mockMvcUtil.playCard(gameId, "player-b", "player-a", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();

        // Then B HP = 3
        assertAllPlayerJson("src/test/resources/TestJsonFile/EquipmentTest/GreenDragonCrescentBlade/gdcb_after_eight_diagram_full_flow_damage_for_%s.json");
    }

    private void givenPlayerAEquippedGDCBAndPlayerBHasEightDiagramTactic_WithRedDeck() {
        Player playerA = createPlayer("player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH,
                new Kill(BS8008), new Kill(BS9009));
        playerA.getEquipment().setWeapon(new GreenDragonCrescentBladeCard(ES5005));

        Player playerB = createPlayer("player-b", 4, General.劉備, HealthStatus.ALIVE, Role.TRAITOR);
        playerB.getEquipment().setArmor(new EightDiagramTactic(ES2015));

        Player playerC = createPlayer("player-c", 4, General.劉備, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.劉備, HealthStatus.ALIVE, Role.MINISTER);

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        Deck deck = new Deck();
        deck.add(List.of(new RedRabbitHorse(BH3029), new RedRabbitHorse(BH4030)));
        game.setDeck(deck);
        repository.save(game);
    }

    private void givenPlayerAEquippedGreenDragonCrescentBlade() {
        Player playerA = createPlayer("player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH,
                new Kill(BS8008), new Kill(BS9009));
        playerA.getEquipment().setWeapon(new GreenDragonCrescentBladeCard(ES5005));

        Player playerB = createPlayer("player-b", 4, General.劉備, HealthStatus.ALIVE, Role.TRAITOR,
                new Dodge(BH2028), new Dodge(BH2041));
        Player playerC = createPlayer("player-c", 4, General.劉備, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.劉備, HealthStatus.ALIVE, Role.MINISTER);

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Kill(BS0010), new Peach(BH3029), new Dodge(BH4030), new Kill(BS7020)));
        game.setDeck(deck);
        repository.save(game);
    }
}
