package com.gaas.threeKingdoms.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.QilinBowCard;
import com.gaas.threeKingdoms.handcard.scrollcard.BorrowedSword;
import com.gaas.threeKingdoms.handcard.scrollcard.Duel;
import com.gaas.threeKingdoms.handcard.scrollcard.Ward;
import com.gaas.threeKingdoms.player.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 「能不能出殺」要把轉化技算進去（龍膽：閃當殺、武聖：紅色牌當殺）。
 * 原本只看手牌的殺與丈八蛇矛：趙雲只有閃 → 借刀殺人直接被奪武器、決鬥直接扣血，沒有機會發動龍膽。
 * 座位 a → b → c → d；a 出借刀／決鬥，b 是被借刀／被決鬥的人。
 */
public class ConversionCanProduceKillTest extends PassiveSkillTestBase {

    private static boolean has(List<DomainEvent> events, Class<?> type) {
        return events.stream().anyMatch(type::isInstance);
    }

    private Game givenBorrowedSwordOn(General b, Runnable... setup) {
        Game game = createGame(General.甘寧, b, General.甘寧, General.甘寧);
        game.getPlayer("player-a").getHand().addCardToHand(List.of(new BorrowedSword(SCK065)));
        game.getPlayer("player-b").getEquipment().setWeapon(new QilinBowCard(EH5031));
        return game;
    }

    private List<DomainEvent> borrowedSword(Game game) {
        game.playerPlayCard("player-a", SCK065.getCardId(), "player-b", PlayType.ACTIVE.getPlayType());
        return game.useBorrowedSwordEffect("player-a", "player-b", "player-c");
    }

    @DisplayName("借刀殺人：趙雲只有閃 → 問他出殺（不直接奪武器）→ 龍膽以閃當殺打 c → c 被問閃，武器留在趙雲")
    @Test
    public void borrowedSwordZhaoYunOnlyDodgeCanLongDan() {
        Game game = givenBorrowedSwordOn(General.趙雲);
        Player zhaoYun = game.getPlayer("player-b");
        zhaoYun.getHand().addCardToHand(List.of(new Dodge(BH2028)));

        List<DomainEvent> events = borrowedSword(game);
        assertTrue(has(events, BorrowedSwordEvent.class), "要問趙雲出殺：" + events);
        assertFalse(has(events, WeaponUsurpationEvent.class), "不能直接奪武器");
        assertEquals("player-b", game.getCurrentRound().getActivePlayer().getId());

        events = game.playerUseSkillEffect("player-b", "龍膽", "KILL", List.of(BH2028.getCardId()), "player-c");
        assertTrue(events.stream().anyMatch(e -> e instanceof AskDodgeEvent a && "player-c".equals(a.getPlayerId())),
                events.toString());
        assertInstanceOf(QilinBowCard.class, zhaoYun.getEquipmentWeaponCard(), "出了殺，武器不被奪");
    }

    @DisplayName("借刀殺人：關羽只有紅色非殺手牌（桃）→ 一樣問他出殺（武聖）")
    @Test
    public void borrowedSwordGuanYuRedCardCanWuSheng() {
        Game game = givenBorrowedSwordOn(General.關羽);
        game.getPlayer("player-b").getHand().addCardToHand(List.of(new Peach(BH3029)));

        List<DomainEvent> events = borrowedSword(game);

        assertTrue(has(events, BorrowedSwordEvent.class), events.toString());
        assertFalse(has(events, WeaponUsurpationEvent.class));
    }

    @DisplayName("借刀殺人：關羽沒有手牌，但武器本身是紅色（麒麟弓 紅心5）→ 武聖可用裝備區的牌 → 問他出殺")
    @Test
    public void borrowedSwordGuanYuRedEquipmentCanWuSheng() {
        Game game = givenBorrowedSwordOn(General.關羽);

        List<DomainEvent> events = borrowedSword(game);

        assertTrue(has(events, BorrowedSwordEvent.class), events.toString());
    }

    @DisplayName("借刀殺人：趙雲沒有殺也沒有閃 → 照舊直接奪武器")
    @Test
    public void borrowedSwordZhaoYunNoKillNoDodgeStillUsurped() {
        Game game = givenBorrowedSwordOn(General.趙雲);
        game.getPlayer("player-b").getHand().addCardToHand(List.of(new Peach(BH3029)));

        List<DomainEvent> events = borrowedSword(game);

        assertTrue(has(events, WeaponUsurpationEvent.class), events.toString());
        assertNull(game.getPlayer("player-b").getEquipmentWeaponCard());
    }

    @DisplayName("借刀殺人經無懈詢問（有人持無懈、全部放棄）後也要把龍膽算進去 → 問趙雲出殺")
    @Test
    public void borrowedSwordAfterWardSkipStillAsksZhaoYun() {
        Game game = givenBorrowedSwordOn(General.趙雲);
        game.getPlayer("player-b").getHand().addCardToHand(List.of(new Dodge(BH2028)));
        game.getPlayer("player-d").getHand().addCardToHand(List.of(new Ward(SCQ077)));

        List<DomainEvent> first = borrowedSword(game);
        assertTrue(has(first, WaitForWardEvent.class), "d 有無懈 → 先問無懈：" + first);

        List<DomainEvent> events = game.playWardCard("player-d", "", PlayType.SKIP.getPlayType());

        assertTrue(has(events, BorrowedSwordEvent.class), "無懈放棄後問趙雲出殺：" + events);
        assertFalse(has(events, WeaponUsurpationEvent.class));
    }

    @DisplayName("決鬥：趙雲只有閃 → 問他出殺（不直接扣血）→ 龍膽以閃當殺 → 換 a 出殺")
    @Test
    public void duelZhaoYunOnlyDodgeCanLongDan() {
        Game game = createGame(General.甘寧, General.趙雲, General.甘寧, General.甘寧);
        game.getPlayer("player-a").getHand().addCardToHand(List.of(new Duel(SSA001)));
        game.getPlayer("player-b").getHand().addCardToHand(List.of(new Dodge(BH2028)));

        List<DomainEvent> events = game.playerPlayCard("player-a", SSA001.getCardId(), "player-b", PlayType.ACTIVE.getPlayType());
        assertTrue(events.stream().anyMatch(e -> e instanceof AskKillEvent a && "player-b".equals(a.getPlayerId())),
                "要問趙雲出殺：" + events);
        assertEquals(4, game.getPlayer("player-b").getHP(), "不能直接扣血");

        events = game.playerUseSkillEffect("player-b", "龍膽", "KILL", List.of(BH2028.getCardId()), null);
        assertEquals(4, game.getPlayer("player-b").getHP());
        assertEquals(3, game.getPlayer("player-a").getHP(), "a 沒有殺 → a 扣血");
    }
}
