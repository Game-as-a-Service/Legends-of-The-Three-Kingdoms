package com.gaas.threeKingdoms.Ward;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.EquipmentPlayType;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.EightDiagramTactic;
import com.gaas.threeKingdoms.handcard.scrollcard.ArrowBarrage;
import com.gaas.threeKingdoms.handcard.scrollcard.Ward;
import com.gaas.threeKingdoms.round.Stage;
import com.gaas.threeKingdoms.skill.PassiveSkillTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 萬箭 → 第二階段無懈（是否替下一位抵消）被放棄 → 下一位有八卦陣 → 問八卦陣。
 * 原本 WardBehavior 在效果發動後一律把 stage 蓋回 Normal，八卦陣 handler 檢查 stage 失敗（線上回 400，諸葛亮卡住）。
 * 座位 a（甄姬，出萬箭）→ b（諸葛亮：無懈 + 八卦陣）→ c → d。
 */
public class WardArrowBarrageEightDiagramTest extends PassiveSkillTestBase {

    private Game givenArrowBarrageToWardHolderWithEightDiagram() {
        Game game = createGame(General.甄姬, General.諸葛亮, General.甘寧, General.甘寧);
        game.getPlayer("player-a").getHand().addCardToHand(List.of(new ArrowBarrage(SHA040)));
        game.getPlayer("player-b").getHand().addCardToHand(List.of(new Ward(SCQ077), new Dodge(BH2028)));
        game.getPlayer("player-b").getEquipment().setArmor(new EightDiagramTactic(EC2067));
        game.playerPlayCard("player-a", SHA040.getCardId(), "player-a", "active");
        game.playWardCard("player-b", "", PlayType.SKIP.getPlayType()); // 第一階段：不抵消整張萬箭
        return game;
    }

    @DisplayName("第二階段無懈放棄 → 問八卦陣時 stage 必須是 Wait_Equipment_Effect")
    @Test
    public void eightDiagramAskKeepsEquipmentStage() {
        Game game = givenArrowBarrageToWardHolderWithEightDiagram();

        List<DomainEvent> events = game.playWardCard("player-b", "", PlayType.SKIP.getPlayType());

        assertTrue(events.stream().anyMatch(e -> e instanceof AskPlayEquipmentEffectEvent a
                && "player-b".equals(a.getPlayerId())), events.toString());
        assertEquals(Stage.Wait_Equipment_Effect, game.getCurrentRound().getStage());
        assertEquals("player-b", game.getCurrentRound().getActivePlayer().getId());
    }

    @DisplayName("接著發動八卦陣：判定黑 → 失敗、問閃（原本拋 stage not Wait_Equipment_Effect）")
    @Test
    public void eightDiagramFailsThenAsksDodge() {
        Game game = givenArrowBarrageToWardHolderWithEightDiagram();
        game.getDeck().putBackOnTop(List.of(new Kill(BS9009)));
        game.playWardCard("player-b", "", PlayType.SKIP.getPlayType());

        List<DomainEvent> events = game.playerUseEquipment("player-b", EC2067.getCardId(), "player-b", EquipmentPlayType.ACTIVE);

        assertTrue(events.stream().anyMatch(e -> e instanceof AskDodgeEvent a && "player-b".equals(a.getPlayerId())),
                events.toString());
    }

    @DisplayName("接著發動八卦陣：判定紅 → 成功、不扣血，輪到下一位（先問是否替他出無懈）")
    @Test
    public void eightDiagramSucceedsThenNextReactor() {
        Game game = givenArrowBarrageToWardHolderWithEightDiagram();
        game.getDeck().putBackOnTop(List.of(new Peach(BH4030)));
        game.playWardCard("player-b", "", PlayType.SKIP.getPlayType());

        List<DomainEvent> events = game.playerUseEquipment("player-b", EC2067.getCardId(), "player-b", EquipmentPlayType.ACTIVE);

        assertEquals(4, game.getPlayer("player-b").getHP());
        // 輪到 c：b 手上仍有無懈 → 先問是否替 c 抵消
        assertTrue(events.stream().anyMatch(e -> e instanceof WaitForWardEvent), events.toString());
        assertEquals(Stage.Wait_Accept_Ward_Effect, game.getCurrentRound().getStage());
        assertEquals("player-c", game.getTopBehavior().get(0).getCurrentReactionPlayer().getId());
    }
}
