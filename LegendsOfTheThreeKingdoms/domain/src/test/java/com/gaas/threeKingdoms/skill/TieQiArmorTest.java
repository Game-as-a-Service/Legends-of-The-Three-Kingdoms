package com.gaas.threeKingdoms.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.AskDodgeEvent;
import com.gaas.threeKingdoms.events.AskPlayEquipmentEffectEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.SkillEffectEvent;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.EightDiagramTactic;
import com.gaas.threeKingdoms.player.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 標準版鐵騎：判定為紅色 → 目標不能使用閃（含八卦陣）。
 * FAQ：馬超先決定是否發動鐵騎，判定紅色則對方不能發動八卦陣與使用閃；判定黑色則對方選擇是否發動八卦陣。
 * 使用者回報：馬超對裝八卦陣的張遼出殺，鐵騎沒發動，直接問八卦陣。
 */
public class TieQiArmorTest extends PassiveSkillTestBase {

    private Game givenMaChaoKillsEightDiagramHolder(com.gaas.threeKingdoms.handcard.HandCard judgement) {
        Game game = createGame(General.馬超, General.張遼, General.甘寧, General.甘寧);
        game.getPlayer("player-a").getHand().addCardToHand(new Kill(BS8008));
        Player b = game.getPlayer("player-b");
        b.getEquipment().setArmor(new EightDiagramTactic(ES2015));
        b.getHand().addCardToHand(new Dodge(BH2028));
        game.getDeck().add(List.of(judgement));
        return game;
    }

    private static SkillEffectEvent tieQiEvent(List<DomainEvent> events) {
        return events.stream().filter(e -> e instanceof SkillEffectEvent se && "鐵騎".equals(se.getSkillName()))
                .map(e -> (SkillEffectEvent) e).findFirst().orElseThrow(() -> new AssertionError("沒有鐵騎事件：" + events));
    }

    @DisplayName("目標有八卦陣、鐵騎判定紅色 → 先判鐵騎、不問八卦陣也不問閃，直接扣血")
    @Test
    public void redTieQiSkipsEightDiagram() {
        Game game = givenMaChaoKillsEightDiagramHolder(new Peach(BH3029));

        List<DomainEvent> events = game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");

        assertTrue(tieQiEvent(events).isAccepted());
        assertTrue(events.stream().noneMatch(e -> e instanceof AskPlayEquipmentEffectEvent), "不能發動八卦陣");
        assertTrue(events.stream().noneMatch(e -> e instanceof AskDodgeEvent), "不能出閃");
        assertEquals(3, game.getPlayer("player-b").getHP());
    }

    @DisplayName("目標有八卦陣、鐵騎判定黑色 → 鐵騎未生效，照常詢問八卦陣")
    @Test
    public void blackTieQiThenAsksEightDiagram() {
        Game game = givenMaChaoKillsEightDiagramHolder(new Kill(BS9009));

        List<DomainEvent> events = game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");

        assertFalse(tieQiEvent(events).isAccepted());
        assertTrue(events.stream().anyMatch(e -> e instanceof AskPlayEquipmentEffectEvent), "照常問八卦陣");
        assertEquals(4, game.getPlayer("player-b").getHP());
    }

    @DisplayName("方塊也是紅色 → 鐵騎生效")
    @Test
    public void diamondIsRed() {
        Game game = givenMaChaoKillsEightDiagramHolder(new Dodge(BD2080));

        List<DomainEvent> events = game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");

        SkillEffectEvent tieQi = tieQiEvent(events);
        assertTrue(tieQi.isAccepted());
        assertEquals("鐵騎判定：方塊2 閃 → 紅色，生效，張遼 不能出閃", tieQi.getMessage());
        assertEquals(3, game.getPlayer("player-b").getHP());
    }
}
