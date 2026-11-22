package com.gaas.threeKingdoms.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.SkillEffectEvent;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.player.HealthStatus;
import com.gaas.threeKingdoms.player.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 仁德一次發動分給多人（標準版：將任意張手牌以任意分配方式交給其他角色）。
 * targetPlayerIds 與 cardIds 逐張對應；全部驗證通過才移動任何一張牌。
 */
public class RenDeMultiRecipientTest extends PassiveSkillTestBase {

    private Game givenLiuBei() {
        Game game = createGame(General.劉備, General.甘寧, General.甘寧, General.甘寧);
        Player a = game.getPlayer("player-a");
        a.getBloodCard().setHp(2);
        a.getHand().addCardToHand(List.of(new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028)));
        return game;
    }

    @DisplayName("一次仁德：殺給 b、桃給 c → 各拿到對的牌，累積 2 張回 1 體力，每位接收者一則 SkillEffectEvent")
    @Test
    public void oneActivationSplitsBetweenTwoPlayers() {
        Game game = givenLiuBei();

        List<DomainEvent> events = game.playerUseSkillEffect("player-a", "仁德", "ACCEPT",
                List.of(BS8008.getCardId(), BH3029.getCardId()), null, List.of("player-b", "player-c"));

        assertTrue(game.getPlayer("player-b").getHand().getCard(BS8008.getCardId()).isPresent());
        assertTrue(game.getPlayer("player-c").getHand().getCard(BH3029.getCardId()).isPresent());
        assertEquals(1, game.getPlayer("player-a").getHandSize());
        assertEquals(3, game.getPlayer("player-a").getHP(), "一次給出 2 張 → 回 1");
        List<SkillEffectEvent> effects = events.stream().filter(e -> e instanceof SkillEffectEvent)
                .map(e -> (SkillEffectEvent) e).toList();
        assertEquals(2, effects.size());
        assertEquals("player-b", effects.get(0).getDataPlayerId());
        assertEquals(List.of(BS8008.getCardId()), effects.get(0).getDataCardIds());
        assertEquals("player-c", effects.get(1).getDataPlayerId());
        assertEquals(List.of(BH3029.getCardId()), effects.get(1).getDataCardIds());
    }

    @DisplayName("同一位接收者的多張牌合併成一則事件；分給多人後同回合再發動不再回血")
    @Test
    public void groupsByRecipientAndHealsOnce() {
        Game game = givenLiuBei();

        List<DomainEvent> events = game.playerUseSkillEffect("player-a", "仁德", "ACCEPT",
                List.of(BS8008.getCardId(), BH3029.getCardId(), BH2028.getCardId()), null,
                List.of("player-b", "player-c", "player-b"));

        List<SkillEffectEvent> effects = events.stream().filter(e -> e instanceof SkillEffectEvent)
                .map(e -> (SkillEffectEvent) e).toList();
        assertEquals(2, effects.size());
        assertEquals(List.of(BS8008.getCardId(), BH2028.getCardId()), effects.get(0).getDataCardIds());
        assertEquals(2, game.getPlayer("player-b").getHandSize());
        assertEquals(3, game.getPlayer("player-a").getHP());

        game.getPlayer("player-a").getHand().addCardToHand(List.of(new Peach(BH4030)));
        game.playerUseSkillEffect("player-a", "仁德", "ACCEPT", List.of(BH4030.getCardId()), "player-d");
        assertEquals(3, game.getPlayer("player-a").getHP(), "每回合只回一次");
    }

    @DisplayName("targetPlayerIds 數量和 cardIds 不一致 → 拒絕，一張牌都不移動")
    @Test
    public void mismatchedLengthRejectedAtomically() {
        Game game = givenLiuBei();
        assertThrows(IllegalArgumentException.class, () -> game.playerUseSkillEffect("player-a", "仁德", "ACCEPT",
                List.of(BS8008.getCardId(), BH3029.getCardId()), null, List.of("player-b")));
        assertEquals(3, game.getPlayer("player-a").getHandSize());
        assertEquals(0, game.getPlayer("player-b").getHandSize());
    }

    @DisplayName("其中一位接收者不合法（自己／已死亡）→ 拒絕，前面合法的那張也不會被給出")
    @Test
    public void invalidRecipientRejectedAtomically() {
        Game game = givenLiuBei();
        assertThrows(IllegalArgumentException.class, () -> game.playerUseSkillEffect("player-a", "仁德", "ACCEPT",
                List.of(BS8008.getCardId(), BH3029.getCardId()), null, List.of("player-b", "player-a")));
        assertEquals(0, game.getPlayer("player-b").getHandSize());

        game.getPlayer("player-c").setHealthStatus(HealthStatus.DEATH);
        assertThrows(IllegalArgumentException.class, () -> game.playerUseSkillEffect("player-a", "仁德", "ACCEPT",
                List.of(BS8008.getCardId(), BH3029.getCardId()), null, List.of("player-b", "player-c")));
        assertEquals(0, game.getPlayer("player-b").getHandSize());
        assertEquals(3, game.getPlayer("player-a").getHandSize());
    }

    @DisplayName("其中一張不在手牌 → 拒絕，一張牌都不移動")
    @Test
    public void cardNotInHandRejectedAtomically() {
        Game game = givenLiuBei();
        assertThrows(IllegalArgumentException.class, () -> game.playerUseSkillEffect("player-a", "仁德", "ACCEPT",
                List.of(BS8008.getCardId(), BH4030.getCardId()), null, List.of("player-b", "player-c")));
        assertEquals(3, game.getPlayer("player-a").getHandSize());
        assertEquals(0, game.getPlayer("player-b").getHandSize());
    }

    @DisplayName("其他技能帶 targetPlayerIds → 拒絕（只有仁德支援逐張指定接收者）")
    @Test
    public void otherSkillRejectsPerCardTargets() {
        Game game = createGame(General.孫權, General.甘寧, General.甘寧, General.甘寧);
        game.getPlayer("player-a").getHand().addCardToHand(List.of(new Kill(BS8008)));
        assertThrows(IllegalArgumentException.class, () -> game.playerUseSkillEffect("player-a", "制衡", "ACCEPT",
                List.of(BS8008.getCardId()), null, List.of("player-b")));
        assertEquals(1, game.getPlayer("player-a").getHandSize());
    }
}
