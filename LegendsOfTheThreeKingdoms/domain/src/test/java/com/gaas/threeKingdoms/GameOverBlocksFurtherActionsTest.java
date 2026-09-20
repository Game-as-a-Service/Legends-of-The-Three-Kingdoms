package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.GameOverEvent;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.QilinBowCard;
import com.gaas.threeKingdoms.handcard.scrollcard.ArrowBarrage;
import com.gaas.threeKingdoms.handcard.scrollcard.Dismantle;
import com.gaas.threeKingdoms.handcard.scrollcard.SomethingForNothing;
import com.gaas.threeKingdoms.player.*;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.rolecard.RoleCard;
import com.gaas.threeKingdoms.round.Round;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 使用者回報：遊戲結束後還可以繼續出牌。
 *
 * <p>主公死亡 → 反賊獲勝、phase 進 GameOver、GameOverEvent 廣播出去之後，回合流程並沒有被關掉：
 * activePlayer 還是那位玩家、behavior stack 是空的，所以出牌 / 結束回合 / 棄牌 / 用裝備
 * 這些 API 全都還吃得下去，牌局會繼續跑下去。勝負已分之後任何玩家操作都該被拒絕。
 */
public class GameOverBlocksFurtherActionsTest {

    /**
     * a=反賊(回合玩家，手上還有無中生有／過河拆橋／麒麟弓)、b=主公(1 HP)、c=忠臣、d=內奸。
     * a 殺 b、全員不出桃 → 主公死亡、反賊獲勝。
     */
    private Game givenMonarchKilledByRebel() {
        Game game = new Game();
        game.initDeck();

        Player a = build("player-a", General.甘寧, Role.REBEL, 4);
        a.getHand().addCardToHand(Arrays.asList(
                new Kill(BS8008), new SomethingForNothing(SH7046),
                new Dismantle(SS3003), new QilinBowCard(EH5031)));
        // 主公用黃蓋：苦肉是主動技，不會插進殺／瀕死流程（諸葛亮的空城會讓空手的主公不能被殺指定）
        Player b = build("player-b", General.黃蓋, Role.MONARCH, 1);
        Player c = build("player-c", General.趙雲, Role.MINISTER, 4);
        Player d = build("player-d", General.孫權, Role.TRAITOR, 4);

        game.setPlayers(Arrays.asList(a, b, c, d));
        game.setCurrentRound(new Round(a));
        game.enterPhase(new Normal(game));

        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", PlayType.ACTIVE.getPlayType());
        game.playerPlayCard("player-b", "", "player-b", PlayType.SKIP.getPlayType()); // 不出閃
        // 依序詢問桃，全部 skip
        game.playerPlayCard("player-b", "", "player-b", PlayType.SKIP.getPlayType());
        game.playerPlayCard("player-c", "", "player-b", PlayType.SKIP.getPlayType());
        game.playerPlayCard("player-d", "", "player-b", PlayType.SKIP.getPlayType());
        List<DomainEvent> events = game.playerPlayCard("player-a", "", "player-b", PlayType.SKIP.getPlayType());

        GameOverEvent gameOver = events.stream().filter(e -> e instanceof GameOverEvent)
                .map(GameOverEvent.class::cast).findFirst().orElseThrow(
                        () -> new AssertionError("fixture 沒有走到遊戲結束"));
        assertEquals(List.of("player-a"), gameOver.getWinners());
        assertEquals("GameOver", game.getGamePhase().getPhaseName());
        return game;
    }

    /** 被拒絕的理由必須是「遊戲已結束」，不能是碰巧撞到其他狀態檢查而丟的例外。 */
    private void assertRejectedBecauseGameIsOver(org.junit.jupiter.api.function.Executable action) {
        IllegalStateException thrown = assertThrows(IllegalStateException.class, action);
        assertTrue(thrown.getMessage().contains("遊戲已結束"),
                "應該因為遊戲已結束而被拒絕，實際訊息：" + thrown.getMessage());
    }

    private Player build(String id, General general, Role role, int hp) {
        return PlayerBuilder.construct().withId(id)
                .withBloodCard(new BloodCard(hp))
                .withGeneralCard(new GeneralCard(general))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(role))
                .withHand(new Hand()).withEquipment(new Equipment()).build();
    }

    @DisplayName("遊戲結束後出牌（無中生有）要被拒絕，手牌與牌堆不能被動到")
    @Test
    public void afterGameOver_playCardIsRejected() {
        Game game = givenMonarchKilledByRebel();
        Player a = game.getPlayer("player-a");
        int handSizeBefore = a.getHandSize();
        int deckSizeBefore = game.getDeck().size();

        assertRejectedBecauseGameIsOver(() -> game.playerPlayCard(
                "player-a", SH7046.getCardId(), "player-a", PlayType.ACTIVE.getPlayType()));

        assertEquals(handSizeBefore, a.getHandSize(), "被拒絕的出牌不能真的把牌打出去");
        assertEquals(deckSizeBefore, game.getDeck().size(), "也不能真的摸到牌");
    }

    @DisplayName("遊戲結束後出牌（過河拆橋指定別人）要被拒絕")
    @Test
    public void afterGameOver_playScrollOnAnotherPlayerIsRejected() {
        Game game = givenMonarchKilledByRebel();
        Player c = game.getPlayer("player-c");
        c.getHand().addCardToHand(new Kill(BS9009));

        assertRejectedBecauseGameIsOver(() -> game.playerPlayCard(
                "player-a", SS3003.getCardId(), "player-c", PlayType.ACTIVE.getPlayType()));

        assertEquals(1, c.getHandSize(), "對手的牌不能被拆掉");
    }

    @DisplayName("遊戲結束後結束回合要被拒絕（否則會開始下一位玩家的回合、牌局繼續跑）")
    @Test
    public void afterGameOver_finishActionIsRejected() {
        Game game = givenMonarchKilledByRebel();
        String roundPlayerBefore = game.getCurrentRoundPlayer().getId();

        assertRejectedBecauseGameIsOver(() -> game.finishAction("player-a"));

        assertEquals(roundPlayerBefore, game.getCurrentRoundPlayer().getId(), "不可以換到下一位玩家的回合");
    }

    @DisplayName("遊戲結束後棄牌要被拒絕")
    @Test
    public void afterGameOver_discardIsRejected() {
        Game game = givenMonarchKilledByRebel();
        Player a = game.getPlayer("player-a");
        int handSizeBefore = a.getHandSize();

        assertRejectedBecauseGameIsOver(() -> game.playerDiscardCard(List.of(SH7046.getCardId())));

        assertEquals(handSizeBefore, a.getHandSize());
    }

    @DisplayName("另一種結束方式（反賊全滅、主公與忠臣獲勝）之後，出牌與結束回合同樣被拒絕")
    @Test
    public void afterMinistersWin_actionsAreRejectedToo() {
        Game game = new Game();
        game.initDeck();
        Player a = build("player-a", General.甘寧, Role.MONARCH, 4);
        a.getHand().addCardToHand(Arrays.asList(
                new ArrowBarrage(SHA040), new SomethingForNothing(SH7046)));
        Player b = build("player-b", General.黃蓋, Role.REBEL, 1);
        Player c = build("player-c", General.趙雲, Role.REBEL, 1);
        Player d = build("player-d", General.孫權, Role.MINISTER, 4);
        game.setPlayers(Arrays.asList(a, b, c, d));
        game.setCurrentRound(new Round(a));
        game.enterPhase(new Normal(game));

        // 萬箭齊發：b、c 都沒有閃，也沒人出桃 → 兩個反賊陸續死亡
        // （問閃與問桃交錯，所以不寫死順序：誰被問到就讓他 skip，直到遊戲結束）
        game.playerPlayCard("player-a", SHA040.getCardId(), "player-a", PlayType.ACTIVE.getPlayType());
        int guard = 0;
        while (!game.isGameOver()) {
            Player active = game.getCurrentRound().getActivePlayer();
            Player dying = game.getCurrentRound().getDyingPlayer();
            String target = dying != null ? dying.getId() : "player-a";
            game.playerPlayCard(active.getId(), "", target, PlayType.SKIP.getPlayType());
            if (++guard > 20) {
                fail("萬箭 + 兩次瀕死沒有收斂到遊戲結束");
            }
        }

        assertRejectedBecauseGameIsOver(() -> game.playerPlayCard(
                "player-a", SH7046.getCardId(), "player-a", PlayType.ACTIVE.getPlayType()));
        assertRejectedBecauseGameIsOver(() -> game.finishAction("player-a"));
    }

    @DisplayName("遊戲結束後裝備牌要被拒絕")
    @Test
    public void afterGameOver_useEquipmentIsRejected() {
        Game game = givenMonarchKilledByRebel();
        Player a = game.getPlayer("player-a");

        assertRejectedBecauseGameIsOver(() -> game.playerUseEquipment(
                "player-a", EH5031.getCardId(), "player-a",
                com.gaas.threeKingdoms.handcard.EquipmentPlayType.ACTIVE));

        assertNull(a.getEquipment().getWeapon(), "不能在遊戲結束後還裝上武器");
    }
}
