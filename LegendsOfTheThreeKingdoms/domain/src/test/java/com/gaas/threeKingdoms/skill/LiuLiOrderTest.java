package com.gaas.threeKingdoms.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.HandCard;
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
 * 流離是「成為殺的目標時」，要早於鐵騎、雌雄雙股劍、八卦陣。
 * FAQ：馬超對大喬使用殺 → 大喬先選擇是否發動流離，然後馬超再選擇是否發動鐵騎。
 * 原本流離放在「問閃前」：鐵騎先判；大喬有八卦陣時根本不問流離；放棄流離直接問閃（跳過八卦陣）。
 * 座位 a → b（大喬）→ c → d；大喬距離 1 內有 a（攻擊者，不可轉移）與 c。
 */
public class LiuLiOrderTest extends PassiveSkillTestBase {

    private Game givenAttackerKillsDaQiao(General attacker, HandCard judgement, boolean daQiaoHasEightDiagram) {
        Game game = createGame(attacker, General.大喬, General.甘寧, General.甘寧);
        game.getPlayer("player-a").getHand().addCardToHand(new Kill(BS8008));
        Player daQiao = game.getPlayer("player-b");
        daQiao.getHand().addCardToHand(List.of(new Peach(BH3029), new Dodge(BH2028)));
        if (daQiaoHasEightDiagram) daQiao.getEquipment().setArmor(new EightDiagramTactic(ES2015));
        if (judgement != null) game.getDeck().add(List.of(judgement));
        return game;
    }

    private static boolean has(List<DomainEvent> events, Class<?> type) {
        return events.stream().anyMatch(type::isInstance);
    }

    private static boolean hasTieQi(List<DomainEvent> events) {
        return events.stream().anyMatch(e -> e instanceof SkillEffectEvent se && "鐵騎".equals(se.getSkillName()));
    }

    @DisplayName("馬超殺大喬 → 先問流離，鐵騎還沒判定")
    @Test
    public void liuLiAskedBeforeTieQi() {
        Game game = givenAttackerKillsDaQiao(General.馬超, new Peach(BH4030), false);

        List<DomainEvent> events = game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");

        assertTrue(events.stream().anyMatch(e -> e instanceof AskSkillEffectEvent a && "流離".equals(a.getSkillName())));
        assertFalse(hasTieQi(events), "流離先於鐵騎");
    }

    @DisplayName("大喬放棄流離 → 接著判鐵騎（紅色）→ 不能出閃，直接受傷")
    @Test
    public void declineLiuLiThenRedTieQiDamages() {
        Game game = givenAttackerKillsDaQiao(General.馬超, new Peach(BH4030), false);
        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");

        List<DomainEvent> events = game.playerUseSkillEffect("player-b", "流離", "SKIP", null, null);

        assertTrue(hasTieQi(events), "放棄流離後才判鐵騎");
        assertFalse(has(events, AskDodgeEvent.class));
        assertEquals(3, game.getPlayer("player-b").getHP());
    }

    @DisplayName("大喬發動流離轉給 c → 對新目標判鐵騎")
    @Test
    public void acceptLiuLiThenTieQiOnNewTarget() {
        Game game = givenAttackerKillsDaQiao(General.馬超, new Peach(BH4030), false);
        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");

        List<DomainEvent> events = game.playerUseSkillEffect("player-b", "流離", "ACCEPT",
                List.of(BH3029.getCardId()), "player-c");

        SkillEffectEvent tieQi = events.stream().filter(e -> e instanceof SkillEffectEvent se && "鐵騎".equals(se.getSkillName()))
                .map(e -> (SkillEffectEvent) e).findFirst().orElseThrow(() -> new AssertionError("新目標要判鐵騎：" + events));
        assertEquals("player-c", tieQi.getDataPlayerId());
        assertEquals(3, game.getPlayer("player-c").getHP(), "紅色 → 新目標不能閃");
        assertEquals(4, game.getPlayer("player-b").getHP());
    }

    @DisplayName("大喬有八卦陣 → 仍會先問流離（原本直接問八卦陣）")
    @Test
    public void liuLiAskedEvenWithEightDiagram() {
        Game game = givenAttackerKillsDaQiao(General.甘寧, null, true);

        List<DomainEvent> events = game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");

        assertTrue(events.stream().anyMatch(e -> e instanceof AskSkillEffectEvent a && "流離".equals(a.getSkillName())));
        assertFalse(has(events, AskPlayEquipmentEffectEvent.class), "流離先於八卦陣");
    }

    @DisplayName("大喬有八卦陣、放棄流離 → 問八卦陣（原本直接問閃）")
    @Test
    public void declineLiuLiThenAskEightDiagram() {
        Game game = givenAttackerKillsDaQiao(General.甘寧, null, true);
        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");

        List<DomainEvent> events = game.playerUseSkillEffect("player-b", "流離", "SKIP", null, null);

        assertTrue(has(events, AskPlayEquipmentEffectEvent.class), "放棄流離後問八卦陣");
        assertFalse(has(events, AskDodgeEvent.class));
    }
}
