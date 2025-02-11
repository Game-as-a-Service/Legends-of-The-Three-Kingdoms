package com.gaas.threeKingdoms.e2e.scrollcard;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.e2e.JsonFileWriterUtil;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.RedRabbitHorse;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.ShadowHorse;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.RepeatingCrossbowCard;
import com.gaas.threeKingdoms.handcard.scrollcard.BorrowedSword;
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

public class BorrowedSwordTest extends AbstractBaseIntegrationTest {

    private String borrowedSwordCardId = "SCK065";

    @Test
    public void givenPlayerABCD_PlayerATurn_PlayerAHasBorrowedSword_WhenPlayerAPlaysBorrowedSwordAndAssignsBToKillC_ThenPlayersABCDReceiveBorrowedSwordEvent_AndPlayerBReceivesRequestToPlayKillEvent() throws Exception {
        //Given
        //玩家ABCD
        //A的回合
        //A有借刀殺人
        //B 有裝備武器，有一張殺，B攻擊範圍內
        //有 C 可以殺
        givenPlayerAHaveBorrowedSwordAndPlayerBEquipedQilinBow(3);

        // When
        //A 出借刀殺人，指定 B 殺 C (先出playcard)
        mockMvcUtil.playCard(gameId, "player-a", "player-b", borrowedSwordCardId, PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        // Then ABCD 玩家收到借刀殺人的 PlayCardEvent
        String playerAPlayBorrowedSwordJsonForA = websocketUtil.getValue("player-a");
        Path path = Paths.get("src/test/resources/TestJsonFile/ScrollTest/BorrowedSword/player_a_playBorrowedSword_for_player_a.json");
        String expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayBorrowedSwordJsonForA);

        String playerAPlayBorrowedSwordJsonForB = websocketUtil.getValue("player-b");
        path = Paths.get("src/test/resources/TestJsonFile/ScrollTest/BorrowedSword/player_a_playBorrowedSword_for_player_b.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayBorrowedSwordJsonForB);

        String playerAPlayBorrowedSwordJsonForC = websocketUtil.getValue("player-c");
        path = Paths.get("src/test/resources/TestJsonFile/ScrollTest/BorrowedSword/player_a_playBorrowedSword_for_player_c.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayBorrowedSwordJsonForC);

        String playerAPlayBorrowedSwordJsonForD = websocketUtil.getValue("player-d");
        path = Paths.get("src/test/resources/TestJsonFile/ScrollTest/BorrowedSword/player_a_playBorrowedSword_for_player_d.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayBorrowedSwordJsonForD);

        // When
        //A 出借刀殺人，指定 B 殺 C (後出useBorrowedSwordEffect)
        mockMvcUtil.useBorrowedSwordEffect(gameId, "player-a", "player-b", "player-c").andExpect(status().isOk()).andReturn();

        //Then
        //ABCD 玩家收到借刀殺人的 event
        //ABCD 玩家收到要求出殺的 event
        playerAPlayBorrowedSwordJsonForA = websocketUtil.getValue("player-a");
        path = Paths.get("src/test/resources/TestJsonFile/ScrollTest/BorrowedSword/player_a_playBorrowedSword_for_player_a_2.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayBorrowedSwordJsonForA);

        playerAPlayBorrowedSwordJsonForB = websocketUtil.getValue("player-b");
        path = Paths.get("src/test/resources/TestJsonFile/ScrollTest/BorrowedSword/player_a_playBorrowedSword_for_player_b_2.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayBorrowedSwordJsonForB);

        playerAPlayBorrowedSwordJsonForC = websocketUtil.getValue("player-c");
        path = Paths.get("src/test/resources/TestJsonFile/ScrollTest/BorrowedSword/player_a_playBorrowedSword_for_player_c_2.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayBorrowedSwordJsonForC);

        playerAPlayBorrowedSwordJsonForD = websocketUtil.getValue("player-d");
        path = Paths.get("src/test/resources/TestJsonFile/ScrollTest/BorrowedSword/player_a_playBorrowedSword_for_player_d_2.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayBorrowedSwordJsonForD);
    }

    @Test
    public void givenPlayerABCD_PlayerATurn_PlayerAHasBorrowedSword_WhenPlayerAPlaysBorrowedSwordAndAssignsBWithNoValidTarget_ThenThrowError() throws Exception {
        //Given
        //玩家ABCD
        //A的回合
        //A有借刀殺人
        //B 有裝備武器，有一張殺，B攻擊範圍內沒有人可以殺
        givenPlayerAHaveBorrowedSwordAndPlayerCAEquipedShadowHorse(3);

        //When
        //A 出借刀殺人，指定 B
        mockMvcUtil.playCard(gameId, "player-a", "player-b", borrowedSwordCardId, PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        //Then
        //拋出錯誤404
        mockMvcUtil.useBorrowedSwordEffect(gameId, "player-a", "player-b", "player-c")
                .andExpect(status().is4xxClientError());

    }

    @Test
    public void givenPlayerABCD_PlayerATurn_PlayerAHasBorrowedSword_WhenPlayerAPlaysBorrowedSwordAndAssignsBWithoutWeaponToKillC_ThenThrowError() throws Exception {
        //Given
        //玩家ABCD
        //A的回合
        //A有借刀殺人
        //B 沒有裝備武器，有一張殺，B攻擊範圍內
        //有 C 可以殺
        givenPlayerAHaveBorrowedSwordAndPlayerBWithoutEquipedWeapon(3);

        //When
        //A 出借刀殺人，指定 B
        mockMvcUtil.playCard(gameId, "player-a", "player-b", borrowedSwordCardId, PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        //Then
        //拋出錯誤404
        mockMvcUtil.useBorrowedSwordEffect(gameId, "player-a", "player-b", "player-c")
                .andExpect(status().is4xxClientError());

    }

    @Test
    public void givenPlayerABCD_PlayerATurn_PlayerAHasBorrowedSword_WhenPlayerAPlaysBorrowedSwordAndAssignsBWithQilinBowToKillD_ThenError404MessageDisplayedDIsOutOfRange() throws Exception {
        //Given
        //玩家ABCD
        //A的回合
        //A有借刀殺人
        //B有諸葛連駑，有一張殺，D不在攻擊範圍內
        givenPlayerAHaveBorrowedSwordAndPlayerBEquipedQilinBow(3);

        //When
        //A 出借刀殺人，指定 B
        mockMvcUtil.playCard(gameId, "player-a", "player-b", borrowedSwordCardId, PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        //Then
        //404 錯誤訊息顯示D不在攻擊範圍
        mockMvcUtil.useBorrowedSwordEffect(gameId, "player-a", "player-b", "player-d")
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void givenPlayerABCD_PlayerATurn_PlayerAHasBorrowedSword_WhenPlayerAPlaysBorrowedSwordAndAssignsBToKillC_AndPlayerBPlaysKill_ThenPlayersABCDReceivePlayerBPlaysKillEvent() throws Exception {
        // Given
        // 玩家ABCD
        // A的回合
        // A有借刀殺人
        // B有裝備武器，有一張殺，B攻擊範圍內
        // 有C可以殺
        // A出借刀殺人, 指定B殺C
        givenPlayerAHaveBorrowedSwordAndPlayerBEquipedQilinBow(3);

        mockMvcUtil.playCard(gameId, "player-a", "player-b", borrowedSwordCardId, PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();
        mockMvcUtil.useBorrowedSwordEffect(gameId, "player-a", "player-b", "player-c").andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // When
        // B玩家出殺
        mockMvcUtil.playCard(gameId, "player-b", "player-c", "BS8008", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        // Then
        // ABCD玩家收到B玩家出殺的event
        String playerBPlayKillJsonForA = websocketUtil.getValue("player-a");
        Path path = Paths.get("src/test/resources/TestJsonFile/ScrollTest/BorrowedSword/player_b_playKill_for_player_a.json");
        String expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerBPlayKillJsonForA);

        String playerBPlayKillJsonForB = websocketUtil.getValue("player-b");
        path = Paths.get("src/test/resources/TestJsonFile/ScrollTest/BorrowedSword/player_b_playKill_for_player_b.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerBPlayKillJsonForB);

        String playerBPlayKillJsonForC = websocketUtil.getValue("player-c");
        path = Paths.get("src/test/resources/TestJsonFile/ScrollTest/BorrowedSword/player_b_playKill_for_player_c.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerBPlayKillJsonForC);

        String playerBPlayKillJsonForD = websocketUtil.getValue("player-d");
        path = Paths.get("src/test/resources/TestJsonFile/ScrollTest/BorrowedSword/player_b_playKill_for_player_d.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerBPlayKillJsonForD);
    }

    @Test
    public void givenPlayerABCD_PlayerATurn_PlayerAHasBorrowedSword_WhenPlayerAPlaysBorrowedSwordAndAssignsBToKillC_AndPlayerBSkips_ThenPlayersABCDReceivePlayerBWeaponCardGivenToPlayerAEvent() throws Exception {
        // Given
        // 玩家ABCD
        // A的回合
        // A有借刀殺人
        // B有裝備武器，有一張殺，B攻擊範圍內
        // 有C可以殺
        // A出借刀殺人, 指定B殺C
        givenPlayerAHaveBorrowedSwordAndPlayerBEquipedQilinBow(3);
        mockMvcUtil.playCard(gameId, "player-a", "player-b", borrowedSwordCardId, PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();
        mockMvcUtil.useBorrowedSwordEffect(gameId, "player-a", "player-b", "player-c").andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // When
        // B玩家出SKIP
        mockMvcUtil.playCard(gameId, "player-b", "player-a", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();

        // Then
        // ABCD玩家收到B玩家的武器卡給A的event
        String playerBPlayKillJsonForA = websocketUtil.getValue("player-a");
        Path path = Paths.get("src/test/resources/TestJsonFile/ScrollTest/BorrowedSword/player_a_get_weapon_for_player_a.json");
        String expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerBPlayKillJsonForA);

        String playerBPlayKillJsonForB = websocketUtil.getValue("player-b");
        path = Paths.get("src/test/resources/TestJsonFile/ScrollTest/BorrowedSword/player_a_get_weapon_for_player_b.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerBPlayKillJsonForB);

        String playerBPlayKillJsonForC = websocketUtil.getValue("player-c");
        path = Paths.get("src/test/resources/TestJsonFile/ScrollTest/BorrowedSword/player_a_get_weapon_for_player_c.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerBPlayKillJsonForC);

        String playerBPlayKillJsonForD = websocketUtil.getValue("player-d");
        path = Paths.get("src/test/resources/TestJsonFile/ScrollTest/BorrowedSword/player_a_get_weapon_for_player_d.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerBPlayKillJsonForD);
    }

    @Test
    public void givenPlayerABCD_PlayerATurn_PlayerAHasBorrowedSword_WhenPlayerAPlaysBorrowedSwordAndAssignsBToKillC_AndPlayerHaveNoKillCard_ThenPlayersABCDReceivePlayerBWeaponCardGivenToPlayerAEvent() throws Exception {
        // Given
        // 玩家ABCD
        // A的回合
        // A有借刀殺人
        // B有裝備武器，沒有殺，B攻擊範圍內
        givenPlayerAHaveBorrowedSwordAndPlayerBEquipedQilinBowButNoKill(3);

        Game game = repository.findById(gameId).get();

        // When
        // A出借刀殺人, 指定B殺C
        mockMvcUtil.playCard(gameId, "player-a", "player-b", borrowedSwordCardId, PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();
        mockMvcUtil.useBorrowedSwordEffect(gameId, "player-a", "player-b", "player-c").andExpect(status().isOk()).andReturn();


        // Then
        // ABCD玩家收到B玩家的武器卡給A的event
        String playerBPlayKillJsonForA = websocketUtil.getValue("player-a");
        Path path = Paths.get("src/test/resources/TestJsonFile/ScrollTest/BorrowedSword/player_a_get_weapon_if_player_b_no_kill_for_player_a.json");
        String expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerBPlayKillJsonForA);

        String playerBPlayKillJsonForB = websocketUtil.getValue("player-b");
        path = Paths.get("src/test/resources/TestJsonFile/ScrollTest/BorrowedSword/player_a_get_weapon_if_player_b_no_kill_for_player_b.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerBPlayKillJsonForB);

        String playerBPlayKillJsonForC = websocketUtil.getValue("player-c");
        path = Paths.get("src/test/resources/TestJsonFile/ScrollTest/BorrowedSword/player_a_get_weapon_if_player_b_no_kill_for_player_c.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerBPlayKillJsonForC);

        String playerBPlayKillJsonForD = websocketUtil.getValue("player-d");
        path = Paths.get("src/test/resources/TestJsonFile/ScrollTest/BorrowedSword/player_a_get_weapon_if_player_b_no_kill_for_player_d.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerBPlayKillJsonForD);
    }


    @Test
    public void givenPlayerABCD_PlayerAPlaysBorrowedSword_WhenNoOneCanDodge_ThenBAndDDieAndACWin() throws Exception {
        //        Given
        //        玩家A B C D
        //        A 為主公 B 為反賊 C 為忠臣 D 為內奸
        //        B hp = 1 C hp = 1 D hp = 1
        //        A 手牌有一張借刀殺人
        //        B 有裝備武器，有一張殺，B攻擊範圍內有 C 可以殺
        //        B D 手牌沒有閃
        //        A B C D 都沒有桃
        Player playerA = createPlayer(
                "player-a",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Kill(BS8008), new Kill(BS8008), new Dodge(BH2028), new Dodge(BHK039), new BorrowedSword(SCK065)
        );
        Player playerB = createPlayer("player-b",
                1,
                General.劉備,
                HealthStatus.ALIVE,
                Role.REBEL,
                new Kill(BS8008), new Kill(BS8008), new Kill(BS8008), new Kill(BS8008)
        );
        Player playerC = createPlayer(
                "player-c",
                1,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MINISTER,
                new Kill(BS8008), new Kill(BS8008), new Kill(BS8008), new Kill(BS8008)
        );
        Player playerD = createPlayer(
                "player-d",
                1,
                General.劉備,
                HealthStatus.ALIVE,
                Role.TRAITOR,
                new Kill(BS8008), new Kill(BS8008), new Kill(BS8008), new Kill(BS8008)
        );
        Equipment equipmentB = new Equipment();
        equipmentB.setWeapon(new RepeatingCrossbowCard(ECA066));
        playerB.setEquipment(equipmentB);
        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        repository.save(game);

        //        When
        //        A 出借刀殺人，指定 B 殺 C
        mockMvcUtil.playCard(gameId, "player-a", "player-b", borrowedSwordCardId, PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();
        mockMvcUtil.useBorrowedSwordEffect(gameId, "player-a", "player-b", "player-c").andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();
        mockMvcUtil.playCard(gameId, "player-b", "player-c", "BS8008", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();
        mockMvcUtil.playCard(gameId, "player-c", "player-c", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();
        //        C skip 瀕臨死亡
        //        C D A B 被詢問要不要出桃 skip
        mockMvcUtil.playCard(gameId, "player-c", "player-c", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();
        mockMvcUtil.playCard(gameId, "player-d", "player-c", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();
        mockMvcUtil.playCard(gameId, "player-a", "player-c", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();
        mockMvcUtil.playCard(gameId, "player-b", "player-c", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();
        //        Then
        //        C 死亡，A 裝備、手牌數量都還在
        //        active player 仍然是 A
        //        檯面上遊戲玩家為 ABD
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/BorrowedSword/player_c_dead_for_%s.json";
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


    private void givenPlayerAHaveBorrowedSwordAndPlayerBEquipedQilinBowButNoKill(int playerCHp) {
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
                General.劉備,
                HealthStatus.ALIVE,
                Role.MINISTER,
                new Peach(BH3029), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028)
        );
        Player playerC = createPlayer(
                "player-c",
                playerCHp,
                General.劉備,
                HealthStatus.ALIVE,
                Role.REBEL,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)
        );
        Player playerD = createPlayer(
                "player-d",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.TRAITOR,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)
        );

        Equipment equipmentB = new Equipment();
        equipmentB.setWeapon(new RepeatingCrossbowCard(ECA066));
        playerB.setEquipment(equipmentB);

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        repository.save(game);
    }

    private void givenPlayerAHaveBorrowedSwordAndPlayerCAEquipedShadowHorse(int playerCHp) {
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
                General.劉備,
                HealthStatus.ALIVE,
                Role.MINISTER,
                new Kill(BS8008), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028)
        );
        Player playerC = createPlayer(
                "player-c",
                playerCHp,
                General.劉備,
                HealthStatus.ALIVE,
                Role.REBEL,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)
        );
        Player playerD = createPlayer(
                "player-d",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.TRAITOR,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)
        );

        Equipment equipmentA = new Equipment();
        Equipment equipmentC = new Equipment();
        equipmentA.setPlusOne(new ShadowHorse(ES5018));
        equipmentC.setPlusOne(new ShadowHorse(ES5018));
        playerA.setEquipment(equipmentA);
        playerC.setEquipment(equipmentC);
        Equipment equipmentB = new Equipment();
        equipmentB.setWeapon(new RepeatingCrossbowCard(ECA066));
        playerB.setEquipment(equipmentB);

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        repository.save(game);
    }

    private void givenPlayerAHaveBorrowedSwordAndPlayerBEquipedQilinBow(int playerCHp) {
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
                General.劉備,
                HealthStatus.ALIVE,
                Role.MINISTER,
                new Kill(BS8008), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028)
        );
        Player playerC = createPlayer(
                "player-c",
                playerCHp,
                General.劉備,
                HealthStatus.ALIVE,
                Role.REBEL,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)
        );
        Player playerD = createPlayer(
                "player-d",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.TRAITOR,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)
        );

        Equipment equipmentB = new Equipment();
        equipmentB.setWeapon(new RepeatingCrossbowCard(ECA066));
        playerB.setEquipment(equipmentB);

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        repository.save(game);
    }

    private void givenPlayerAHaveBorrowedSwordAndPlayerBWithoutEquipedWeapon(int playerCHp) {
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
                General.劉備,
                HealthStatus.ALIVE,
                Role.MINISTER,
                new Kill(BS8008), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028)
        );
        Player playerC = createPlayer(
                "player-c",
                playerCHp,
                General.劉備,
                HealthStatus.ALIVE,
                Role.REBEL,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)
        );
        Player playerD = createPlayer(
                "player-d",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.TRAITOR,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)
        );

        Equipment equipmentB = new Equipment();
        playerB.setEquipment(equipmentB);

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        repository.save(game);
    }

    private void givenPlayerAHaveBorrowedSwordAndPlayerBEquipedQilinBowAndRedRabbitHorse(int playerCHp) {
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
                General.劉備,
                HealthStatus.ALIVE,
                Role.MINISTER,
                new Kill(BS8008), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028)
        );
        Player playerC = createPlayer(
                "player-c",
                playerCHp,
                General.劉備,
                HealthStatus.ALIVE,
                Role.REBEL,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)
        );
        Player playerD = createPlayer(
                "player-d",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.TRAITOR,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)
        );

        Equipment equipmentB = new Equipment();
        equipmentB.setMinusOne(new RedRabbitHorse(EH5044));
        equipmentB.setWeapon(new RepeatingCrossbowCard(ECA066));
        playerB.setEquipment(equipmentB);

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        repository.save(game);
    }

    private void popAllPlayerMessage() {
        websocketUtil.getValue("player-a");
        websocketUtil.getValue("player-b");
        websocketUtil.getValue("player-c");
        websocketUtil.getValue("player-d");
    }
}
