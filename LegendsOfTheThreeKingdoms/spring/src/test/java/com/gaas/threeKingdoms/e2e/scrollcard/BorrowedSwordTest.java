package com.gaas.threeKingdoms.e2e.scrollcard;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.e2e.JsonFileValidateHelper;
import com.gaas.threeKingdoms.e2e.MockMvcUtil;
import com.gaas.threeKingdoms.e2e.WebsocketUtil;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.RedRabbitHorse;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.ShadowHorse;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.QilinBowCard;
import com.gaas.threeKingdoms.handcard.scrollcard.BarbarianInvasion;
import com.gaas.threeKingdoms.handcard.scrollcard.BorrowedSword;
import com.gaas.threeKingdoms.outport.GameRepository;
import com.gaas.threeKingdoms.player.Equipment;
import com.gaas.threeKingdoms.player.HealthStatus;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.rolecard.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.e2e.MockUtil.createPlayer;
import static com.gaas.threeKingdoms.e2e.MockUtil.initGame;
import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static com.gaas.threeKingdoms.handcard.PlayCard.BHK039;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@AutoConfigureMockMvc
public class BorrowedSwordTest {

    @MockBean
    private GameRepository repository;

    @Autowired
    private MockMvc mockMvc;

    private MockMvcUtil mockMvcUtil;

    private JsonFileValidateHelper helper;

    private WebsocketUtil websocketUtil;

    @Value(value = "${local.server.port}")
    private Integer port;
    private final String gameId = "my-id";

    private String borrowedSwordCardId = "SCQ064";

    @BeforeEach
    public void setup() throws Exception {
        mockMvcUtil = new MockMvcUtil(mockMvc);
        websocketUtil = new WebsocketUtil(port, gameId);
        helper = new JsonFileValidateHelper(websocketUtil);
        Thread.sleep(1000);
    }
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
        //A 出借刀殺人，指定 B 殺 C
        mockMvcUtil.playCard(gameId, "player-a", "player-c", borrowedSwordCardId, PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        //ABCD 玩家收到借刀殺人的 event
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

        mockMvcUtil.useBorrowedSword(gameId, "player-a", "player-b", "player-c");
        //Then
        //ABCD 玩家收到借刀殺人的 event
        //B 玩家收到要求出殺的 event
        playerAPlayBorrowedSwordJsonForA = websocketUtil.getValue("player-a");
        path = Paths.get("src/test/resources/TestJsonFile/ScrollTest/BorrowedSword/player_a_playBorrowedSword_for_player_a.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayBorrowedSwordJsonForA);

        playerAPlayBorrowedSwordJsonForB = websocketUtil.getValue("player-b");
        path = Paths.get("src/test/resources/TestJsonFile/ScrollTest/BorrowedSword/player_a_playBorrowedSword_for_player_b.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayBorrowedSwordJsonForB);

        playerAPlayBorrowedSwordJsonForC = websocketUtil.getValue("player-c");
        path = Paths.get("src/test/resources/TestJsonFile/ScrollTest/BorrowedSword/player_a_playBorrowedSword_for_player_c.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayBorrowedSwordJsonForC);

        playerAPlayBorrowedSwordJsonForD = websocketUtil.getValue("player-d");
        path = Paths.get("src/test/resources/TestJsonFile/ScrollTest/BorrowedSword/player_a_playBorrowedSword_for_player_d.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayBorrowedSwordJsonForD);
    }

    @Test
    public void givenPlayerABCD_PlayerATurn_PlayerAHasBorrowedSword_WhenPlayerAPlaysBorrowedSwordAndAssignsBWithNoValidTarget_ThenThrowError() throws Exception {
        //Given
        //玩家ABCD
        //A的回合
        //A有借刀殺人
        //B 有裝備武器，有一張殺，B攻擊範圍內
        //沒有人可以殺
        givenPlayerAHaveBorrowedSwordAndPlayerCAEquipedShadowHorse(3);
        //When
        //A 出借刀殺人，指定 B

        //Then
        //拋出錯誤
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

        //Then
        //拋出錯誤


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
        //A 出借刀殺人，指定B殺D

        //Then
        //404 錯誤訊息顯示D不在攻擊範圍
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

        // When
        // B玩家出殺

        // Then
        // ABCD玩家收到B玩家出殺的event
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

        // When
        // B玩家出SKIP

        // Then
        // ABCD玩家收到B玩家的武器卡給A的event
    }

    private void givenPlayerAHaveBorrowedSwordAndPlayerCAEquipedShadowHorse(int playerCHp) {
        Player playerA = createPlayer(
                "player-a",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039), new BorrowedSword(SCQ064)
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

        Equipment equipmentA= new Equipment();
        Equipment equipmentC= new Equipment();
        equipmentA.setPlusOne(new ShadowHorse(ES5018));
        equipmentC.setPlusOne(new ShadowHorse(ES5018));
        playerA.setEquipment(equipmentA);
        playerC.setEquipment(equipmentC);

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        Mockito.when(repository.findById(gameId)).thenReturn(game);
    }

    private void givenPlayerAHaveBorrowedSwordAndPlayerBEquipedQilinBow(int playerCHp) {
        Player playerA = createPlayer(
                "player-a",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039), new BorrowedSword(SCQ064)
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

        Equipment equipmentB= new Equipment();
        equipmentB.setWeapon(new QilinBowCard(EH5031));
        playerB.setEquipment(equipmentB);

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        Mockito.when(repository.findById(gameId)).thenReturn(game);
    }

    private void givenPlayerAHaveBorrowedSwordAndPlayerBWithoutEquipedWeapon(int playerCHp) {
        Player playerA = createPlayer(
                "player-a",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039), new BorrowedSword(SCQ064)
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

        Equipment equipmentB= new Equipment();
        playerB.setEquipment(equipmentB);

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        Mockito.when(repository.findById(gameId)).thenReturn(game);
    }

    private void givenPlayerAHaveBorrowedSwordAndPlayerBEquipedQilinBowAndRedRabbitHorse(int playerCHp) {
        Player playerA = createPlayer(
                "player-a",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039), new BorrowedSword(SCQ064)
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

        Equipment equipmentB= new Equipment();
        equipmentB.setMinusOne(new RedRabbitHorse(EH5044));
        equipmentB.setWeapon(new QilinBowCard(EH5031));
        playerB.setEquipment(equipmentB);

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        Mockito.when(repository.findById(gameId)).thenReturn(game);
    }
}