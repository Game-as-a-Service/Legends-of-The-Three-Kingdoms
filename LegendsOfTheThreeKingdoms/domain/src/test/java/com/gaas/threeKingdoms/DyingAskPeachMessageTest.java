package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.events.AskPeachEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.PlayerDamagedEvent;
import com.gaas.threeKingdoms.events.PlayerDyingEvent;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.player.*;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.rolecard.RoleCard;
import com.gaas.threeKingdoms.round.Round;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.BS8008;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DyingAskPeachMessageTest {

    @DisplayName("扣血、瀕死、求桃的訊息要寫出是哪位武將；有人不出桃後改問下一位，訊息也跟著換人")
    @Test
    public void dyingAndAskPeachMessagesNameThePlayers() {
        Game game = new Game();
        game.initDeck();
        Player playerA = buildPlayer("player-a", General.呂蒙, 1, Role.MONARCH);
        Player playerB = buildPlayer("player-b", General.張飛, 4, Role.REBEL);
        Player playerC = buildPlayer("player-c", General.甘寧, 4, Role.MINISTER);
        Player playerD = buildPlayer("player-d", General.黃蓋, 4, Role.TRAITOR);
        playerB.getHand().addCardToHand(Arrays.asList(new Kill(BS8008)));

        game.setPlayers(Arrays.asList(playerA, playerB, playerC, playerD));
        game.setCurrentRound(new Round(playerB));
        game.enterPhase(new Normal(game));

        // 張飛 對 呂蒙 出殺，呂蒙 不出閃 → 扣血進入瀕死，先問自己
        game.playerPlayCard(playerB.getId(), BS8008.getCardId(), playerA.getId(), "active");
        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), "", playerB.getId(), "skip");

        assertEquals("呂蒙 扣 1 點血", find(events, PlayerDamagedEvent.class).getMessage());
        assertEquals("呂蒙 已瀕臨死亡", find(events, PlayerDyingEvent.class).getMessage());
        AskPeachEvent askSelf = find(events, AskPeachEvent.class);
        assertEquals("player-a", askSelf.getPlayerId());
        assertEquals("要求 呂蒙 出桃（救 呂蒙）", askSelf.getMessage());

        // 呂蒙 不出桃 → 改問 張飛
        events = game.playerPlayCard(playerA.getId(), "", playerA.getId(), "skip");
        AskPeachEvent askB = find(events, AskPeachEvent.class);
        assertEquals("player-b", askB.getPlayerId());
        assertEquals("要求 張飛 出桃（救 呂蒙）", askB.getMessage());

        // 張飛 不出桃 → 改問 甘寧
        events = game.playerPlayCard(playerB.getId(), "", playerA.getId(), "skip");
        AskPeachEvent askC = find(events, AskPeachEvent.class);
        assertEquals("player-c", askC.getPlayerId());
        assertEquals("要求 甘寧 出桃（救 呂蒙）", askC.getMessage());
    }

    private static <T extends DomainEvent> T find(List<DomainEvent> events, Class<T> type) {
        return events.stream().filter(type::isInstance).map(type::cast).findFirst()
                .orElseThrow(() -> new AssertionError("missing " + type.getSimpleName() + " in " + events));
    }

    private static Player buildPlayer(String id, General general, int hp, Role role) {
        return PlayerBuilder.construct()
                .withId(id)
                .withBloodCard(new BloodCard(hp))
                .withGeneralCard(new GeneralCard(general))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(role))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();
    }
}
