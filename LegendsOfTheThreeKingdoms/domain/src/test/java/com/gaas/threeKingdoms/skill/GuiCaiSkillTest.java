package com.gaas.threeKingdoms.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.AskDodgeEvent;
import com.gaas.threeKingdoms.events.AskSkillEffectEvent;
import com.gaas.threeKingdoms.events.ContentmentEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.EightDiagramTacticEffectEvent;
import com.gaas.threeKingdoms.events.GameStatusEvent;
import com.gaas.threeKingdoms.events.LightningEvent;
import com.gaas.threeKingdoms.events.LightningTransferredEvent;
import com.gaas.threeKingdoms.events.SkillEffectEvent;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.EquipmentPlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.EightDiagramTactic;
import com.gaas.threeKingdoms.handcard.scrollcard.Lightning;
import com.gaas.threeKingdoms.player.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 司馬懿 鬼才（#164）— 覆蓋全部判定路徑：閃電 / 樂不思蜀 / 八卦陣 / 鐵騎 / 剛烈 / 洛神。
 * 注意：handleLightningJudgement 以參數收閃電卡（產品 caller judgePlayerShouldDelay
 * 判定前已從判定區 pop）；測試不可把卡放進判定區，否則 resolve 後
 * continueJudgementAndDraw 會對判定區殘留的閃電再判一次（隨機牌 → 不確定性）。
 * 判定牌以 deck.add 控制（Stack — list 最後一個元素最先被抽）。
 */
public class GuiCaiSkillTest extends PassiveSkillTestBase {

    @DisplayName("場上有司馬懿（有手牌）→ 閃電判定抽牌後暫停，詢問鬼才")
    @Test
    public void guiCaiAskedBeforeLightningJudgementResolves() {
        Game game = createGame(General.甘寧, General.司馬懿, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        game.getPlayer("player-b").getHand().addCardToHand(new Peach(BH3029));
        Lightning lightning = new Lightning(SSA014);

        // 判定牌：黑桃 8（原本會命中）
        game.getDeck().add(List.of(new Kill(BS8008)));
        List<DomainEvent> events = game.handleLightningJudgement(lightning, a);

        AskSkillEffectEvent ask = events.stream()
                .filter(e -> e instanceof AskSkillEffectEvent).map(e -> (AskSkillEffectEvent) e)
                .findFirst().orElseThrow();
        assertEquals("鬼才", ask.getSkillName());
        assertEquals("player-b", ask.getPlayerId());
        assertEquals(List.of(BS8008.getCardId()), ask.getDataCardIds());
        assertFalse(events.stream().anyMatch(e -> e instanceof LightningEvent), "判定尚未結算");
        assertEquals(4, a.getHP());
    }

    @DisplayName("鬼才 ACCEPT 以紅心替換黑桃判定 → 閃電不中，轉移下家")
    @Test
    public void guiCaiReplaceSavesOwnerFromLightning() {
        Game game = createGame(General.甘寧, General.司馬懿, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        b.getHand().addCardToHand(new Peach(BH3029)); // 紅心 — 替換用
        Lightning lightning = new Lightning(SSA014);

        game.getDeck().add(List.of(new Kill(BS8008))); // 黑桃 8 原判定：命中
        game.handleLightningJudgement(lightning, a);

        List<DomainEvent> events = game.playerUseSkillEffect(
                "player-b", "鬼才", "ACCEPT", List.of(BH3029.getCardId()), null);

        assertEquals(4, a.getHP(), "替換成紅心 → 閃電不中");
        assertTrue(events.stream().anyMatch(e -> e instanceof LightningTransferredEvent), "閃電轉移");
        assertEquals(0, b.getHandSize(), "替換牌已打出");
        assertTrue(game.getGraveyard().contains(BH3029.getCardId()));
    }

    @DisplayName("鬼才 SKIP → 原黑桃判定生效，閃電命中扣 3 血")
    @Test
    public void guiCaiSkipOriginalJudgementApplies() {
        Game game = createGame(General.甘寧, General.司馬懿, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        game.getPlayer("player-b").getHand().addCardToHand(new Peach(BH3029));
        Lightning lightning = new Lightning(SSA014);

        game.getDeck().add(List.of(new Kill(BS8008)));
        game.handleLightningJudgement(lightning, a);

        game.playerUseSkillEffect("player-b", "鬼才", "SKIP", null, null);

        assertEquals(1, a.getHP(), "黑桃 8 判定生效 → 閃電 3 點傷害");
    }

    @DisplayName("司馬懿無手牌 → 鬼才不觸發，判定直接結算")
    @Test
    public void guiCaiNotTriggeredWithoutHandCards() {
        Game game = createGame(General.甘寧, General.司馬懿, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Lightning lightning = new Lightning(SSA014);

        game.getDeck().add(List.of(new Kill(BS8008)));
        List<DomainEvent> events = game.handleLightningJudgement(lightning, a);

        assertFalse(events.stream().anyMatch(e -> e instanceof AskSkillEffectEvent));
        assertTrue(events.stream().anyMatch(e -> e instanceof LightningEvent));
        assertEquals(1, a.getHP(), "直接結算命中");
    }

    @DisplayName("場上無司馬懿 → 判定直接結算（迴歸保護）")
    @Test
    public void noGuiCaiHolderJudgementResolvesDirectly() {
        Game game = createGame(General.甘寧, General.孫權, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Lightning lightning = new Lightning(SSA014);

        game.getDeck().add(List.of(new Peach(BH3029))); // 紅心：不中
        List<DomainEvent> events = game.handleLightningJudgement(lightning, a);

        assertFalse(events.stream().anyMatch(e -> e instanceof AskSkillEffectEvent));
        assertTrue(events.stream().anyMatch(e -> e instanceof LightningTransferredEvent));
    }

    // ===== 樂不思蜀判定 =====

    @DisplayName("樂不思蜀判定抽牌後暫停，詢問鬼才")
    @Test
    public void guiCaiAskedBeforeContentmentJudgementResolves() {
        Game game = createGame(General.甘寧, General.司馬懿, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        game.getPlayer("player-b").getHand().addCardToHand(new Peach(BH3029));

        game.getDeck().add(List.of(new Kill(BS8008))); // 黑桃：原判定生效（跳過出牌）
        List<DomainEvent> events = game.handleContentmentJudgement(a);

        AskSkillEffectEvent ask = events.stream()
                .filter(e -> e instanceof AskSkillEffectEvent).map(e -> (AskSkillEffectEvent) e)
                .findFirst().orElseThrow();
        assertEquals("鬼才", ask.getSkillName());
        assertEquals("player-b", ask.getPlayerId());
        assertEquals(List.of(BS8008.getCardId()), ask.getDataCardIds());
        assertFalse(events.stream().anyMatch(e -> e instanceof ContentmentEvent), "判定尚未結算");
    }

    @DisplayName("鬼才 ACCEPT 以紅心替換 → 樂不思蜀判定不生效")
    @Test
    public void guiCaiReplaceMakesContentmentFail() {
        Game game = createGame(General.甘寧, General.司馬懿, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        b.getHand().addCardToHand(new Peach(BH3029)); // 紅心 — 替換用

        game.getDeck().add(List.of(new Kill(BS8008)));
        game.handleContentmentJudgement(a);

        List<DomainEvent> events = game.playerUseSkillEffect(
                "player-b", "鬼才", "ACCEPT", List.of(BH3029.getCardId()), null);

        ContentmentEvent contentment = events.stream()
                .filter(e -> e instanceof ContentmentEvent).map(e -> (ContentmentEvent) e)
                .findFirst().orElseThrow();
        assertFalse(contentment.isSuccess(), "替換成紅心 → 判定不生效");
        assertEquals(0, b.getHandSize(), "替換牌已打出");
        assertTrue(game.getGraveyard().contains(BH3029.getCardId()));
    }

    @DisplayName("鬼才 SKIP → 原黑桃樂不思蜀判定生效")
    @Test
    public void guiCaiSkipContentmentOriginalApplies() {
        Game game = createGame(General.甘寧, General.司馬懿, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        game.getPlayer("player-b").getHand().addCardToHand(new Peach(BH3029));

        game.getDeck().add(List.of(new Kill(BS8008)));
        game.handleContentmentJudgement(a);

        List<DomainEvent> events = game.playerUseSkillEffect("player-b", "鬼才", "SKIP", null, null);

        ContentmentEvent contentment = events.stream()
                .filter(e -> e instanceof ContentmentEvent).map(e -> (ContentmentEvent) e)
                .findFirst().orElseThrow();
        assertTrue(contentment.isSuccess(), "原黑桃判定生效");
    }

    // ===== 八卦陣判定 =====

    @DisplayName("八卦陣判定抽牌後暫停，詢問鬼才")
    @Test
    public void guiCaiAskedBeforeEightDiagramJudgementResolves() {
        Game game = createGame(General.甘寧, General.司馬懿, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(new Kill(BS8008));
        b.getHand().addCardToHand(new Peach(BH3029));
        b.getEquipment().setArmor(new EightDiagramTactic(EC2067));

        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");
        game.getDeck().add(List.of(new Kill(BS9009))); // 黑桃：原判定失敗（要出閃）
        List<DomainEvent> events = game.playerUseEquipment(
                "player-b", EC2067.getCardId(), "player-b", EquipmentPlayType.ACTIVE);

        AskSkillEffectEvent ask = events.stream()
                .filter(e -> e instanceof AskSkillEffectEvent).map(e -> (AskSkillEffectEvent) e)
                .findFirst().orElseThrow();
        assertEquals("鬼才", ask.getSkillName());
        assertEquals("player-b", ask.getPlayerId());
        assertFalse(events.stream().anyMatch(e -> e instanceof EightDiagramTacticEffectEvent),
                "八卦陣尚未結算");
    }

    @DisplayName("鬼才 ACCEPT 以紅心替換 → 八卦陣成功（視為出閃）")
    @Test
    public void guiCaiReplaceMakesEightDiagramSucceed() {
        Game game = createGame(General.甘寧, General.司馬懿, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(new Kill(BS8008));
        b.getHand().addCardToHand(new Peach(BH3029));
        b.getEquipment().setArmor(new EightDiagramTactic(EC2067));

        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");
        game.getDeck().add(List.of(new Kill(BS9009)));
        game.playerUseEquipment("player-b", EC2067.getCardId(), "player-b", EquipmentPlayType.ACTIVE);

        List<DomainEvent> events = game.playerUseSkillEffect(
                "player-b", "鬼才", "ACCEPT", List.of(BH3029.getCardId()), null);

        EightDiagramTacticEffectEvent effect = events.stream()
                .filter(e -> e instanceof EightDiagramTacticEffectEvent)
                .map(e -> (EightDiagramTacticEffectEvent) e)
                .findFirst().orElseThrow();
        assertTrue(effect.isSuccess(), "替換成紅心 → 八卦陣成功");
        assertEquals(4, b.getHP(), "視為出閃，不受傷");
        assertTrue(game.isTopBehaviorEmpty(), "殺已結算完畢");
    }

    @DisplayName("使用者回報：鬼才換牌結束後 activePlayer 應回到回合主 A（最後一個 GameStatusEvent 快照）")
    @Test
    public void guiCaiResolveFinalStatusSnapshotHasActivePlayerA() {
        Game game = createGame(General.甘寧, General.孫權, General.司馬懿, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(new Kill(BS8008));
        b.getEquipment().setArmor(new EightDiagramTactic(EC2067));
        game.getPlayer("player-c").getHand().addCardToHand(new Peach(BH3029));

        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");
        game.getDeck().add(List.of(new Kill(BS9009)));
        game.playerUseEquipment("player-b", EC2067.getCardId(), "player-b", EquipmentPlayType.ACTIVE);
        assertEquals("player-c", game.getCurrentRound().getActivePlayer().getId(), "鬼才詢問中");

        List<DomainEvent> events = game.playerUseSkillEffect(
                "player-c", "鬼才", "ACCEPT", List.of(BH3029.getCardId()), null);

        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
        GameStatusEvent last = events.stream().filter(e -> e instanceof GameStatusEvent)
                .map(e -> (GameStatusEvent) e).reduce((x, y) -> y).orElseThrow();
        assertEquals("player-a", last.getRound().getActivePlayer(),
                "presenter 取最後一個 GameStatusEvent → 須為推進後的最終狀態（回合主 A）");
    }

    @DisplayName("鬼才 SKIP → 原黑桃判定八卦陣失敗，續問閃")
    @Test
    public void guiCaiSkipEightDiagramFailsAsksDodge() {
        Game game = createGame(General.甘寧, General.司馬懿, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(new Kill(BS8008));
        b.getHand().addCardToHand(new Peach(BH3029));
        b.getEquipment().setArmor(new EightDiagramTactic(EC2067));

        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");
        game.getDeck().add(List.of(new Kill(BS9009)));
        game.playerUseEquipment("player-b", EC2067.getCardId(), "player-b", EquipmentPlayType.ACTIVE);

        List<DomainEvent> events = game.playerUseSkillEffect("player-b", "鬼才", "SKIP", null, null);

        EightDiagramTacticEffectEvent effect = events.stream()
                .filter(e -> e instanceof EightDiagramTacticEffectEvent)
                .map(e -> (EightDiagramTacticEffectEvent) e)
                .findFirst().orElseThrow();
        assertFalse(effect.isSuccess(), "原黑桃判定 → 八卦陣失敗");
        assertTrue(events.stream().anyMatch(e -> e instanceof AskDodgeEvent
                        && ((AskDodgeEvent) e).getPlayerId().equals("player-b")),
                "失敗後續問閃");
    }

    // ===== 鐵騎判定 =====

    @DisplayName("馬超鐵騎判定抽牌後暫停，詢問鬼才")
    @Test
    public void guiCaiAskedBeforeTieQiJudgementResolves() {
        Game game = createGame(General.馬超, General.司馬懿, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(new Kill(BS8008));
        b.getHand().addCardToHand(List.of(new Peach(BH3029), new Dodge(BH2028)));

        game.getDeck().add(List.of(new Kill(BS9009))); // 黑桃：原本鐵騎生效
        List<DomainEvent> events = game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");

        AskSkillEffectEvent ask = events.stream()
                .filter(e -> e instanceof AskSkillEffectEvent).map(e -> (AskSkillEffectEvent) e)
                .findFirst().orElseThrow();
        assertEquals("鬼才", ask.getSkillName());
        assertEquals("player-b", ask.getPlayerId());
        assertEquals("player-a", ask.getDataPlayerId(), "判定 owner 為馬超");
        assertFalse(events.stream().anyMatch(e -> e instanceof SkillEffectEvent
                && ((SkillEffectEvent) e).getSkillName().equals("鐵騎")), "鐵騎尚未結算");
        assertEquals(4, b.getHP());
    }

    @DisplayName("鬼才 ACCEPT 以紅心替換 → 鐵騎不生效，照常問閃")
    @Test
    public void guiCaiReplaceMakesTieQiFail() {
        Game game = createGame(General.馬超, General.司馬懿, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(new Kill(BS8008));
        b.getHand().addCardToHand(List.of(new Peach(BH3029), new Dodge(BH2028)));

        game.getDeck().add(List.of(new Kill(BS9009)));
        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");

        List<DomainEvent> events = game.playerUseSkillEffect(
                "player-b", "鬼才", "ACCEPT", List.of(BH3029.getCardId()), null);

        assertTrue(events.stream().anyMatch(e -> e instanceof SkillEffectEvent
                        && ((SkillEffectEvent) e).getSkillName().equals("鐵騎")
                        && !((SkillEffectEvent) e).isAccepted()),
                "替換成紅心 → 鐵騎不生效");
        assertTrue(events.stream().anyMatch(e -> e instanceof AskDodgeEvent
                && ((AskDodgeEvent) e).getPlayerId().equals("player-b")), "照常問閃");
        assertEquals(4, b.getHP());

        // 迴歸保護：resume 後 activePlayer 須還原為殺目標，否則目標出閃會被擋
        game.playerPlayCard("player-b", BH2028.getCardId(), "player-a", "active");
        assertEquals(4, b.getHP(), "閃擋下殺");
        assertTrue(game.isTopBehaviorEmpty());
    }

    @DisplayName("鬼才 SKIP → 原黑桃判定鐵騎生效，目標不能閃直接扣血")
    @Test
    public void guiCaiSkipTieQiSuccessDamagesTarget() {
        Game game = createGame(General.馬超, General.司馬懿, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(new Kill(BS8008));
        b.getHand().addCardToHand(List.of(new Peach(BH3029), new Dodge(BH2028)));

        game.getDeck().add(List.of(new Kill(BS9009)));
        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");

        List<DomainEvent> events = game.playerUseSkillEffect("player-b", "鬼才", "SKIP", null, null);

        assertFalse(events.stream().anyMatch(e -> e instanceof AskDodgeEvent), "鐵騎生效不問閃");
        assertEquals(3, b.getHP(), "直接扣血");
        assertEquals(2, b.getHandSize(), "SKIP 不消耗手牌，閃也未被消耗");
        assertTrue(game.isTopBehaviorEmpty());
    }

    // ===== 剛烈判定 =====

    @DisplayName("夏侯惇剛烈 ACCEPT 後判定抽牌暫停，詢問鬼才（巢狀 waiting）")
    @Test
    public void guiCaiAskedBeforeGangLieJudgementResolves() {
        Game game = createGame(General.甘寧, General.夏侯惇, General.司馬懿, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(new Kill(BS8008));
        game.getPlayer("player-c").getHand().addCardToHand(new Peach(BH3029));

        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");
        game.playerPlayCard("player-b", "", "player-a", "skip");
        game.getDeck().add(List.of(new Kill(BS9009))); // 黑桃：原本剛烈生效
        List<DomainEvent> events = game.playerUseSkillEffect("player-b", "剛烈", "ACCEPT", null, null);

        AskSkillEffectEvent ask = events.stream()
                .filter(e -> e instanceof AskSkillEffectEvent).map(e -> (AskSkillEffectEvent) e)
                .findFirst().orElseThrow();
        assertEquals("鬼才", ask.getSkillName());
        assertEquals("player-c", ask.getPlayerId());
        assertFalse(events.stream().anyMatch(e -> e instanceof SkillEffectEvent
                && ((SkillEffectEvent) e).getSkillName().equals("剛烈")
                && ((SkillEffectEvent) e).isAccepted()), "剛烈判定尚未結算");
    }

    @DisplayName("鬼才 ACCEPT 以紅心替換 → 剛烈判定不生效，不問來源")
    @Test
    public void guiCaiReplaceMakesGangLieFail() {
        Game game = createGame(General.甘寧, General.夏侯惇, General.司馬懿, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(new Kill(BS8008));
        Player c = game.getPlayer("player-c");
        c.getHand().addCardToHand(new Peach(BH3029));

        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");
        game.playerPlayCard("player-b", "", "player-a", "skip");
        game.getDeck().add(List.of(new Kill(BS9009)));
        game.playerUseSkillEffect("player-b", "剛烈", "ACCEPT", null, null);

        List<DomainEvent> events = game.playerUseSkillEffect(
                "player-c", "鬼才", "ACCEPT", List.of(BH3029.getCardId()), null);

        assertFalse(events.stream().anyMatch(e -> e instanceof AskSkillEffectEvent
                        && ((AskSkillEffectEvent) e).getSkillName().equals("剛烈")),
                "判定紅心不生效 → 不問來源");
        assertTrue(game.isTopBehaviorEmpty());
        assertEquals(4, a.getHP());
    }

    @DisplayName("鬼才 SKIP → 原黑桃剛烈判定生效，問來源棄兩張或受 1 傷")
    @Test
    public void guiCaiSkipGangLieSuccessAsksSource() {
        Game game = createGame(General.甘寧, General.夏侯惇, General.司馬懿, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(new Kill(BS8008));
        game.getPlayer("player-c").getHand().addCardToHand(new Peach(BH3029));

        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");
        game.playerPlayCard("player-b", "", "player-a", "skip");
        game.getDeck().add(List.of(new Kill(BS9009)));
        game.playerUseSkillEffect("player-b", "剛烈", "ACCEPT", null, null);

        List<DomainEvent> events = game.playerUseSkillEffect("player-c", "鬼才", "SKIP", null, null);

        assertTrue(events.stream().anyMatch(e -> e instanceof AskSkillEffectEvent
                        && ((AskSkillEffectEvent) e).getSkillName().equals("剛烈")
                        && ((AskSkillEffectEvent) e).getPlayerId().equals("player-a")),
                "剛烈生效 → 問來源");

        game.playerUseSkillEffect("player-a", "剛烈", "DAMAGE", null, null);
        assertEquals(3, a.getHP(), "來源選擇受 1 點傷害");
    }

    // ===== 洛神判定 =====

    @DisplayName("甄姬洛神每張判定牌抽出後暫停，詢問鬼才；ACCEPT 紅心替換 → 停止收牌")
    @Test
    public void guiCaiReplaceStopsLuoShenLoop() {
        Game game = createGame(General.甄姬, General.司馬懿, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        b.getHand().addCardToHand(new Peach(BH3029));

        // 抽序 = BS8008(黑，原本收牌續判) → 洛神停止後摸 2 = BH4030 + BD2093。
        // 摸牌也必須疊牌控制：initDeck 牌堆裡有同 id 的 BS8008，隨機摸到會誤觸
        // 「原判定牌不收」斷言（CI flake）
        game.getDeck().add(List.of(new Dodge(BD2093), new Peach(BH4030), new Kill(BS8008)));
        List<DomainEvent> events = game.playerTakeTurnStartInJudgement(a);

        AskSkillEffectEvent ask = events.stream()
                .filter(e -> e instanceof AskSkillEffectEvent).map(e -> (AskSkillEffectEvent) e)
                .findFirst().orElseThrow();
        assertEquals("鬼才", ask.getSkillName());
        assertEquals(List.of(BS8008.getCardId()), ask.getDataCardIds());
        assertFalse(events.stream().anyMatch(e -> e instanceof SkillEffectEvent
                && ((SkillEffectEvent) e).getSkillName().equals("洛神")), "判定尚未結算");

        List<DomainEvent> resumeEvents = game.playerUseSkillEffect(
                "player-b", "鬼才", "ACCEPT", List.of(BH3029.getCardId()), null);

        assertTrue(resumeEvents.stream().anyMatch(e -> e instanceof SkillEffectEvent
                        && ((SkillEffectEvent) e).getSkillName().equals("洛神")
                        && !((SkillEffectEvent) e).isAccepted()),
                "替換成紅心 → 洛神停止");
        assertFalse(a.getHand().getCards().stream().anyMatch(c -> c.getId().equals(BS8008.getCardId())),
                "原判定牌不收");
        assertTrue(game.getGraveyard().contains(BS8008.getCardId()));
        assertTrue(game.getGraveyard().contains(BH3029.getCardId()));
    }

    @DisplayName("鬼才 SKIP → 黑色判定牌照收並續判（下一張再次詢問）")
    @Test
    public void guiCaiSkipLuoShenBlackCollectsAndContinues() {
        Game game = createGame(General.甄姬, General.司馬懿, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        b.getHand().addCardToHand(new Peach(BH3029));

        // 抽序 = BS8008(黑) → BH4030(紅停) → 摸 2 = BD2093 + BD3094（摸牌疊牌控制，避免隨機）
        game.getDeck().add(List.of(new Dodge(BD3094), new Dodge(BD2093), new Peach(BH4030), new Kill(BS8008)));
        game.playerTakeTurnStartInJudgement(a);

        List<DomainEvent> firstResume = game.playerUseSkillEffect("player-b", "鬼才", "SKIP", null, null);

        assertTrue(a.getHand().getCards().stream().anyMatch(c -> c.getId().equals(BS8008.getCardId())),
                "黑色判定牌照收");
        assertTrue(firstResume.stream().anyMatch(e -> e instanceof AskSkillEffectEvent
                        && ((AskSkillEffectEvent) e).getSkillName().equals("鬼才")),
                "續判下一張 → 再次詢問鬼才");

        List<DomainEvent> secondResume = game.playerUseSkillEffect("player-b", "鬼才", "SKIP", null, null);

        assertTrue(secondResume.stream().anyMatch(e -> e instanceof SkillEffectEvent
                        && ((SkillEffectEvent) e).getSkillName().equals("洛神")
                        && !((SkillEffectEvent) e).isAccepted()),
                "紅色 → 洛神停止");
        assertEquals(3, a.getHandSize(), "收 1 張判定牌 + 摸 2 張");
        assertTrue(game.isTopBehaviorEmpty());
    }
}
