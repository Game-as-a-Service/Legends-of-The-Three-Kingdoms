package com.gaas.threeKingdoms.presenter;

import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.player.HealthStatus;
import com.gaas.threeKingdoms.rolecard.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.gaas.threeKingdoms.e2e.MockUtil.createPlayer;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 觀星的牌只有諸葛亮能看：per-player GameViewModel 建構時遮蔽其他玩家的 dataCardIds / 摸牌 cards。
 */
public class PrivateSkillCardMaskerTest {

    private static final List<String> PEEKED = List.of("BS8008", "BS9009", "BH3029", "BH4030");

    private GameStatusEvent status() {
        List<PlayerEvent> seats = List.of(
                new PlayerEvent(createPlayer("player-a", 4, General.甘寧, HealthStatus.ALIVE, Role.MONARCH)),
                new PlayerEvent(createPlayer("player-b", 3, General.諸葛亮, HealthStatus.ALIVE, Role.MINISTER)),
                new PlayerEvent(createPlayer("player-c", 4, General.甘寧, HealthStatus.ALIVE, Role.REBEL)));
        return new GameStatusEvent("g1", seats,
                new RoundEvent("Judgement", "player-b", "player-b", null, false), "Normal", "觀星");
    }

    private Map<String, UseSkillEffectPresenter.GameViewModel> render(List<DomainEvent> events) {
        UseSkillEffectPresenter presenter = new UseSkillEffectPresenter();
        presenter.renderEvents(events);
        return presenter.present().stream()
                .collect(Collectors.toMap(UseSkillEffectPresenter.GameViewModel::getPlayerId, Function.identity()));
    }

    @SuppressWarnings("unchecked")
    private <T extends ViewModel<?>> T find(UseSkillEffectPresenter.GameViewModel vm, Class<T> type) {
        return (T) vm.getEvents().stream().filter(type::isInstance).findFirst().orElseThrow();
    }

    @DisplayName("觀星詢問：諸葛亮看到 dataCardIds，其他人為空但仍收到詢問（skillName / options / message 不變）")
    @Test
    public void askGuanXingMaskedForOthers() {
        Map<String, UseSkillEffectPresenter.GameViewModel> byPlayer = render(List.of(
                new AskSkillEffectEvent("觀星", "player-b", PEEKED, null, List.of("ARRANGE", "SKIP"), "觀看牌堆頂 4 張"),
                status()));

        var own = find(byPlayer.get("player-b"), UseSkillEffectPresenter.AskSkillEffectViewModel.class);
        assertEquals(PEEKED, own.getData().getDataCardIds());

        for (String other : List.of("player-a", "player-c")) {
            var ask = find(byPlayer.get(other), UseSkillEffectPresenter.AskSkillEffectViewModel.class);
            assertEquals(List.of(), ask.getData().getDataCardIds(), other);
            assertEquals("觀星", ask.getData().getSkillName());
            assertEquals("player-b", ask.getData().getPlayerId());
            assertEquals(List.of("ARRANGE", "SKIP"), ask.getData().getOptions());
            assertEquals("觀看牌堆頂 4 張", ask.getMessage());
        }
    }

    @DisplayName("觀星 ARRANGE 結果與接續摸牌：只有諸葛亮看到牌 id")
    @Test
    public void arrangeResultAndFollowingDrawMaskedForOthers() {
        List<String> top = List.of("BH4030", "BH3029");
        Map<String, UseSkillEffectPresenter.GameViewModel> byPlayer = render(List.of(
                new SkillEffectEvent("觀星", "player-b", true, top, null),
                new DrawCardEvent(2, top, "摸牌", "g1", status().getSeats(), status().getRound(), "Normal", "player-b"),
                status()));

        var ownEffect = find(byPlayer.get("player-b"), UseSkillEffectPresenter.SkillEffectViewModel.class);
        assertEquals(top, ownEffect.getData().getDataCardIds());
        var ownDraw = find(byPlayer.get("player-b"), RoundStartPresenter.DrawCardViewModel.class);
        assertEquals(top, ownDraw.getData().getCards());

        for (String other : List.of("player-a", "player-c")) {
            var effect = find(byPlayer.get(other), UseSkillEffectPresenter.SkillEffectViewModel.class);
            assertEquals(List.of(), effect.getData().getDataCardIds(), other);
            assertTrue(effect.getData().isAccepted());
            var draw = find(byPlayer.get(other), RoundStartPresenter.DrawCardViewModel.class);
            assertEquals(List.of(), draw.getData().getCards(), other);
            assertEquals(2, draw.getData().getSize(), other + " 仍看得到張數");
        }
    }

    @DisplayName("其他技能（如反饋）的 dataCardIds 是公開資訊，不遮蔽")
    @Test
    public void otherSkillsNotMasked() {
        Map<String, UseSkillEffectPresenter.GameViewModel> byPlayer = render(List.of(
                new AskSkillEffectEvent("反饋", "player-c", List.of("EC2067"), "player-a", List.of("ACCEPT", "SKIP"), "反饋"),
                status()));
        for (String playerId : List.of("player-a", "player-b", "player-c")) {
            var ask = find(byPlayer.get(playerId), UseSkillEffectPresenter.AskSkillEffectViewModel.class);
            assertEquals(List.of("EC2067"), ask.getData().getDataCardIds(), playerId);
        }
    }
}
