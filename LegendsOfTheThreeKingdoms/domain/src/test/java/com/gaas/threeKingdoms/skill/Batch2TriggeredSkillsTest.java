package com.gaas.threeKingdoms.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.AskDodgeEvent;
import com.gaas.threeKingdoms.events.AskSkillEffectEvent;
import com.gaas.threeKingdoms.events.AskStonePiercingAxeEffectEvent;
import com.gaas.threeKingdoms.events.DiscardEquipmentEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.DrawCardEvent;
import com.gaas.threeKingdoms.events.GameStatusEvent;
import com.gaas.threeKingdoms.events.PlayEquipmentCardEvent;
import com.gaas.threeKingdoms.events.PlayerDamagedEvent;
import com.gaas.threeKingdoms.events.PlayerEvent;
import com.gaas.threeKingdoms.events.SkillEffectEvent;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.EquipmentPlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.EquipmentCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.EightDiagramTactic;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.HexMark;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.RedRabbitHorse;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.ShadowHorse;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.VioletStallion;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.QilinBowCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.RepeatingCrossbowCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.StonePiercingAxeCard;
import com.gaas.threeKingdoms.handcard.scrollcard.BorrowedSword;
import com.gaas.threeKingdoms.handcard.scrollcard.Dismantle;
import com.gaas.threeKingdoms.handcard.scrollcard.Lightning;
import com.gaas.threeKingdoms.player.BloodCard;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.skill.registry.SkillEngine;
import com.gaas.threeKingdoms.skill.wu.ZhiHengSkill;
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

    // ===== 梟姬 × 反饋：司馬懿取走孫尚香裝備區的牌，孫尚香也算「失去裝備」 =====

    @DisplayName("孫尚香殺司馬懿、被反饋取走唯一裝備 → 梟姬摸兩張")
    @Test
    public void xiaoJiDrawsTwoWhenFanKuiTakesHerEquipment() {
        Game game = createGame(General.孫尚香, General.司馬懿, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(new Kill(BS8008)); // 出殺後空手，反饋只能取裝備
        a.getEquipment().setArmor(new EightDiagramTactic(ES2015));

        killAndSkip(game, "player-a", "player-b");
        game.playerUseSkillEffect("player-b", "反饋", "ACCEPT", null, null);

        assertEquals(2, a.getHandSize(), "梟姬：裝備被反饋取走也是失去裝備，要摸兩張");
        assertFalse(a.getEquipment().hasAnyEquipment(), "裝備已離開孫尚香的裝備區");
        assertTrue(b.getHand().getCards().stream().anyMatch(c -> c.getId().equals(ES2015.getCardId())),
                "反饋取走的裝備進司馬懿手牌，不進棄牌堆");
        assertFalse(game.getGraveyard().contains(ES2015.getCardId()));
    }

    @DisplayName("反饋指定取孫尚香的裝備（來源還有手牌）→ 梟姬摸兩張")
    @Test
    public void xiaoJiDrawsTwoWhenFanKuiPicksEquipmentExplicitly() {
        Game game = createGame(General.孫尚香, General.司馬懿, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029)));
        a.getEquipment().setArmor(new EightDiagramTactic(ES2015));

        killAndSkip(game, "player-a", "player-b");
        game.playerUseSkillEffect("player-b", "反饋", "ACCEPT",
                List.of(ES2015.getCardId()), null);

        assertEquals(3, a.getHandSize(), "原本剩的桃 + 梟姬摸的兩張");
        assertNull(a.getEquipment().getArmor());
    }

    @DisplayName("反饋取的是手牌 → 沒有失去裝備，梟姬不觸發")
    @Test
    public void xiaoJiNotTriggeredWhenFanKuiTakesHandCard() {
        Game game = createGame(General.孫尚香, General.司馬懿, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029)));
        a.getEquipment().setArmor(new EightDiagramTactic(ES2015));

        killAndSkip(game, "player-a", "player-b");
        game.playerUseSkillEffect("player-b", "反饋", "ACCEPT", null, null); // 來源有手牌 → 取手牌

        assertEquals(0, a.getHandSize(), "取手牌不是失去裝備，不可摸牌");
        assertEquals(ES2015.getCardId(), a.getEquipment().getArmor().getId(), "裝備沒被動到");
    }

    // ===== 梟姬 × 麒麟弓：被棄掉的馬也是失去裝備，且摸牌要排在傷害之前 =====

    /** A 裝麒麟弓殺 B、B 不出閃、A 發動麒麟弓效果；回傳發動效果的事件。 */
    private List<DomainEvent> qilinBowKill(Game game) {
        Player a = game.getPlayer("player-a");
        a.getEquipment().setWeapon(new QilinBowCard(EH5031));
        a.getHand().addCardToHand(new Kill(BS8008));
        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");
        game.playerPlayCard("player-b", "", "player-a", "skip");
        return game.playerUseEquipment("player-a", EH5031.getCardId(), "player-b", EquipmentPlayType.ACTIVE);
    }

    @DisplayName("孫尚香只有一匹馬被麒麟弓棄掉 → 梟姬摸兩張，且摸牌事件排在傷害事件之前")
    @Test
    public void xiaoJiDrawsTwoWhenQilinBowRemovesHerOnlyMount() {
        Game game = createGame(General.劉備, General.孫尚香, General.孫權, General.孫權);
        Player b = game.getPlayer("player-b");
        b.getEquipment().setPlusOne(new ShadowHorse(ES5018));

        List<DomainEvent> events = qilinBowKill(game);

        assertEquals(2, b.getHandSize(), "梟姬：馬被麒麟弓棄掉也要摸兩張");
        assertNull(b.getEquipment().getPlusOne());
        assertEquals(3, b.getHP(), "麒麟弓棄馬不影響殺的傷害");

        int drawIndex = indexOfFirst(events, DrawCardEvent.class);
        int damagedIndex = indexOfFirst(events, PlayerDamagedEvent.class);
        assertTrue(drawIndex >= 0, "應有梟姬的摸牌事件");
        assertTrue(damagedIndex >= 0, "應有殺的傷害事件");
        assertTrue(drawIndex < damagedIndex,
                "官方順序是先失去裝備（摸牌）再受到傷害，前端才不會先看到扣血再看到摸牌");

        GameStatusEvent lastStatus = lastGameStatusEvent(events);
        assertEquals(2, seatOf(lastStatus, "player-b").getHand().getSize(),
                "最後的 GameStatusEvent 要含摸到的兩張（快照在摸牌之後才取）");
    }

    @DisplayName("孫尚香有兩匹馬、攻擊方選一匹棄掉 → 梟姬摸兩張")
    @Test
    public void xiaoJiDrawsTwoWhenAttackerChoosesWhichMountToDiscard() {
        Game game = createGame(General.劉備, General.孫尚香, General.孫權, General.孫權);
        Player b = game.getPlayer("player-b");
        b.getEquipment().setPlusOne(new ShadowHorse(ES5018));
        b.getEquipment().setMinusOne(new RedRabbitHorse(EH5044));

        List<DomainEvent> askEvents = qilinBowKill(game);
        assertEquals(0, b.getHandSize(), "還沒選馬就不算失去裝備");

        List<DomainEvent> chooseEvents = game.playerChooseHorseForQilinBow("player-a", ES5018.getCardId());

        assertEquals(2, b.getHandSize(), "梟姬：被選中的馬離開裝備區 → 摸兩張");
        assertNull(b.getEquipment().getPlusOne());
        assertNotNull(b.getEquipment().getMinusOne(), "沒被選中的馬留著，只算失去一張");
        assertTrue(indexOfFirst(chooseEvents, DrawCardEvent.class) >= 0, "選馬後要推摸牌事件");
    }

    @DisplayName("非孫尚香被麒麟弓棄馬 → 不摸牌")
    @Test
    public void nonSunShangXiangLosingMountToQilinBowDrawsNothing() {
        Game game = createGame(General.劉備, General.關羽, General.孫權, General.孫權);
        Player b = game.getPlayer("player-b");
        b.getEquipment().setPlusOne(new ShadowHorse(ES5018));

        qilinBowKill(game);

        assertEquals(0, b.getHandSize(), "沒有梟姬不該摸牌");
        assertNull(b.getEquipment().getPlusOne());
        assertEquals(3, b.getHP());
    }

    // ===== 梟姬 × 借刀殺人：武器被奪走也是失去裝備 =====

    @DisplayName("孫尚香有殺卻選擇不出 → 武器被借刀殺人奪走，梟姬摸兩張")
    @Test
    public void xiaoJiDrawsTwoWhenBorrowedSwordUsurpsWeaponAfterSkip() {
        Game game = createGame(General.劉備, General.孫尚香, General.關羽, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(new BorrowedSword(SCK065));
        b.getEquipment().setWeapon(new RepeatingCrossbowCard(ECA066));
        b.getHand().addCardToHand(new Kill(BS8008));

        game.playerPlayCard("player-a", SCK065.getCardId(), "player-b", "active");
        game.useBorrowedSwordEffect("player-a", "player-b", "player-c");
        List<DomainEvent> events = game.playerPlayCard("player-b", "", "player-c", "skip");

        assertEquals(3, b.getHandSize(), "留著沒出的殺 + 梟姬摸的兩張");
        assertNull(b.getEquipmentWeaponCard(), "武器已離開孫尚香的裝備區");
        assertTrue(a.getHand().getCards().stream().anyMatch(c -> c.getId().equals(ECA066.getCardId())),
                "被奪的武器進出借刀殺人者手牌");
        assertTrue(indexOfFirst(events, DrawCardEvent.class) >= 0, "要推梟姬的摸牌事件");
        assertEquals(3, seatOf(lastGameStatusEvent(events), "player-b").getHand().getSize(),
                "最後的 GameStatusEvent 手牌數要含摸到的兩張");
    }

    @DisplayName("孫尚香沒有殺 → 借刀殺人直接奪走武器，梟姬摸兩張")
    @Test
    public void xiaoJiDrawsTwoWhenBorrowedSwordUsurpsWeaponWithoutKill() {
        Game game = createGame(General.劉備, General.孫尚香, General.關羽, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(new BorrowedSword(SCK065));
        b.getEquipment().setWeapon(new RepeatingCrossbowCard(ECA066));

        game.playerPlayCard("player-a", SCK065.getCardId(), "player-b", "active");
        List<DomainEvent> events = game.useBorrowedSwordEffect("player-a", "player-b", "player-c");

        assertEquals(2, b.getHandSize(), "梟姬：武器被直接奪走也要摸兩張");
        assertNull(b.getEquipmentWeaponCard());
        assertTrue(indexOfFirst(events, DrawCardEvent.class) >= 0, "要推梟姬的摸牌事件");
        assertEquals(2, seatOf(lastGameStatusEvent(events), "player-b").getHand().getSize());
    }

    @DisplayName("非孫尚香武器被借刀殺人奪走 → 不摸牌")
    @Test
    public void nonSunShangXiangLosingWeaponToBorrowedSwordDrawsNothing() {
        Game game = createGame(General.劉備, General.關羽, General.張飛, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(new BorrowedSword(SCK065));
        b.getEquipment().setWeapon(new RepeatingCrossbowCard(ECA066));

        game.playerPlayCard("player-a", SCK065.getCardId(), "player-b", "active");
        game.useBorrowedSwordEffect("player-a", "player-b", "player-c");

        assertEquals(0, b.getHandSize(), "沒有梟姬不該摸牌");
        assertNull(b.getEquipmentWeaponCard());
    }

    // ===== 梟姬 × 貫石斧：代價棄到裝備區的牌也算失去裝備 =====

    /** A 裝貫石斧殺 B、B 出閃 → 停在 AskStonePiercingAxeEffectEvent 等 A 選擇。 */
    private void axeKillDodged(Game game) {
        game.getPlayer("player-a").getEquipment().setWeapon(new StonePiercingAxeCard(ED5083));
        game.getPlayer("player-a").getHand().addCardToHand(new Kill(BS8008));
        game.getPlayer("player-b").getHand().addCardToHand(new Dodge(BH2028));
        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");
        game.playerPlayCard("player-b", BH2028.getCardId(), "player-a", "active");
    }

    @DisplayName("孫尚香貫石斧代價棄一手牌一裝備 → 梟姬摸兩張，且摸牌排在傷害之前")
    @Test
    public void xiaoJiDrawsTwoWhenAxeCostDiscardsOneEquipment() {
        Game game = createGame(General.孫尚香, General.劉備, General.關羽, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getEquipment().setMinusOne(new RedRabbitHorse(EH5044));
        a.getHand().addCardToHand(new Peach(BH3029));
        axeKillDodged(game);

        List<DomainEvent> events = game.playerUseStonePiercingAxeEffect("player-a",
                AskStonePiercingAxeEffectEvent.Choice.DISCARD_TWO,
                List.of(BH3029.getCardId(), EH5044.getCardId()));

        assertEquals(2, a.getHandSize(), "殺與桃都出掉了，剩下的是梟姬摸的兩張");
        assertNull(a.getEquipment().getMinusOne(), "當代價的馬已離開裝備區");
        assertTrue(game.getGraveyard().contains(EH5044.getCardId()));
        assertEquals(3, b.getHP(), "貫石斧強制命中，閃無效");

        int drawIndex = indexOfFirst(events, DrawCardEvent.class);
        int damagedIndex = indexOfFirst(events, PlayerDamagedEvent.class);
        assertTrue(drawIndex >= 0, "應有梟姬的摸牌事件");
        assertTrue(drawIndex < damagedIndex, "先付出代價（失去裝備 → 摸牌）再結算強制命中的傷害");
        assertEquals(2, seatOf(lastGameStatusEvent(events), "player-a").getHand().getSize(),
                "最後的 GameStatusEvent 手牌數要含摸到的兩張");
    }

    @DisplayName("孫尚香貫石斧代價棄兩張裝備 → 梟姬 per-card 摸四張")
    @Test
    public void xiaoJiDrawsFourWhenAxeCostDiscardsTwoEquipment() {
        Game game = createGame(General.孫尚香, General.劉備, General.關羽, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getEquipment().setMinusOne(new RedRabbitHorse(EH5044));
        a.getEquipment().setPlusOne(new ShadowHorse(ES5018));
        axeKillDodged(game);

        game.playerUseStonePiercingAxeEffect("player-a",
                AskStonePiercingAxeEffectEvent.Choice.DISCARD_TWO,
                List.of(EH5044.getCardId(), ES5018.getCardId()));

        assertEquals(4, a.getHandSize(), "一次失去 2 張裝備 → 每張各摸 2，共 4 張");
        assertNull(a.getEquipment().getMinusOne());
        assertNull(a.getEquipment().getPlusOne());
        assertNotNull(a.getEquipmentWeaponCard(), "貫石斧本身沒當代價棄掉");
        assertEquals(3, b.getHP());
    }

    @DisplayName("非孫尚香用貫石斧棄裝備當代價 → 不摸牌")
    @Test
    public void nonSunShangXiangAxeCostDrawsNothing() {
        Game game = createGame(General.關羽, General.劉備, General.張飛, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getEquipment().setMinusOne(new RedRabbitHorse(EH5044));
        a.getHand().addCardToHand(new Peach(BH3029));
        axeKillDodged(game);

        game.playerUseStonePiercingAxeEffect("player-a",
                AskStonePiercingAxeEffectEvent.Choice.DISCARD_TWO,
                List.of(BH3029.getCardId(), EH5044.getCardId()));

        assertEquals(0, a.getHandSize(), "沒有梟姬不該摸牌");
        assertNull(a.getEquipment().getMinusOne());
    }

    // ===== 梟姬 × 制衡：棄到裝備區的牌也算失去裝備 =====
    // 制衡是孫權的技、梟姬是孫尚香的，同一牌局不會有玩家同時擁有兩者；
    // 這裡直接呼叫 ZhiHengSkill.activate 驗證 hook 本身（見 ZhiHengSkill 內註解）。

    @DisplayName("制衡棄掉裝備的人若有梟姬 → 制衡摸完再摸 2N")
    @Test
    public void zhiHengDiscardingEquipmentAlsoTriggersXiaoJi() {
        Game game = createGame(General.孫尚香, General.劉備, General.關羽, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(new Peach(BH3029));
        a.getEquipment().setArmor(new EightDiagramTactic(ES2015));

        new ZhiHengSkill().activate(game, a, null,
                List.of(BH3029.getCardId(), ES2015.getCardId()), null);

        assertEquals(4, a.getHandSize(), "制衡棄 2 摸 2 + 梟姬失去 1 張裝備摸 2");
        assertFalse(a.getEquipment().hasAnyEquipment());
    }

    @DisplayName("孫權自己制衡棄裝備 → 只有制衡的等量摸牌")
    @Test
    public void zhiHengWithoutXiaoJiDrawsOnlyReplacement() {
        Game game = createGame(General.孫權, General.劉備, General.關羽, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getEquipment().setArmor(new EightDiagramTactic(ES2015));

        new ZhiHengSkill().activate(game, a, null, List.of(ES2015.getCardId()), null);

        assertEquals(1, a.getHandSize(), "棄 1 摸 1，沒有梟姬不額外摸");
    }

    // ===== 梟姬 × 主公殺忠臣：一次棄光裝備區，per-card 摸 2N =====

    /** A(主公) 殺死 hp=1 的忠臣 B，b→c→d→a 依序不出桃；回傳結算事件。 */
    private List<DomainEvent> monarchKillsMinister(Game game) {
        game.getPlayer("player-b").setBloodCard(new BloodCard(1));
        game.getPlayer("player-a").getHand().addCardToHand(new Kill(BS8008));
        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");
        game.playerPlayCard("player-b", "", "player-a", "skip"); // 不出閃 → 瀕死
        game.playerPlayCard("player-b", "", "player-b", "skip");
        game.playerPlayCard("player-c", "", "player-b", "skip");
        game.playerPlayCard("player-d", "", "player-b", "skip");
        return game.playerPlayCard("player-a", "", "player-b", "skip");
    }

    @DisplayName("孫尚香主公殺死忠臣、棄光兩張裝備 → 梟姬摸四張")
    @Test
    public void xiaoJiDrawsPerEquipmentWhenMonarchDiscardsAllAfterKillingMinister() {
        Game game = createGame(General.孫尚香, General.劉備, General.關羽, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getEquipment().setArmor(new EightDiagramTactic(ES2015));
        a.getEquipment().setMinusOne(new RedRabbitHorse(EH5044));

        List<DomainEvent> events = monarchKillsMinister(game);

        assertFalse(a.getEquipment().hasAnyEquipment(), "主公殺忠臣罰則：手牌與裝備全棄");
        assertEquals(4, a.getHandSize(),
                "梟姬 per-card：罰則棄光 2 張裝備 → 摸 4 張（棄牌罰則已先結算完，摸到的留在手上）");

        DiscardEquipmentEvent discardEquipmentEvent = events.stream()
                .filter(e -> e instanceof DiscardEquipmentEvent).map(e -> (DiscardEquipmentEvent) e)
                .findFirst().orElseThrow();
        assertEquals(2, discardEquipmentEvent.getEquipmentCardIds().size());
        assertTrue(indexOfFirst(events, DrawCardEvent.class)
                        > indexOfFirst(events, DiscardEquipmentEvent.class),
                "摸牌事件要排在棄裝備事件之後（先失去才摸）");
    }

    @DisplayName("非孫尚香主公殺忠臣棄光裝備 → 不摸牌")
    @Test
    public void nonSunShangXiangMonarchDiscardingAllDrawsNothing() {
        Game game = createGame(General.甘寧, General.劉備, General.關羽, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getEquipment().setArmor(new EightDiagramTactic(ES2015));
        a.getEquipment().setMinusOne(new RedRabbitHorse(EH5044));

        monarchKillsMinister(game);

        assertFalse(a.getEquipment().hasAnyEquipment());
        assertEquals(0, a.getHandSize(), "沒有梟姬不該摸牌");
    }

    private static int indexOfFirst(List<DomainEvent> events, Class<? extends DomainEvent> type) {
        for (int i = 0; i < events.size(); i++) {
            if (type.isInstance(events.get(i))) {
                return i;
            }
        }
        return -1;
    }

    private static GameStatusEvent lastGameStatusEvent(List<DomainEvent> events) {
        return events.stream()
                .filter(e -> e instanceof GameStatusEvent).map(e -> (GameStatusEvent) e)
                .reduce((first, second) -> second)
                .orElseThrow();
    }

    private static PlayerEvent seatOf(GameStatusEvent event, String playerId) {
        return event.getSeats().stream()
                .filter(seat -> seat.getId().equals(playerId))
                .findFirst().orElseThrow();
    }
}
