package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.scrollcard.Contentment;
import com.gaas.threeKingdoms.handcard.scrollcard.Dismantle;
import com.gaas.threeKingdoms.handcard.scrollcard.Lightning;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.skill.PassiveSkillTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 延時錦囊（樂不思蜀 / 閃電）在判定區時不該同時在棄牌堆：
 * 否則牌堆不夠、棄牌堆洗回牌堆時，判定區裡的那張牌會被洗回去被人抽走（同一張牌出現兩次）；
 * 被過河拆橋拆掉時又再進一次棄牌堆（棄牌堆有兩張一樣的牌）。
 * 正確時機：真正離開判定區時才進棄牌堆（判定完 / 閃電劈中 / 被拆）；閃電轉移給下家不進。
 */
public class DelayScrollGraveyardTest extends PassiveSkillTestBase {

    private static long countInGraveyard(Game game, String cardId) {
        return game.getGraveyard().getGraveYardDeck().stream().filter(c -> c.getId().equals(cardId)).count();
    }

    private Game createGame() {
        return createGame(General.甘寧, General.甘寧, General.甘寧, General.甘寧);
    }

    @DisplayName("出樂不思蜀到別人判定區 → 牌在判定區，不在棄牌堆")
    @Test
    public void contentmentInJudgementAreaIsNotInGraveyard() {
        Game game = createGame();
        game.getPlayer("player-a").getHand().addCardToHand(List.of(new Contentment(SH6045)));

        game.playerPlayCard("player-a", SH6045.getCardId(), "player-b", PlayType.ACTIVE.getPlayType());

        assertTrue(game.getPlayer("player-b").hasThisDelayScrollCard(SH6045.getCardId()));
        assertEquals(0, countInGraveyard(game, SH6045.getCardId()), "還在判定區的牌不能同時在棄牌堆");
    }

    @DisplayName("出閃電到自己判定區 → 牌在判定區，不在棄牌堆")
    @Test
    public void lightningInJudgementAreaIsNotInGraveyard() {
        Game game = createGame();
        game.getPlayer("player-a").getHand().addCardToHand(List.of(new Lightning(SSA014)));

        game.playerPlayCard("player-a", SSA014.getCardId(), "player-a", PlayType.ACTIVE.getPlayType());

        assertTrue(game.getPlayer("player-a").hasThisDelayScrollCard(SSA014.getCardId()));
        assertEquals(0, countInGraveyard(game, SSA014.getCardId()));
    }

    @DisplayName("牌堆不夠洗回棄牌堆時，判定區的樂不思蜀不會被洗進牌堆")
    @Test
    public void contentmentIsNotReshuffledIntoDeckWhileInJudgementArea() {
        Game game = createGame();
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(List.of(new Contentment(SH6045)));
        game.playerPlayCard("player-a", SH6045.getCardId(), "player-b", PlayType.ACTIVE.getPlayType());

        // 抽乾牌堆、棄牌堆只有一張別的牌 → 下一次摸牌會把棄牌堆洗回牌堆
        game.getDeck().deal(game.getDeck().size());
        game.getGraveyard().add(new Peach(BH4030));
        game.drawCardToPlayer(a, false, 1);

        assertTrue(a.getHand().getCard(BH4030.getCardId()).isPresent(), "洗回來的只有棄牌堆裡的那張桃");

        assertTrue(game.getPlayer("player-b").hasThisDelayScrollCard(SH6045.getCardId()));
        assertFalse(a.getHand().getCard(SH6045.getCardId()).isPresent(), "判定區的樂不思蜀被洗回牌堆、又被摸到");
        assertTrue(game.getDeck().getCardDeck().stream().noneMatch(c -> c.getId().equals(SH6045.getCardId())),
                "判定區的樂不思蜀不能出現在牌堆");
    }

    @DisplayName("過河拆橋拆掉判定區的樂不思蜀 → 棄牌堆只有一張")
    @Test
    public void dismantledContentmentIsInGraveyardOnce() {
        Game game = createGame();
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(List.of(new Contentment(SH6045), new Dismantle(SS3003)));
        game.playerPlayCard("player-a", SH6045.getCardId(), "player-b", PlayType.ACTIVE.getPlayType());

        game.playerPlayCard("player-a", SS3003.getCardId(), "player-b", PlayType.ACTIVE.getPlayType());
        game.useDismantleEffect("player-a", "player-b", SH6045.getCardId(), null);

        assertFalse(game.getPlayer("player-b").hasThisDelayScrollCard(SH6045.getCardId()));
        assertEquals(1, countInGraveyard(game, SH6045.getCardId()), "被拆的牌只能進棄牌堆一次");
    }

    @DisplayName("樂不思蜀判定完離開判定區 → 進棄牌堆一次")
    @Test
    public void judgedContentmentGoesToGraveyardOnce() {
        Game game = createGame();
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(List.of(new Contentment(SH6045)));
        game.playerPlayCard("player-a", SH6045.getCardId(), "player-b", PlayType.ACTIVE.getPlayType());
        game.getDeck().add(List.of(new Peach(BH4030), new Peach(BH7033), new Kill(BS8008)));

        game.finishAction("player-a");

        assertFalse(game.getPlayer("player-b").hasThisDelayScrollCard(SH6045.getCardId()));
        assertEquals(1, countInGraveyard(game, SH6045.getCardId()));
    }

    @DisplayName("閃電劈中（黑桃7）→ 離開判定區、進棄牌堆一次")
    @Test
    public void lightningHitGoesToGraveyardOnce() {
        Game game = createGame();
        game.getPlayer("player-b").addDelayScrollCard(new Lightning(SSA014));
        // Deck 是 Stack：最後一張最先被抽 → BS7020（黑桃7）是 player-b 的閃電判定牌
        game.getDeck().add(List.of(new Peach(BH4030), new Peach(BH7033), new Kill(BS7020)));

        game.finishAction("player-a");

        assertFalse(game.getPlayer("player-b").hasThisDelayScrollCard(SSA014.getCardId()));
        assertEquals(1, countInGraveyard(game, SSA014.getCardId()));
    }

    @DisplayName("閃電沒劈中（紅心）→ 轉移給下家，留在判定區、不進棄牌堆")
    @Test
    public void lightningMissMovesToNextPlayerNotGraveyard() {
        Game game = createGame();
        game.getPlayer("player-b").addDelayScrollCard(new Lightning(SSA014));
        game.getDeck().add(List.of(new Peach(BH4030), new Peach(BH7033), new Peach(BH3029)));

        game.finishAction("player-a");

        assertTrue(game.getPlayer("player-c").hasThisDelayScrollCard(SSA014.getCardId()), "閃電轉給下家");
        assertEquals(0, countInGraveyard(game, SSA014.getCardId()));
    }
}
