package com.gaas.threeKingdoms.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.behavior.BarbarianInvasionBehavior;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.LightningTransferredEvent;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.scrollcard.ArrowBarrage;
import com.gaas.threeKingdoms.handcard.scrollcard.BarbarianInvasion;
import com.gaas.threeKingdoms.handcard.scrollcard.Contentment;
import com.gaas.threeKingdoms.handcard.scrollcard.Lightning;
import com.gaas.threeKingdoms.player.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;

public class QianXunSkillTest extends PassiveSkillTestBase {

    @DisplayName("南蠻入侵 → 陸遜（謙遜）不在 reactionPlayers 中")
    @Test
    public void luXunExcludedFromBarbarianInvasion() {
        Game game = createGame(General.劉備, General.陸遜, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(new BarbarianInvasion(SS7007));

        game.playerPlayCard("player-a", SS7007.getCardId(), "player-a", "active");

        BarbarianInvasionBehavior bi = (BarbarianInvasionBehavior) game.peekTopBehavior();
        assertFalse(bi.getReactionPlayers().contains("player-b"),
                "陸遜不應成為南蠻入侵目標");
        assertEquals(List.of("player-c", "player-d"), bi.getReactionPlayers());
    }

    @DisplayName("萬箭齊發 → 陸遜（謙遜）不在 reactionPlayers 中")
    @Test
    public void luXunExcludedFromArrowBarrage() {
        Game game = createGame(General.劉備, General.陸遜, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(new ArrowBarrage(SHA040));

        game.playerPlayCard("player-a", SHA040.getCardId(), "player-a", "active");

        assertFalse(game.peekTopBehavior().getReactionPlayers().contains("player-b"));
    }

    @DisplayName("樂不思蜀指定陸遜 → 拋例外")
    @Test
    public void contentmentTargetingLuXunThrows() {
        Game game = createGame(General.劉備, General.陸遜, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(new Contentment(SS6006));

        assertThrows(IllegalStateException.class, () ->
                game.playerPlayCard("player-a", SS6006.getCardId(), "player-b", "active"));
    }

    @DisplayName("閃電判定未中 → 轉移時跳過陸遜，移到下一位非免疫玩家")
    @Test
    public void lightningTransferSkipsLuXun() {
        Game game = createGame(General.劉備, General.陸遜, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Lightning lightning = new Lightning(SSA014);
        a.addDelayScrollCard(lightning);

        // 控制判定牌為非黑桃 2~9（紅心）→ 判定不中 → 轉移
        game.getDeck().add(List.of(new com.gaas.threeKingdoms.handcard.basiccard.Peach(BH3029)));

        List<DomainEvent> events = game.handleLightningJudgement(lightning, a);

        LightningTransferredEvent transferred = events.stream()
                .filter(e -> e instanceof LightningTransferredEvent)
                .map(e -> (LightningTransferredEvent) e)
                .findFirst().orElseThrow();
        assertEquals("player-c", transferred.getTargetPlayerId(),
                "應跳過 player-b（陸遜）轉移到 player-c");
        assertTrue(game.getPlayer("player-c").getDelayScrollCardIds().contains(lightning.getId()));
        assertFalse(game.getPlayer("player-b").getDelayScrollCardIds().contains(lightning.getId()));
    }
}
