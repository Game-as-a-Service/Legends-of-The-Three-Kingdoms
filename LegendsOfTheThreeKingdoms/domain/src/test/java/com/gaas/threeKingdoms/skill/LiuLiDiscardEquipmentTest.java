package com.gaas.threeKingdoms.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.RedRabbitHorse;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.QilinBowCard;
import com.gaas.threeKingdoms.player.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 流離「棄一張牌」可棄手牌或裝備區的牌。
 * 座位 a（攻擊者）→ b（大喬）→ c → d；大喬距離 1 內：a、c；裝 -1 馬後 d 也在距離 1 內。
 * 轉移目標的距離以棄牌前計算（選目標與棄牌同時宣告）。
 */
public class LiuLiDiscardEquipmentTest extends PassiveSkillTestBase {

    private Game givenAKillsDaQiao() {
        Game game = createGame(General.甘寧, General.大喬, General.甘寧, General.甘寧);
        game.getPlayer("player-a").getHand().addCardToHand(new Kill(BS8008));
        return game;
    }

    private static boolean askedLiuLi(List<DomainEvent> events) {
        return events.stream().anyMatch(e -> e instanceof AskSkillEffectEvent a && "流離".equals(a.getSkillName()));
    }

    private static boolean askedDodge(List<DomainEvent> events, String playerId) {
        return events.stream().anyMatch(e -> e instanceof AskDodgeEvent a && playerId.equals(a.getPlayerId()));
    }

    @DisplayName("大喬沒手牌、只有裝備 → 仍會問流離")
    @Test
    public void askLiuLiWhenOnlyEquipment() {
        Game game = givenAKillsDaQiao();
        game.getPlayer("player-b").getEquipment().setMinusOne(new RedRabbitHorse(EH5044));

        List<DomainEvent> events = game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");

        assertTrue(askedLiuLi(events), "有裝備可棄就該問流離：" + events);
    }

    @DisplayName("大喬沒手牌也沒裝備 → 不問流離，直接問閃")
    @Test
    public void noCardsNoLiuLi() {
        Game game = givenAKillsDaQiao();

        List<DomainEvent> events = game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");

        assertFalse(askedLiuLi(events));
        assertTrue(askedDodge(events, "player-b"));
    }

    @DisplayName("流離棄武器 → 武器移出裝備區進棄牌堆、殺轉給 c")
    @Test
    public void discardWeaponRedirects() {
        Game game = givenAKillsDaQiao();
        Player daQiao = game.getPlayer("player-b");
        daQiao.getHand().addCardToHand(new Peach(BH3029));
        daQiao.getEquipment().setWeapon(new QilinBowCard(EH5031));
        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");

        List<DomainEvent> events = game.playerUseSkillEffect("player-b", "流離", "ACCEPT",
                List.of(EH5031.getCardId()), "player-c");

        assertNull(daQiao.getEquipment().getWeapon());
        assertTrue(game.getGraveyard().contains(EH5031.getCardId()));
        assertEquals(1, daQiao.getHandSize(), "手牌不動");
        assertTrue(askedDodge(events, "player-c"), "殺轉給 c：" + events);
        assertEquals(4, daQiao.getHP());
    }

    @DisplayName("只有 -1 馬：棄 -1 馬轉給 d（距離以棄牌前計算，棄前 d 在距離 1 內）")
    @Test
    public void discardMinusHorseRangeBeforeDiscard() {
        Game game = givenAKillsDaQiao();
        Player daQiao = game.getPlayer("player-b");
        daQiao.getEquipment().setMinusOne(new RedRabbitHorse(EH5044));
        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");

        List<DomainEvent> events = game.playerUseSkillEffect("player-b", "流離", "ACCEPT",
                List.of(EH5044.getCardId()), "player-d");

        assertNull(daQiao.getEquipment().getMinusOne());
        assertTrue(game.getGraveyard().contains(EH5044.getCardId()));
        assertTrue(askedDodge(events, "player-d"), "殺轉給 d：" + events);
    }

    @DisplayName("棄不屬於大喬的牌 → 拒絕，狀態不變")
    @Test
    public void discardUnownedCardRejected() {
        Game game = givenAKillsDaQiao();
        Player daQiao = game.getPlayer("player-b");
        daQiao.getEquipment().setMinusOne(new RedRabbitHorse(EH5044));
        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");

        assertThrows(IllegalArgumentException.class, () -> game.playerUseSkillEffect("player-b", "流離", "ACCEPT",
                List.of(EH5031.getCardId()), "player-c"));
        assertNotNull(daQiao.getEquipment().getMinusOne());
    }
}
