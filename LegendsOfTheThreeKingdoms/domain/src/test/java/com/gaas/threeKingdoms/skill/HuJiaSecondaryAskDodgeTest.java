package com.gaas.threeKingdoms.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.behavior.ArrowBarrageBehavior;
import com.gaas.threeKingdoms.behavior.behavior.NormalActiveKillBehavior;
import com.gaas.threeKingdoms.behavior.behavior.WaitingSkillEffectBehavior;
import com.gaas.threeKingdoms.events.AskActivateYinYangSwordsEvent;
import com.gaas.threeKingdoms.events.AskDodgeEvent;
import com.gaas.threeKingdoms.events.AskGreenDragonCrescentBladeEffectEvent;
import com.gaas.threeKingdoms.events.AskHuJiaEffectEvent;
import com.gaas.threeKingdoms.events.AskSkillEffectEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.EquipmentPlayType;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.EightDiagramTactic;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.GreenDragonCrescentBladeCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.YinYangSwordsCard;
import com.gaas.threeKingdoms.handcard.scrollcard.ArrowBarrage;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.round.Round;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 護駕（曹操主公技）在「二次問閃」場景也要攔截：
 * 八卦陣判定失敗／放棄八卦陣、萬箭中八卦陣成功後輪到曹操、青龍偃月刀追殺、雌雄雙股劍結算後、
 * 萬箭瀕死救回後接回輪詢輪到曹操。
 */
public class HuJiaSecondaryAskDodgeTest extends PassiveSkillTestBase {

    private static final String ACTIVE = PlayType.ACTIVE.getPlayType();
    private static final String SKIP = PlayType.SKIP.getPlayType();

    /** 座位 a（攻擊者、回合主）→ b → c → d；caoCaoId 為主公曹操，其餘身分照座位給。 */
    private Game createGame(General a, General b, General c, General d, String caoCaoId) {
        Game game = new Game();
        game.initDeck();
        Player pa = buildPlayer("player-a", a, Role.REBEL);
        Player pb = buildPlayer("player-b", b, "player-b".equals(caoCaoId) ? Role.MONARCH : Role.MINISTER);
        Player pc = buildPlayer("player-c", c, "player-c".equals(caoCaoId) ? Role.MONARCH : Role.MINISTER);
        Player pd = buildPlayer("player-d", d, Role.TRAITOR);
        game.setPlayers(asList(pa, pb, pc, pd));
        game.setCurrentRound(new Round(pa));
        game.enterPhase(new Normal(game));
        return game;
    }

    private static boolean asksHuJia(List<DomainEvent> events, String caoCaoId) {
        return events.stream().anyMatch(e -> e instanceof AskSkillEffectEvent a
                && "護駕".equals(a.getSkillName()) && caoCaoId.equals(a.getPlayerId()));
    }

    private static boolean asksDodge(List<DomainEvent> events, String playerId) {
        return events.stream().anyMatch(e -> e instanceof AskDodgeEvent d && playerId.equals(d.getPlayerId()));
    }

    // ===== 八卦陣（殺）=====

    /** a 出殺 → b 曹操（八卦陣）；c 夏侯惇（魏）有閃。牌堆頂為判定牌。 */
    private Game givenKillOnCaoCaoWithEightDiagram(com.gaas.threeKingdoms.handcard.HandCard judgement) {
        Game game = createGame(General.甘寧, General.曹操, General.夏侯惇, General.甘寧, "player-b");
        game.getPlayer("player-a").getHand().addCardToHand(new Kill(BS8008));
        game.getPlayer("player-b").getEquipment().setArmor(new EightDiagramTactic(ES2015));
        game.getPlayer("player-c").getHand().addCardToHand(new Dodge(BH2028));
        game.getDeck().add(List.of(judgement));
        return game;
    }

    @DisplayName("殺：曹操八卦陣判定失敗（黑） → 詢問護駕；魏將代閃 → 曹操不扣血、殺結算完畢")
    @Test
    public void killEightDiagramFailAsksHuJia() {
        Game game = givenKillOnCaoCaoWithEightDiagram(new Kill(BS9009));
        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", ACTIVE);

        List<DomainEvent> events = game.playerUseEquipment("player-b", ES2015.getCardId(), "player-b", EquipmentPlayType.ACTIVE);

        assertTrue(asksHuJia(events, "player-b"), "八卦陣失敗後應詢問護駕：" + events);
        assertFalse(asksDodge(events, "player-b"), "護駕攔截時不應直接問閃");
        assertTrue(game.peekTopBehavior() instanceof WaitingSkillEffectBehavior);

        List<DomainEvent> accept = game.playerUseSkillEffect("player-b", "護駕", "ACCEPT", null, null);
        assertTrue(accept.stream().anyMatch(e -> e instanceof AskHuJiaEffectEvent h && h.getPlayerId().equals("player-c")));

        game.playerUseHuJiaEffect("player-c", AskHuJiaEffectEvent.Choice.ACCEPT, BH2028.getCardId());

        assertEquals(4, game.getPlayer("player-b").getHP());
        assertEquals(0, game.getPlayer("player-c").getHandSize());
        assertTrue(game.isTopBehaviorEmpty(), "殺應結算完畢");
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
    }

    @DisplayName("殺：曹操八卦陣判定失敗 → 放棄護駕 → 自己被問閃，不出 → 扣血")
    @Test
    public void killEightDiagramFailSkipHuJiaThenTakeDamage() {
        Game game = givenKillOnCaoCaoWithEightDiagram(new Kill(BS9009));
        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", ACTIVE);
        game.playerUseEquipment("player-b", ES2015.getCardId(), "player-b", EquipmentPlayType.ACTIVE);

        List<DomainEvent> skip = game.playerUseSkillEffect("player-b", "護駕", "SKIP", null, null);
        assertTrue(asksDodge(skip, "player-b"));
        assertTrue(game.peekTopBehavior() instanceof NormalActiveKillBehavior);

        game.playerPlayCard("player-b", "", "player-a", SKIP);
        assertEquals(3, game.getPlayer("player-b").getHP());
        assertEquals(1, game.getPlayer("player-c").getHandSize(), "放棄護駕不可動用魏將的閃");
    }

    @DisplayName("殺：曹操放棄發動八卦陣 → 詢問護駕；魏將代閃 → 曹操不扣血")
    @Test
    public void killEightDiagramSkippedAsksHuJia() {
        Game game = givenKillOnCaoCaoWithEightDiagram(new Kill(BS9009));
        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", ACTIVE);

        List<DomainEvent> events = game.playerUseEquipment("player-b", ES2015.getCardId(), "player-b", EquipmentPlayType.SKIP);

        assertTrue(asksHuJia(events, "player-b"), "放棄八卦陣後應詢問護駕：" + events);
        assertFalse(asksDodge(events, "player-b"));

        game.playerUseSkillEffect("player-b", "護駕", "ACCEPT", null, null);
        game.playerUseHuJiaEffect("player-c", AskHuJiaEffectEvent.Choice.ACCEPT, BH2028.getCardId());

        assertEquals(4, game.getPlayer("player-b").getHP());
        assertTrue(game.isTopBehaviorEmpty());
    }

    // ===== 八卦陣（萬箭齊發）=====

    @DisplayName("萬箭：曹操八卦陣判定失敗 → 詢問護駕；魏將代閃後輪詢推進到下一位")
    @Test
    public void arrowBarrageEightDiagramFailAsksHuJiaThenAdvances() {
        Game game = createGame(General.甘寧, General.曹操, General.夏侯惇, General.甘寧, "player-b");
        game.getPlayer("player-a").getHand().addCardToHand(new ArrowBarrage(SHA040));
        game.getPlayer("player-b").getEquipment().setArmor(new EightDiagramTactic(ES2015));
        game.getPlayer("player-c").getHand().addCardToHand(new Dodge(BH2028));
        game.getDeck().add(List.of(new Kill(BS9009)));

        game.playerPlayCard("player-a", SHA040.getCardId(), "player-a", ACTIVE);
        List<DomainEvent> events = game.playerUseEquipment("player-b", ES2015.getCardId(), "player-b", EquipmentPlayType.ACTIVE);

        assertTrue(asksHuJia(events, "player-b"), "八卦陣失敗後應詢問護駕：" + events);

        game.playerUseSkillEffect("player-b", "護駕", "ACCEPT", null, null);
        List<DomainEvent> afterAccept = game.playerUseHuJiaEffect("player-c", AskHuJiaEffectEvent.Choice.ACCEPT, BH2028.getCardId());

        assertEquals(4, game.getPlayer("player-b").getHP());
        assertTrue(asksDodge(afterAccept, "player-c"), "代閃後輪到 c：" + afterAccept);
        assertTrue(game.peekTopBehavior() instanceof ArrowBarrageBehavior);
        assertEquals("player-c", game.getCurrentRound().getActivePlayer().getId());
    }

    /** 萬箭：b（甘寧，八卦陣）判定成功 → 下一位 c 為曹操；d 夏侯惇（魏）有閃。 */
    private Game givenArrowBarrageEightDiagramSuccessThenCaoCao() {
        Game game = createGame(General.甘寧, General.甘寧, General.曹操, General.夏侯惇, "player-c");
        game.getPlayer("player-a").getHand().addCardToHand(new ArrowBarrage(SHA040));
        game.getPlayer("player-b").getEquipment().setArmor(new EightDiagramTactic(ES2015));
        game.getPlayer("player-d").getHand().addCardToHand(new Dodge(BH2028));
        game.getDeck().add(List.of(new Peach(BH3029)));
        return game;
    }

    @DisplayName("萬箭：前一位八卦陣成功、輪到曹操 → 詢問護駕；魏將代閃後輪到 d")
    @Test
    public void arrowBarrageEightDiagramSuccessThenCaoCaoAsksHuJia() {
        Game game = givenArrowBarrageEightDiagramSuccessThenCaoCao();
        game.playerPlayCard("player-a", SHA040.getCardId(), "player-a", ACTIVE);

        List<DomainEvent> events = game.playerUseEquipment("player-b", ES2015.getCardId(), "player-b", EquipmentPlayType.ACTIVE);

        assertTrue(asksHuJia(events, "player-c"), "輪到曹操應詢問護駕：" + events);
        assertFalse(asksDodge(events, "player-c"));

        game.playerUseSkillEffect("player-c", "護駕", "ACCEPT", null, null);
        List<DomainEvent> afterAccept = game.playerUseHuJiaEffect("player-d", AskHuJiaEffectEvent.Choice.ACCEPT, BH2028.getCardId());

        assertEquals(4, game.getPlayer("player-b").getHP());
        assertEquals(4, game.getPlayer("player-c").getHP());
        assertTrue(asksDodge(afterAccept, "player-d"), "代閃後輪到 d：" + afterAccept);
    }

    @DisplayName("萬箭：前一位八卦陣成功、輪到曹操 → 放棄護駕、不出閃 → 扣曹操的血（不是前一位）")
    @Test
    public void arrowBarrageEightDiagramSuccessThenCaoCaoSkipsAndIsDamaged() {
        Game game = givenArrowBarrageEightDiagramSuccessThenCaoCao();
        game.playerPlayCard("player-a", SHA040.getCardId(), "player-a", ACTIVE);
        game.playerUseEquipment("player-b", ES2015.getCardId(), "player-b", EquipmentPlayType.ACTIVE);

        List<DomainEvent> skip = game.playerUseSkillEffect("player-c", "護駕", "SKIP", null, null);
        assertTrue(asksDodge(skip, "player-c"));

        List<DomainEvent> damaged = game.playerPlayCard("player-c", "", "player-a", SKIP);

        assertEquals(4, game.getPlayer("player-b").getHP(), "八卦陣成功者不該被扣血");
        assertEquals(3, game.getPlayer("player-c").getHP());
        assertTrue(damaged.stream().anyMatch(e -> e instanceof com.gaas.threeKingdoms.events.AskJianXiongEffectEvent));

        List<DomainEvent> afterJianXiong = game.playerUseJianXiongEffect(
                "player-c", com.gaas.threeKingdoms.events.AskJianXiongEffectEvent.Choice.SKIP);
        assertTrue(asksDodge(afterJianXiong, "player-d"), "曹操結算完輪到 d：" + afterJianXiong);
    }

    // ===== 青龍偃月刀追殺 =====

    @DisplayName("青龍偃月刀：曹操出閃後攻擊者追殺 → 再次詢問護駕；魏將代閃 → 攻擊者可再追殺")
    @Test
    public void greenDragonFollowUpKillAsksHuJia() {
        Game game = createGame(General.甘寧, General.曹操, General.夏侯惇, General.甘寧, "player-b");
        Player a = game.getPlayer("player-a");
        a.getEquipment().setWeapon(new GreenDragonCrescentBladeCard(ES5005));
        a.getHand().addCardToHand(List.of(new Kill(BS8008), new Kill(BS9009)));
        game.getPlayer("player-b").getHand().addCardToHand(new Dodge(BHK039));
        game.getPlayer("player-c").getHand().addCardToHand(new Dodge(BH2028));

        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", ACTIVE);
        game.playerUseSkillEffect("player-b", "護駕", "SKIP", null, null);
        List<DomainEvent> dodged = game.playerPlayCard("player-b", BHK039.getCardId(), "player-a", ACTIVE);
        assertTrue(dodged.stream().anyMatch(e -> e instanceof AskGreenDragonCrescentBladeEffectEvent));

        List<DomainEvent> followUp = game.playerUseGreenDragonCrescentBladeEffect(
                "player-a", AskGreenDragonCrescentBladeEffectEvent.Choice.KILL, BS9009.getCardId());

        assertTrue(asksHuJia(followUp, "player-b"), "青龍追殺應再次詢問護駕：" + followUp);
        assertFalse(asksDodge(followUp, "player-b"));

        game.playerUseSkillEffect("player-b", "護駕", "ACCEPT", null, null);
        List<DomainEvent> afterAccept = game.playerUseHuJiaEffect("player-c", AskHuJiaEffectEvent.Choice.ACCEPT, BH2028.getCardId());

        assertEquals(4, game.getPlayer("player-b").getHP());
        assertTrue(afterAccept.stream().anyMatch(e -> e instanceof AskGreenDragonCrescentBladeEffectEvent),
                "代閃視為曹操出閃 → 攻擊者可再發動青龍：" + afterAccept);

        game.playerUseGreenDragonCrescentBladeEffect("player-a", AskGreenDragonCrescentBladeEffectEvent.Choice.SKIP, null);
        assertTrue(game.isTopBehaviorEmpty());
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
    }

    @DisplayName("青龍偃月刀：追殺時放棄護駕 → 曹操不出閃 → 扣血、殺結算完畢")
    @Test
    public void greenDragonFollowUpKillSkipHuJiaThenTakeDamage() {
        Game game = createGame(General.甘寧, General.曹操, General.夏侯惇, General.甘寧, "player-b");
        Player a = game.getPlayer("player-a");
        a.getEquipment().setWeapon(new GreenDragonCrescentBladeCard(ES5005));
        a.getHand().addCardToHand(List.of(new Kill(BS8008), new Kill(BS9009)));
        game.getPlayer("player-b").getHand().addCardToHand(new Dodge(BHK039));
        game.getPlayer("player-c").getHand().addCardToHand(new Dodge(BH2028));

        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", ACTIVE);
        game.playerUseSkillEffect("player-b", "護駕", "SKIP", null, null);
        game.playerPlayCard("player-b", BHK039.getCardId(), "player-a", ACTIVE);
        game.playerUseGreenDragonCrescentBladeEffect("player-a", AskGreenDragonCrescentBladeEffectEvent.Choice.KILL, BS9009.getCardId());

        List<DomainEvent> skip = game.playerUseSkillEffect("player-b", "護駕", "SKIP", null, null);
        assertTrue(asksDodge(skip, "player-b"));
        assertTrue(game.peekTopBehavior() instanceof NormalActiveKillBehavior);

        game.playerPlayCard("player-b", "", "player-a", SKIP);
        assertEquals(3, game.getPlayer("player-b").getHP());
        // 殺已結算（曹操受傷 → 只剩奸雄詢問）
        assertTrue(game.getTopBehavior().stream().noneMatch(b -> b instanceof NormalActiveKillBehavior));
    }

    // ===== 雌雄雙股劍 =====

    @DisplayName("雌雄雙股劍：攻擊者放棄發動 → 接回殺的問閃仍詢問護駕；魏將代閃 → 曹操不扣血")
    @Test
    public void yinYangSwordsContinuationAsksHuJia() {
        Game game = createGame(General.黃月英, General.曹操, General.夏侯惇, General.甘寧, "player-b");
        Player a = game.getPlayer("player-a");
        a.getEquipment().setWeapon(new YinYangSwordsCard(ES2002));
        a.getHand().addCardToHand(new Kill(BS8008));
        game.getPlayer("player-c").getHand().addCardToHand(new Dodge(BH2028));

        List<DomainEvent> played = game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", ACTIVE);
        assertTrue(played.stream().anyMatch(e -> e instanceof AskActivateYinYangSwordsEvent));

        List<DomainEvent> events = game.playerActivateYinYangSwords("player-a", AskActivateYinYangSwordsEvent.Choice.SKIP);
        assertTrue(asksHuJia(events, "player-b"), "雙股劍結算後問閃應詢問護駕：" + events);

        game.playerUseSkillEffect("player-b", "護駕", "ACCEPT", null, null);
        game.playerUseHuJiaEffect("player-c", AskHuJiaEffectEvent.Choice.ACCEPT, BH2028.getCardId());

        assertEquals(4, game.getPlayer("player-b").getHP());
        assertTrue(game.isTopBehaviorEmpty());
    }

    // ===== 萬箭瀕死救回接回輪詢 =====

    @DisplayName("萬箭：b 瀕死被救回、接回輪詢輪到曹操 → 詢問護駕；魏將代閃後輪到 d")
    @Test
    public void arrowBarrageRevivalResumeAsksHuJia() {
        Game game = createGame(General.甘寧, General.甘寧, General.曹操, General.夏侯惇, "player-c");
        game.getPlayer("player-a").getHand().addCardToHand(new ArrowBarrage(SHA040));
        game.getPlayer("player-b").getBloodCard().setHp(1);
        game.getPlayer("player-b").getHand().addCardToHand(new Peach(BH3029));
        game.getPlayer("player-d").getHand().addCardToHand(new Dodge(BH2028));

        game.playerPlayCard("player-a", SHA040.getCardId(), "player-a", ACTIVE);
        game.playerPlayCard("player-b", "", "player-a", SKIP);                 // b 1 → 0 瀕死
        List<DomainEvent> revived = game.playerPlayCard("player-b", BH3029.getCardId(), "player-b", ACTIVE);

        assertEquals(1, game.getPlayer("player-b").getHP());
        assertTrue(asksHuJia(revived, "player-c"), "接回輪詢輪到曹操應詢問護駕：" + revived);
        assertFalse(asksDodge(revived, "player-c"));

        game.playerUseSkillEffect("player-c", "護駕", "ACCEPT", null, null);
        List<DomainEvent> afterAccept = game.playerUseHuJiaEffect("player-d", AskHuJiaEffectEvent.Choice.ACCEPT, BH2028.getCardId());

        assertEquals(4, game.getPlayer("player-c").getHP());
        assertTrue(asksDodge(afterAccept, "player-d"), "代閃後輪到 d：" + afterAccept);
        assertTrue(game.peekTopBehavior() instanceof ArrowBarrageBehavior);
    }
}
