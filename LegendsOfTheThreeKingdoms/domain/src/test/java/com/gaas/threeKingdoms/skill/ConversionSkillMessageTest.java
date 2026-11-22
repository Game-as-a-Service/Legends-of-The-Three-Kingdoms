package com.gaas.threeKingdoms.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.SkillEffectEvent;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.QilinBowCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.RepeatingCrossbowCard;
import com.gaas.threeKingdoms.handcard.scrollcard.BarbarianInvasion;
import com.gaas.threeKingdoms.handcard.scrollcard.BorrowedSword;
import com.gaas.threeKingdoms.player.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 轉化技（武聖 / 龍膽 / 奇襲 / 國色 / 傾國）的 SkillEffectEvent 訊息要寫出武將名、來源牌、轉化後的牌與目標。
 * 使用者回報：趙雲龍膽把閃當殺使用，log 只看到「趙雲 對 甘寧 使用 閃」與「龍膽：player-a 發動」。
 */
public class ConversionSkillMessageTest extends PassiveSkillTestBase {

    private static SkillEffectEvent skillEvent(List<DomainEvent> events, String skillName) {
        return events.stream()
                .filter(e -> e instanceof SkillEffectEvent s && s.getSkillName().equals(skillName))
                .map(e -> (SkillEffectEvent) e)
                .findFirst()
                .orElseThrow(() -> new AssertionError("no SkillEffectEvent for " + skillName));
    }

    @DisplayName("龍膽主動：閃當殺 →「趙雲 發動龍膽，將 紅心2 閃 當 殺 對 甘寧 使用」")
    @Test
    public void longDanDodgeAsActiveKillMessage() {
        Game game = createGame(General.趙雲, General.甘寧, General.甘寧, General.甘寧);
        game.getPlayer("player-a").getHand().addCardToHand(new Dodge(BH2028));

        List<DomainEvent> events = game.playerUseSkillEffect(
                "player-a", "龍膽", "KILL", List.of(BH2028.getCardId()), "player-b");

        SkillEffectEvent e = skillEvent(events, "龍膽");
        assertEquals("趙雲 發動龍膽，將 紅心2 閃 當 殺 對 甘寧 使用", e.getMessage());
        assertEquals(List.of(BH2028.getCardId()), e.getDataCardIds(), "event data 形狀不變");
        assertEquals("player-b", e.getDataPlayerId());
    }

    @DisplayName("龍膽回應問閃：殺當閃 →「趙雲 發動龍膽，將 黑桃9 殺 當 閃 打出」")
    @Test
    public void longDanKillAsDodgeResponseMessage() {
        Game game = createGame(General.甘寧, General.趙雲, General.甘寧, General.甘寧);
        game.getPlayer("player-a").getHand().addCardToHand(new Kill(BS8008));
        game.getPlayer("player-b").getHand().addCardToHand(new Kill(BS9009));

        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");
        List<DomainEvent> events = game.playerUseSkillEffect(
                "player-b", "龍膽", "DODGE", List.of(BS9009.getCardId()), null);

        assertEquals("趙雲 發動龍膽，將 黑桃9 殺 當 閃 打出", skillEvent(events, "龍膽").getMessage());
    }

    @DisplayName("龍膽回應問閃（直接 playCard 自動轉化）也帶同樣訊息")
    @Test
    public void longDanViaPlainPlayCardMessage() {
        Game game = createGame(General.甘寧, General.趙雲, General.甘寧, General.甘寧);
        game.getPlayer("player-a").getHand().addCardToHand(new Kill(BS8008));
        game.getPlayer("player-b").getHand().addCardToHand(new Kill(BS9009));

        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");
        List<DomainEvent> events = game.playerPlayCard("player-b", BS9009.getCardId(), "player-a", "active");

        assertEquals("趙雲 發動龍膽，將 黑桃9 殺 當 閃 打出", skillEvent(events, "龍膽").getMessage());
    }

    @DisplayName("龍膽回應南蠻：閃當殺打出 →「趙雲 發動龍膽，將 紅心2 閃 當 殺 打出」")
    @Test
    public void longDanDodgeAsKillResponseMessage() {
        Game game = createGame(General.甘寧, General.趙雲, General.甘寧, General.甘寧);
        game.getPlayer("player-a").getHand().addCardToHand(new BarbarianInvasion(SS7007));
        game.getPlayer("player-b").getHand().addCardToHand(new Dodge(BH2028));

        game.playerPlayCard("player-a", SS7007.getCardId(), "player-a", "active");
        List<DomainEvent> events = game.playerUseSkillEffect(
                "player-b", "龍膽", "KILL", List.of(BH2028.getCardId()), null);

        assertEquals("趙雲 發動龍膽，將 紅心2 閃 當 殺 打出", skillEvent(events, "龍膽").getMessage());
    }

    @DisplayName("龍膽回應借刀殺人：閃當殺對指定目標使用 →「趙雲 發動龍膽，將 紅心2 閃 當 殺 對 甘寧 使用」")
    @Test
    public void longDanDodgeAsKillForBorrowedSwordMessage() {
        Game game = createGame(General.甘寧, General.趙雲, General.甘寧, General.甘寧);
        Player b = game.getPlayer("player-b");
        game.getPlayer("player-a").getHand().addCardToHand(new BorrowedSword(SCK065));
        b.getEquipment().setWeapon(new RepeatingCrossbowCard(ECA066));
        // 手上也有殺才會被問出殺（只有閃時借刀殺人直接拿武器，與本次訊息修正無關）
        b.getHand().addCardToHand(java.util.Arrays.asList(new Dodge(BH2028), new Kill(BS9009)));

        game.playerPlayCard("player-a", SCK065.getCardId(), "player-b", PlayType.ACTIVE.getPlayType());
        game.useBorrowedSwordEffect("player-a", "player-b", "player-c");
        List<DomainEvent> events = game.playerUseSkillEffect(
                "player-b", "龍膽", "KILL", List.of(BH2028.getCardId()), "player-c");

        assertEquals("趙雲 發動龍膽，將 紅心2 閃 當 殺 對 甘寧 使用", skillEvent(events, "龍膽").getMessage());
    }

    @DisplayName("武聖：紅色手牌當殺 →「關羽 發動武聖，將 紅心3 桃 當 殺 對 甘寧 使用」")
    @Test
    public void wuShengMessage() {
        Game game = createGame(General.關羽, General.甘寧, General.甘寧, General.甘寧);
        game.getPlayer("player-a").getHand().addCardToHand(new Peach(BH3029));

        List<DomainEvent> events = game.playerUseSkillEffect(
                "player-a", "武聖", "KILL", List.of(BH3029.getCardId()), "player-b");

        assertEquals("關羽 發動武聖，將 紅心3 桃 當 殺 對 甘寧 使用", skillEvent(events, "武聖").getMessage());
    }

    @DisplayName("武聖：裝備區紅色武器當殺 → 訊息寫出裝備牌")
    @Test
    public void wuShengEquipmentSourceMessage() {
        Game game = createGame(General.關羽, General.甘寧, General.甘寧, General.甘寧);
        game.getPlayer("player-a").getEquipment().setWeapon(new QilinBowCard(EH5031));

        List<DomainEvent> events = game.playerUseSkillEffect(
                "player-a", "武聖", "KILL", List.of(EH5031.getCardId()), "player-b");

        assertEquals("關羽 發動武聖，將 紅心5 麒麟弓 當 殺 對 甘寧 使用", skillEvent(events, "武聖").getMessage());
    }

    @DisplayName("奇襲：黑色牌當過河拆橋 →「甘寧 發動奇襲，將 黑桃8 殺 當 過河拆橋 對 趙雲 使用」")
    @Test
    public void qiXiMessage() {
        Game game = createGame(General.甘寧, General.趙雲, General.甘寧, General.甘寧);
        game.getPlayer("player-a").getHand().addCardToHand(new Kill(BS8008));
        game.getPlayer("player-b").getEquipment().setWeapon(new QilinBowCard(EH5031));

        List<DomainEvent> events = game.playerUseSkillEffect(
                "player-a", "奇襲", "DISMANTLE", List.of(BS8008.getCardId()), "player-b");

        assertEquals("甘寧 發動奇襲，將 黑桃8 殺 當 過河拆橋 對 趙雲 使用", skillEvent(events, "奇襲").getMessage());
    }

    @DisplayName("國色：方塊牌當樂不思蜀 →「大喬 發動國色，將 方塊2 閃 當 樂不思蜀 對 甘寧 使用」")
    @Test
    public void guoSeMessage() {
        Game game = createGame(General.大喬, General.甘寧, General.甘寧, General.甘寧);
        game.getPlayer("player-a").getHand().addCardToHand(new Dodge(BD2080));

        List<DomainEvent> events = game.playerUseSkillEffect(
                "player-a", "國色", "CONTENTMENT", List.of(BD2080.getCardId()), "player-b");

        assertEquals("大喬 發動國色，將 方塊2 閃 當 樂不思蜀 對 甘寧 使用", skillEvent(events, "國色").getMessage());
    }

    @DisplayName("傾國：黑色手牌當閃 →「甄姬 發動傾國，將 黑桃9 殺 當 閃 打出」")
    @Test
    public void qingGuoMessage() {
        Game game = createGame(General.甘寧, General.甄姬, General.甘寧, General.甘寧);
        game.getPlayer("player-a").getHand().addCardToHand(new Kill(BS8008));
        game.getPlayer("player-b").getHand().addCardToHand(new Kill(BS9009));

        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");
        List<DomainEvent> events = game.playerUseSkillEffect(
                "player-b", "傾國", "DODGE", List.of(BS9009.getCardId()), null);

        assertEquals("甄姬 發動傾國，將 黑桃9 殺 當 閃 打出", skillEvent(events, "傾國").getMessage());
    }
}
