package com.gaas.threeKingdoms.e2e.scrollcard;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.e2e.JsonFileWriterUtil;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.RedRabbitHorse;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.ShadowHorse;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.QilinBowCard;
import com.gaas.threeKingdoms.handcard.scrollcard.Contentment;
import com.gaas.threeKingdoms.handcard.scrollcard.Duel;
import com.gaas.threeKingdoms.handcard.scrollcard.Snatch;
import com.gaas.threeKingdoms.player.HealthStatus;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.rolecard.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import static com.gaas.threeKingdoms.e2e.MockUtil.createPlayer;
import static com.gaas.threeKingdoms.e2e.MockUtil.initGame;
import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SnatchTest extends AbstractBaseIntegrationTest {

    @DisplayName("""
        Given
        玩家ABCD
        B沒有裝備，沒有手牌
        A有順手牽羊

        When
        A 出順手牽羊，指定 B

        Then
        拋出錯誤
    """)
    @Test
    public void givenPlayerABCD_PlayerBHasNoEquipmentsOrCards_PlayerAHasSnatch_WhenPlayerAPlaysSnatchAndTargetsB_ThenThrowException() throws Exception {
        // Given
        givenPlayerBHasNoCardsOrEquipments();

        // When & Then - 根據 Domain 邏輯，應該拋出 IllegalArgumentException，轉換為 400 Bad Request
        mockMvcUtil.playCard(gameId, "player-a", "player-b", "SSJ024", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isBadRequest());
    }

    @DisplayName("""
        Given
        玩家ABCD
        B沒有裝備，沒有手牌，判定區有樂不思蜀
        A有順手牽羊
    
        When
        A 出順手牽羊，指定 B
    
        Then
        拋出錯誤
    """)
    @Test
    public void givenPlayerBHasOnlyJudgementCard_WhenPlayerAPlaySnatchOnB_ThenThrowException() throws Exception {
        // Given
        givenPlayerBHasOnlyDelayScrollCard();

        // When & Then
        mockMvcUtil.playCard(gameId, "player-a", "player-b", "SS3016", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isBadRequest());
    }

    @DisplayName("""
        Given
        玩家ABCD
        B有一張手牌
        A 有順手牽羊
        
        When
        A 出順手牽羊，指定 B
        
        Then
        回傳 PlayCardEvent
    """)
    @Test
    public void givenPlayerABCD_PlayerBHasOneCard_PlayerAHasSnatch_WhenPlayerAPlaysSnatchAndTargetsB_ThenReturnPlayCardEvent() throws Exception {
        // Given
        givenPlayerBHasOneCard();

        // When
        mockMvcUtil.playCard(gameId, "player-a", "player-b", "SS3016", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());

        // Then - verify WebSocket messages for each player
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/SnatchBehavior/player_a_play_snatch_for_%s.json";
        for (String testPlayerId : playerIds) {
            String testPlayerJson = websocketUtil.getValue(testPlayerId);
            testPlayerId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, testPlayerId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }
    }

    @DisplayName("""
        Given
        玩家ABCD
        B有一張裝備 麒麟弓
        A有順手牽羊
    
        When
        A 出順手牽羊，指定 B
    
        Then
        回傳 PlayCardEvent
    """)
    @Test
    public void givenPlayerBHasWeapon_PlayerAHasSnatch_WhenPlayerAPlaysSnatchOnB_ThenReturnPlayCardEvent() throws Exception {
        // Given
        givenPlayerBHasWeapon();

        // When
        mockMvcUtil.playCard(gameId, "player-a", "player-b", "SS3016", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());

        // Then - verify WebSocket messages for each player
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/SnatchBehavior/player_a_play_snatch_weapon_for_%s.json";
        for (String testPlayerId : playerIds) {
//            String testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            String testPlayerJson = websocketUtil.getValue(testPlayerId);
            testPlayerId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, testPlayerId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }
    }

    @DisplayName("""
        Given
        玩家ABCD
        A沒有 - 1馬、有麒麟弓
        A有順手牽羊
    
        When
        A 出順手牽羊，指定 C
    
        Then
        拋出錯誤
    """)
    @Test
    public void givenPlayerAHasQilinBowButNoMinusOneHorse_WhenPlayerAPlaySnatchOnFarPlayer_ThenThrowException() throws Exception {
        // Given
        givenPlayerAHasQilinBowNoMinus1Horse();

        // When & Then
        mockMvcUtil.playCard(gameId, "player-a", "player-c", "SS3016", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isBadRequest());
    }

    @DisplayName("""
        Given
        玩家ABCD
        A沒有馬、 B有 + 1 馬，
        A有順手牽羊
    
        When
        A 出順手牽羊，指定 B
    
        Then
        拋出錯誤
    """)
    @Test
    public void givenPlayerAHasNoMount_PlayerBHasPlusOneMount_WhenPlayerAPlaySnatchOnB_ThenThrowException() throws Exception {
        // Given
        givenPlayerBHasPlusOneMount();

        // When & Then
        mockMvcUtil.playCard(gameId, "player-a", "player-b", "SS3016", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isBadRequest());
    }

    @DisplayName("""
        Given
        玩家ABCD
        A有 - 1馬、沒有麒麟弓
        A有順手牽羊
    
        When
        A 出順手牽羊，指定 C
    
        Then
        回傳 PlayCardEvent
    """)
    @Test
    public void givenPlayerAHasMinusOneMountButNoQilinBow_WhenPlayerAPlaySnatchOnC_ThenReturnPlayCardEvent() throws Exception {
        // Given
        givenPlayerAHasMinusOneMountNoWeapon();

        // When
        mockMvcUtil.playCard(gameId, "player-a", "player-c", "SS3016", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());

        // Then - verify WebSocket messages for each player
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/SnatchBehavior/player_a_play_snatch_with_minus_one_for_%s.json";
        for (String testPlayerId : playerIds) {
//            String testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            String testPlayerJson = websocketUtil.getValue(testPlayerId);
            testPlayerId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, testPlayerId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }
    }

    @DisplayName("""
        Given
        玩家ABCD
        B有一麒麟弓，五張手牌，第一張是 KILL
        第二張到五張是 Peach
        A有順手牽羊、沒有其他手牌
    
        When
        A 出順手牽羊，指定 B
        A 指定 index 0
    
        Then
        B 的手牌沒有 KILL ，並剩下四張
        A 手牌有 KILL
    """)
    @Test
    public void givenBHasWeaponAndFiveCards_WhenAPlaySnatchTargetingBAndSelectsIndex0_ThenAKeepsKillAndBHasFourPeachCards() throws Exception {
        // Given
        givenPlayerBHasWeaponAndFiveCards();

        // When
        mockMvcUtil.playCard(gameId, "player-a", "player-b", "SS3016", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        popAllPlayerMessage();

        mockMvcUtil.useSnatchEffect(gameId, "player-a", "player-b", "", 0);
        // Then - verify WebSocket messages for each player
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/SnatchBehavior/player_a_play_snatch_and_use_snatch_effect_for_%s.json";
        for (String testPlayerId : playerIds) {
//            String testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            String testPlayerJson = websocketUtil.getValue(testPlayerId);
            testPlayerId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, testPlayerId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }
    }

    @DisplayName("""
        Given
        玩家ABCD
        B有一麒麟弓，五張手牌，第一張是 KILL
        第二張到五張是 Peach
        A有順手牽羊
    
        When
        A 出順手牽羊，指定 B
        A 指定 index 5
    
        Then
        拋出錯誤
    """)
    @Test
    public void givenBHasFiveCards_WhenAPlaySnatchAndChoosesInvalidIndex5_ThenThrowException() throws Exception {
        // Given
        givenPlayerBHasWeaponAndFiveCards();

        // When
        mockMvcUtil.playCard(gameId, "player-a", "player-b", "SS3016", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        popAllPlayerMessage();

        // Then
        mockMvcUtil.useSnatchEffect(gameId, "player-a", "player-b", "", 5)
                .andExpect(status().isBadRequest());
    }

    @DisplayName("""
            Given
            玩家 ABCD
            A 的回合
            A 有樂不思蜀 x 1
            A 出樂不思蜀，指定 C
            A 結束回合
        
            When
            B 的回合，出順手牽羊，指定 C
        
            Then
            拋出錯誤(沒有手牌)
    """)
    @Test
    public void givenCIsTargetedByContentment_WhenBPlaysSnatchOnC_ThenThrowException() throws Exception {
        // Given
        givenPlayerAHasContentmentPlayerBHasSnatch();

        // A plays Contentment on C and finishes turn
        mockMvcUtil.playCard(gameId, "player-a", "player-c", "SC6071", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        popAllPlayerMessage();

        mockMvcUtil.finishAction(gameId, "player-a")
                .andExpect(status().isOk());
        popAllPlayerMessage();

        // When & Then - B tries to play Snatch on C (should fail)
        mockMvcUtil.playCard(gameId, "player-b", "player-c", "SS3016", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isBadRequest());
    }

    // Helper methods to set up game states
    private void givenPlayerBHasNoCardsOrEquipments() {
        Player playerA = createPlayer("player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH,
                new Snatch(SSJ024), new Dodge(BH2028), new Dodge(BHK039), new Duel(SSA001));
        Player playerB = createPlayer("player-b", 4, General.劉備, HealthStatus.ALIVE, Role.TRAITOR);
        Player playerC = createPlayer("player-c", 4, General.劉備, HealthStatus.ALIVE, Role.TRAITOR);
        Player playerD = createPlayer("player-d", 4, General.劉備, HealthStatus.ALIVE, Role.TRAITOR);

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        repository.save(game);
    }

    private void givenPlayerBHasOnlyDelayScrollCard() {
        Player playerA = createPlayer("player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH,
                new Snatch(SS3016), new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039));
        Player playerB = createPlayer("player-b", 4, General.劉備, HealthStatus.ALIVE, Role.TRAITOR);
        Stack<com.gaas.threeKingdoms.handcard.scrollcard.ScrollCard> delayScrollCards = new Stack<>();
        delayScrollCards.add(new Contentment(SH6045));
        playerB.setDelayScrollCards(delayScrollCards);
        
        Player playerC = createPlayer("player-c", 4, General.劉備, HealthStatus.ALIVE, Role.TRAITOR);
        Player playerD = createPlayer("player-d", 4, General.劉備, HealthStatus.ALIVE, Role.TRAITOR);

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        repository.save(game);
    }

    private void givenPlayerBHasOneCard() {
        Player playerA = createPlayer("player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH,
                new Snatch(SS3016), new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039));
        Player playerB = createPlayer("player-b", 4, General.劉備, HealthStatus.ALIVE, Role.TRAITOR,
                new Dodge(BH2028));
        Player playerC = createPlayer("player-c", 4, General.劉備, HealthStatus.ALIVE, Role.TRAITOR);
        Player playerD = createPlayer("player-d", 4, General.劉備, HealthStatus.ALIVE, Role.TRAITOR);

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        repository.save(game);
    }

    private void givenPlayerBHasWeapon() {
        Player playerA = createPlayer("player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH,
                new Snatch(SS3016), new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039));
        Player playerB = createPlayer("player-b", 4, General.張飛, HealthStatus.ALIVE, Role.TRAITOR);
        playerB.getEquipment().setWeapon(new QilinBowCard(EH5031));
        Player playerC = createPlayer("player-c", 4, General.關羽, HealthStatus.ALIVE, Role.TRAITOR);
        Player playerD = createPlayer("player-d", 4, General.趙雲, HealthStatus.ALIVE, Role.TRAITOR);

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        repository.save(game);
    }

    private void givenPlayerAHasQilinBowNoMinus1Horse() {
        Player playerA = createPlayer("player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH,
                new Snatch(SS3016), new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039));
        playerA.getEquipment().setWeapon(new QilinBowCard(EH5031));
        Player playerB = createPlayer("player-b", 4, General.張飛, HealthStatus.ALIVE, Role.TRAITOR);
        Player playerC = createPlayer("player-c", 4, General.關羽, HealthStatus.ALIVE, Role.TRAITOR);
        Player playerD = createPlayer("player-d", 4, General.趙雲, HealthStatus.ALIVE, Role.TRAITOR);

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        repository.save(game);
    }

    private void givenPlayerBHasPlusOneMount() {
        Player playerA = createPlayer("player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH,
                new Snatch(SS3016), new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039));
        Player playerB = createPlayer("player-b", 4, General.張飛, HealthStatus.ALIVE, Role.TRAITOR);
        playerB.getEquipment().setPlusOne(new ShadowHorse(ES5018));
        Player playerC = createPlayer("player-c", 4, General.關羽, HealthStatus.ALIVE, Role.TRAITOR);
        Player playerD = createPlayer("player-d", 4, General.趙雲, HealthStatus.ALIVE, Role.TRAITOR);

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        repository.save(game);
    }

    private void givenPlayerAHasMinusOneMountNoWeapon() {
        Player playerA = createPlayer("player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH,
                new Snatch(SS3016), new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039));
        playerA.getEquipment().setMinusOne(new RedRabbitHorse(EH5044));
        Player playerB = createPlayer("player-b", 4, General.張飛, HealthStatus.ALIVE, Role.TRAITOR);
        Player playerC = createPlayer("player-c", 4, General.關羽, HealthStatus.ALIVE, Role.TRAITOR,
                new Dodge(BHK039));
        Player playerD = createPlayer("player-d", 4, General.趙雲, HealthStatus.ALIVE, Role.TRAITOR);

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        repository.save(game);
    }

    private void givenPlayerBHasWeaponAndFiveCards() {
        Player playerA = createPlayer("player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH,
                new Snatch(SS3016));
        Player playerB = createPlayer("player-b", 4, General.張飛, HealthStatus.ALIVE, Role.TRAITOR,
                new Kill(BS8008), new Peach(BH3029), new Peach(BH4030), new Peach(BH6032), new Peach(BH7033));
        playerB.getEquipment().setWeapon(new QilinBowCard(EH5031));
        Player playerC = createPlayer("player-c", 4, General.關羽, HealthStatus.ALIVE, Role.TRAITOR);
        Player playerD = createPlayer("player-d", 4, General.趙雲, HealthStatus.ALIVE, Role.TRAITOR);

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        repository.save(game);
    }

    private void givenPlayerAHasContentmentPlayerBHasSnatch() {
        Player playerA = createPlayer("player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH,
                new Contentment(SC6071));
        Player playerB = createPlayer("player-b", 4, General.張飛, HealthStatus.ALIVE, Role.MINISTER,
                new Snatch(SS3016), new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039));
        Player playerC = createPlayer("player-c", 4, General.關羽, HealthStatus.ALIVE, Role.TRAITOR);
        Player playerD = createPlayer("player-d", 4, General.趙雲, HealthStatus.ALIVE, Role.TRAITOR);

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        Deck deck = new Deck();
        deck.init();
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
