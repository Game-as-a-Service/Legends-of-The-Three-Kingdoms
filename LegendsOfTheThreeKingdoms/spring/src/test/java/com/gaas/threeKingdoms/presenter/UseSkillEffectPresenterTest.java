package com.gaas.threeKingdoms.presenter;

import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.GameStatusEvent;
import com.gaas.threeKingdoms.events.PlayerEvent;
import com.gaas.threeKingdoms.events.RoundEvent;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.player.HealthStatus;
import com.gaas.threeKingdoms.rolecard.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.gaas.threeKingdoms.e2e.MockUtil.createPlayer;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 使用者回報：鬼才換牌結束後前端看到 activePlayer 停在司馬懿 —
 * 技能 resolve 先發 GameStatusEvent（activePlayer=被詢問者）再推進，presenter 取第一個就拿到舊快照。
 */
public class UseSkillEffectPresenterTest {

    private GameStatusEvent status(String activePlayer, String message) {
        List<PlayerEvent> seats = List.of(
                new PlayerEvent(createPlayer("player-a", 4, General.甘寧, HealthStatus.ALIVE, Role.MONARCH)),
                new PlayerEvent(createPlayer("player-c", 4, General.司馬懿, HealthStatus.ALIVE, Role.REBEL)));
        return new GameStatusEvent("g1", seats,
                new RoundEvent("Action", "player-a", activePlayer, null, false), "Normal", message);
    }

    @DisplayName("多個 GameStatusEvent → round data 取最後一個（推進後的最終狀態）")
    @Test
    public void usesLastGameStatusEventForRoundData() {
        List<DomainEvent> events = List.of(
                status("player-c", "player-c 發動鬼才"),   // resume 前快照（舊）
                status("player-a", "發動八卦陣效果"),      // 八卦陣結算後
                status("player-a", "鬼才 結算"));          // 最終快照

        UseSkillEffectPresenter presenter = new UseSkillEffectPresenter();
        presenter.renderEvents(events);

        for (UseSkillEffectPresenter.GameViewModel vm : presenter.present()) {
            assertEquals("player-a", vm.getData().getRound().getActivePlayer(),
                    "每位玩家收到的 round.activePlayer 應為最終狀態 A，而非第一個快照的 C");
            assertEquals("鬼才 結算", vm.getMessage());
        }
        assertEquals(2, presenter.present().size());
    }
}
