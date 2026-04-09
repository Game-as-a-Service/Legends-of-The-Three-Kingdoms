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
import com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.EightDiagramTactic;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.EighteenSpanViperSpearCard;
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

public class EighteenSpanViperSpearTest extends AbstractBaseIntegrationTest {

    private static final List<String> PLAYER_IDS = List.of("player-a", "player-b", "player-c", "player-d");

    @Test
    public void testEquipEighteenSpanViperSpear() throws Exception {
        Player playerA = createPlayer("player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH,
                new EighteenSpanViperSpearCard(ESQ025));
        Player playerB = createPlayer("player-b", 4, General.劉備, HealthStatus.ALIVE, Role.TRAITOR);
        Player playerC = createPlayer("player-c", 4, General.劉備, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.劉備, HealthStatus.ALIVE, Role.MINISTER);
        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Kill(BS9009)));
        game.setDeck(deck);
        repository.save(game);

        mockMvcUtil.playCard(gameId, "player-a", "player-a", "ESQ025", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        assertAllPlayerJson("src/test/resources/TestJsonFile/EquipmentTest/EighteenSpanViperSpear/player_a_equip_viper_spear_for_%s.json");
    }

    @Test
    public void testUseViperSpearKill_TriggerEventBroadcast() throws Exception {
        givenPlayerAEquippedViperSpear();

        // A 棄兩張牌當殺攻擊 B
        mockMvcUtil.useViperSpearKill(gameId, "player-a", "player-b",
                        List.of("BH3029", "BH4030"))
                .andExpect(status().isOk()).andReturn();

        // 驗證 ViperSpearKillTriggerEvent payload 與 AskDodgeEvent 有廣播到 4 位玩家
        assertAllPlayerJson("src/test/resources/TestJsonFile/EquipmentTest/EighteenSpanViperSpear/viper_spear_kill_trigger_for_%s.json");
    }

    @Test
    public void testUseViperSpearKill_TargetPlaysDodge() throws Exception {
        givenPlayerAEquippedViperSpear();

        // A 棄兩張牌當作殺，指定 B 為目標
        mockMvcUtil.useViperSpearKill(gameId, "player-a", "player-b",
                        List.of("BH3029", "BH4030"))
                .andExpect(status().isOk()).andReturn();
        websocketUtil.popAllPlayerMessage();

        // B 出閃
        mockMvcUtil.playCard(gameId, "player-b", "player-a", "BH2028", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        assertAllPlayerJson("src/test/resources/TestJsonFile/EquipmentTest/EighteenSpanViperSpear/viper_spear_kill_dodge_for_%s.json");
    }

    @Test
    public void testUseViperSpearKill_TargetSkipsDodge() throws Exception {
        givenPlayerAEquippedViperSpear();

        // A 棄兩張牌當作殺，指定 B 為目標
        mockMvcUtil.useViperSpearKill(gameId, "player-a", "player-b",
                        List.of("BH3029", "BH4030"))
                .andExpect(status().isOk()).andReturn();
        websocketUtil.popAllPlayerMessage();

        // B 跳過出閃
        mockMvcUtil.playCard(gameId, "player-b", "player-a", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();

        assertAllPlayerJson("src/test/resources/TestJsonFile/EquipmentTest/EighteenSpanViperSpear/viper_spear_kill_skip_for_%s.json");
    }

    @Test
    public void testUseViperSpearKill_TargetHasEightDiagram_AsksEquipmentEffect() throws Exception {
        Player playerA = createPlayer("player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH,
                new Peach(BH3029), new Peach(BH4030));
        playerA.getEquipment().setWeapon(new EighteenSpanViperSpearCard(ESQ025));

        Player playerB = createPlayer("player-b", 4, General.劉備, HealthStatus.ALIVE, Role.TRAITOR);
        playerB.getEquipment().setArmor(new EightDiagramTactic(ES2015));

        Player playerC = createPlayer("player-c", 4, General.劉備, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.劉備, HealthStatus.ALIVE, Role.MINISTER);

        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Kill(BS0010), new Peach(BH6032), new Dodge(BH7033), new Kill(BS7020)));
        game.setDeck(deck);
        repository.save(game);

        // A 棄兩張牌當殺攻擊 B（B 有八卦陣）
        mockMvcUtil.useViperSpearKill(gameId, "player-a", "player-b",
                        List.of("BH3029", "BH4030"))
                .andExpect(status().isOk()).andReturn();

        // 預期收到 AskPlayEquipmentEffectEvent（先問八卦陣，而不是直接問閃）
        assertAllPlayerJson("src/test/resources/TestJsonFile/EquipmentTest/EighteenSpanViperSpear/viper_spear_kill_bagua_for_%s.json");
    }

    @Test
    public void testUseViperSpearKill_TargetDying_AsksPeach() throws Exception {
        Player playerA = createPlayer("player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH,
                new Peach(BH3029), new Peach(BH4030));
        playerA.getEquipment().setWeapon(new EighteenSpanViperSpearCard(ESQ025));

        Player playerB = createPlayer("player-b", 1, General.劉備, HealthStatus.ALIVE, Role.TRAITOR);
        Player playerC = createPlayer("player-c", 4, General.劉備, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.劉備, HealthStatus.ALIVE, Role.MINISTER);

        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Kill(BS0010), new Peach(BH6032), new Dodge(BH7033), new Kill(BS7020)));
        game.setDeck(deck);
        repository.save(game);

        // A 棄兩張牌當殺攻擊 B（B HP=1）
        mockMvcUtil.useViperSpearKill(gameId, "player-a", "player-b",
                        List.of("BH3029", "BH4030"))
                .andExpect(status().isOk()).andReturn();
        websocketUtil.popAllPlayerMessage();

        // B 不出閃 → 扣血至 0 → 瀕死 → AskPeachEvent
        mockMvcUtil.playCard(gameId, "player-b", "player-a", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();

        assertAllPlayerJson("src/test/resources/TestJsonFile/EquipmentTest/EighteenSpanViperSpear/viper_spear_kill_dying_for_%s.json");
    }

    @Test
    public void testUseViperSpearKill_NotEquipped_Returns4xx() throws Exception {
        // A 沒裝備丈八蛇矛
        Player playerA = createPlayer("player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH,
                new Peach(BH3029), new Peach(BH4030));
        Player playerB = createPlayer("player-b", 4, General.劉備, HealthStatus.ALIVE, Role.TRAITOR);
        Player playerC = createPlayer("player-c", 4, General.劉備, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.劉備, HealthStatus.ALIVE, Role.MINISTER);

        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        game.setDeck(new Deck());
        repository.save(game);

        mockMvcUtil.useViperSpearKill(gameId, "player-a", "player-b",
                        List.of("BH3029", "BH4030"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testUseViperSpearKill_WrongDiscardCount_Returns4xx() throws Exception {
        givenPlayerAEquippedViperSpear();

        // 只傳一張
        mockMvcUtil.useViperSpearKill(gameId, "player-a", "player-b",
                        List.of("BH3029"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testUseViperSpearKill_DiscardCardNotInHand_Returns4xx() throws Exception {
        givenPlayerAEquippedViperSpear();

        // BS9999 不在 A 手牌
        mockMvcUtil.useViperSpearKill(gameId, "player-a", "player-b",
                        List.of("BH3029", "BS9999"))
                .andExpect(status().is4xxClientError());
    }

    private void givenPlayerAEquippedViperSpear() {
        Player playerA = createPlayer("player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH,
                new Peach(BH3029), new Peach(BH4030));
        playerA.getEquipment().setWeapon(new EighteenSpanViperSpearCard(ESQ025));

        Player playerB = createPlayer("player-b", 4, General.劉備, HealthStatus.ALIVE, Role.TRAITOR,
                new Dodge(BH2028));
        Player playerC = createPlayer("player-c", 4, General.劉備, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.劉備, HealthStatus.ALIVE, Role.MINISTER);

        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Kill(BS0010), new Peach(BH6032), new Dodge(BH7033), new Kill(BS7020)));
        game.setDeck(deck);
        repository.save(game);
    }

    private void assertAllPlayerJson(String filePathTemplate) throws Exception {
        for (String testPlayerId : PLAYER_IDS) {
            String testPlayerJson = websocketUtil.getValue(testPlayerId);
//            String testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            String filePlayerId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, filePlayerId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }
    }
}
