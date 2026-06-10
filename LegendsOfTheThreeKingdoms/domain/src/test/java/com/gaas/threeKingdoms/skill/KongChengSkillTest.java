package com.gaas.threeKingdoms.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.scrollcard.Duel;
import com.gaas.threeKingdoms.player.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;

public class KongChengSkillTest extends PassiveSkillTestBase {

    @DisplayName("諸葛亮手牌 0 → 不能被殺指定")
    @Test
    public void killTargetingEmptyHandZhugeLiangThrows() {
        Game game = createGame(General.劉備, General.諸葛亮, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(new Kill(BS8008));

        assertThrows(IllegalStateException.class, () ->
                game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active"));
    }

    @DisplayName("諸葛亮手牌 0 → 不能被決鬥指定")
    @Test
    public void duelTargetingEmptyHandZhugeLiangThrows() {
        Game game = createGame(General.劉備, General.諸葛亮, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(new Duel(SSA001));

        assertThrows(IllegalStateException.class, () ->
                game.playerPlayCard("player-a", SSA001.getCardId(), "player-b", "active"));
    }

    @DisplayName("諸葛亮有手牌 → 可被殺指定（空城不生效）")
    @Test
    public void killTargetingZhugeLiangWithHandCardSucceeds() {
        Game game = createGame(General.劉備, General.諸葛亮, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(new Kill(BS8008));
        b.getHand().addCardToHand(new Peach(BH3029));

        assertDoesNotThrow(() ->
                game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active"));
    }

    @DisplayName("方天畫戟多目標含手牌 0 的諸葛亮 → 拋例外")
    @Test
    public void halberdTargetingEmptyHandZhugeLiangThrows() {
        Game game = createGame(General.劉備, General.諸葛亮, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getEquipment().setWeapon(
                new com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.HeavenlyDoubleHalberdCard(EDQ103));
        a.getHand().addCardToHand(new Kill(BS8008));

        assertThrows(IllegalStateException.class, () ->
                game.playerUseHeavenlyDoubleHalberdKill("player-a", BS8008.getCardId(),
                        java.util.List.of("player-b", "player-c")));
    }
}
