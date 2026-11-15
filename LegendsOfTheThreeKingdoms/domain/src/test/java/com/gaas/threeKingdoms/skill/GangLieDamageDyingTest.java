package com.gaas.threeKingdoms.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.behavior.DyingAskPeachBehavior;
import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.events.AskPeachEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.PlayerDyingEvent;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.scrollcard.Dismantle;
import com.gaas.threeKingdoms.handcard.scrollcard.Duel;
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
 * 剛烈反傷（來源選擇受 1 點傷害）原本只 source.damage(1)：1 血來源 HP 歸 0 也不問桃，
 * 擊殺獎勵也找不到人。改走 Game#dealSkillDamage，傷害來源為夏侯惇。
 * 情境：a = 甘寧（反賊、回合主、1 血）對 b = 夏侯惇（主公）出決鬥，夏侯惇不出殺受傷 → 剛烈判定黑桃生效
 * → 甘寧選 DAMAGE。
 */
public class GangLieDamageDyingTest {

    private static final String SKIP = PlayType.SKIP.getPlayType();

    private Game givenRebelDuelsXiaHouDun() {
        Game game = new Game();
        game.initDeck();
        Player a = build("player-a", General.甘寧, Role.REBEL);
        Player b = build("player-b", General.夏侯惇, Role.MONARCH);
        Player c = build("player-c", General.甘寧, Role.MINISTER);
        Player d = build("player-d", General.甘寧, Role.TRAITOR);
        game.setPlayers(asList(a, b, c, d));
        game.setCurrentRound(new Round(a));
        game.enterPhase(new Normal(game));
        a.getBloodCard().setHp(1);
        a.getHand().addCardToHand(List.of(new Duel(SSA001)));
        game.getDeck().add(List.of(new Dismantle(SS3003)));   // Stack 頂：剛烈判定黑桃3 → 生效
        return game;
    }

    private Player build(String id, General general, Role role) {
        return PlayerBuilder.construct().withId(id)
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(general))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(role))
                .withHand(new Hand()).withEquipment(new Equipment()).build();
    }

    /** 決鬥 → 夏侯惇受傷 → 剛烈判定生效 → 甘寧選受 1 點傷害。 */
    private List<DomainEvent> gangLieDamagesRebel(Game game) {
        game.playerPlayCard("player-a", SSA001.getCardId(), "player-b", PlayType.ACTIVE.getPlayType());
        game.playerPlayCard("player-b", "", "player-a", SKIP);                 // 夏侯惇不出殺 → 受傷
        game.playerUseSkillEffect("player-b", "剛烈", "ACCEPT", null, null);  // 判定黑桃 → 生效，問甘寧
        return game.playerUseSkillEffect("player-a", "剛烈", "DAMAGE", null, null);
    }

    @DisplayName("1 血來源選受剛烈傷害 → 進入瀕死求桃，傷害來源記為夏侯惇")
    @Test
    public void oneHpSourceEntersDying() {
        Game game = givenRebelDuelsXiaHouDun();

        List<DomainEvent> events = gangLieDamagesRebel(game);

        assertEquals(0, game.getPlayer("player-a").getHP());
        assertTrue(events.stream().anyMatch(e -> e instanceof PlayerDyingEvent));
        assertTrue(events.stream().anyMatch(e -> e instanceof AskPeachEvent));
        DyingAskPeachBehavior dying = assertInstanceOf(DyingAskPeachBehavior.class, game.peekTopBehavior());
        assertEquals("player-b", dying.getPendingAttackerPlayerId());
    }

    @DisplayName("沒人救 → 反賊甘寧死亡，擊殺者夏侯惇摸 3 張獎勵")
    @Test
    public void rebelKilledByGangLieRewardsXiaHouDun() {
        Game game = givenRebelDuelsXiaHouDun();
        gangLieDamagesRebel(game);
        int xiaHouHand = game.getPlayer("player-b").getHandSize();

        for (String p : List.of("player-a", "player-b", "player-c", "player-d")) {
            game.playerPlayCard(p, "", "player-a", SKIP);
        }

        assertTrue(game.getPlayer("player-a").isAlreadyDeath());
        // 擊殺獎勵 3 張；甘寧是回合玩家，死後輪到下家夏侯惇，摸牌階段再摸 2 張
        assertEquals(xiaHouHand + 3 + 2, game.getPlayer("player-b").getHandSize(), "剛烈的傷害來源是夏侯惇 → 擊殺反賊摸 3");
        assertEquals("player-b", game.getCurrentRound().getCurrentRoundPlayer().getId());
    }

    @DisplayName("被桃救回 → 回 1 血，回到甘寧自己的出牌階段")
    @Test
    public void rescuedSourceContinuesOwnTurn() {
        Game game = givenRebelDuelsXiaHouDun();
        game.getPlayer("player-c").getHand().addCardToHand(List.of(new Peach(BH3029)));
        gangLieDamagesRebel(game);

        game.playerPlayCard("player-a", "", "player-a", SKIP);
        game.playerPlayCard("player-b", "", "player-a", SKIP);
        game.playerPlayCard("player-c", BH3029.getCardId(), "player-a", PlayType.ACTIVE.getPlayType());

        assertEquals(1, game.getPlayer("player-a").getHP());
        assertTrue(game.isTopBehaviorEmpty());
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
    }
}
