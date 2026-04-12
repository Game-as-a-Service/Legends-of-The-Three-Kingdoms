package com.gaas.threeKingdoms.e2e.equipment;

import com.gaas.threeKingdoms.Game;
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

import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.e2e.MockUtil.createPlayer;
import static com.gaas.threeKingdoms.e2e.MockUtil.initGame;
import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class YinYangSwordsTest extends AbstractBaseIntegrationTest {

    // @Override protected boolean shouldRegenerateFixtures() { return true; }

    @Test
    public void testPlayerAEquipYinYangSwords() throws Exception {
        givenPlayerAHasYinYangSwordsInHand();

        mockMvcUtil.playCard(gameId, "player-a", "player-a", "ES2002", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        assertAllPlayerJson("src/test/resources/TestJsonFile/EquipmentTest/YinYangSwords/player_a_equip_yin_yang_swords_for_%s.json");
    }

    @Test
    public void testPlayerAKillsOppositeGender_AskActivateEvent() throws Exception {
        givenPlayerAEquippedYinYangSwordsAndPlayerBIsFemale();

        // A 出殺打 B → 收到 AskActivateYinYangSwordsEvent（詢問 A 是否發動）
        mockMvcUtil.playCard(gameId, "player-a", "player-b", "BS8008", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        assertAllPlayerJson("src/test/resources/TestJsonFile/EquipmentTest/YinYangSwords/ask_activate_for_%s.json");
    }

    @Test
    public void testPlayerAActivatesAndTargetChoosesDiscard() throws Exception {
        givenPlayerAEquippedYinYangSwordsAndPlayerBIsFemale();

        // A 出殺
        mockMvcUtil.playCard(gameId, "player-a", "player-b", "BS8008", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        websocketUtil.popAllPlayerMessage();

        // A 選擇發動
        mockMvcUtil.activateYinYangSwords(gameId, "player-a", "ACTIVATE")
                .andExpect(status().isOk()).andReturn();
        websocketUtil.popAllPlayerMessage();

        // B 選擇棄一張手牌 (BH3029 Peach)
        mockMvcUtil.useYinYangSwordsEffect(gameId, "player-b", "TARGET_DISCARDS", "BH3029")
                .andExpect(status().isOk()).andReturn();

        assertAllPlayerJson("src/test/resources/TestJsonFile/EquipmentTest/YinYangSwords/target_discards_for_%s.json");
    }

    @Test
    public void testPlayerAActivatesAndTargetChoosesAttackerDraws() throws Exception {
        givenPlayerAEquippedYinYangSwordsAndPlayerBIsFemale();

        // A 出殺
        mockMvcUtil.playCard(gameId, "player-a", "player-b", "BS8008", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        websocketUtil.popAllPlayerMessage();

        // A 選擇發動
        mockMvcUtil.activateYinYangSwords(gameId, "player-a", "ACTIVATE")
                .andExpect(status().isOk()).andReturn();
        websocketUtil.popAllPlayerMessage();

        // B 選擇讓 A 摸一張牌
        mockMvcUtil.useYinYangSwordsEffect(gameId, "player-b", "ATTACKER_DRAWS", "")
                .andExpect(status().isOk()).andReturn();

        assertAllPlayerJson("src/test/resources/TestJsonFile/EquipmentTest/YinYangSwords/attacker_draws_for_%s.json");
    }

    @Test
    public void testPlayerASkipsActivation_ProceedsToDodge() throws Exception {
        givenPlayerAEquippedYinYangSwordsAndPlayerBIsFemale();

        // A 出殺
        mockMvcUtil.playCard(gameId, "player-a", "player-b", "BS8008", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        websocketUtil.popAllPlayerMessage();

        // A 選擇不發動 → 直接問 B 出閃
        mockMvcUtil.activateYinYangSwords(gameId, "player-a", "SKIP")
                .andExpect(status().isOk()).andReturn();

        assertAllPlayerJson("src/test/resources/TestJsonFile/EquipmentTest/YinYangSwords/skip_activation_for_%s.json");
    }

    @Test
    public void testPlayerAKillsSameGenderAndYinYangSwordsNotTriggered() throws Exception {
        givenPlayerAEquippedYinYangSwordsAndPlayerBIsFemale();

        // A 出殺對 C (同性) → 不觸發雌雄雙股劍
        mockMvcUtil.playCard(gameId, "player-a", "player-c", "BS8008", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        assertAllPlayerJson("src/test/resources/TestJsonFile/EquipmentTest/YinYangSwords/same_gender_no_trigger_for_%s.json");
    }

    private void givenPlayerAHasYinYangSwordsInHand() {
        Player playerA = createPlayer("player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH,
                new YinYangSwordsCard(ES2002));
        Player playerB = createPlayer("player-b", 4, General.甄姬, HealthStatus.ALIVE, Role.TRAITOR);
        Player playerC = createPlayer("player-c", 4, General.劉備, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.劉備, HealthStatus.ALIVE, Role.MINISTER);

        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
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

        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Kill(BS9009)));
        game.setDeck(deck);
        repository.save(game);
    }
}
