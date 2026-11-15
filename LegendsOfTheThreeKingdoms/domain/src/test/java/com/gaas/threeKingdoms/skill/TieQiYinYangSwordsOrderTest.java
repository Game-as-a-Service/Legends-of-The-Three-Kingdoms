package com.gaas.threeKingdoms.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.EightDiagramTactic;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.YinYangSwordsCard;
import com.gaas.threeKingdoms.player.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 馬超裝雌雄雙股劍對異性出殺：先發動武將技（鐵騎），後發動武器技（雙股劍）。
 * FAQ：馬超對黃月英使用殺，發動鐵騎判定為紅桃，此時黃月英再選擇棄手牌或讓馬超摸一張，之後黃月英受到傷害。
 * 原本觸發雙股劍時鐵騎根本不判定（雙股劍結算完直接問防具／閃）。
 */
public class TieQiYinYangSwordsOrderTest extends PassiveSkillTestBase {

    /** a = 馬超（裝雌雄雙股劍、手上一張殺）、b = 黃月英（手上一張閃）；牌堆頂 = 鐵騎判定牌。 */
    private Game givenMaChaoWithYinYangSwords(com.gaas.threeKingdoms.handcard.HandCard judgement, boolean bHasEightDiagram) {
        Game game = createGame(General.馬超, General.黃月英, General.甘寧, General.甘寧);
        Player a = game.getPlayer("player-a");
        a.getEquipment().setWeapon(new YinYangSwordsCard(ES2002));
        a.getHand().addCardToHand(new Kill(BS8008));
        Player b = game.getPlayer("player-b");
        b.getHand().addCardToHand(new Dodge(BH2028));
        if (bHasEightDiagram) b.getEquipment().setArmor(new EightDiagramTactic(ES2015));
        game.getDeck().add(List.of(new Peach(BH7033), judgement));   // Stack：judgement 最先被抽
        return game;
    }

    private static int indexOf(List<DomainEvent> events, Class<?> type) {
        for (int i = 0; i < events.size(); i++) if (type.isInstance(events.get(i))) return i;
        return -1;
    }

    @DisplayName("出殺 → 先判定鐵騎，再問攻擊者是否發動雙股劍")
    @Test
    public void tieQiJudgedBeforeYinYangSwordsAsk() {
        Game game = givenMaChaoWithYinYangSwords(new Peach(BH3029), false);

        List<DomainEvent> events = game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");

        int tieQi = indexOf(events, SkillEffectEvent.class);
        int askYinYang = indexOf(events, AskActivateYinYangSwordsEvent.class);
        assertTrue(tieQi >= 0, "要判定鐵騎：" + events);
        assertTrue(askYinYang > tieQi, "鐵騎（武將技）先於雙股劍（武器技）：" + events);
    }

    @DisplayName("鐵騎紅色 + 雙股劍發動、黃月英讓馬超摸一張 → 之後黃月英直接受傷（不問閃）")
    @Test
    public void redTieQiThenYinYangThenDamage() {
        Game game = givenMaChaoWithYinYangSwords(new Peach(BH3029), false);
        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");
        game.playerActivateYinYangSwords("player-a", AskActivateYinYangSwordsEvent.Choice.ACTIVATE);

        List<DomainEvent> events = game.playerUseYinYangSwordsEffect("player-b",
                YinYangSwordsEffectEvent.Choice.ATTACKER_DRAWS, null);

        assertTrue(events.stream().noneMatch(e -> e instanceof AskDodgeEvent), "鐵騎已生效，不能出閃");
        assertEquals(3, game.getPlayer("player-b").getHP());
        assertEquals(1, game.getPlayer("player-b").getHandSize(), "閃沒被用掉");
    }

    @DisplayName("鐵騎紅色 + 攻擊者放棄雙股劍 → 直接受傷")
    @Test
    public void redTieQiSkipYinYangThenDamage() {
        Game game = givenMaChaoWithYinYangSwords(new Peach(BH3029), false);
        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");

        List<DomainEvent> events = game.playerActivateYinYangSwords("player-a", AskActivateYinYangSwordsEvent.Choice.SKIP);

        assertTrue(events.stream().noneMatch(e -> e instanceof AskDodgeEvent));
        assertEquals(3, game.getPlayer("player-b").getHP());
    }

    @DisplayName("鐵騎黑色 + 攻擊者放棄雙股劍 → 照常問閃")
    @Test
    public void blackTieQiSkipYinYangThenAskDodge() {
        Game game = givenMaChaoWithYinYangSwords(new Kill(BS9009), false);
        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");

        List<DomainEvent> events = game.playerActivateYinYangSwords("player-a", AskActivateYinYangSwordsEvent.Choice.SKIP);

        assertTrue(events.stream().anyMatch(e -> e instanceof AskDodgeEvent ask && ask.getPlayerId().equals("player-b")));
        assertEquals(4, game.getPlayer("player-b").getHP());
    }

    @DisplayName("鐵騎黑色 + 目標有八卦陣 + 雙股劍目標棄牌 → 照常問八卦陣")
    @Test
    public void blackTieQiYinYangDiscardThenAskEightDiagram() {
        Game game = givenMaChaoWithYinYangSwords(new Kill(BS9009), true);
        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");
        game.playerActivateYinYangSwords("player-a", AskActivateYinYangSwordsEvent.Choice.ACTIVATE);

        List<DomainEvent> events = game.playerUseYinYangSwordsEffect("player-b",
                YinYangSwordsEffectEvent.Choice.TARGET_DISCARDS, BH2028.getCardId());

        assertTrue(events.stream().anyMatch(e -> e instanceof AskPlayEquipmentEffectEvent), "鐵騎未生效 → 問八卦陣");
    }
}
