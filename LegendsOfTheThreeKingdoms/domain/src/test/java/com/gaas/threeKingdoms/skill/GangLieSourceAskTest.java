package com.gaas.threeKingdoms.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.events.AskSkillEffectEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.SkillEffectEvent;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.scrollcard.Dismantle;
import com.gaas.threeKingdoms.handcard.scrollcard.Duel;
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
 * 使用者回報（決鬥 → 剛烈 → 鬼才 的實戰紀錄）：
 * <ol>
 *   <li>剛烈判定結果不展示 —— 看不出是哪張判定牌造成生效／未生效。</li>
 *   <li>判定生效後「系統問司馬懿要不要觸發剛烈」，隨後卡局。</li>
 * </ol>
 * 第 2 點的成因：第二段問的是傷害來源「棄兩張手牌或受 1 點傷害」，但事件與一般
 * 「是否發動武將技」長得一模一樣（沒有 options、message 又被 presenter 寫死），
 * 前端只畫得出發動／放棄，而舊版對 ACCEPT/SKIP 直接丟 IllegalArgumentException
 * → 玩家怎麼按都回不了合法答案。
 */
public class GangLieSourceAskTest {

    /** a = 司馬懿（決鬥來源，兼鬼才）、b = 夏侯惇，牌堆頂為黑桃（剛烈判定生效）。 */
    private Game givenSiMaYiDuelsXiaHouDun() {
        Game game = new Game();
        game.initDeck();
        Player a = build("player-a", General.司馬懿, Role.MONARCH);
        Player b = build("player-b", General.夏侯惇, Role.MINISTER);
        Player c = build("player-c", General.甘寧, Role.REBEL);
        Player d = build("player-d", General.甘寧, Role.TRAITOR);
        game.setPlayers(asList(a, b, c, d));
        game.setCurrentRound(new Round(a));
        game.enterPhase(new Normal(game));

        a.getHand().addCardToHand(List.of(new Duel(SSA001), new Peach(BH3029), new Kill(BS8008)));
        b.getHand().addCardToHand(List.of(new Kill(BS9009)));
        game.setDeck(new Deck(List.of(new Dismantle(SS3003)))); // 判定牌：黑桃 3 → 剛烈生效
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

    /** 走到「剛烈判定生效、等來源選擇」為止，回傳鬼才 SKIP 那一步的事件。 */
    private List<DomainEvent> playUntilSourceAsked(Game game) {
        game.playerPlayCard("player-a", SSA001.getCardId(), "player-b", PlayType.ACTIVE.getPlayType());
        game.playerPlayCard("player-b", "", "player-a", PlayType.SKIP.getPlayType()); // 夏侯惇不出殺 → 扣血
        game.playerUseSkillEffect("player-b", "剛烈", "ACCEPT", null, null);          // 判定牌抽出 → 問鬼才
        return game.playerUseSkillEffect("player-a", "鬼才", "SKIP", null, null);
    }

    private AskSkillEffectEvent askOf(List<DomainEvent> events, String skillName, String playerId) {
        return events.stream().filter(e -> e instanceof AskSkillEffectEvent ask
                        && ask.getSkillName().equals(skillName) && ask.getPlayerId().equals(playerId))
                .map(AskSkillEffectEvent.class::cast).findFirst().orElse(null);
    }

    private String gangLieMessage(List<DomainEvent> events) {
        return events.stream()
                .filter(e -> e instanceof SkillEffectEvent se && se.getSkillName().equals("剛烈"))
                .findFirst().orElseThrow().getMessage();
    }

    @DisplayName("使用者回報 1：剛烈判定訊息要寫出是哪張判定牌造成生效")
    @Test
    public void gangLieJudgementMessageNamesTheCard() {
        Game game = givenSiMaYiDuelsXiaHouDun();
        List<DomainEvent> events = playUntilSourceAsked(game);

        assertEquals("剛烈判定：黑桃3 過河拆橋 → 非紅桃，生效", gangLieMessage(events));
    }

    @DisplayName("使用者回報 2：問來源的事件要帶 DISCARD/DAMAGE 選項與真正的問題，不是「是否發動剛烈」")
    @Test
    public void sourceAskCarriesItsOwnOptionsAndQuestion() {
        Game game = givenSiMaYiDuelsXiaHouDun();
        List<DomainEvent> events = playUntilSourceAsked(game);

        AskSkillEffectEvent ask = askOf(events, "剛烈", "player-a");
        assertNotNull(ask, "判定生效應詢問傷害來源");
        assertEquals(List.of("DISCARD", "DAMAGE"), ask.getOptions(),
                "前端要靠 options 才畫得出正確的按鈕；只有 ACCEPT/SKIP 的話玩家回不了合法答案");
        assertEquals("剛烈判定生效：請 player-a 選擇棄兩張手牌或受 1 點傷害", ask.getMessage());
    }

    @DisplayName("使用者回報 2：來源按了通用對話框的「放棄」(SKIP) → 受 1 點傷害收束，不卡局")
    @Test
    public void sourceAnsweringSkipTakesDamageInsteadOfHanging() {
        Game game = givenSiMaYiDuelsXiaHouDun();
        playUntilSourceAsked(game);

        List<DomainEvent> events = game.playerUseSkillEffect("player-a", "剛烈", "SKIP", null, null);

        assertEquals(3, game.getPlayer("player-a").getHP(), "沒有選擇棄牌 → 受剛烈 1 點傷害");
        assertEquals("player-a 受剛烈 1 點傷害（4→3）", gangLieMessage(events));
        assertTrue(game.getTopBehavior().isEmpty(), "剛烈結束後 stack 要清空，回合才走得下去");
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
    }

    @DisplayName("使用者回報 2：來源送 ACCEPT 但挑了兩張手牌 → 當成棄牌處理")
    @Test
    public void sourceAnsweringAcceptWithTwoCardsDiscardsThem() {
        Game game = givenSiMaYiDuelsXiaHouDun();
        playUntilSourceAsked(game);

        game.playerUseSkillEffect("player-a", "剛烈", "ACCEPT",
                List.of(BH3029.getCardId(), BS8008.getCardId()), null);

        Player a = game.getPlayer("player-a");
        assertEquals(4, a.getHP(), "棄牌則不受傷");
        assertEquals(0, a.getHandSize());
        assertTrue(game.getGraveyard().contains(BH3029.getCardId()));
        assertTrue(game.getTopBehavior().isEmpty());
    }

    @DisplayName("使用者回報 1：鬼才詢問要寫出正在替換的是哪張判定牌")
    @Test
    public void guiCaiAskNamesTheJudgementCard() {
        Game game = givenSiMaYiDuelsXiaHouDun();
        game.playerPlayCard("player-a", SSA001.getCardId(), "player-b", PlayType.ACTIVE.getPlayType());
        game.playerPlayCard("player-b", "", "player-a", PlayType.SKIP.getPlayType());

        List<DomainEvent> events = game.playerUseSkillEffect("player-b", "剛烈", "ACCEPT", null, null);

        AskSkillEffectEvent ask = askOf(events, "鬼才", "player-a");
        assertNotNull(ask);
        assertEquals("鬼才：player-a 可替換 player-b 的剛烈判定牌 黑桃3 過河拆橋", ask.getMessage());
        assertEquals(List.of("ACCEPT", "SKIP"), ask.getOptions());
    }

    @DisplayName("鬼才換牌後：換上來的那張牌要出現在剛烈判定訊息裡")
    @Test
    public void guiCaiReplacementCardAppearsInGangLieMessage() {
        Game game = givenSiMaYiDuelsXiaHouDun();
        game.playerPlayCard("player-a", SSA001.getCardId(), "player-b", PlayType.ACTIVE.getPlayType());
        game.playerPlayCard("player-b", "", "player-a", PlayType.SKIP.getPlayType());
        game.playerUseSkillEffect("player-b", "剛烈", "ACCEPT", null, null);

        // 司馬懿以紅心桃替換黑桃判定牌 → 剛烈改為未生效
        List<DomainEvent> events = game.playerUseSkillEffect("player-a", "鬼才", "ACCEPT",
                List.of(BH3029.getCardId()), null);

        assertEquals("剛烈判定：紅心3 桃 → 紅桃，未生效", gangLieMessage(events));
        assertEquals(4, game.getPlayer("player-a").getHP(), "未生效 → 來源不受傷");
        assertNull(askOf(events, "剛烈", "player-a"), "未生效不問來源");
    }
}
