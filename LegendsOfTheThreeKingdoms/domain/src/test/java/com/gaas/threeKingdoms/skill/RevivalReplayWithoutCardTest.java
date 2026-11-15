package com.gaas.threeKingdoms.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.AskSkillEffectEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 瀕死被救回後要補觸發受傷後技能（FAQ：曹操瀕死被救回後仍可發動奸雄）。
 * 原本這段 replay 需要「造成傷害的牌」，沒有牌的傷害（反間、轉化殺）被救回後反饋等不會觸發。
 * createGame：player-a 主公、player-b 忠臣、player-c 反賊、player-d 內奸；player-b 為 1 血司馬懿。
 */
public class RevivalReplayWithoutCardTest extends PassiveSkillTestBase {

    private static final String SKIP = PlayType.SKIP.getPlayType();

    private static Optional<AskSkillEffectEvent> fanKuiAsk(List<DomainEvent> events) {
        return events.stream().filter(e -> e instanceof AskSkillEffectEvent a && "反饋".equals(a.getSkillName()))
                .map(e -> (AskSkillEffectEvent) e).findFirst();
    }

    /** 司馬懿瀕死：自己不出桃 → player-c 出桃救回，回傳救回那一步的事件。 */
    private List<DomainEvent> rescueSiMaYi(Game game) {
        game.playerPlayCard("player-b", "", "player-b", SKIP);
        return game.playerPlayCard("player-c", BH3029.getCardId(), "player-b", PlayType.ACTIVE.getPlayType());
    }

    @DisplayName("周瑜反間打到 1 血司馬懿 → 瀕死被救回 → 補詢問反饋（來源周瑜）")
    @Test
    public void fanJianDyingRescuedThenFanKui() {
        Game game = createGame(General.周瑜, General.司馬懿, General.甘寧, General.甘寧);
        game.getPlayer("player-a").getHand().addCardToHand(List.of(new Kill(BS8008), new Dodge(BH2028)));
        game.getPlayer("player-b").getBloodCard().setHp(1);
        game.getPlayer("player-c").getHand().addCardToHand(List.of(new Peach(BH3029)));
        game.playerUseSkillEffect("player-a", "反間", "ACCEPT", List.of(BS8008.getCardId()), "player-b");
        game.playerUseSkillEffect("player-b", "反間", "HEART", null, null);   // 黑桃8 → 猜錯，HP 1 → 0

        List<DomainEvent> events = rescueSiMaYi(game);

        assertEquals(1, game.getPlayer("player-b").getHP());
        AskSkillEffectEvent ask = fanKuiAsk(events).orElseThrow(() -> new AssertionError("救回後應詢問反饋：" + events));
        assertEquals("player-b", ask.getPlayerId());
        assertEquals("player-a", ask.getDataPlayerId());
        // 反饋放棄後回到周瑜的出牌階段
        game.playerUseSkillEffect("player-b", "反饋", "SKIP", null, null);
        assertTrue(game.isTopBehaviorEmpty());
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
    }

    @DisplayName("關羽武聖（紅色手牌當殺）打到 1 血司馬懿 → 瀕死被救回 → 補詢問反饋（原本轉化殺也不會觸發）")
    @Test
    public void conversionKillDyingRescuedThenFanKui() {
        Game game = createGame(General.關羽, General.司馬懿, General.甘寧, General.甘寧);
        game.getPlayer("player-a").getHand().addCardToHand(List.of(new Dodge(BH2028), new Kill(BS9009)));
        game.getPlayer("player-b").getBloodCard().setHp(1);
        game.getPlayer("player-c").getHand().addCardToHand(List.of(new Peach(BH3029)));
        game.playerUseSkillEffect("player-a", "武聖", "KILL", List.of(BH2028.getCardId()), "player-b");
        game.playerPlayCard("player-b", "", "player-a", SKIP);                 // 不出閃 → HP 1 → 0

        List<DomainEvent> events = rescueSiMaYi(game);

        assertEquals(1, game.getPlayer("player-b").getHP());
        AskSkillEffectEvent ask = fanKuiAsk(events).orElseThrow(() -> new AssertionError("救回後應詢問反饋：" + events));
        assertEquals("player-a", ask.getDataPlayerId());
    }
}
