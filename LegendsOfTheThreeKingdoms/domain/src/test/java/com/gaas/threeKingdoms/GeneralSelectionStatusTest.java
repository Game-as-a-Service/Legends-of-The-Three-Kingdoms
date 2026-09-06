package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.GeneralSelectionStatusEvent;
import com.gaas.threeKingdoms.events.InitialEndEvent;
import com.gaas.threeKingdoms.events.RoundStartEvent;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.player.Hand;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.rolecard.RoleCard;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 選將進度廣播（issue #237）：每次有人選完武將就發 GeneralSelectionStatusEvent。
 * 先前 othersChoosePlayerGeneral 在還有人沒選完時回空 events → 前端無從得知進度。
 * 進度事件不帶武將 id（非主公暗選，由 InitialEndEvent 開局揭曉）。
 */
public class GeneralSelectionStatusTest {

    private Game createInitialGame() {
        Player a = createPlayer("player-a", Role.MONARCH);
        Player b = createPlayer("player-b", Role.MINISTER);
        Player c = createPlayer("player-c", Role.REBEL);
        Player d = createPlayer("player-d", Role.TRAITOR);
        Game game = new Game("test-game", List.of(a, b, c, d));
        game.initDeck();
        return game;
    }

    private Player createPlayer(String id, Role role) {
        Player player = new Player();
        player.setId(id);
        player.setHand(new Hand());
        player.setRoleCard(new RoleCard(role));
        return player;
    }

    private Optional<GeneralSelectionStatusEvent> statusEvent(List<DomainEvent> events) {
        return events.stream()
                .filter(e -> e instanceof GeneralSelectionStatusEvent)
                .map(e -> (GeneralSelectionStatusEvent) e)
                .findFirst();
    }

    @DisplayName("主公選完武將 → 進度事件 1/4，pending 為其餘三位")
    @Test
    public void monarchChooseGeneral_emitsSelectionStatus() {
        Game game = createInitialGame();

        List<DomainEvent> events = game.monarchChoosePlayerGeneral(
                "player-a", General.甘寧.getGeneralId());

        GeneralSelectionStatusEvent status = statusEvent(events).orElseThrow();
        assertEquals(List.of("player-a"), status.getSelectedPlayerIds());
        assertEquals(List.of("player-b", "player-c", "player-d"), status.getPendingPlayerIds());
        assertFalse(status.isAllSelected());
        assertEquals("player-a 已選擇武將（1/4）", status.getMessage());
        assertEquals("test-game", status.getGameId());
        assertEquals(List.of("player-a", "player-b", "player-c", "player-d"), status.getPlayerIds());
    }

    @DisplayName("其他玩家選完（非最後一位）→ 進度事件 2/4（先前回空 events）")
    @Test
    public void othersChooseGeneral_midway_emitsSelectionStatus() {
        Game game = createInitialGame();
        game.monarchChoosePlayerGeneral("player-a", General.甘寧.getGeneralId());

        List<DomainEvent> events = game.othersChoosePlayerGeneral(
                "player-b", General.孫權.getGeneralId());

        assertEquals(1, events.size(), "選將中只發進度事件");
        GeneralSelectionStatusEvent status = statusEvent(events).orElseThrow();
        assertEquals(List.of("player-a", "player-b"), status.getSelectedPlayerIds());
        assertEquals(List.of("player-c", "player-d"), status.getPendingPlayerIds());
        assertFalse(status.isAllSelected());
        assertEquals("player-b 已選擇武將（2/4）", status.getMessage());
    }

    @DisplayName("最後一位選完 → 進度 allSelected=true，且照舊發 InitialEnd + RoundStart 開局")
    @Test
    public void lastPlayerChooseGeneral_emitsAllSelectedThenInitialEnd() {
        Game game = createInitialGame();
        game.monarchChoosePlayerGeneral("player-a", General.甘寧.getGeneralId());
        game.othersChoosePlayerGeneral("player-b", General.孫權.getGeneralId());
        game.othersChoosePlayerGeneral("player-c", General.孫權.getGeneralId());

        List<DomainEvent> events = game.othersChoosePlayerGeneral(
                "player-d", General.孫權.getGeneralId());

        GeneralSelectionStatusEvent status = statusEvent(events).orElseThrow();
        assertTrue(status.isAllSelected());
        assertEquals(4, status.getSelectedPlayerIds().size());
        assertTrue(status.getPendingPlayerIds().isEmpty());
        // 開局流程不變：InitialEnd + RoundStart 照舊，且進度事件在 InitialEnd 之前
        assertTrue(events.stream().anyMatch(e -> e instanceof InitialEndEvent));
        assertTrue(events.stream().anyMatch(e -> e instanceof RoundStartEvent));
        assertTrue(events.indexOf(status) < events.stream()
                        .filter(e -> e instanceof InitialEndEvent).findFirst().map(events::indexOf).orElseThrow(),
                "進度事件應在 InitialEnd 之前");
    }
}
