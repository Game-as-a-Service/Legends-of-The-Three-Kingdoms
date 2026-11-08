package com.gaas.threeKingdoms.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.round.RoundPhase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;

public class YingZiSkillTest extends PassiveSkillTestBase {

    @DisplayName("周瑜摸牌階段 → 英姿多摸一張（共 3 張）")
    @Test
    public void zhouYuDrawsThreeCards() {
        Game game = createGame(General.周瑜, General.劉備, General.劉備, General.劉備);
        Player a = game.getPlayer("player-a");

        game.drawCardToPlayer(a, false);

        assertEquals(3, a.getHandSize(), "英姿：2 + 1 = 3");
    }

    @DisplayName("非周瑜摸牌階段 → 摸 2 張")
    @Test
    public void nonZhouYuDrawsTwoCards() {
        Game game = createGame(General.劉備, General.劉備, General.劉備, General.劉備);
        Player a = game.getPlayer("player-a");

        game.drawCardToPlayer(a, false);

        assertEquals(2, a.getHandSize());
    }

    @DisplayName("使用者回報：周瑜 HP=3、手牌 4 張 → 要棄 1 張（標準版英姿不改手牌上限）")
    @Test
    public void zhouYuWithThreeHpAndFourCardsDiscardsOne() {
        Game game = createGame(General.周瑜, General.劉備, General.劉備, General.劉備);
        Player a = game.getPlayer("player-a");
        a.getBloodCard().setHp(3);
        a.getHand().addCardToHand(java.util.Arrays.asList(new Kill(BS8008), new Kill(BS9009), new Dodge(BH2028),
                new Peach(BH3029)));
        game.getCurrentRound().setRoundPhase(RoundPhase.Discard);

        assertEquals(1, game.getCurrentRoundPlayerDiscardCount(), "手牌上限 = HP 3 → 4-3=1");
    }

    @DisplayName("周瑜 HP=1、手牌 6 張 → 棄牌數 = 6 - 1 = 5（不再有 4 張保底）")
    @Test
    public void zhouYuHandLimitIsHp() {
        Game game = createGame(General.周瑜, General.劉備, General.劉備, General.劉備);
        Player a = game.getPlayer("player-a");
        a.getBloodCard().setHp(1);
        a.getHand().addCardToHand(java.util.Arrays.asList(new Kill(BS8008), new Kill(BS9009), new Dodge(BH2028),
                new Dodge(BHK039), new Peach(BH3029), new Peach(BH4030)));
        game.getCurrentRound().setRoundPhase(RoundPhase.Discard);

        assertEquals(5, game.getCurrentRoundPlayerDiscardCount());
    }

    @DisplayName("非周瑜 HP=3、手牌 6 張 → 棄牌數 = 6 - 3 = 3")
    @Test
    public void nonZhouYuHandLimitIsHp() {
        Game game = createGame(General.劉備, General.劉備, General.劉備, General.劉備);
        Player a = game.getPlayer("player-a");
        a.getBloodCard().setHp(3);
        a.getHand().addCardToHand(java.util.Arrays.asList(new Kill(BS8008), new Kill(BS9009), new Dodge(BH2028),
                new Dodge(BHK039), new Peach(BH3029), new Peach(BH4030)));
        game.getCurrentRound().setRoundPhase(RoundPhase.Discard);

        assertEquals(3, game.getCurrentRoundPlayerDiscardCount());
    }
}
