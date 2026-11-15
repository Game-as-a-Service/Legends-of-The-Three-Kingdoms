package com.gaas.threeKingdoms.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.AskSkillEffectEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.SkillEffectEvent;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.BS8008;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 使用者回報：反間的 log 只有「反間：詢問 Dino 是否發動」「反間：Dino 發動」，
 * 看不出是誰對誰發動、目標選了什麼花色、拿到哪張牌、有沒有受傷。
 * 前端 log 只顯示事件的 message，所以訊息必須寫在 AskSkillEffectEvent / SkillEffectEvent 上。
 */
public class FanJianMessageTest extends PassiveSkillTestBase {

    private Game givenZhouYuFanJianOnSunShangXiang() {
        Game game = createGame(General.周瑜, General.孫尚香, General.甘寧, General.甘寧);
        game.getPlayer("player-a").getHand().addCardToHand(new Kill(BS8008)); // 黑桃8 殺
        return game;
    }

    @DisplayName("詢問目標：寫出誰對誰發動、要選花色；options 是四種花色而不是 ACCEPT/SKIP")
    @Test
    public void askMessageAndSuitOptions() {
        Game game = givenZhouYuFanJianOnSunShangXiang();

        List<DomainEvent> events = game.playerUseSkillEffect("player-a", "反間", "ACCEPT",
                List.of(BS8008.getCardId()), "player-b");

        AskSkillEffectEvent ask = events.stream().filter(e -> e instanceof AskSkillEffectEvent)
                .map(e -> (AskSkillEffectEvent) e).findFirst().orElseThrow();
        assertEquals("player-b", ask.getPlayerId());
        assertEquals("反間：周瑜 對 孫尚香 發動反間，請 孫尚香 選擇一種花色（猜錯受到 1 點傷害）", ask.getMessage());
        assertEquals(List.of("SPADE", "HEART", "DIAMOND", "CLUB"), ask.getOptions());
    }

    @DisplayName("猜錯：寫出選的花色、拿到的牌、受到 1 點傷害")
    @Test
    public void wrongGuessResultMessage() {
        Game game = givenZhouYuFanJianOnSunShangXiang();
        game.playerUseSkillEffect("player-a", "反間", "ACCEPT", List.of(BS8008.getCardId()), "player-b");

        List<DomainEvent> events = game.playerUseSkillEffect("player-b", "反間", "HEART", null, null);

        SkillEffectEvent result = events.stream().filter(e -> e instanceof SkillEffectEvent)
                .map(e -> (SkillEffectEvent) e).findFirst().orElseThrow();
        assertEquals("反間：孫尚香 選擇紅心，獲得 黑桃8 殺 → 花色不符，受到 1 點傷害", result.getMessage());
        assertTrue(result.isAccepted(), "accepted = 反間生效（目標受傷）");
    }

    @DisplayName("猜中：寫出選的花色、拿到的牌、不受傷")
    @Test
    public void correctGuessResultMessage() {
        Game game = givenZhouYuFanJianOnSunShangXiang();
        game.playerUseSkillEffect("player-a", "反間", "ACCEPT", List.of(BS8008.getCardId()), "player-b");

        List<DomainEvent> events = game.playerUseSkillEffect("player-b", "反間", "SPADE", null, null);

        SkillEffectEvent result = events.stream().filter(e -> e instanceof SkillEffectEvent)
                .map(e -> (SkillEffectEvent) e).findFirst().orElseThrow();
        assertEquals("反間：孫尚香 選擇黑桃，獲得 黑桃8 殺 → 猜中花色，不受傷", result.getMessage());
        assertFalse(result.isAccepted());
    }
}
