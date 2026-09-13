package com.gaas.threeKingdoms.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.AskDodgeEvent;
import com.gaas.threeKingdoms.events.AskSkillEffectEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.PlayEquipmentCardEvent;
import com.gaas.threeKingdoms.events.SkillEffectEvent;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.EquipmentCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.EightDiagramTactic;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.HexMark;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.RedRabbitHorse;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.ShadowHorse;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.VioletStallion;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.RepeatingCrossbowCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.StonePiercingAxeCard;
import com.gaas.threeKingdoms.handcard.scrollcard.Dismantle;
import com.gaas.threeKingdoms.handcard.scrollcard.Lightning;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.skill.registry.SkillEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Batch 2：反饋 / 遺計 / 剛烈 / 天妒 / 洛神 / 鐵騎 / 梟姬。
 * 判定牌以 deck.add 控制（Stack — list 最後一個元素最先被抽）。
 */
public class Batch2TriggeredSkillsTest extends PassiveSkillTestBase {

    private List<DomainEvent> killAndSkip(Game game, String attacker, String target) {
        game.playerPlayCard(attacker, BS8008.getCardId(), target, "active");
        return game.playerPlayCard(target, "", attacker, "skip");
    }

    // ===== 反饋 =====

    @DisplayName("司馬懿受殺傷 → AskSkillEffectEvent(反饋)；ACCEPT 取走攻擊者第一張手牌")
    @Test
    public void fanKuiAcceptTakesAttackerHandCard() {
        Game game = createGame(General.劉備, General.司馬懿, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029)));

        List<DomainEvent> events = killAndSkip(game, "player-a", "player-b");

        AskSkillEffectEvent ask = events.stream()
                .filter(e -> e instanceof AskSkillEffectEvent).map(e -> (AskSkillEffectEvent) e)
                .findFirst().orElseThrow();
        assertEquals("反饋", ask.getSkillName());
        assertEquals("player-b", ask.getPlayerId());

        game.playerUseSkillEffect("player-b", "反饋", "ACCEPT", null, null);

        assertTrue(b.getHand().getCards().stream().anyMatch(c -> c.getId().equals(BH3029.getCardId())),
                "司馬懿應取走攻擊者剩下的那張手牌");
        assertEquals(0, a.getHandSize());
        assertTrue(game.isTopBehaviorEmpty());
    }

    @DisplayName("反饋 SKIP → 不取牌")
    @Test
    public void fanKuiSkipTakesNothing() {
        Game game = createGame(General.劉備, General.司馬懿, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029)));

        killAndSkip(game, "player-a", "player-b");
        game.playerUseSkillEffect("player-b", "反饋", "SKIP", null, null);

        assertEquals(1, a.getHandSize());
        assertEquals(0, game.getPlayer("player-b").getHandSize());
        assertTrue(game.isTopBehaviorEmpty());
    }

    @DisplayName("攻擊者無手牌無裝備 → 反饋不觸發")
    @Test
    public void fanKuiNotTriggeredWhenAttackerHasNothing() {
        Game game = createGame(General.劉備, General.司馬懿, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(new Kill(BS8008)); // 出殺後空手且無裝備

        List<DomainEvent> events = killAndSkip(game, "player-a", "player-b");

        assertFalse(events.stream().anyMatch(e -> e instanceof AskSkillEffectEvent));
        assertTrue(game.isTopBehaviorEmpty());
    }

    @DisplayName("反饋 ACCEPT cardIds[0] 為數字 → 依 0-based index 取來源手牌（同順手牽羊語意）")
    @Test
    public void fanKuiAcceptTakesHandCardByIndex() {
        Game game = createGame(General.劉備, General.司馬懿, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        // 出殺後 a 手牌順序 = [BH3029, BH4030]
        a.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new Peach(BH4030)));

        killAndSkip(game, "player-a", "player-b");
        game.playerUseSkillEffect("player-b", "反饋", "ACCEPT", List.of("1"), null);

        assertTrue(b.getHand().getCards().stream().anyMatch(c -> c.getId().equals(BH4030.getCardId())),
                "index 1 → 取第二張手牌");
        assertEquals(1, a.getHandSize());
        assertTrue(a.getHand().getCards().stream().anyMatch(c -> c.getId().equals(BH3029.getCardId())),
                "第一張手牌留在來源");
    }

    @DisplayName("反饋 ACCEPT index 越界 → IllegalArgumentException，不動任何牌")
    @Test
    public void fanKuiAcceptIndexOverSizeThrows() {
        Game game = createGame(General.劉備, General.司馬懿, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029)));

        killAndSkip(game, "player-a", "player-b");

        assertThrows(IllegalArgumentException.class,
                () -> game.playerUseSkillEffect("player-b", "反饋", "ACCEPT", List.of("1"), null));
        assertEquals(1, a.getHandSize(), "越界不取牌");
        assertEquals(0, game.getPlayer("player-b").getHandSize());
    }

    @DisplayName("反饋 ACCEPT cardIds[0] 既非 index 也非來源裝備 id → IllegalArgumentException")
    @Test
    public void fanKuiAcceptInvalidPickThrows() {
        Game game = createGame(General.劉備, General.司馬懿, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029)));

        killAndSkip(game, "player-a", "player-b");

        assertThrows(IllegalArgumentException.class,
                () -> game.playerUseSkillEffect("player-b", "反饋", "ACCEPT", List.of("EH5031"), null));
        assertEquals(1, a.getHandSize());
    }

    @DisplayName("反饋詢問的 dataCardIds 只列實際存在的裝備 id（不含空欄位 \"\"）")
    @Test
    public void fanKuiAskListsOnlyExistingEquipmentIds() {
        Game game = createGame(General.劉備, General.司馬懿, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(new Kill(BS8008));
        a.getEquipment().setArmor(new com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.EightDiagramTactic(EC2067));

        List<DomainEvent> events = killAndSkip(game, "player-a", "player-b");

        AskSkillEffectEvent ask = events.stream()
                .filter(e -> e instanceof AskSkillEffectEvent).map(e -> (AskSkillEffectEvent) e)
                .findFirst().orElseThrow();
        assertEquals(List.of(EC2067.getCardId()), ask.getDataCardIds(), "只有防具一件，不該有 \"\" 佔位");
    }

    @DisplayName("來源無手牌、只有防具（武器欄空）→ 反饋 ACCEPT 不指定也能取到防具（修 fallback 取到 \"\" 噴錯）")
    @Test
    public void fanKuiFallbackTakesFirstExistingEquipment() {
        Game game = createGame(General.劉備, General.司馬懿, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(new Kill(BS8008)); // 出殺後無手牌
        a.getEquipment().setArmor(new com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.EightDiagramTactic(EC2067));

        killAndSkip(game, "player-a", "player-b");
        game.playerUseSkillEffect("player-b", "反饋", "ACCEPT", null, null);

        assertTrue(b.getHand().getCards().stream().anyMatch(c -> c.getId().equals(EC2067.getCardId())),
                "取走唯一的裝備（防具）");
        assertFalse(a.getEquipment().hasAnyEquipment());
    }

    @DisplayName("使用者回報：前端 skip 時 targetPlayerId 為空字串 → 傷害來源仍應為出殺者，反饋照常詢問")
    @Test
    public void fanKuiAskedWhenSkipHasEmptyTargetPlayerId() {
        Game game = createGame(General.劉備, General.司馬懿, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029)));

        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");
        List<DomainEvent> events = game.playerPlayCard("player-b", "", "", "skip"); // targetPlayerId = ""

        assertTrue(events.stream().anyMatch(e -> e instanceof AskSkillEffectEvent
                && ((AskSkillEffectEvent) e).getSkillName().equals("反饋")
                && "player-a".equals(((AskSkillEffectEvent) e).getDataPlayerId())),
                "來源應由 behavior 記錄的出殺者決定，不依賴 request 的 targetPlayerId");
    }

    // ===== 遺計 =====

    @DisplayName("郭嘉受傷 → 遺計 ACCEPT 摸兩張")
    @Test
    public void yiJiAcceptDrawsTwo() {
        Game game = createGame(General.劉備, General.郭嘉, General.孫權, General.孫權);
        game.getPlayer("player-a").getHand().addCardToHand(new Kill(BS8008));

        List<DomainEvent> events = killAndSkip(game, "player-a", "player-b");
        assertTrue(events.stream().anyMatch(e -> e instanceof AskSkillEffectEvent
                && ((AskSkillEffectEvent) e).getSkillName().equals("遺計")));

        game.playerUseSkillEffect("player-b", "遺計", "ACCEPT", null, null);

        assertEquals(2, game.getPlayer("player-b").getHandSize());
    }

    @DisplayName("郭嘉受傷 → 遺計 GIVE 令另一角色獲得一張")
    @Test
    public void yiJiGiveLetsAnotherPlayerDraw() {
        Game game = createGame(General.劉備, General.郭嘉, General.孫權, General.孫權);
        game.getPlayer("player-a").getHand().addCardToHand(new Kill(BS8008));

        killAndSkip(game, "player-a", "player-b");
        game.playerUseSkillEffect("player-b", "遺計", "GIVE", null, "player-c");

        assertEquals(0, game.getPlayer("player-b").getHandSize());
        assertEquals(1, game.getPlayer("player-c").getHandSize());
    }

    // ===== 剛烈 =====

    @DisplayName("夏侯惇剛烈 ACCEPT、判定非紅桃 → 來源選 DAMAGE 受 1 傷")
    @Test
    public void gangLieJudgementSuccessSourceTakesDamage() {
        Game game = createGame(General.劉備, General.夏侯惇, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(new Kill(BS8008));

        killAndSkip(game, "player-a", "player-b");
        // 判定牌：黑桃 → 剛烈生效
        game.getDeck().add(List.of(new Kill(BS9009)));
        List<DomainEvent> events = game.playerUseSkillEffect("player-b", "剛烈", "ACCEPT", null, null);

        assertTrue(events.stream().anyMatch(e -> e instanceof AskSkillEffectEvent
                        && ((AskSkillEffectEvent) e).getPlayerId().equals("player-a")),
                "判定生效應詢問傷害來源");

        assertEquals("剛烈判定生效，player-a 選擇棄兩張手牌或受 1 點傷害", events.stream()
                .filter(e -> e instanceof SkillEffectEvent se && se.getSkillName().equals("剛烈"))
                .findFirst().orElseThrow().getMessage(), "事件層 message 帶具體結算文字（issue #235）");

        List<DomainEvent> damageEvents = game.playerUseSkillEffect("player-a", "剛烈", "DAMAGE", null, null);
        assertEquals(3, a.getHP(), "來源選擇受 1 點傷害");
        assertEquals("player-a 受剛烈 1 點傷害（4→3）", damageEvents.stream()
                .filter(e -> e instanceof SkillEffectEvent se && se.getSkillName().equals("剛烈"))
                .findFirst().orElseThrow().getMessage());
    }

    @DisplayName("夏侯惇剛烈、判定生效 → 來源選 DISCARD 棄兩張手牌")
    @Test
    public void gangLieSourceDiscardsTwoCards() {
        Game game = createGame(General.劉備, General.夏侯惇, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new Peach(BH4030)));

        killAndSkip(game, "player-a", "player-b");
        game.getDeck().add(List.of(new Kill(BS9009)));
        game.playerUseSkillEffect("player-b", "剛烈", "ACCEPT", null, null);

        game.playerUseSkillEffect("player-a", "剛烈", "DISCARD",
                List.of(BH3029.getCardId(), BH4030.getCardId()), null);

        assertEquals(0, a.getHandSize());
        assertEquals(4, a.getHP(), "棄牌則不受傷");
        assertTrue(game.getGraveyard().contains(BH3029.getCardId()));
    }

    @DisplayName("剛烈判定紅桃 → 不生效，不問來源")
    @Test
    public void gangLieJudgementHeartFails() {
        Game game = createGame(General.劉備, General.夏侯惇, General.孫權, General.孫權);
        game.getPlayer("player-a").getHand().addCardToHand(new Kill(BS8008));

        killAndSkip(game, "player-a", "player-b");
        // 判定牌：紅心 → 不生效
        game.getDeck().add(List.of(new Peach(BH3029)));
        List<DomainEvent> events = game.playerUseSkillEffect("player-b", "剛烈", "ACCEPT", null, null);

        assertFalse(events.stream().anyMatch(e -> e instanceof AskSkillEffectEvent));
        assertTrue(game.isTopBehaviorEmpty());
        assertEquals(4, game.getPlayer("player-a").getHP());
        // 事件層 message 帶具體結算文字（issue #235：前端顯示的是事件的 message）
        assertEquals("剛烈判定紅桃，未生效", events.stream()
                .filter(e -> e instanceof SkillEffectEvent se && se.getSkillName().equals("剛烈"))
                .findFirst().orElseThrow().getMessage());
    }

    // ===== 天妒 =====

    @DisplayName("郭嘉閃電判定未中 → 天妒收判定牌入手")
    @Test
    public void tianDuTakesJudgementCard() {
        Game game = createGame(General.郭嘉, General.劉備, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Lightning lightning = new Lightning(SSA014);
        a.addDelayScrollCard(lightning);

        // 判定牌紅心 → 閃電不中 → 天妒收牌
        game.getDeck().add(List.of(new Peach(BH3029)));
        game.handleLightningJudgement(lightning, a);

        assertTrue(a.getHand().getCards().stream().anyMatch(c -> c.getId().equals(BH3029.getCardId())),
                "天妒：判定牌應入手");
        assertFalse(game.getGraveyard().contains(BH3029.getCardId()));
    }

    // ===== 洛神 =====

    @DisplayName("甄姬回合開始先詢問洛神；ACCEPT → 黑色判定牌全收，紅色停")
    @Test
    public void luoShenCollectsBlackCardsUntilRed() {
        Game game = createGame(General.甄姬, General.劉備, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        // Stack：後 add 的先抽 → 抽序 = BS9009(黑) → BS8008(黑) → BH3029(紅停) → 摸 2 = BH4030、BH2028
        // 摸牌也疊死：initDeck 牌堆裡有另一張同 id 的 BH3029，隨機摸到會誤中
        // 「紅色判定牌不收」斷言（CI 機率性 flake）
        game.getDeck().add(List.of(new Dodge(BH2028), new Peach(BH4030),
                new Peach(BH3029), new Kill(BS8008), new Kill(BS9009)));

        List<DomainEvent> askEvents = game.playerTakeTurnStartInJudgement(a);

        assertTrue(askEvents.stream().anyMatch(e -> e instanceof AskSkillEffectEvent
                        && ((AskSkillEffectEvent) e).getSkillName().equals("洛神")
                        && ((AskSkillEffectEvent) e).getPlayerId().equals("player-a")),
                "回合開始先詢問洛神，不自動判定");
        assertFalse(a.getHand().getCards().stream().anyMatch(c -> c.getId().equals(BS9009.getCardId())),
                "詢問前不收牌");

        List<DomainEvent> events = game.playerUseSkillEffect("player-a", "洛神", "ACCEPT", null, null);

        assertTrue(a.getHand().getCards().stream().anyMatch(c -> c.getId().equals(BS9009.getCardId())));
        assertTrue(a.getHand().getCards().stream().anyMatch(c -> c.getId().equals(BS8008.getCardId())));
        assertFalse(a.getHand().getCards().stream().anyMatch(c -> c.getId().equals(BH3029.getCardId())),
                "紅色判定牌不收");
        long luoShenEvents = events.stream().filter(e -> e instanceof SkillEffectEvent
                && ((SkillEffectEvent) e).getSkillName().equals("洛神")).count();
        assertEquals(3, luoShenEvents, "兩黑一紅共三次判定事件（ACCEPT 後不逐輪詢問）");
        assertTrue(game.isTopBehaviorEmpty(), "resolve 後 stack 清空");
    }

    @DisplayName("洛神 SKIP → 不判定，直接進入摸牌流程")
    @Test
    public void luoShenSkipProceedsToDraw() {
        Game game = createGame(General.甄姬, General.劉備, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        int handBefore = a.getHandSize();
        game.playerTakeTurnStartInJudgement(a);

        List<DomainEvent> events = game.playerUseSkillEffect("player-a", "洛神", "SKIP", null, null);

        assertTrue(events.stream().anyMatch(e -> e instanceof SkillEffectEvent
                        && ((SkillEffectEvent) e).getSkillName().equals("洛神")
                        && !((SkillEffectEvent) e).isAccepted()),
                "SKIP 廣播放棄事件");
        assertEquals(handBefore + 2, a.getHandSize(), "不判定，直接摸 2");
        assertTrue(game.isTopBehaviorEmpty());
        assertEquals(a, game.getCurrentRound().getActivePlayer());
    }

    @DisplayName("洛神先於延遲錦囊判定：詢問時閃電尚未判定，resolve 後才判閃電")
    @Test
    public void luoShenAskedBeforeLightningJudgement() {
        Game game = createGame(General.甄姬, General.劉備, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.addDelayScrollCard(new Lightning(SSA014));
        // 抽序 = BH3029(洛神紅停) → BH4030(閃電判定紅心，不中轉移) → 摸 2 = BD2093 + BD3094
        game.getDeck().add(List.of(new Dodge(BD3094), new Dodge(BD2093), new Peach(BH4030), new Peach(BH3029)));

        game.playerTakeTurnStartInJudgement(a);

        assertTrue(a.getDelayScrollCards().stream().anyMatch(c -> c.getId().equals(SSA014.getCardId())),
                "洛神詢問期間閃電尚未判定");

        game.playerUseSkillEffect("player-a", "洛神", "ACCEPT", null, null);

        assertFalse(a.getDelayScrollCards().stream().anyMatch(c -> c.getId().equals(SSA014.getCardId())),
                "洛神結算後才判閃電（紅心不中 → 移出 A 判定區）");
        assertTrue(b.getDelayScrollCards().stream().anyMatch(c -> c.getId().equals(SSA014.getCardId())),
                "閃電不中轉移給下家");
        assertEquals(4, a.getHP(), "閃電未命中");
    }

    @DisplayName("非甄姬回合開始 → 洛神不觸發")
    @Test
    public void luoShenNotTriggeredForOthers() {
        Game game = createGame(General.劉備, General.劉備, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");

        List<DomainEvent> events = game.playerTakeTurnStartInJudgement(a);

        assertFalse(events.stream().anyMatch(e -> e instanceof SkillEffectEvent
                && ((SkillEffectEvent) e).getSkillName().equals("洛神")));
        assertFalse(events.stream().anyMatch(e -> e instanceof AskSkillEffectEvent
                && ((AskSkillEffectEvent) e).getSkillName().equals("洛神")));
    }

    // ===== 鐵騎 =====

    @DisplayName("馬超出殺、鐵騎判定非紅桃 → 目標不能閃，直接扣血")
    @Test
    public void tieQiSuccessSkipsDodge() {
        Game game = createGame(General.馬超, General.劉備, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(new Kill(BS8008));
        b.getHand().addCardToHand(new Dodge(BH2028)); // 有閃也沒用

        // 判定牌：黑桃 → 鐵騎生效
        game.getDeck().add(List.of(new Kill(BS9009)));
        List<DomainEvent> events = game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");

        assertFalse(events.stream().anyMatch(e -> e instanceof AskDodgeEvent),
                "鐵騎生效不問閃");
        assertEquals(3, b.getHP(), "直接扣血");
        assertEquals(1, b.getHandSize(), "閃未被消耗");
        // 使用者回報：訊息須帶判定牌與結果（原本只有「鐵騎：xxx 發動」）
        SkillEffectEvent tieQi = events.stream()
                .filter(e -> e instanceof SkillEffectEvent).map(e -> (SkillEffectEvent) e)
                .filter(e -> e.getSkillName().equals("鐵騎")).findFirst().orElseThrow();
        assertTrue(tieQi.getMessage().contains("鐵騎判定") && tieQi.getMessage().contains("黑桃9"),
                "訊息應含判定牌：" + tieQi.getMessage());
        assertTrue(tieQi.getMessage().contains("生效"), tieQi.getMessage());
    }

    @DisplayName("馬超出殺、鐵騎判定紅桃 → 照常問閃")
    @Test
    public void tieQiHeartFailsAsksDodge() {
        Game game = createGame(General.馬超, General.劉備, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(new Kill(BS8008));

        // 判定牌：紅心 → 鐵騎不生效
        game.getDeck().add(List.of(new Peach(BH3029)));
        List<DomainEvent> events = game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");

        assertTrue(events.stream().anyMatch(e -> e instanceof AskDodgeEvent));
        assertEquals(4, game.getPlayer("player-b").getHP());
        SkillEffectEvent tieQi = events.stream()
                .filter(e -> e instanceof SkillEffectEvent).map(e -> (SkillEffectEvent) e)
                .filter(e -> e.getSkillName().equals("鐵騎")).findFirst().orElseThrow();
        assertTrue(tieQi.getMessage().contains("鐵騎判定") && tieQi.getMessage().contains("未生效"),
                "訊息應含判定結果：" + tieQi.getMessage());
    }

    // ===== 梟姬 =====

    @DisplayName("孫尚香被拆裝備 → 梟姬摸兩張")
    @Test
    public void xiaoJiDrawsTwoAfterEquipmentDismantled() {
        Game game = createGame(General.劉備, General.孫尚香, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(new Dismantle(SS3003));
        b.getEquipment().setWeapon(new RepeatingCrossbowCard(EH5031));

        game.playerPlayCard("player-a", SS3003.getCardId(), "player-b", "active");
        game.useDismantleEffect("player-a", "player-b", EH5031.getCardId(), null);

        assertEquals(2, b.getHandSize(), "梟姬：失去裝備摸兩張");
        assertFalse(b.getEquipment().hasAnyEquipment());
    }

    @DisplayName("孫尚香被順走裝備 → 梟姬摸兩張")
    @Test
    public void xiaoJiDrawsTwoAfterEquipmentSnatched() {
        Game game = createGame(General.劉備, General.孫尚香, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(new com.gaas.threeKingdoms.handcard.scrollcard.Snatch(SS3016));
        b.getEquipment().setWeapon(new RepeatingCrossbowCard(EH5031));

        game.playerPlayCard("player-a", SS3016.getCardId(), "player-b", "active");
        game.useSnatchEffect("player-a", "player-b", EH5031.getCardId(), null);

        assertEquals(2, b.getHandSize(), "梟姬：失去裝備摸兩張");
    }

    // ===== 梟姬：主動換裝（使用者回報「孫尚香 貫石斧替換武器後梟姬未生效」）=====

    /** 孫尚香當回合主，手上一張新裝備；回傳出牌事件。 */
    private List<DomainEvent> sunShangXiangEquips(Game game, EquipmentCard newEquipment) {
        game.getPlayer("player-a").getHand().addCardToHand(newEquipment);
        return game.playerPlayCard("player-a", newEquipment.getId(), "player-a", "active");
    }

    @DisplayName("孫尚香換武器蓋掉舊武器 → 梟姬摸兩張、舊武器進棄牌堆")
    @Test
    public void xiaoJiDrawsTwoAfterReplacingOwnWeapon() {
        Game game = createGame(General.孫尚香, General.劉備, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getEquipment().setWeapon(new RepeatingCrossbowCard(ECA066));

        sunShangXiangEquips(game, new StonePiercingAxeCard(ED5083));

        assertEquals(2, a.getHandSize(), "梟姬：主動換裝失去舊武器也要摸兩張");
        assertEquals(ED5083.getCardId(), a.getEquipment().getWeapon().getId(), "裝備區應為新武器");
        assertTrue(game.getGraveyard().contains(ECA066.getCardId()), "被蓋掉的舊武器要進棄牌堆，不能人間蒸發");
        assertFalse(game.getGraveyard().contains(ED5083.getCardId()), "新武器在裝備區，不該同時在棄牌堆");
    }

    @DisplayName("孫尚香裝備區原本沒武器 → 沒有失去裝備，梟姬不觸發")
    @Test
    public void xiaoJiDoesNotTriggerWhenNoOriginEquipment() {
        Game game = createGame(General.孫尚香, General.劉備, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");

        sunShangXiangEquips(game, new StonePiercingAxeCard(ED5083));

        assertEquals(0, a.getHandSize(), "空欄位裝新武器沒有「失去」，不可摸牌");
        assertEquals(ED5083.getCardId(), a.getEquipment().getWeapon().getId());
    }

    @DisplayName("孫尚香換防具 / +1 馬 / -1 馬 → 各摸兩張、舊裝備進棄牌堆")
    @Test
    public void xiaoJiDrawsTwoAfterReplacingArmorAndMounts() {
        // 防具
        Game armorGame = createGame(General.孫尚香, General.劉備, General.孫權, General.孫權);
        Player armorOwner = armorGame.getPlayer("player-a");
        armorOwner.getEquipment().setArmor(new EightDiagramTactic(ES2015));
        sunShangXiangEquips(armorGame, new EightDiagramTactic(EC2067));
        assertEquals(2, armorOwner.getHandSize(), "梟姬：換防具摸兩張");
        assertEquals(EC2067.getCardId(), armorOwner.getEquipment().getArmor().getId());
        assertTrue(armorGame.getGraveyard().contains(ES2015.getCardId()));

        // +1 馬
        Game plusGame = createGame(General.孫尚香, General.劉備, General.孫權, General.孫權);
        Player plusOwner = plusGame.getPlayer("player-a");
        plusOwner.getEquipment().setPlusOne(new ShadowHorse(ES5018));
        sunShangXiangEquips(plusGame, new HexMark(EC5070));
        assertEquals(2, plusOwner.getHandSize(), "梟姬：換 +1 馬摸兩張");
        assertEquals(EC5070.getCardId(), plusOwner.getEquipment().getPlusOne().getId());
        assertTrue(plusGame.getGraveyard().contains(ES5018.getCardId()));

        // -1 馬
        Game minusGame = createGame(General.孫尚香, General.劉備, General.孫權, General.孫權);
        Player minusOwner = minusGame.getPlayer("player-a");
        minusOwner.getEquipment().setMinusOne(new RedRabbitHorse(EH5044));
        sunShangXiangEquips(minusGame, new VioletStallion(EDK104));
        assertEquals(2, minusOwner.getHandSize(), "梟姬：換 -1 馬摸兩張");
        assertEquals(EDK104.getCardId(), minusOwner.getEquipment().getMinusOne().getId());
        assertTrue(minusGame.getGraveyard().contains(EH5044.getCardId()));
    }

    @DisplayName("新裝上的 +1 馬留在裝備區，不可同時進棄牌堆（PlusMountsBehavior 原本誤用 playerPlayCard）")
    @Test
    public void newlyEquippedPlusMountsDoesNotGoToGraveyard() {
        Game game = createGame(General.劉備, General.孫權, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");

        a.getHand().addCardToHand(new HexMark(EC5070));
        game.playerPlayCard("player-a", EC5070.getCardId(), "player-a", "active");

        assertEquals(EC5070.getCardId(), a.getEquipment().getPlusOne().getId());
        assertFalse(game.getGraveyard().contains(EC5070.getCardId()),
                "裝備中的牌若同時在棄牌堆，洗牌後會憑空多一張");
    }

    @DisplayName("非孫尚香換裝 → 不摸牌，但舊裝備仍要進棄牌堆")
    @Test
    public void nonSunShangXiangReplacingWeaponStillDiscardsOldEquipment() {
        Game game = createGame(General.劉備, General.孫尚香, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getEquipment().setWeapon(new RepeatingCrossbowCard(ECA066));

        game.getPlayer("player-a").getHand().addCardToHand(new StonePiercingAxeCard(ED5083));
        game.playerPlayCard("player-a", ED5083.getCardId(), "player-a", "active");

        assertEquals(0, a.getHandSize(), "沒有梟姬不該摸牌");
        assertTrue(game.getGraveyard().contains(ECA066.getCardId()), "舊裝備進棄牌堆與武將技無關");
    }

    @DisplayName("換武器時 deprecatedCardId 是舊武器，不是防具（EquipWeaponBehavior 誤讀 getArmor()）")
    @Test
    public void playEquipmentCardEventReportsReplacedWeaponNotArmor() {
        Game game = createGame(General.孫尚香, General.劉備, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getEquipment().setArmor(new EightDiagramTactic(ES2015));
        a.getEquipment().setWeapon(new RepeatingCrossbowCard(ECA066));

        List<DomainEvent> events = sunShangXiangEquips(game, new StonePiercingAxeCard(ED5083));

        PlayEquipmentCardEvent equipEvent = events.stream()
                .filter(e -> e instanceof PlayEquipmentCardEvent).map(e -> (PlayEquipmentCardEvent) e)
                .findFirst().orElseThrow();
        assertEquals(ECA066.getCardId(), equipEvent.getDeprecatedCardId(),
                "被換掉的是舊武器，防具沒有離開裝備區");
        assertEquals(ES2015.getCardId(), a.getEquipment().getArmor().getId(), "防具不該被動到");
        assertFalse(game.getGraveyard().contains(ES2015.getCardId()), "防具沒有失去，不該進棄牌堆");
    }

    @DisplayName("梟姬 per-card：一次失去 N 張裝備 → 摸 2N")
    @Test
    public void xiaoJiDrawsPerLostEquipmentCard() {
        Game game = createGame(General.孫尚香, General.劉備, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");

        SkillEngine.afterLoseEquipment(game, a, 2);

        assertEquals(4, a.getHandSize(), "官方梟姬每張各觸發一次 → 失去 2 張摸 4 張");
    }

    @DisplayName("梟姬 helper：沒失去裝備或非孫尚香 → 無事件、不摸牌")
    @Test
    public void afterLoseEquipmentIsNoOpWithoutLossOrSkill() {
        Game game = createGame(General.孫尚香, General.劉備, General.孫權, General.孫權);
        Player sunShangXiang = game.getPlayer("player-a");
        Player liuBei = game.getPlayer("player-b");

        assertTrue(SkillEngine.afterLoseEquipment(game, sunShangXiang, 0).isEmpty(), "失去 0 張不觸發");
        assertEquals(0, sunShangXiang.getHandSize());
        assertTrue(SkillEngine.afterLoseEquipment(game, liuBei, 1).isEmpty(), "無梟姬不觸發");
        assertEquals(0, liuBei.getHandSize());
    }
}
