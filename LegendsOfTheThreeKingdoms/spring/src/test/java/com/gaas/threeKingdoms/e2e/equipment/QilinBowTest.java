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
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        // Given A玩家有麒麟弓
        givenPlayerAHaveQilinBowPlayerBHaveRedRabbitHorse();

        // A 玩家出麒麟弓
        // B 有赤兔馬
        playerAPlayQilinBow();

        // A 玩家出殺
        // B 玩家出skip
        playerAAskUseEquipmentEffect();

        // A決定要發動效果
        // B玩家只有一隻馬，棄置馬，並扣血
        playerAUseEquipmentEffect();

        // A 玩家裝備赤兔馬
        playAUseRedRabbitHorse();

    }

    @Test
    public void testPlayerAPlayQilinBowAndPlayerUseIt() throws Exception {
        // Given A玩家有麒麟弓
        givenPlayerAHaveQilinBowPlayerBHaveRedRabbitHorse();

        // A 玩家出麒麟弓
        // B 有赤兔馬
        playerAPlayQilinBow();

        // A 玩家出殺
        // B 玩家出skip
        playerAAskUseEquipmentEffect();

        // B決定要發動效果
        String currentPlayer = "player-b";
        String targetPlayerId = "player-b";
        String playedCardId = "BS8008";

        mockMvcUtil.playCard(gameId, currentPlayer, targetPlayerId, playedCardId, "active")
                .andExpect(status().is4xxClientError()).andReturn();

    }

    private void playAUseRedRabbitHorse() throws Exception {
        // When A 玩家裝備赤兔馬
        String currentPlayer = "player-a";
        String targetPlayerId = "player-a";
        String playedCardId = "EH5044";

        mockMvcUtil.playCard(gameId, currentPlayer, targetPlayerId, playedCardId, "active")
                .andExpect(status().isOk()).andReturn();



    }

    @Test
    public void testPlayerAPlayQilinBowPlayerBWithoutHorse() throws Exception {
        givenPlayerAHaveQilinBowPlayerBHaveRedRabbitHorse();
        // A 玩家出麒麟弓
        playerAPlayQilinBow();

        // A 玩家出殺 B 玩家沒有馬 不會收到發動裝備卡效果的 Event
        whenAKillBThenAShouldNotHaveEquipmentEvent();

    }

    @Test
    public void testPlayerAPlayQilinBowPlayerBWithTwoHorse() throws Exception {
        //玩家 A 有麒麟弓 B 有裝備兩隻馬，B 有 4 HP
        givenPlayerAHaveQilinBowPlayerBHaveTwoHorse(4);

        // A 玩家出麒麟弓
        playerAPlayQilinBowWhenBHaveTwoHorse();

        // A 玩家出殺
        // B 玩家skip
        whenAKillAndBSkip();

        // A發動效果 B有兩隻馬
        whenAUseEquipmentEffectAndBHaveTwoHorse();

        // A玩家選擇一張馬
        whenAChooseHorse();
    }

    @Test
    public void testPlayerAPlayQilinBowPlayerBWithTwoHorseAndBDie() throws Exception {
        //玩家 A 有麒麟弓 B 有裝備兩隻馬，B只有 1 HP
        givenPlayerAHaveQilinBowPlayerBHaveTwoHorse(1);

        // A 玩家出麒麟弓
        playerAPlayQilinBowWhenBHaveTwoHorse();

        // A 玩家出殺
        // B 玩家skip
        whenAKillAndBSkip();

        // A發動效果 B有兩隻馬
        whenAUseEquipmentEffectAndBHaveTwoHorseNoCheck();

        // A玩家選擇一張馬，B玩家瀕死
        whenAChooseHorseAndBDie();
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
        Path path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayQilinBow/player_a_receive_userquipment_event_for_player_a.json");
        String expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForA);

        String playerAPlayPeachJsonForB = websocketUtil.getValue("player-b");
        path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayQilinBow/player_a_receive_userquipment_event_for_player_b.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForB);

        String playerAPlayPeachJsonForC = websocketUtil.getValue("player-c");
        path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayQilinBow/player_a_receive_userquipment_event_for_player_c.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForC);

        String playerAPlayPeachJsonForD = websocketUtil.getValue("player-d");
        path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayQilinBow/player_a_receive_userquipment_event_for_player_d.json");
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

    private void whenAChooseHorseAndBDie() throws Exception {
        // When A 選擇一張馬
        String currentPlayerId = "player-a";
        String cardId = "EH5044";
        mockMvcUtil.chooseHorse(gameId, currentPlayerId, cardId)
                .andExpect(status().isOk()).andReturn();


        //Then A 收到選擇馬的event  B玩家瀕死
        String playerAPlayPeachJsonForA = websocketUtil.getValue("player-a");
        Path path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayQilinBow/player_a_receive_removehorse_b_die_event_for_player_a.json");
        String expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForA);
    }

    private void whenAChooseHorse() throws Exception {

        // When A 選擇一張馬
        String currentPlayerId = "player-a";
        String cardId = "EH5044";
        mockMvcUtil.chooseHorse(gameId, currentPlayerId, cardId)
                .andExpect(status().isOk()).andReturn();


        //Then A 收到選擇馬的event
        String playerAPlayPeachJsonForA = websocketUtil.getValue("player-a");
        Path path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayQilinBow/player_a_receive_removehorse_event_for_player_a.json");
        String expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForA);

        String playerAPlayPeachJsonForB = websocketUtil.getValue("player-b");
        path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayQilinBow/player_a_receive_removehorse_event_for_player_b.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForB);

        String playerAPlayPeachJsonForE = websocketUtil.getValue("player-e");
        path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayQilinBow/player_a_receive_removehorse_event_for_player_e.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForE);
    }

    private void playerAPlayQilinBowWhenBHaveTwoHorse() throws Exception {
        // When A 玩家出麒麟弓
        String currentPlayer = "player-a";
        String targetPlayerId = "player-a";
        String playedCardId = "EH5031";

        mockMvcUtil.playCard(gameId, currentPlayer, targetPlayerId, playedCardId, "active")
                .andExpect(status().isOk()).andReturn();

        //Then A玩家有麒麟弓
        String playerAPlayQilinBowJsonForA = websocketUtil.getValue("player-a");
        String playerAPlayQilinBowJsonForB = websocketUtil.getValue("player-b");
        String playerAPlayQilinBowJsonForC = websocketUtil.getValue("player-c");
        String playerAPlayQilinBowJsonForD = websocketUtil.getValue("player-d");
        String playerAPlayQilinBowJsonForE = websocketUtil.getValue("player-e");
        String playerAPlayQilinBowJsonForF = websocketUtil.getValue("player-f");
        String playerAPlayQilinBowJsonForG = websocketUtil.getValue("player-g");

    }

    private void whenAUseEquipmentEffectAndBHaveTwoHorseNoCheck() throws Exception {
        // When A 玩家發動效果 B有兩隻馬
        String currentPlayer = "player-a";
        String targetPlayerId = "player-b";
        String playedCardId = "EH5031";

        mockMvcUtil.useEquipment(gameId, currentPlayer, targetPlayerId, playedCardId, EquipmentPlayType.ACTIVE)
                .andExpect(status().isOk()).andReturn();

        String playerAPlayKillJsonForA = websocketUtil.getValue("player-a");
        String playerAPlayKillJsonForB = websocketUtil.getValue("player-b");
        String playerAPlayKillJsonForC = websocketUtil.getValue("player-c");
        String playerAPlayKillJsonForD = websocketUtil.getValue("player-d");
        String playerAPlayKillJsonForE = websocketUtil.getValue("player-e");
        String playerAPlayKillJsonForF = websocketUtil.getValue("player-f");
        String playerAPlayKillJsonForG = websocketUtil.getValue("player-g");
    }

    private void whenAUseEquipmentEffectAndBHaveTwoHorse() throws Exception {
        // When A 玩家發動效果 B有兩隻馬
        String currentPlayer = "player-a";
        String targetPlayerId = "player-b";
        String playedCardId = "EH5031";

        mockMvcUtil.useEquipment(gameId, currentPlayer, targetPlayerId, playedCardId, EquipmentPlayType.ACTIVE)
                .andExpect(status().isOk()).andReturn();

        //Then A 收到選擇馬的event
        String playerAPlayPeachJsonForA = websocketUtil.getValue("player-a");
        Path path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayQilinBow/player_a_receive_askchoosemountcard_event_for_player_a.json");
        String expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForA);

        String playerAPlayPeachJsonForB = websocketUtil.getValue("player-b");
        path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayQilinBow/player_a_receive_askchoosemountcard_event_for_player_b.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForB);

        String playerAPlayPeachJsonForE = websocketUtil.getValue("player-e");
        path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayQilinBow/player_a_receive_askchoosemountcard_event_for_player_e.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForE);
    }

    private void whenAKillAndBSkip() throws Exception {
        // When A攻擊B玩家
        String currentPlayer = "player-a";
        String targetPlayerId = "player-b";
        String playedCardId = "BS8008";

        mockMvcUtil.playCard(gameId, currentPlayer, targetPlayerId, playedCardId, PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        String playerAPlayKillJsonForA = websocketUtil.getValue("player-a");
        String playerAPlayKillJsonForB = websocketUtil.getValue("player-b");
        String playerAPlayKillJsonForC = websocketUtil.getValue("player-c");
        String playerAPlayKillJsonForD = websocketUtil.getValue("player-d");
        String playerAPlayKillJsonForE = websocketUtil.getValue("player-e");
        String playerAPlayKillJsonForF = websocketUtil.getValue("player-f");
        String playerAPlayKillJsonForG = websocketUtil.getValue("player-g");

        // When B Skip
        mockMvcUtil.playCard(gameId, "player-b", "player-a", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();

        playerAPlayKillJsonForA = websocketUtil.getValue("player-a");
        playerAPlayKillJsonForB = websocketUtil.getValue("player-b");
        playerAPlayKillJsonForC = websocketUtil.getValue("player-c");
        playerAPlayKillJsonForD = websocketUtil.getValue("player-d");
        playerAPlayKillJsonForE = websocketUtil.getValue("player-e");
        playerAPlayKillJsonForF = websocketUtil.getValue("player-f");
        playerAPlayKillJsonForG = websocketUtil.getValue("player-g");
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

    private void givenPlayerAHaveQilinBowPlayerBHaveTwoHorse(int playerBHP) {
        Player playerA = createPlayer(
                "player-a",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039), new QilinBowCard(EH5031)
        );
        Player playerB = createPlayer("player-b",
                playerBHP,
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
                "player-e",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.TRAITOR,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)
        );
        Player playerF = createPlayer(
                "player-f",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.TRAITOR,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)
        );
        Player playerG = createPlayer(
                "player-g",
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
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039), new QilinBowCard(EH5031), new RedRabbitHorse(EH5044)
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
