package com.gaas.threeKingdoms.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.exception.DistanceErrorException;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.scrollcard.Snatch;
import com.gaas.threeKingdoms.player.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;

public class MaShuSkillTest extends PassiveSkillTestBase {

    @DisplayName("馬超無武器對距離 2 的玩家出殺 → 馬術 -1 使其在攻擊範圍內")
    @Test
    public void maChaoKillsDistanceTwoTargetWithoutWeapon() {
        Game game = createGame(General.馬超, General.劉備, General.劉備, General.劉備);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(new Kill(BS8008));

        // c 與 a 座位距離 2；一般武將無武器攻擊距離 1 打不到
        assertDoesNotThrow(() ->
                game.playerPlayCard("player-a", BS8008.getCardId(), "player-c", "active"));
    }

    @DisplayName("非馬超無武器對距離 2 的玩家出殺 → 拋距離例外")
    @Test
    public void nonMaChaoCannotKillDistanceTwoTarget() {
        Game game = createGame(General.劉備, General.劉備, General.劉備, General.劉備);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(new Kill(BS8008));

        assertThrows(DistanceErrorException.class, () ->
                game.playerPlayCard("player-a", BS8008.getCardId(), "player-c", "active"));
    }

    @DisplayName("馬超對距離 2 的玩家出順手牽羊 → 馬術 -1 使其在範圍內")
    @Test
    public void maChaoSnatchesDistanceTwoTarget() {
        Game game = createGame(General.馬超, General.劉備, General.劉備, General.劉備);
        Player a = game.getPlayer("player-a");
        Player c = game.getPlayer("player-c");
        a.getHand().addCardToHand(new Snatch(SS3016));
        c.getHand().addCardToHand(new Kill(BS9009));

        assertDoesNotThrow(() ->
                game.playerPlayCard("player-a", SS3016.getCardId(), "player-c", "active"));
    }

    @DisplayName("馬術不影響別人打馬超的距離（單向修正）")
    @Test
    public void maShuDoesNotShortenIncomingDistance() {
        Game game = createGame(General.劉備, General.劉備, General.馬超, General.劉備);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(new Kill(BS8008));

        // a（劉備）到 c（馬超）距離 2 — 馬術是 c 的技能，不縮短 a 的攻擊距離
        assertThrows(DistanceErrorException.class, () ->
                game.playerPlayCard("player-a", BS8008.getCardId(), "player-c", "active"));
    }
}
