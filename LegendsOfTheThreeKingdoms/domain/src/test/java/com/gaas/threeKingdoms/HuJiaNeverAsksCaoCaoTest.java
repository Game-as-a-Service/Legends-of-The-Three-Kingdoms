package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.behavior.behavior.WaitingHuJiaResponseBehavior;
import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.events.AskDodgeEvent;
import com.gaas.threeKingdoms.events.AskHuJiaEffectEvent;
import com.gaas.threeKingdoms.events.AskSkillEffectEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.scrollcard.ArrowBarrage;
import com.gaas.threeKingdoms.player.*;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.rolecard.RoleCard;
import com.gaas.threeKingdoms.round.Round;
import com.gaas.threeKingdoms.skill.wei.HuJiaSkill;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 使用者提問：「有沒有路徑，曹操的護駕會問自己要不要出閃？」
 *
 * <p>本測試把「代閃輪詢」的每條路徑跑過一遍，斷言 {@link AskHuJiaEffectEvent}（＝問某人要不要
 * 代主公出閃）的被詢問者永遠不是曹操本人，名單也不會重複。護駕唯一會問到曹操的是另一種事件：
 * 發動詢問 {@link AskSkillEffectEvent}(skillName=護駕)（issue #217 刻意加的「要不要發動護駕」），
 * 以及全部魏將拒絕後回到曹操自己出閃的 {@link AskDodgeEvent} —— 這兩者都不是「代閃」。
 *
 * <p>5 人座位：a(甘寧,反賊,攻擊者) → b(曹操,主公) → c(夏侯惇,魏) → d(張遼,魏) → e(許褚,魏)
 * 故正確的代閃順序是 c → d → e。
 */
public class HuJiaNeverAsksCaoCaoTest {

    private static final String CAO_CAO = "player-b";

    private Game fivePlayerGameWithThreeWeiHelpers() {
        Game game = new Game();
        game.initDeck();
        Player a = build("player-a", General.甘寧, Role.REBEL);
        Player b = build(CAO_CAO, General.曹操, Role.MONARCH);
        Player c = build("player-c", General.夏侯惇, Role.MINISTER);
        Player d = build("player-d", General.張遼, Role.MINISTER);
        Player e = build("player-e", General.許褚, Role.MINISTER);
        game.setPlayers(Arrays.asList(a, b, c, d, e));
        game.setCurrentRound(new Round(a));
        game.enterPhase(new Normal(game));
        a.getHand().addCardToHand(new Kill(BS8008));
        return game;
    }

    private Player build(String id, General general, Role role) {
        return PlayerBuilder.construct().withId(id)
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(general))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(role))
                .withHand(new Hand()).withEquipment(new Equipment()).build();
    }

    private List<String> huJiaAskedPlayers(List<DomainEvent> events) {
        return events.stream().filter(e -> e instanceof AskHuJiaEffectEvent)
                .map(e -> ((AskHuJiaEffectEvent) e).getPlayerId()).toList();
    }

    /**
     * 跑完整條代閃輪詢（每個魏將都 DECLINE）；被問到代閃的玩家累加到 askedOut，
     * 回傳最後一次 DECLINE 的事件（該處會 fallback 回曹操自己出閃）。
     */
    private List<DomainEvent> declineThroughWholeChain(Game game, List<String> askedOut) {
        List<DomainEvent> events = List.of();
        int guard = 0;
        while (game.peekTopBehavior() instanceof WaitingHuJiaResponseBehavior waiting) {
            String current = waiting.getCurrentWei();
            assertNotEquals(CAO_CAO, current, "代閃輪詢的當前被問者不可以是曹操自己");
            events = game.playerUseHuJiaEffect(current, AskHuJiaEffectEvent.Choice.DECLINE, null);
            askedOut.addAll(huJiaAskedPlayers(events));
            if (++guard > 10) {
                fail("代閃輪詢沒有收斂（可能有人被重複詢問）：" + askedOut);
            }
        }
        return events;
    }

    private void assertNeverAsksCaoCao(List<String> askedPlayers) {
        assertFalse(askedPlayers.contains(CAO_CAO),
                "曹操不可以被問「要不要代主公出閃」，實際名單：" + askedPlayers);
        assertEquals(new HashSet<>(askedPlayers).size(), askedPlayers.size(),
                "同一個魏將不該被問兩次，實際名單：" + askedPlayers);
    }

    @DisplayName("普通殺：代閃只問 c → d → e，不含曹操；全部拒絕後才回到曹操自己出閃")
    @Test
    public void normalKill_huJiaPollingNeverIncludesCaoCao() {
        Game game = fivePlayerGameWithThreeWeiHelpers();

        List<DomainEvent> killEvents = game.playerPlayCard("player-a", BS8008.getCardId(), CAO_CAO, "active");
        // 這一則是「是否發動護駕」，被問的人本來就是曹操 —— 不是代閃詢問
        AskSkillEffectEvent activationAsk = killEvents.stream()
                .filter(e -> e instanceof AskSkillEffectEvent).map(AskSkillEffectEvent.class::cast)
                .findFirst().orElseThrow();
        assertEquals("護駕", activationAsk.getSkillName());
        assertEquals(CAO_CAO, activationAsk.getPlayerId());
        assertTrue(huJiaAskedPlayers(killEvents).isEmpty(), "曹操還沒 ACCEPT，不該有代閃詢問");

        List<DomainEvent> acceptEvents = game.playerUseSkillEffect(CAO_CAO, "護駕", "ACCEPT", null, null);
        WaitingHuJiaResponseBehavior waiting = (WaitingHuJiaResponseBehavior) game.peekTopBehavior();
        assertEquals(List.of("player-c", "player-d", "player-e"), waiting.getWeiOrder(),
                "代閃名單＝座位順序的其他存活魏將，不含曹操");

        List<String> asked = new ArrayList<>(huJiaAskedPlayers(acceptEvents));
        List<DomainEvent> lastDecline = declineThroughWholeChain(game, asked);
        assertNeverAsksCaoCao(asked);
        assertEquals(List.of("player-c", "player-d", "player-e"), asked, "代閃只問這三位、各一次");

        // 全部拒絕 → 回到曹操自己出閃（AskDodgeEvent，不是護駕代閃）
        assertTrue(lastDecline.stream().anyMatch(e -> e instanceof AskDodgeEvent ask
                && ask.getPlayerId().equals(CAO_CAO)), "全部拒絕後曹操自己出閃");
        assertTrue(huJiaAskedPlayers(lastDecline).isEmpty(), "不會再多問一輪代閃");
    }

    @DisplayName("有玩家死亡（座位表比 players 短）：代閃名單仍不含曹操、也不重複")
    @Test
    public void afterSomeoneDies_huJiaPollingNeverIncludesCaoCao() {
        Game game = fivePlayerGameWithThreeWeiHelpers();
        // d 死亡結算 → 離開座位表，但仍留在 game.players（護駕的迴圈上限用的是 players.size()）
        game.removeDyingPlayer(game.getPlayer("player-d"));

        game.playerPlayCard("player-a", BS8008.getCardId(), CAO_CAO, "active");
        List<DomainEvent> acceptEvents = game.playerUseSkillEffect(CAO_CAO, "護駕", "ACCEPT", null, null);

        WaitingHuJiaResponseBehavior waiting = (WaitingHuJiaResponseBehavior) game.peekTopBehavior();
        assertEquals(List.of("player-c", "player-e"), waiting.getWeiOrder(), "死亡的 d 被跳過");

        List<String> asked = new ArrayList<>(huJiaAskedPlayers(acceptEvents));
        declineThroughWholeChain(game, asked);
        assertNeverAsksCaoCao(asked);
        assertEquals(List.of("player-c", "player-e"), asked);
    }

    @DisplayName("萬箭齊發輪到曹操：代閃名單仍不含曹操")
    @Test
    public void arrowBarrage_huJiaPollingNeverIncludesCaoCao() {
        Game game = fivePlayerGameWithThreeWeiHelpers();
        game.getPlayer("player-a").getHand().addCardToHand(new ArrowBarrage(SS7007));

        game.playerPlayCard("player-a", SS7007.getCardId(), "player-a", "active");
        List<DomainEvent> acceptEvents = game.playerUseSkillEffect(CAO_CAO, "護駕", "ACCEPT", null, null);

        WaitingHuJiaResponseBehavior waiting = (WaitingHuJiaResponseBehavior) game.peekTopBehavior();
        assertFalse(waiting.getWeiOrder().contains(CAO_CAO));
        assertNeverAsksCaoCao(huJiaAskedPlayers(acceptEvents));
    }

    @DisplayName("曹操是場上唯一魏勢力：護駕不觸發，只會叫曹操自己出閃（不會問自己代閃）")
    @Test
    public void caoCaoIsTheOnlyWei_noHuJiaAskAtAll() {
        Game game = new Game();
        game.initDeck();
        Player a = build("player-a", General.甘寧, Role.REBEL);
        Player b = build(CAO_CAO, General.曹操, Role.MONARCH);
        Player c = build("player-c", General.趙雲, Role.MINISTER);
        Player d = build("player-d", General.孫權, Role.MINISTER);
        game.setPlayers(Arrays.asList(a, b, c, d));
        game.setCurrentRound(new Round(a));
        game.enterPhase(new Normal(game));
        a.getHand().addCardToHand(new Kill(BS8008));

        List<DomainEvent> events = game.playerPlayCard("player-a", BS8008.getCardId(), CAO_CAO, "active");

        assertTrue(huJiaAskedPlayers(events).isEmpty(), "沒有其他魏將，不會有代閃詢問");
        assertFalse(events.stream().anyMatch(e -> e instanceof AskSkillEffectEvent),
                "連發動詢問都不該出現");
        assertTrue(events.stream().anyMatch(e -> e instanceof AskDodgeEvent ask
                && ask.getPlayerId().equals(CAO_CAO)), "直接叫曹操自己出閃");
    }

    @DisplayName("刻意把座位表與 players 弄成不一致（曹操不在座位表）：代閃名單仍不含曹操")
    @Test
    public void evenWhenSeatingChartDivergesFromPlayers_caoCaoIsNeverAHelper() {
        Game game = fivePlayerGameWithThreeWeiHelpers();
        Player caoCao = game.getPlayer(CAO_CAO);
        // 護駕的名單是沿著座位表走、遇到曹操就停；這裡讓它遇不到曹操（防禦性檢查：
        // 正常牌局不會發生，只有死亡結算才會離開座位表，而死掉的曹操不會被要求出閃）
        game.getSeatingChart().getPlayers().remove(caoCao);

        List<DomainEvent> events = new HuJiaSkill().beforeAskDodge(game, caoCao, null).orElseThrow();
        assertTrue(events.stream().anyMatch(e -> e instanceof AskSkillEffectEvent ask
                && ask.getSkillName().equals("護駕")), "仍是先問曹操是否發動");

        List<DomainEvent> acceptEvents = game.playerUseSkillEffect(CAO_CAO, "護駕", "ACCEPT", null, null);
        WaitingHuJiaResponseBehavior waiting = (WaitingHuJiaResponseBehavior) game.peekTopBehavior();
        assertFalse(waiting.getWeiOrder().contains(CAO_CAO),
                "即使座位表裡沒有曹操，代閃名單也不能把曹操算進去：" + waiting.getWeiOrder());
        assertNeverAsksCaoCao(huJiaAskedPlayers(acceptEvents));
    }
}
