package com.gaas.threeKingdoms.e2e.equipment;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.e2e.JsonFileValidateHelper;
import com.gaas.threeKingdoms.e2e.MockMvcUtil;
import com.gaas.threeKingdoms.e2e.WebsocketUtil;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.EquipmentPlayType;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.RedRabbitHorse;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.ShadowHorse;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.QilinBowCard;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@AutoConfigureMockMvc
public class QilinBowTest {

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

    @BeforeEach
    public void setup() throws Exception {
        mockMvcUtil = new MockMvcUtil(mockMvc);
        websocketUtil = new WebsocketUtil(port, gameId);
        helper = new JsonFileValidateHelper(websocketUtil);
        Thread.sleep(1000);
    }

    @Test
    public void testPlayerAPlayQilinBow() throws Exception {
        // A 玩家出麒麟弓
        // B 有赤兔馬
        playerAPlayQilinBow();

        // A 玩家出殺
        // B 玩家出skip
        playerAAskUseEquipmentEffect();

        // A決定要發動效果
        // B玩家只有一隻馬，棄置馬，並扣血
        playerAUseEquipmentEffect();

    }

    private void playerAUseEquipmentEffect() throws Exception {
        // When A 玩家發動效果
        String currentPlayer = "player-a";
        String targetPlayerId = "player-b";
        String playedCardId = "EH5031";

        mockMvcUtil.useEquipment(gameId, currentPlayer, targetPlayerId, playedCardId, EquipmentPlayType.ACTIVE)
                .andExpect(status().isOk()).andReturn();


        //Then 收到B玩家棄置馬的 event與 B玩家扣血的event
        String playerAPlayPeachJsonForA = websocketUtil.getValue("player-a");
        Path path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayQilinBow/player_a_receive_askqilinbow_event_for_player_a.json");
        String expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForA);

        String playerAPlayPeachJsonForB = websocketUtil.getValue("player-b");
        path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayQilinBow/player_a_receive_askqilinbow_event_for_player_b.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForB);

        String playerAPlayPeachJsonForC = websocketUtil.getValue("player-c");
        path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayQilinBow/player_a_receive_askqilinbow_event_for_player_c.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForC);

        String playerAPlayPeachJsonForD = websocketUtil.getValue("player-d");
        path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayQilinBow/player_a_receive_askqilinbow_event_for_player_d.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForD);

    }

    private void playerAAskUseEquipmentEffect() throws Exception {
        // When A 玩家出殺
        String currentPlayer = "player-a";
        String targetPlayerId = "player-b";
        String playedCardId = "BS8008";

        mockMvcUtil.playCard(gameId, currentPlayer, targetPlayerId, playedCardId, "active")
                .andExpect(status().isOk()).andReturn();

        String playerAPlayKillJsonForA = websocketUtil.getValue("player-a");
        String playerAPlayKillJsonForB = websocketUtil.getValue("player-b");
        String playerAPlayKillJsonForC = websocketUtil.getValue("player-c");
        String playerAPlayKillJsonForD = websocketUtil.getValue("player-d");

        mockMvcUtil.playCard(gameId, targetPlayerId, currentPlayer, "", "skip")
                .andExpect(status().isOk()).andReturn();


        //Then A玩家收到是否發動效果的event
        String playerAPlayPeachJsonForA = websocketUtil.getValue("player-a");
        Path path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayQilinBow/player_a_receive_askqilinbow_event_for_player_a.json");
        String expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForA);

        String playerAPlayPeachJsonForB = websocketUtil.getValue("player-b");
        path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayQilinBow/player_a_receive_askqilinbow_event_for_player_b.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForB);

        String playerAPlayPeachJsonForC = websocketUtil.getValue("player-c");
        path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayQilinBow/player_a_receive_askqilinbow_event_for_player_c.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForC);

        String playerAPlayPeachJsonForD = websocketUtil.getValue("player-d");
        path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayQilinBow/player_a_receive_askqilinbow_event_for_player_d.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForD);


    }

    @Test
    public void testPlayerAPlayQilinBowPlayerBWithoutHorse() throws Exception {
        givenPlayerAHaveQilinBow();
        // A 玩家出麒麟弓
        playerAPlayQilinBow();

        // A 玩家出殺 B 玩家沒有馬 不會收到發動裝備卡效果的 Event
//        whenAKillBThenAShouldNotHaveEquipmentEvent();

    }

    @Test
    public void testPlayerAPlayQilinBowPlayerBWithTwoHorse() throws Exception {
        givenPlayerAHaveQilinBowPlayerBHaveTwoHorse();
        // A 玩家出麒麟弓
        playerAPlayQilinBow();

        // A 玩家出殺 B 玩家沒有馬 不會收到發動裝備卡效果的 Event
        whenAKillBThenAShouldNotHaveEquipmentEvent();

    }

    private void whenAKillBThenAShouldNotHaveEquipmentEvent() throws Exception {
        // When B 玩家攻擊 A 玩家
        String currentPlayer = "player-a";
        String targetPlayerId = "player-b";
        String playedCardId = "BS8008";

        mockMvcUtil.playCard(gameId, currentPlayer, targetPlayerId, playedCardId, PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        //Then A 不會收到要不要發動裝備卡的event
        String playerAPlayPeachJsonForA = websocketUtil.getValue("player-a");
        Path path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayQilinBow/player_a_playqilinbow_player_b_withoutHorse_for_player_a.json");
        String expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForA);

        String playerAPlayPeachJsonForB = websocketUtil.getValue("player-b");
        path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayQilinBow/player_a_playqilinbow_player_b_withoutHorse_for_player_b.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForB);

        String playerAPlayPeachJsonForC = websocketUtil.getValue("player-c");
        path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayQilinBow/player_a_playqilinbow_player_b_withoutHorse_for_player_c.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForC);

        String playerAPlayPeachJsonForD = websocketUtil.getValue("player-d");
        path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayQilinBow/player_a_playqilinbow_player_b_withoutHorse_for_player_d.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForD);
    }

    private void playerAPlayQilinBow() throws Exception {
        //Given A玩家有麒麟弓
        givenPlayerAHaveQilinBowPlayerBHaveRedRabbitHorse();

        // When A 玩家出麒麟弓
        String currentPlayer = "player-a";
        String targetPlayerId = "player-a";
        String playedCardId = "EH5031";

        mockMvcUtil.playCard(gameId, currentPlayer, targetPlayerId, playedCardId, "active")
                .andExpect(status().isOk()).andReturn();

        //Then A玩家有麒麟弓
        String playerAPlayPeachJsonForA = websocketUtil.getValue("player-a");
        Path path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayQilinBow/player_a_playqilinbow_for_player_a.json");
        String expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForA);

        String playerAPlayPeachJsonForB = websocketUtil.getValue("player-b");
        path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayQilinBow/player_a_playqilinbow_for_player_b.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForB);

        String playerAPlayPeachJsonForC = websocketUtil.getValue("player-c");
        path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayQilinBow/player_a_playqilinbow_for_player_c.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForC);

        String playerAPlayPeachJsonForD = websocketUtil.getValue("player-d");
        path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayQilinBow/player_a_playqilinbow_for_player_d.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForD);
    }

    private void givenPlayerAHaveQilinBowPlayerBHaveTwoHorse() {
        Player playerA = createPlayer(
                "player-a",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039), new QilinBowCard(EH5031)
        );
        Player playerB = createPlayer("player-b",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MINISTER,
                new Kill(BS8008), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028)
        );
        Equipment equipmentB= new Equipment();
        equipmentB.setMinusOne(new RedRabbitHorse(EH5044));
        equipmentB.setPlusOne(new ShadowHorse(ES5018));
        playerB.setEquipment(equipmentB);
        Player playerC = createPlayer(
                "player-c",
                4,
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
        Player playerE = createPlayer(
                "player-d",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.TRAITOR,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)
        );
        Player playerF = createPlayer(
                "player-d",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.TRAITOR,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)
        );
        Player playerG = createPlayer(
                "player-d",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.TRAITOR,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)
        );
        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD, playerE, playerF, playerG);
        Game game = initGame(gameId, players, playerA);
        Mockito.when(repository.findById(gameId)).thenReturn(game);
    }

    private void givenPlayerAHaveQilinBowPlayerBHaveRedRabbitHorse() {
        Player playerA = createPlayer(
                "player-a",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039), new QilinBowCard(EH5031)
        );
        Player playerB = createPlayer("player-b",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MINISTER,
                new Kill(BS8008), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028)
        );
        Equipment equipmentB= new Equipment();
        equipmentB.setMinusOne(new RedRabbitHorse(EH5044));
        playerB.setEquipment(equipmentB);
        Player playerC = createPlayer(
                "player-c",
                4,
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
        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        Mockito.when(repository.findById(gameId)).thenReturn(game);
    }


    private void givenPlayerAHaveQilinBow() {
        Player playerA = createPlayer(
                "player-a",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039), new QilinBowCard(EH5031)
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
                4,
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
        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        Mockito.when(repository.findById(gameId)).thenReturn(game);
    }

}
