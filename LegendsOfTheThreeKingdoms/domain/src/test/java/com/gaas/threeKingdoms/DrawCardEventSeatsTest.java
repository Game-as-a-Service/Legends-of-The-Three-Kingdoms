package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.events.DelayScrollEvent;
import com.gaas.threeKingdoms.events.DrawCardEvent;
import com.gaas.threeKingdoms.events.PlayerEvent;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.scrollcard.Contentment;
import com.gaas.threeKingdoms.handcard.scrollcard.Lightning;
import com.gaas.threeKingdoms.skill.PassiveSkillTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.SH6045;
import static com.gaas.threeKingdoms.handcard.PlayCard.SSA014;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 摸牌事件帶的 seats 要反映實際的判定區。原本寫死空陣列：
 * 拿這份 seats 當遊戲狀態的 presenter（RoundStartPresenter）會把判定區顯示成空的。
 */
public class DrawCardEventSeatsTest extends PassiveSkillTestBase {

    @DisplayName("判定區有樂不思蜀與閃電時，摸牌事件的 seats 帶出判定區內容")
    @Test
    public void drawCardEventSeatsIncludeDelayScrolls() {
        Game game = createGame(General.甘寧, General.甘寧, General.甘寧, General.甘寧);
        game.getPlayer("player-b").addDelayScrollCard(new Contentment(SH6045));
        game.getPlayer("player-b").addDelayScrollCard(new Lightning(SSA014));

        DrawCardEvent event = (DrawCardEvent) game.drawCardToPlayer(game.getPlayer("player-a"), false, 2);

        PlayerEvent b = event.getSeats().stream().filter(s -> s.getId().equals("player-b")).findFirst().orElseThrow();
        assertEquals(List.of(
                new DelayScrollEvent(SH6045.getCardId(), DelayScrollEvent.CONTENTMENT, "樂不思蜀"),
                new DelayScrollEvent(SSA014.getCardId(), DelayScrollEvent.LIGHTNING, "閃電")
        ), b.getDelayScrolls());
        PlayerEvent a = event.getSeats().stream().filter(s -> s.getId().equals("player-a")).findFirst().orElseThrow();
        assertEquals(List.of(), a.getDelayScrolls());
    }
}
