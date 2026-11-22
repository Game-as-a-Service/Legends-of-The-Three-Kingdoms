package com.gaas.threeKingdoms.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.PlayCardEvent;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.scrollcard.ArrowBarrage;
import com.gaas.threeKingdoms.handcard.scrollcard.BarbarianInvasion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 回應南蠻（出殺）／萬箭（出閃）的 PlayCardEvent，targetPlayerId 必須是 AOE 使用者，
 * 不能照抄前端帶的 targetPlayerId（線上前端誤帶已死亡的 round.dyingPlayer → 戰報「孫權 對 甘寧 使用 殺」）。
 */
public class AoeResponseTargetTest extends PassiveSkillTestBase {

    private static PlayCardEvent responseOf(List<DomainEvent> events, String playerId) {
        return events.stream().filter(e -> e instanceof PlayCardEvent p && playerId.equals(p.getPlayerId()))
                .map(e -> (PlayCardEvent) e).findFirst().orElseThrow(() -> new AssertionError(events.toString()));
    }

    @DisplayName("南蠻：b 出殺回應，前端帶錯的 target（d）→ 事件 target 仍是南蠻使用者 a")
    @Test
    public void barbarianKillResponseTargetsUser() {
        Game game = createGame(General.甘寧, General.甘寧, General.甘寧, General.甘寧);
        game.getPlayer("player-a").getHand().addCardToHand(List.of(new BarbarianInvasion(SS7007)));
        game.getPlayer("player-b").getHand().addCardToHand(List.of(new Kill(BS8008)));
        game.playerPlayCard("player-a", SS7007.getCardId(), "player-a", "active");

        List<DomainEvent> events = game.playerPlayCard("player-b", BS8008.getCardId(), "player-d", "active");

        assertEquals("player-a", responseOf(events, "player-b").getTargetPlayerId());
    }

    @DisplayName("萬箭：b 出閃回應，前端帶錯的 target（d）→ 事件 target 仍是萬箭使用者 a")
    @Test
    public void arrowBarrageDodgeResponseTargetsUser() {
        Game game = createGame(General.甘寧, General.甘寧, General.甘寧, General.甘寧);
        game.getPlayer("player-a").getHand().addCardToHand(List.of(new ArrowBarrage(SHA040)));
        game.getPlayer("player-b").getHand().addCardToHand(List.of(new Dodge(BH2028)));
        game.playerPlayCard("player-a", SHA040.getCardId(), "player-a", "active");

        List<DomainEvent> events = game.playerPlayCard("player-b", BH2028.getCardId(), "player-d", "active");

        assertEquals("player-a", responseOf(events, "player-b").getTargetPlayerId());
    }
}
