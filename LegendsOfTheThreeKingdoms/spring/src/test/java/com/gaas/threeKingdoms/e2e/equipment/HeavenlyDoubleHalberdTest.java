package com.gaas.threeKingdoms.e2e.equipment;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.HeavenlyDoubleHalberdCard;
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

public class HeavenlyDoubleHalberdTest extends AbstractBaseIntegrationTest {

    // 若需要重產本 class 的 fixture，把下列 @Override 取消註解並改為 return true，
    // 產完後務必改回 false 或刪除 override 再 commit。
    // @Override protected boolean shouldRegenerateFixtures() { return true; }

    @Test
    public void testEquipHeavenlyDoubleHalberd() throws Exception {
        Player playerA = createPlayer("player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH,
                new HeavenlyDoubleHalberdCard(EDQ103));
        Player playerB = createPlayer("player-b", 4, General.劉備, HealthStatus.ALIVE, Role.TRAITOR);
        Player playerC = createPlayer("player-c", 4, General.劉備, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.劉備, HealthStatus.ALIVE, Role.MINISTER);
        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Kill(BS9009)));
        game.setDeck(deck);
        repository.save(game);

        mockMvcUtil.playCard(gameId, "player-a", "player-a", "EDQ103", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        assertAllPlayerJson("src/test/resources/TestJsonFile/EquipmentTest/HeavenlyDoubleHalberd/player_a_equip_halberd_for_%s.json");
    }

    @Test
    public void testUseHalberdKill_TriggerEventBroadcast() throws Exception {
        givenPlayerAEquippedHalberdWithSingleKill();

        mockMvcUtil.useHeavenlyDoubleHalberdKill(gameId, "player-a", "BS8008",
                        "player-b", List.of("player-c"))
                .andExpect(status().isOk()).andReturn();

        // 驗證 HeavenlyDoubleHalberdKillTriggerEvent + AskDodgeEvent(B) 有廣播到 4 位玩家
        assertAllPlayerJson("src/test/resources/TestJsonFile/EquipmentTest/HeavenlyDoubleHalberd/halberd_kill_trigger_for_%s.json");
    }

    @Test
    public void testUseHalberdKill_TwoTargetsSkip_BothTakeDamage() throws Exception {
        givenPlayerAEquippedHalberdWithSingleKill();

        mockMvcUtil.useHeavenlyDoubleHalberdKill(gameId, "player-a", "BS8008",
                        "player-b", List.of("player-c"))
                .andExpect(status().isOk()).andReturn();
        websocketUtil.popAllPlayerMessage();

        mockMvcUtil.playCard(gameId, "player-b", "player-a", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();
        websocketUtil.popAllPlayerMessage();

        mockMvcUtil.playCard(gameId, "player-c", "player-a", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();

        assertAllPlayerJson("src/test/resources/TestJsonFile/EquipmentTest/HeavenlyDoubleHalberd/halberd_kill_all_skip_for_%s.json");
    }

    @Test
    public void testUseHalberdKill_MixedDodgeAndSkip() throws Exception {
        Player playerA = createPlayer("player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH,
                new Kill(BS8008));
        playerA.getEquipment().setWeapon(new HeavenlyDoubleHalberdCard(EDQ103));

        Player playerB = createPlayer("player-b", 4, General.劉備, HealthStatus.ALIVE, Role.TRAITOR,
                new Dodge(BH2028));
        Player playerC = createPlayer("player-c", 4, General.劉備, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.劉備, HealthStatus.ALIVE, Role.MINISTER);

        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Kill(BS0010), new Peach(BH6032), new Dodge(BH7033), new Kill(BS7020)));
        game.setDeck(deck);
        repository.save(game);

        mockMvcUtil.useHeavenlyDoubleHalberdKill(gameId, "player-a", "BS8008",
                        "player-b", List.of("player-c"))
                .andExpect(status().isOk()).andReturn();
        websocketUtil.popAllPlayerMessage();

        // B 出閃
        mockMvcUtil.playCard(gameId, "player-b", "player-a", "BH2028", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        websocketUtil.popAllPlayerMessage();

        // C 不出閃 → 扣血
        mockMvcUtil.playCard(gameId, "player-c", "player-a", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();

        assertAllPlayerJson("src/test/resources/TestJsonFile/EquipmentTest/HeavenlyDoubleHalberd/halberd_kill_mixed_for_%s.json");
    }

    @Test
    public void testUseHalberdKill_ZeroAdditional_ShortCircuitsToNormalKill() throws Exception {
        givenPlayerAEquippedHalberdWithSingleKill();

        // additional 空陣列 → 短路為一般殺
        mockMvcUtil.useHeavenlyDoubleHalberdKill(gameId, "player-a", "BS8008",
                        "player-b", List.of())
                .andExpect(status().isOk()).andReturn();

        // 驗證沒有 HeavenlyDoubleHalberdKillTriggerEvent，只有一般 PlayCardEvent + AskDodgeEvent
        assertAllPlayerJson("src/test/resources/TestJsonFile/EquipmentTest/HeavenlyDoubleHalberd/halberd_kill_short_circuit_for_%s.json");
    }

    @Test
    public void testUseHalberdKill_NotEquipped_Returns4xx() throws Exception {
        Player playerA = createPlayer("player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH,
                new Kill(BS8008));
        Player playerB = createPlayer("player-b", 4, General.劉備, HealthStatus.ALIVE, Role.TRAITOR);
        Player playerC = createPlayer("player-c", 4, General.劉備, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.劉備, HealthStatus.ALIVE, Role.MINISTER);

        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        game.setDeck(new Deck());
        repository.save(game);

        mockMvcUtil.useHeavenlyDoubleHalberdKill(gameId, "player-a", "BS8008",
                        "player-b", List.of("player-c"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testUseHalberdKill_NotLastHandCard_Returns4xx() throws Exception {
        Player playerA = createPlayer("player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH,
                new Kill(BS8008), new Peach(BH3029));
        playerA.getEquipment().setWeapon(new HeavenlyDoubleHalberdCard(EDQ103));

        Player playerB = createPlayer("player-b", 4, General.劉備, HealthStatus.ALIVE, Role.TRAITOR);
        Player playerC = createPlayer("player-c", 4, General.劉備, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.劉備, HealthStatus.ALIVE, Role.MINISTER);

        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        game.setDeck(new Deck());
        repository.save(game);

        // A 有兩張手牌 → halberd 不可觸發
        mockMvcUtil.useHeavenlyDoubleHalberdKill(gameId, "player-a", "BS8008",
                        "player-b", List.of("player-c"))
                .andExpect(status().is4xxClientError());
    }

    private void givenPlayerAEquippedHalberdWithSingleKill() {
        Player playerA = createPlayer("player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH,
                new Kill(BS8008));
        playerA.getEquipment().setWeapon(new HeavenlyDoubleHalberdCard(EDQ103));

        Player playerB = createPlayer("player-b", 4, General.劉備, HealthStatus.ALIVE, Role.TRAITOR);
        Player playerC = createPlayer("player-c", 4, General.劉備, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.劉備, HealthStatus.ALIVE, Role.MINISTER);

        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Kill(BS0010), new Peach(BH6032), new Dodge(BH7033), new Kill(BS7020)));
        game.setDeck(deck);
        repository.save(game);
    }
}
