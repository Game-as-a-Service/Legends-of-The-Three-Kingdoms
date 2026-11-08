package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.EightDiagramTactic;
import com.gaas.threeKingdoms.handcard.scrollcard.Contentment;
import com.gaas.threeKingdoms.handcard.scrollcard.Lightning;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.skill.PassiveSkillTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 玩家死亡時，手牌、裝備、判定區的牌都要進棄牌堆；
 * 原本留在死者身上，從牌堆循環中永久消失。
 */
public class DeadPlayerCardsTest extends PassiveSkillTestBase {

    private static boolean inGraveyard(Game game, String cardId) {
        return game.getGraveyard().getGraveYardDeck().stream().anyMatch(c -> c.getId().equals(cardId));
    }

    @DisplayName("玩家死亡 → 手牌、裝備、判定區全部進棄牌堆")
    @Test
    public void deadPlayerCardsGoToGraveyard() {
        Game game = createGame(General.甘寧, General.甘寧, General.甘寧, General.甘寧);
        Player rebel = game.getPlayer("player-c");
        rebel.getHand().addCardToHand(List.of(new Kill(BS9009), new Dodge(BH2028)));
        rebel.getEquipment().setArmor(new EightDiagramTactic(ES2015));
        rebel.addDelayScrollCard(new Contentment(SH6045));
        rebel.addDelayScrollCard(new Lightning(SSA014));

        // 直接走死亡結算（不經出殺流程，避免八卦陣判定等干擾）
        game.removeDyingPlayer(rebel);

        assertTrue(rebel.isAlreadyDeath());
        assertEquals(0, rebel.getHandSize());
        assertFalse(rebel.getEquipment().hasAnyEquipment());
        assertTrue(rebel.getDelayScrollCards().isEmpty());
        for (String id : List.of(BS9009.getCardId(), BH2028.getCardId(), ES2015.getCardId(),
                SH6045.getCardId(), SSA014.getCardId())) {
            assertTrue(inGraveyard(game, id), id + " 應進棄牌堆");
        }
    }

    @DisplayName("死者的牌進棄牌堆後，牌堆抽乾時會被洗回來繼續使用")
    @Test
    public void deadPlayerCardsReturnToCirculation() {
        Game game = createGame(General.甘寧, General.甘寧, General.甘寧, General.甘寧);
        Player rebel = game.getPlayer("player-c");
        rebel.getHand().addCardToHand(List.of(new Kill(BS9009)));
        game.removeDyingPlayer(rebel);

        game.getDeck().deal(game.getDeck().size());
        game.drawCardToPlayer(game.getPlayer("player-a"), false, 1);

        assertTrue(game.getPlayer("player-a").getHand().getCard(BS9009.getCardId()).isPresent(),
                "死者的殺洗回牌堆後被摸到");
    }
}
