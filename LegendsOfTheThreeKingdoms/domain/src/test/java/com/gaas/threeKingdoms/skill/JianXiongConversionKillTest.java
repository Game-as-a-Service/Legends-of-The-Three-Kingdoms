package com.gaas.threeKingdoms.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.AskJianXiongEffectEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 奸雄 × 轉化殺：曹操受到武聖 / 龍膽的殺傷害 → 可獲得被轉化的真實來源牌。
 * createGame：player-a 回合主（關羽 / 趙雲），player-b 曹操。
 */
public class JianXiongConversionKillTest extends PassiveSkillTestBase {

    private static final String SKIP = PlayType.SKIP.getPlayType();

    private static Optional<AskJianXiongEffectEvent> jianXiongAsk(List<DomainEvent> events) {
        return events.stream().filter(e -> e instanceof AskJianXiongEffectEvent)
                .map(e -> (AskJianXiongEffectEvent) e).findFirst();
    }

    @DisplayName("關羽武聖（紅桃閃當殺）打曹操 → 不出閃 → 奸雄詢問來源牌 BH2028 → ACCEPT 拿進手牌")
    @Test
    public void wuShengKill_jianXiongTakesSourceCard() {
        Game game = createGame(General.關羽, General.曹操, General.甘寧, General.甘寧);
        game.getPlayer("player-a").getHand().addCardToHand(List.of(new Dodge(BH2028)));
        game.playerUseSkillEffect("player-a", "武聖", "KILL", List.of(BH2028.getCardId()), "player-b");

        List<DomainEvent> events = game.playerPlayCard("player-b", "", "player-a", SKIP);

        assertEquals(3, game.getPlayer("player-b").getHP());
        AskJianXiongEffectEvent ask = jianXiongAsk(events)
                .orElseThrow(() -> new AssertionError("轉化殺應觸發奸雄：" + events));
        assertEquals("player-b", ask.getPlayerId());
        assertEquals(List.of(BH2028.getCardId()), ask.getSourceCardIds());

        game.playerUseJianXiongEffect("player-b", AskJianXiongEffectEvent.Choice.ACCEPT);
        assertTrue(game.getPlayer("player-b").getHand().getCard(BH2028.getCardId()).isPresent());
        assertFalse(game.getGraveyard().contains(BH2028.getCardId()));
        assertTrue(game.isTopBehaviorEmpty());
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
    }

    @DisplayName("趙雲龍膽（閃當殺）打曹操 → 奸雄詢問來源閃 → SKIP 牌留在墓地")
    @Test
    public void longDanKill_jianXiongSkipKeepsCardInGraveyard() {
        Game game = createGame(General.趙雲, General.曹操, General.甘寧, General.甘寧);
        game.getPlayer("player-a").getHand().addCardToHand(List.of(new Dodge(BD2080)));
        game.playerUseSkillEffect("player-a", "龍膽", "KILL", List.of(BD2080.getCardId()), "player-b");

        List<DomainEvent> events = game.playerPlayCard("player-b", "", "player-a", SKIP);

        AskJianXiongEffectEvent ask = jianXiongAsk(events)
                .orElseThrow(() -> new AssertionError("龍膽殺應觸發奸雄：" + events));
        assertEquals(List.of(BD2080.getCardId()), ask.getSourceCardIds());

        game.playerUseJianXiongEffect("player-b", AskJianXiongEffectEvent.Choice.SKIP);
        assertTrue(game.getGraveyard().contains(BD2080.getCardId()));
        assertFalse(game.getPlayer("player-b").getHand().getCard(BD2080.getCardId()).isPresent());
    }

    @DisplayName("武聖殺打到 1 血曹操 → 瀕死被救回 → 補詢問奸雄（來源牌 BH2028）→ ACCEPT 拿進手牌")
    @Test
    public void wuShengKill_dyingRescued_jianXiongTakesSourceCard() {
        Game game = createGame(General.關羽, General.曹操, General.甘寧, General.甘寧);
        game.getPlayer("player-a").getHand().addCardToHand(List.of(new Dodge(BH2028)));
        game.getPlayer("player-b").getBloodCard().setHp(1);
        game.getPlayer("player-c").getHand().addCardToHand(List.of(new Peach(BH3029)));
        game.playerUseSkillEffect("player-a", "武聖", "KILL", List.of(BH2028.getCardId()), "player-b");
        game.playerPlayCard("player-b", "", "player-a", SKIP);                 // HP 1 → 0 瀕死

        game.playerPlayCard("player-b", "", "player-b", SKIP);
        List<DomainEvent> events = game.playerPlayCard("player-c", BH3029.getCardId(), "player-b", PlayType.ACTIVE.getPlayType());

        assertEquals(1, game.getPlayer("player-b").getHP());
        AskJianXiongEffectEvent ask = jianXiongAsk(events)
                .orElseThrow(() -> new AssertionError("救回後應補詢問奸雄：" + events));
        assertEquals(List.of(BH2028.getCardId()), ask.getSourceCardIds());

        game.playerUseJianXiongEffect("player-b", AskJianXiongEffectEvent.Choice.ACCEPT);
        assertTrue(game.getPlayer("player-b").getHand().getCard(BH2028.getCardId()).isPresent());
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
    }
}
