package com.gaas.threeKingdoms.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.AskDodgeEvent;
import com.gaas.threeKingdoms.events.AskKillEvent;
import com.gaas.threeKingdoms.events.AskSkillEffectEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.scrollcard.ArrowBarrage;
import com.gaas.threeKingdoms.handcard.scrollcard.BarbarianInvasion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 南蠻／萬箭輪詢中，1 血司馬懿受傷瀕死 → 被桃救回 → 補觸發反饋。
 * 原本救回後同時發出「反饋詢問」與「問下一位出殺／閃」兩個詢問（衝突）；
 * 應先讓司馬懿結算反饋，結算完才繼續輪詢。
 * 座位 a（甘寧，出 AOE）→ b（司馬懿 1 血）→ c → d；救桃的是 c。
 */
public class AoeRevivalReplayTest extends PassiveSkillTestBase {

    private static final String SKIP = PlayType.SKIP.getPlayType();

    private Game givenSiMaYiAtOneHp() {
        Game game = createGame(General.甘寧, General.司馬懿, General.甘寧, General.甘寧);
        game.getPlayer("player-a").getHand().addCardToHand(List.of(new Dodge(BH2028)));   // 讓反饋有牌可拿
        game.getPlayer("player-b").getBloodCard().setHp(1);
        game.getPlayer("player-c").getHand().addCardToHand(List.of(new Peach(BH3029), new Kill(BS9009), new Dodge(BHK039)));
        return game;
    }

    private static boolean asksFanKui(List<DomainEvent> events) {
        return events.stream().anyMatch(e -> e instanceof AskSkillEffectEvent a && "反饋".equals(a.getSkillName()));
    }

    /** 司馬懿瀕死 → 自己不出桃 → c 出桃救回。 */
    private List<DomainEvent> rescue(Game game) {
        game.playerPlayCard("player-b", "", "player-b", SKIP);
        return game.playerPlayCard("player-c", BH3029.getCardId(), "player-b", PlayType.ACTIVE.getPlayType());
    }

    @DisplayName("南蠻：救回後只問反饋，不同時問下一位出殺；反饋結算完才輪到 c")
    @Test
    public void barbarianInvasionRevivalAsksFanKuiThenResumes() {
        Game game = givenSiMaYiAtOneHp();
        game.getPlayer("player-a").getHand().addCardToHand(List.of(new BarbarianInvasion(SS7007)));
        game.playerPlayCard("player-a", SS7007.getCardId(), "player-a", PlayType.ACTIVE.getPlayType());
        game.playerPlayCard("player-b", "", "player-a", SKIP);                   // 司馬懿不出殺 → 1 → 0

        List<DomainEvent> revived = rescue(game);

        assertEquals(1, game.getPlayer("player-b").getHP());
        assertTrue(asksFanKui(revived), "救回後要補問反饋：" + revived);
        assertTrue(revived.stream().noneMatch(e -> e instanceof AskKillEvent), "反饋結算前不能同時問下一位出殺：" + revived);

        List<DomainEvent> afterFanKui = game.playerUseSkillEffect("player-b", "反饋", "SKIP", null, null);

        assertTrue(afterFanKui.stream().anyMatch(e -> e instanceof AskKillEvent k && k.getPlayerId().equals("player-c")),
                "反饋結算完才繼續問 c：" + afterFanKui);
        assertEquals("player-c", game.getCurrentRound().getActivePlayer().getId());
    }

    @DisplayName("萬箭：救回後只問反饋，不同時問下一位出閃；反饋結算完才輪到 c")
    @Test
    public void arrowBarrageRevivalAsksFanKuiThenResumes() {
        Game game = givenSiMaYiAtOneHp();
        game.getPlayer("player-a").getHand().addCardToHand(List.of(new ArrowBarrage(SHA040)));
        game.playerPlayCard("player-a", SHA040.getCardId(), "player-a", PlayType.ACTIVE.getPlayType());
        game.playerPlayCard("player-b", "", "player-a", SKIP);                   // 司馬懿不出閃 → 1 → 0

        List<DomainEvent> revived = rescue(game);

        assertTrue(asksFanKui(revived), "救回後要補問反饋：" + revived);
        assertTrue(revived.stream().noneMatch(e -> e instanceof AskDodgeEvent), "反饋結算前不能同時問下一位出閃：" + revived);

        List<DomainEvent> afterFanKui = game.playerUseSkillEffect("player-b", "反饋", "SKIP", null, null);

        assertTrue(afterFanKui.stream().anyMatch(e -> e instanceof AskDodgeEvent d && d.getPlayerId().equals("player-c")),
                "反饋結算完才繼續問 c：" + afterFanKui);
    }
}
