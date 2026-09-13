package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.events.AskDodgeEvent;
import com.gaas.threeKingdoms.events.AskKillEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.scrollcard.ArrowBarrage;
import com.gaas.threeKingdoms.handcard.scrollcard.BarbarianInvasion;
import com.gaas.threeKingdoms.player.*;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.rolecard.RoleCard;
import com.gaas.threeKingdoms.round.Round;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 使用者回報：「馬超被南蠻死了後變殭屍」。
 * <p>
 * AOE（南蠻/萬箭）輪詢的**最後一位** reactor 被扣死時，BI/AB 的 advanceAfterDamage
 * 只設 isOneRound=true，currentReactionPlayer 仍指著那位瀕死者（沒有下一位可推進）。
 * 瀕死結算完（沒人給桃 → 死亡）後 DyingAskPeachBehavior 的 resume hook 若照舊執行，
 * 就會對死者再發 AskKill/AskDodge 並把他設成 activePlayer，死人於是能繼續出牌；
 * 且因 events 帶了 ask 事件，外層的 goNextRound / setActivePlayer 整段被跳過。
 */
public class AoeLastReactorDiesTest {

    private Game createGame() {
        Game game = new Game();
        game.initDeck();
        // d = 內奸且僅剩 1 滴血 → 被 AOE 扣死但不觸發遊戲結束
        Player a = build("player-a", General.甘寧, Role.MONARCH, 4);
        Player b = build("player-b", General.甘寧, Role.MINISTER, 4);
        Player c = build("player-c", General.孫權, Role.REBEL, 4);
        Player d = build("player-d", General.馬超, Role.TRAITOR, 1);
        game.setPlayers(asList(a, b, c, d));
        game.setCurrentRound(new Round(a));
        game.enterPhase(new Normal(game));
        return game;
    }

    private Player build(String id, General general, Role role, int hp) {
        return PlayerBuilder.construct().withId(id)
                .withBloodCard(new BloodCard(hp))
                .withGeneralCard(new GeneralCard(general))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(role))
                .withHand(new Hand()).withEquipment(new Equipment()).build();
    }

    /** 全員（含瀕死者自己）放棄出桃 → 瀕死者死亡；回傳最後一次回應產生的 events。 */
    private List<DomainEvent> allDeclinePeach(Game game, String dyingPlayerId) {
        game.playerPlayCard(dyingPlayerId, "", dyingPlayerId, PlayType.SKIP.getPlayType());
        game.playerPlayCard("player-a", "", dyingPlayerId, PlayType.SKIP.getPlayType());
        game.playerPlayCard("player-b", "", dyingPlayerId, PlayType.SKIP.getPlayType());
        return game.playerPlayCard("player-c", "", dyingPlayerId, PlayType.SKIP.getPlayType());
    }

    @DisplayName("南蠻最後一位 reactor 被扣死 → 不可再對死者發 AskKill，activePlayer 回到回合玩家")
    @Test
    public void barbarianInvasionLastReactorDies_noAskKillToDeadPlayer() {
        Game game = createGame();
        game.getPlayer("player-a").getHand().addCardToHand(new BarbarianInvasion(SS7007));
        game.getPlayer("player-b").getHand().addCardToHand(new Kill(BS8008));
        game.getPlayer("player-c").getHand().addCardToHand(new Kill(BS9009));

        game.playerPlayCard("player-a", SS7007.getCardId(), "player-a", "active");
        game.playerPlayCard("player-b", BS8008.getCardId(), "player-a", PlayType.ACTIVE.getPlayType());
        game.playerPlayCard("player-c", BS9009.getCardId(), "player-a", PlayType.ACTIVE.getPlayType());

        // d 是最後一位 reactor，沒有殺 → skip → 扣死 → 瀕死
        game.playerPlayCard("player-d", "", "player-a", PlayType.SKIP.getPlayType());
        assertEquals("player-d", game.getCurrentRound().getActivePlayer().getId(), "瀕死者自己先被問桃");

        List<DomainEvent> deathEvents = allDeclinePeach(game, "player-d");

        Player dead = game.getPlayer("player-d");
        assertTrue(dead.isAlreadyDeath(), "d 應已死亡");
        assertTrue(deathEvents.stream().noneMatch(e -> e instanceof AskKillEvent),
                "不可對已死的最後一位 reactor 再發 AskKillEvent（殭屍成因）");
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId(),
                "activePlayer 必須回到回合玩家，不能留在死者身上");
        assertTrue(game.getTopBehavior().isEmpty(), "南蠻輪詢已結束，behavior stack 應清空");
    }

    @DisplayName("萬箭最後一位 reactor 被扣死 → 不可再對死者發 AskDodge，activePlayer 回到回合玩家")
    @Test
    public void arrowBarrageLastReactorDies_noAskDodgeToDeadPlayer() {
        Game game = createGame();
        game.getPlayer("player-a").getHand().addCardToHand(new ArrowBarrage(SHA040));

        game.playerPlayCard("player-a", SHA040.getCardId(), "player-a", "active");
        game.playerPlayCard("player-b", "", "player-a", PlayType.SKIP.getPlayType());
        game.playerPlayCard("player-c", "", "player-a", PlayType.SKIP.getPlayType());

        // d 是最後一位 reactor，沒有閃 → skip → 扣死 → 瀕死
        game.playerPlayCard("player-d", "", "player-a", PlayType.SKIP.getPlayType());
        assertEquals("player-d", game.getCurrentRound().getActivePlayer().getId());

        List<DomainEvent> deathEvents = allDeclinePeach(game, "player-d");

        assertTrue(game.getPlayer("player-d").isAlreadyDeath());
        assertTrue(deathEvents.stream().noneMatch(e -> e instanceof AskDodgeEvent),
                "不可對已死的最後一位 reactor 再發 AskDodgeEvent");
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
        assertTrue(game.getTopBehavior().isEmpty());
    }

    @DisplayName("殭屍守門：死亡玩家即使被設為 activePlayer 也不能出牌")
    @Test
    public void deadPlayerCannotPlayCard_evenIfSetAsActivePlayer() {
        Game game = createGame();
        game.getPlayer("player-a").getHand().addCardToHand(new BarbarianInvasion(SS7007));
        game.getPlayer("player-b").getHand().addCardToHand(new Kill(BS8008));
        game.getPlayer("player-c").getHand().addCardToHand(new Kill(BS9009));

        game.playerPlayCard("player-a", SS7007.getCardId(), "player-a", "active");
        game.playerPlayCard("player-b", BS8008.getCardId(), "player-a", PlayType.ACTIVE.getPlayType());
        game.playerPlayCard("player-c", BS9009.getCardId(), "player-a", PlayType.ACTIVE.getPlayType());
        game.playerPlayCard("player-d", "", "player-a", PlayType.SKIP.getPlayType());
        allDeclinePeach(game, "player-d");

        Player dead = game.getPlayer("player-d");
        assertTrue(dead.isAlreadyDeath());

        // 模擬狀態機漏洞把死者設回 activePlayer（本 bug 的第二層破口：
        // checkIsCurrentRoundValid 原本只比對 id，不檢查生死）
        dead.getHand().addCardToHand(new ArrowBarrage(SHA040));
        game.getCurrentRound().setActivePlayer(dead);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                game.playerPlayCard("player-d", SHA040.getCardId(), "player-d", "active"));
        assertTrue(ex.getMessage().contains("already dead"), ex.getMessage());
    }
}
