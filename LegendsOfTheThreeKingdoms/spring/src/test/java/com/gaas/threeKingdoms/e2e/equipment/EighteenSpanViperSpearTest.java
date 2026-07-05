package com.gaas.threeKingdoms.e2e.equipment;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.EightDiagramTactic;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.EighteenSpanViperSpearCard;
import com.gaas.threeKingdoms.handcard.scrollcard.BarbarianInvasion;
import com.gaas.threeKingdoms.handcard.scrollcard.BorrowedSword;
import com.gaas.threeKingdoms.handcard.scrollcard.Duel;
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

public class EighteenSpanViperSpearTest extends AbstractBaseIntegrationTest {

    // 若需要重產本 class 的 fixture，把以下 method 的 return 改為 true，
    // 產完後務必 revert 回 false 再 commit。
    // @Override
    // protected boolean shouldRegenerateFixtures() { return true; }

    @Test
    public void testEquipEighteenSpanViperSpear() throws Exception {
        Player playerA = createPlayer("player-a", 4, General.甘寧, HealthStatus.ALIVE, Role.MONARCH,
                new EighteenSpanViperSpearCard(ESQ025));
        Player playerB = createPlayer("player-b", 4, General.甘寧, HealthStatus.ALIVE, Role.TRAITOR);
        Player playerC = createPlayer("player-c", 4, General.甘寧, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.甘寧, HealthStatus.ALIVE, Role.MINISTER);
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
        Player playerA = createPlayer("player-a", 4, General.甘寧, HealthStatus.ALIVE, Role.MONARCH,
                new Peach(BH3029), new Peach(BH4030));
        playerA.getEquipment().setWeapon(new EighteenSpanViperSpearCard(ESQ025));

        Player playerB = createPlayer("player-b", 4, General.甘寧, HealthStatus.ALIVE, Role.TRAITOR);
        playerB.getEquipment().setArmor(new EightDiagramTactic(ES2015));

        Player playerC = createPlayer("player-c", 4, General.甘寧, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.甘寧, HealthStatus.ALIVE, Role.MINISTER);

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
        Player playerA = createPlayer("player-a", 4, General.甘寧, HealthStatus.ALIVE, Role.MONARCH,
                new Peach(BH3029), new Peach(BH4030));
        playerA.getEquipment().setWeapon(new EighteenSpanViperSpearCard(ESQ025));

        Player playerB = createPlayer("player-b", 1, General.甘寧, HealthStatus.ALIVE, Role.TRAITOR);
        Player playerC = createPlayer("player-c", 4, General.甘寧, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.甘寧, HealthStatus.ALIVE, Role.MINISTER);

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
    public void testPassiveViperSpear_RespondingBarbarianInvasion() throws Exception {
        // A 出南蠻入侵；B 裝丈八蛇矛 + 兩張手牌
        Player playerA = createPlayer("player-a", 4, General.甘寧, HealthStatus.ALIVE, Role.MONARCH,
                new BarbarianInvasion(SS7007));
        Player playerB = createPlayer("player-b", 4, General.甘寧, HealthStatus.ALIVE, Role.TRAITOR,
                new Peach(BH3029), new Peach(BH4030));
        playerB.getEquipment().setWeapon(new EighteenSpanViperSpearCard(ESQ025));
        Player playerC = createPlayer("player-c", 4, General.甘寧, HealthStatus.ALIVE, Role.REBEL,
                new Kill(BS8008));
        Player playerD = createPlayer("player-d", 4, General.甘寧, HealthStatus.ALIVE, Role.MINISTER,
                new Kill(BS9009));

        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Kill(BS0010), new Peach(BH6032), new Dodge(BH7033), new Kill(BS7020)));
        game.setDeck(deck);
        repository.save(game);

        // A 出南蠻 → B 第一個被詢問
        mockMvcUtil.playCard(gameId, "player-a", "", SS7007.getCardId(), PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        websocketUtil.popAllPlayerMessage();

        // B 用丈八蛇矛棄兩張當殺回應
        mockMvcUtil.useViperSpearKill(gameId, "player-b", null,
                        List.of(BH3029.getCardId(), BH4030.getCardId()))
                .andExpect(status().isOk()).andReturn();

        assertAllPlayerJson("src/test/resources/TestJsonFile/EquipmentTest/EighteenSpanViperSpear/passive_barbarian_for_%s.json");
    }

    @Test
    public void testPassiveViperSpear_RespondingDuel() throws Exception {
        // A 對 B 出決鬥；B 裝丈八蛇矛 + 兩張手牌
        Player playerA = createPlayer("player-a", 4, General.甘寧, HealthStatus.ALIVE, Role.MONARCH,
                new Duel(SSA001), new Kill(BS8008));
        Player playerB = createPlayer("player-b", 4, General.甘寧, HealthStatus.ALIVE, Role.TRAITOR,
                new Peach(BH3029), new Peach(BH4030));
        playerB.getEquipment().setWeapon(new EighteenSpanViperSpearCard(ESQ025));
        Player playerC = createPlayer("player-c", 4, General.甘寧, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.甘寧, HealthStatus.ALIVE, Role.MINISTER);

        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Kill(BS0010), new Peach(BH6032), new Dodge(BH7033), new Kill(BS7020)));
        game.setDeck(deck);
        repository.save(game);

        // A 對 B 出決鬥 → B 第一個被詢問出殺
        mockMvcUtil.playCard(gameId, "player-a", "player-b", SSA001.getCardId(), PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        websocketUtil.popAllPlayerMessage();

        // B 用丈八蛇矛棄兩張當殺回應 → A 換被詢問出殺
        mockMvcUtil.useViperSpearKill(gameId, "player-b", null,
                        List.of(BH3029.getCardId(), BH4030.getCardId()))
                .andExpect(status().isOk()).andReturn();

        assertAllPlayerJson("src/test/resources/TestJsonFile/EquipmentTest/EighteenSpanViperSpear/passive_duel_for_%s.json");
    }

    @Test
    public void testPassiveViperSpear_RespondingBorrowedSword() throws Exception {
        // A 出借刀殺人，借 B 攻擊 C；B 裝丈八蛇矛 + 兩張手牌
        Player playerA = createPlayer("player-a", 4, General.甘寧, HealthStatus.ALIVE, Role.MONARCH,
                new BorrowedSword(SCK065));
        Player playerB = createPlayer("player-b", 4, General.甘寧, HealthStatus.ALIVE, Role.TRAITOR,
                new Peach(BH3029), new Peach(BH4030));
        playerB.getEquipment().setWeapon(new EighteenSpanViperSpearCard(ESQ025));
        Player playerC = createPlayer("player-c", 4, General.甘寧, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.甘寧, HealthStatus.ALIVE, Role.MINISTER);

        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Kill(BS0010), new Peach(BH6032), new Dodge(BH7033), new Kill(BS7020)));
        game.setDeck(deck);
        repository.save(game);

        // A 出借刀殺人 → 指定 B 借
        mockMvcUtil.playCard(gameId, "player-a", "player-b", SCK065.getCardId(), PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        websocketUtil.popAllPlayerMessage();

        // A 完成借刀效果 → B 攻擊 C
        mockMvcUtil.useBorrowedSwordEffect(gameId, "player-a", "player-b", "player-c")
                .andExpect(status().isOk()).andReturn();
        websocketUtil.popAllPlayerMessage();

        // B 用丈八蛇矛棄兩張當殺攻擊 C → C 被詢問出閃
        mockMvcUtil.useViperSpearKill(gameId, "player-b", "player-c",
                        List.of(BH3029.getCardId(), BH4030.getCardId()))
                .andExpect(status().isOk()).andReturn();

        assertAllPlayerJson("src/test/resources/TestJsonFile/EquipmentTest/EighteenSpanViperSpear/passive_borrowed_sword_for_%s.json");
    }

    @Test
    public void testUseViperSpearKill_NotEquipped_Returns4xx() throws Exception {
        // A 沒裝備丈八蛇矛
        Player playerA = createPlayer("player-a", 4, General.甘寧, HealthStatus.ALIVE, Role.MONARCH,
                new Peach(BH3029), new Peach(BH4030));
        Player playerB = createPlayer("player-b", 4, General.甘寧, HealthStatus.ALIVE, Role.TRAITOR);
        Player playerC = createPlayer("player-c", 4, General.甘寧, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.甘寧, HealthStatus.ALIVE, Role.MINISTER);

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
        Player playerA = createPlayer("player-a", 4, General.甘寧, HealthStatus.ALIVE, Role.MONARCH,
                new Peach(BH3029), new Peach(BH4030));
        playerA.getEquipment().setWeapon(new EighteenSpanViperSpearCard(ESQ025));

        Player playerB = createPlayer("player-b", 4, General.甘寧, HealthStatus.ALIVE, Role.TRAITOR,
                new Dodge(BH2028));
        Player playerC = createPlayer("player-c", 4, General.甘寧, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.甘寧, HealthStatus.ALIVE, Role.MINISTER);

        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Kill(BS0010), new Peach(BH6032), new Dodge(BH7033), new Kill(BS7020)));
        game.setDeck(deck);
        repository.save(game);
    }

}
