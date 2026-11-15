package com.gaas.threeKingdoms.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.RedRabbitHorse;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.*;
import com.gaas.threeKingdoms.handcard.scrollcard.BarbarianInvasion;
import com.gaas.threeKingdoms.handcard.scrollcard.Dismantle;
import com.gaas.threeKingdoms.handcard.scrollcard.Duel;
import com.gaas.threeKingdoms.handcard.scrollcard.Ward;
import com.gaas.threeKingdoms.player.*;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.rolecard.RoleCard;
import com.gaas.threeKingdoms.round.Round;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 連營（陸遜）：失去最後一張手牌 → 摸一張。
 * 涵蓋「陸遜自己用掉／棄掉最後手牌」但不走一般 playCard／棄牌階段的路徑：
 * 剛烈棄兩張、雌雄雙股劍棄一張、貫石斧棄兩張、丈八蛇矛（主動／被動）、方天畫戟多目標、
 * 青龍偃月刀追殺、無懈可擊、主公殺忠臣棄光所有牌。
 */
public class LianYingSelfLosePathsTest extends PassiveSkillTestBase {

    private static long drawEventsFor(List<DomainEvent> events, String playerId) {
        return events.stream()
                .filter(e -> e instanceof DrawCardEvent d && playerId.equals(d.getDrawCardPlayerId()))
                .count();
    }

    private static int indexOf(List<DomainEvent> events, Class<?> type) {
        for (int i = 0; i < events.size(); i++) {
            if (type.isInstance(events.get(i))) return i;
        }
        return -1;
    }

    // ===== 剛烈 =====

    @DisplayName("陸遜決鬥夏侯惇，剛烈生效後陸遜棄最後兩張手牌 → 連營摸一張")
    @Test
    public void gangLieDiscardLastTwoTriggersLianYing() {
        Game game = createGame(General.陸遜, General.夏侯惇, General.甘寧, General.甘寧);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(List.of(new Duel(SSA001), new Peach(BH3029), new Kill(BS8008)));
        game.getPlayer("player-b").getHand().addCardToHand(new Kill(BS9009));
        // 剛烈判定（黑桃 → 生效），之後才是連營摸的牌
        game.getDeck().putBackOnTop(List.of(new Dismantle(SS3003), new Peach(BH4030)));

        game.playerPlayCard("player-a", SSA001.getCardId(), "player-b", PlayType.ACTIVE.getPlayType());
        game.playerPlayCard("player-b", "", "player-a", PlayType.SKIP.getPlayType());
        game.playerUseSkillEffect("player-b", "剛烈", "ACCEPT", null, null);
        List<DomainEvent> events = game.playerUseSkillEffect("player-a", "剛烈", "DISCARD",
                List.of(BH3029.getCardId(), BS8008.getCardId()), null);

        assertEquals(4, a.getHP(), "選棄牌，不受傷");
        assertEquals(1, a.getHandSize(), "棄光 → 連營摸 1");
        assertTrue(a.getHand().getCard(BH4030.getCardId()).isPresent());
        assertEquals(1, drawEventsFor(events, "player-a"));
        assertTrue(game.getTopBehavior().isEmpty());
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
    }

    // ===== 雌雄雙股劍 =====

    @DisplayName("甄姬雌雄雙股劍殺陸遜，陸遜棄最後一張手牌 → 先連營摸牌再問閃，摸到的閃可以出")
    @Test
    public void yinYangSwordsTargetDiscardLastTriggersLianYingBeforeAskDodge() {
        Game game = createGame(General.甄姬, General.陸遜, General.甘寧, General.甘寧);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getEquipment().setWeapon(new YinYangSwordsCard(ES2002));
        a.getHand().addCardToHand(new Kill(BS8008));
        b.getHand().addCardToHand(new Peach(BH3029));
        game.getDeck().putBackOnTop(List.of(new Dodge(BH2028), new Peach(BH4030)));

        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", PlayType.ACTIVE.getPlayType());
        game.playerActivateYinYangSwords("player-a", AskActivateYinYangSwordsEvent.Choice.ACTIVATE);
        List<DomainEvent> events = game.playerUseYinYangSwordsEffect("player-b",
                YinYangSwordsEffectEvent.Choice.TARGET_DISCARDS, BH3029.getCardId());

        assertTrue(b.getHand().getCard(BH2028.getCardId()).isPresent(), "棄光 → 連營摸到閃");
        assertEquals(1, b.getHandSize());
        int drawIdx = indexOf(events, DrawCardEvent.class);
        int askDodgeIdx = indexOf(events, AskDodgeEvent.class);
        assertTrue(drawIdx >= 0 && askDodgeIdx > drawIdx, "連營摸牌在問閃之前");
        assertEquals("player-b", game.getCurrentRound().getActivePlayer().getId());

        game.playerPlayCard("player-b", BH2028.getCardId(), "player-a", PlayType.ACTIVE.getPlayType());
        assertEquals(4, b.getHP(), "用連營摸到的閃抵銷殺");
    }

    // ===== 貫石斧 =====

    @DisplayName("陸遜貫石斧棄最後兩張手牌強制命中 → 連營摸一張")
    @Test
    public void stonePiercingAxeDiscardLastTwoTriggersLianYing() {
        Game game = createGame(General.陸遜, General.甘寧, General.甘寧, General.甘寧);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getEquipment().setWeapon(new StonePiercingAxeCard(ED5083));
        a.getHand().addCardToHand(List.of(new Kill(BS8008), new Peach(BH3029), new Peach(BH4030)));
        b.getHand().addCardToHand(new Dodge(BH2028));
        game.getDeck().putBackOnTop(List.of(new Dodge(BH2041)));

        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", PlayType.ACTIVE.getPlayType());
        game.playerPlayCard("player-b", BH2028.getCardId(), "player-a", PlayType.ACTIVE.getPlayType());
        List<DomainEvent> events = game.playerUseStonePiercingAxeEffect("player-a",
                AskStonePiercingAxeEffectEvent.Choice.DISCARD_TWO,
                List.of(BH3029.getCardId(), BH4030.getCardId()));

        assertEquals(3, b.getHP(), "強制命中");
        assertEquals(1, a.getHandSize(), "棄光 → 連營摸 1");
        assertTrue(a.getHand().getCard(BH2041.getCardId()).isPresent());
        assertEquals(1, drawEventsFor(events, "player-a"));
        assertTrue(game.getTopBehavior().isEmpty());
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
    }

    @DisplayName("陸遜貫石斧棄最後一張手牌＋一張裝備 → 連營摸一張")
    @Test
    public void stonePiercingAxeDiscardLastHandPlusEquipmentTriggersLianYing() {
        Game game = createGame(General.陸遜, General.甘寧, General.甘寧, General.甘寧);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getEquipment().setWeapon(new StonePiercingAxeCard(ED5083));
        a.getEquipment().setMinusOne(new RedRabbitHorse(EH5044));
        a.getHand().addCardToHand(List.of(new Kill(BS8008), new Peach(BH3029)));
        b.getHand().addCardToHand(new Dodge(BH2028));
        game.getDeck().putBackOnTop(List.of(new Dodge(BH2041)));

        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", PlayType.ACTIVE.getPlayType());
        game.playerPlayCard("player-b", BH2028.getCardId(), "player-a", PlayType.ACTIVE.getPlayType());
        game.playerUseStonePiercingAxeEffect("player-a", AskStonePiercingAxeEffectEvent.Choice.DISCARD_TWO,
                List.of(BH3029.getCardId(), EH5044.getCardId()));

        assertEquals(1, a.getHandSize(), "手牌歸零 → 連營摸 1");
        assertTrue(a.getHand().getCard(BH2041.getCardId()).isPresent());
    }

    @DisplayName("陸遜貫石斧棄兩張後仍有手牌 → 連營不觸發")
    @Test
    public void stonePiercingAxeWithHandLeftDoesNotTrigger() {
        Game game = createGame(General.陸遜, General.甘寧, General.甘寧, General.甘寧);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getEquipment().setWeapon(new StonePiercingAxeCard(ED5083));
        a.getHand().addCardToHand(List.of(new Kill(BS8008), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2041)));
        b.getHand().addCardToHand(new Dodge(BH2028));

        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", PlayType.ACTIVE.getPlayType());
        game.playerPlayCard("player-b", BH2028.getCardId(), "player-a", PlayType.ACTIVE.getPlayType());
        List<DomainEvent> events = game.playerUseStonePiercingAxeEffect("player-a",
                AskStonePiercingAxeEffectEvent.Choice.DISCARD_TWO,
                List.of(BH3029.getCardId(), BH4030.getCardId()));

        assertEquals(1, a.getHandSize());
        assertEquals(0, drawEventsFor(events, "player-a"));
    }

    // ===== 丈八蛇矛 =====

    @DisplayName("陸遜主動用丈八蛇矛把最後兩張手牌當殺 → 連營摸一張，仍問目標出閃")
    @Test
    public void viperSpearActiveLastTwoTriggersLianYing() {
        Game game = createGame(General.陸遜, General.甘寧, General.甘寧, General.甘寧);
        Player a = game.getPlayer("player-a");
        a.getEquipment().setWeapon(new EighteenSpanViperSpearCard(ESQ025));
        a.getHand().addCardToHand(List.of(new Peach(BH3029), new Peach(BH4030)));
        game.getDeck().putBackOnTop(List.of(new Dodge(BH2041)));

        List<DomainEvent> events = game.playerUseViperSpearKill("player-a", "player-b",
                List.of(BH3029.getCardId(), BH4030.getCardId()));

        assertEquals(1, a.getHandSize(), "棄光 → 連營摸 1");
        assertTrue(a.getHand().getCard(BH2041.getCardId()).isPresent());
        assertEquals(1, drawEventsFor(events, "player-a"));
        assertTrue(indexOf(events, AskDodgeEvent.class) >= 0);
        assertEquals("player-b", game.getCurrentRound().getActivePlayer().getId(), "activePlayer 仍是被殺的目標");
    }

    @DisplayName("南蠻入侵時陸遜被動用丈八蛇矛把最後兩張手牌當殺 → 連營摸一張")
    @Test
    public void viperSpearPassiveLastTwoTriggersLianYing() {
        Game game = createGame(General.甘寧, General.陸遜, General.甘寧, General.甘寧);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(new BarbarianInvasion(SSK013));
        b.getEquipment().setWeapon(new EighteenSpanViperSpearCard(ESQ025));
        b.getHand().addCardToHand(List.of(new Peach(BH3029), new Peach(BH4030)));

        game.playerPlayCard("player-a", SSK013.getCardId(), "player-a", PlayType.ACTIVE.getPlayType());
        game.getDeck().putBackOnTop(List.of(new Dodge(BH2041)));
        List<DomainEvent> events = game.playerUseViperSpearKill("player-b", "player-a",
                List.of(BH3029.getCardId(), BH4030.getCardId()));

        assertEquals(4, b.getHP());
        assertEquals(1, b.getHandSize(), "棄光 → 連營摸 1");
        assertTrue(b.getHand().getCard(BH2041.getCardId()).isPresent());
        assertEquals(1, drawEventsFor(events, "player-b"));
        assertEquals("player-c", game.getCurrentRound().getActivePlayer().getId(), "南蠻輪到下一位");
    }

    // ===== 方天畫戟 =====

    @DisplayName("陸遜用方天畫戟以最後一張殺指定兩名目標 → 連營摸一張")
    @Test
    public void halberdMultiTargetTriggersLianYing() {
        Game game = createGame(General.陸遜, General.甘寧, General.甘寧, General.甘寧);
        Player a = game.getPlayer("player-a");
        a.getEquipment().setWeapon(new HeavenlyDoubleHalberdCard(EDQ103));
        a.getHand().addCardToHand(new Kill(BS8008));
        game.getDeck().putBackOnTop(List.of(new Dodge(BH2041)));

        List<DomainEvent> events = game.playerUseHeavenlyDoubleHalberdKill("player-a", BS8008.getCardId(),
                List.of("player-b", "player-c"));

        assertEquals(1, a.getHandSize(), "最後一張殺出完 → 連營摸 1");
        assertTrue(a.getHand().getCard(BH2041.getCardId()).isPresent());
        assertEquals(1, drawEventsFor(events, "player-a"));
        assertEquals("player-b", game.getCurrentRound().getActivePlayer().getId());
    }

    // ===== 青龍偃月刀 =====

    @DisplayName("陸遜青龍偃月刀追加的殺是最後一張手牌 → 連營摸一張")
    @Test
    public void greenDragonFollowUpKillLastTriggersLianYing() {
        Game game = createGame(General.陸遜, General.甘寧, General.甘寧, General.甘寧);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getEquipment().setWeapon(new GreenDragonCrescentBladeCard(ES5005));
        a.getHand().addCardToHand(List.of(new Kill(BS8008), new Kill(BS9009)));
        b.getHand().addCardToHand(new Dodge(BH2028));
        game.getDeck().putBackOnTop(List.of(new Dodge(BH2041)));

        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", PlayType.ACTIVE.getPlayType());
        game.playerPlayCard("player-b", BH2028.getCardId(), "player-a", PlayType.ACTIVE.getPlayType());
        List<DomainEvent> events = game.playerUseGreenDragonCrescentBladeEffect("player-a",
                AskGreenDragonCrescentBladeEffectEvent.Choice.KILL, BS9009.getCardId());

        assertEquals(1, a.getHandSize(), "追殺用掉最後一張 → 連營摸 1");
        assertTrue(a.getHand().getCard(BH2041.getCardId()).isPresent());
        assertEquals(1, drawEventsFor(events, "player-a"));
        assertEquals("player-b", game.getCurrentRound().getActivePlayer().getId());
    }

    // ===== 無懈可擊 =====

    @DisplayName("陸遜以最後一張手牌（無懈可擊）抵銷過河拆橋 → 連營摸一張")
    @Test
    public void wardLastHandCardTriggersLianYing() {
        Game game = createGame(General.甘寧, General.陸遜, General.甘寧, General.甘寧);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(new Dismantle(SS4004));
        b.getHand().addCardToHand(new Ward(SSJ011));
        game.getDeck().putBackOnTop(List.of(new Peach(BH4030)));

        game.playerPlayCard("player-a", SS4004.getCardId(), "player-b", PlayType.ACTIVE.getPlayType());
        game.useDismantleEffect("player-a", "player-b", "", 0);
        List<DomainEvent> events = game.playWardCard("player-b", SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());

        assertEquals(1, b.getHandSize(), "無懈用掉最後一張 → 連營摸 1，且過河拆橋被抵銷");
        assertTrue(b.getHand().getCard(BH4030.getCardId()).isPresent());
        assertEquals(1, drawEventsFor(events, "player-b"));
        assertTrue(game.getTopBehavior().isEmpty());
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
    }

    // ===== 主公殺忠臣棄光所有牌 =====

    @DisplayName("主公陸遜殺死忠臣，棄光所有手牌 → 連營摸一張")
    @Test
    public void monarchKillsMinisterDiscardAllTriggersLianYing() {
        Game game = new Game();
        game.initDeck();
        Player a = buildPlayer("player-a", General.陸遜, Role.MONARCH);
        Player b = buildPlayer("player-b", General.甘寧, Role.REBEL);
        Player c = PlayerBuilder.construct().withId("player-c")
                .withBloodCard(new BloodCard(1))
                .withGeneralCard(new GeneralCard(General.甘寧))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withHand(new Hand()).withEquipment(new Equipment()).build();
        Player d = buildPlayer("player-d", General.甘寧, Role.TRAITOR);
        game.setPlayers(asList(a, b, c, d));
        game.setCurrentRound(new Round(a));
        game.enterPhase(new Normal(game));
        a.getHand().addCardToHand(List.of(new Duel(SSA001), new Kill(BS8008)));

        game.playerPlayCard("player-a", SSA001.getCardId(), "player-c", PlayType.ACTIVE.getPlayType());
        game.playerPlayCard("player-c", "", "player-a", PlayType.SKIP.getPlayType());
        game.getDeck().putBackOnTop(List.of(new Peach(BH4030)));
        // 求桃輪詢：全部不出，最後一位不是陸遜（避免一般 response 路徑的連營掩蓋結果）
        List<DomainEvent> events = List.of();
        for (int i = 0; i < 4 && events.stream().noneMatch(e -> e instanceof SettlementEvent); i++) {
            String asked = game.getCurrentRound().getActivePlayer().getId();
            events = game.playerPlayCard(asked, "", "player-c", PlayType.SKIP.getPlayType());
            if (events.stream().anyMatch(e -> e instanceof SettlementEvent)) {
                assertNotEquals("player-a", asked, "測試前提：最後拒絕出桃的不是陸遜");
            }
        }

        assertTrue(events.stream().anyMatch(e -> e instanceof SettlementEvent), "忠臣死亡");
        assertTrue(events.stream().anyMatch(e -> e instanceof DiscardEvent), "主公棄所有牌");
        assertEquals(1, a.getHandSize(), "棄光手牌 → 連營摸 1");
        assertTrue(a.getHand().getCard(BH4030.getCardId()).isPresent());
        assertEquals(1, drawEventsFor(events, "player-a"));
    }
}
