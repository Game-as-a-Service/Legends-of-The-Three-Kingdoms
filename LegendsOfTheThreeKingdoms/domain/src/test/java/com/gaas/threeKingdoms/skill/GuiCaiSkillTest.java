package com.gaas.threeKingdoms.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.AskSkillEffectEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.LightningEvent;
import com.gaas.threeKingdoms.events.LightningTransferredEvent;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.scrollcard.Lightning;
import com.gaas.threeKingdoms.player.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 司馬懿 鬼才（#164）— v1：閃電判定路徑。
 */
public class GuiCaiSkillTest extends PassiveSkillTestBase {

    @DisplayName("場上有司馬懿（有手牌）→ 閃電判定抽牌後暫停，詢問鬼才")
    @Test
    public void guiCaiAskedBeforeLightningJudgementResolves() {
        Game game = createGame(General.甘寧, General.司馬懿, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        game.getPlayer("player-b").getHand().addCardToHand(new Peach(BH3029));
        Lightning lightning = new Lightning(SSA014);
        a.addDelayScrollCard(lightning);

        // 判定牌：黑桃 8（原本會命中）
        game.getDeck().add(List.of(new Kill(BS8008)));
        List<DomainEvent> events = game.handleLightningJudgement(lightning, a);

        AskSkillEffectEvent ask = events.stream()
                .filter(e -> e instanceof AskSkillEffectEvent).map(e -> (AskSkillEffectEvent) e)
                .findFirst().orElseThrow();
        assertEquals("鬼才", ask.getSkillName());
        assertEquals("player-b", ask.getPlayerId());
        assertEquals(List.of(BS8008.getCardId()), ask.getDataCardIds());
        assertFalse(events.stream().anyMatch(e -> e instanceof LightningEvent), "判定尚未結算");
        assertEquals(4, a.getHP());
    }

    @DisplayName("鬼才 ACCEPT 以紅心替換黑桃判定 → 閃電不中，轉移下家")
    @Test
    public void guiCaiReplaceSavesOwnerFromLightning() {
        Game game = createGame(General.甘寧, General.司馬懿, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        b.getHand().addCardToHand(new Peach(BH3029)); // 紅心 — 替換用
        Lightning lightning = new Lightning(SSA014);
        a.addDelayScrollCard(lightning);

        game.getDeck().add(List.of(new Kill(BS8008))); // 黑桃 8 原判定：命中
        game.handleLightningJudgement(lightning, a);

        List<DomainEvent> events = game.playerUseSkillEffect(
                "player-b", "鬼才", "ACCEPT", List.of(BH3029.getCardId()), null);

        assertEquals(4, a.getHP(), "替換成紅心 → 閃電不中");
        assertTrue(events.stream().anyMatch(e -> e instanceof LightningTransferredEvent), "閃電轉移");
        assertEquals(0, b.getHandSize(), "替換牌已打出");
        assertTrue(game.getGraveyard().contains(BH3029.getCardId()));
    }

    @DisplayName("鬼才 SKIP → 原黑桃判定生效，閃電命中扣 3 血")
    @Test
    public void guiCaiSkipOriginalJudgementApplies() {
        Game game = createGame(General.甘寧, General.司馬懿, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        game.getPlayer("player-b").getHand().addCardToHand(new Peach(BH3029));
        Lightning lightning = new Lightning(SSA014);
        a.addDelayScrollCard(lightning);

        game.getDeck().add(List.of(new Kill(BS8008)));
        game.handleLightningJudgement(lightning, a);

        game.playerUseSkillEffect("player-b", "鬼才", "SKIP", null, null);

        assertEquals(1, a.getHP(), "黑桃 8 判定生效 → 閃電 3 點傷害");
    }

    @DisplayName("司馬懿無手牌 → 鬼才不觸發，判定直接結算")
    @Test
    public void guiCaiNotTriggeredWithoutHandCards() {
        Game game = createGame(General.甘寧, General.司馬懿, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Lightning lightning = new Lightning(SSA014);
        a.addDelayScrollCard(lightning);

        game.getDeck().add(List.of(new Kill(BS8008)));
        List<DomainEvent> events = game.handleLightningJudgement(lightning, a);

        assertFalse(events.stream().anyMatch(e -> e instanceof AskSkillEffectEvent));
        assertTrue(events.stream().anyMatch(e -> e instanceof LightningEvent));
        assertEquals(1, a.getHP(), "直接結算命中");
    }

    @DisplayName("場上無司馬懿 → 判定直接結算（迴歸保護）")
    @Test
    public void noGuiCaiHolderJudgementResolvesDirectly() {
        Game game = createGame(General.甘寧, General.孫權, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Lightning lightning = new Lightning(SSA014);
        a.addDelayScrollCard(lightning);

        game.getDeck().add(List.of(new Peach(BH3029))); // 紅心：不中
        List<DomainEvent> events = game.handleLightningJudgement(lightning, a);

        assertFalse(events.stream().anyMatch(e -> e instanceof AskSkillEffectEvent));
        assertTrue(events.stream().anyMatch(e -> e instanceof LightningTransferredEvent));
    }
}
