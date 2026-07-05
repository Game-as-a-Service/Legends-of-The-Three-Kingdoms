package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.AskKillEvent;
import com.gaas.threeKingdoms.events.AskSkillEffectEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.SkillEffectEvent;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.VirtualKill;
import com.gaas.threeKingdoms.player.Player;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * 激將（主公技）等待蜀將回應 — 依序詢問每個存活蜀勢力武將是否代替主公劉備打出殺。
 * Mirror {@link WaitingHuJiaResponseBehavior}；回應走通用 useSkillEffect（skillName=激將）。
 *
 * ACCEPT（cardIds[0] = 蜀將手中的殺）：殺棄入墓地、pop self、由 parent 的
 * acceptVirtualKillResponse 通道接手（南蠻 / 決鬥皆已實作該通道）。
 * DECLINE：問下一位蜀將；全拒 → fallback AskKillEvent(劉備)。
 */
@Getter
public class WaitingJiJiangResponseBehavior extends Behavior {

    private final String liuBeiPlayerId;
    private final List<String> shuOrder;
    private int currentIndex;

    public WaitingJiJiangResponseBehavior(Game game, Player liuBei, List<String> shuOrder, int currentIndex) {
        super(game, liuBei, List.of(liuBei.getId()), liuBei, null,
                PlayType.SYSTEM_INTERNAL.getPlayType(), null, false, false, true);
        this.liuBeiPlayerId = liuBei.getId();
        this.shuOrder = List.copyOf(shuOrder);
        this.currentIndex = currentIndex;
    }

    public WaitingJiJiangResponseBehavior(Game game, Player liuBei, List<String> shuOrder) {
        this(game, liuBei, shuOrder, 0);
    }

    public String getCurrentShu() {
        return shuOrder.get(currentIndex);
    }

    public List<DomainEvent> resolveChoice(String respondingPlayerId, String choice, List<String> cardIds) {
        if (!getCurrentShu().equals(respondingPlayerId)) {
            throw new IllegalStateException(String.format(
                    "player %s is not the current Shu reactor (%s)", respondingPlayerId, getCurrentShu()));
        }
        if ("ACCEPT".equals(choice)) {
            return resolveAccept(respondingPlayerId, cardIds);
        }
        if ("DECLINE".equals(choice)) {
            return resolveDecline(respondingPlayerId);
        }
        throw new IllegalArgumentException("Invalid JiJiang choice: " + choice);
    }

    private List<DomainEvent> resolveAccept(String shuPlayerId, List<String> cardIds) {
        if (cardIds == null || cardIds.size() != 1) {
            throw new IllegalArgumentException("激將 ACCEPT requires exactly 1 kill card");
        }
        String killCardId = cardIds.get(0);
        Player shu = game.getPlayer(shuPlayerId);
        HandCard killCard = shu.getHand().getCard(killCardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not in hand: " + killCardId));
        if (!(killCard instanceof Kill)) {
            throw new IllegalArgumentException("Card is not a Kill: " + killCardId);
        }

        shu.playCard(killCardId);
        game.getGraveyard().add(killCard);

        isOneRound = true;
        game.removeCompletedBehaviors();

        Behavior parent = game.peekTopBehavior();
        List<DomainEvent> events = new ArrayList<>();
        events.add(new SkillEffectEvent("激將", shuPlayerId, true, List.of(killCardId), liuBeiPlayerId));
        // 視為劉備打出殺 — 走南蠻/決鬥既有的 virtual kill response 通道
        events.addAll(parent.acceptVirtualKillResponse(liuBeiPlayerId, null, new VirtualKill(), List.of(killCardId)));
        return events;
    }

    private List<DomainEvent> resolveDecline(String shuPlayerId) {
        List<DomainEvent> events = new ArrayList<>();
        events.add(new SkillEffectEvent("激將", shuPlayerId, false, List.of(), liuBeiPlayerId));

        if (currentIndex + 1 < shuOrder.size()) {
            currentIndex++;
            Player next = game.getPlayer(getCurrentShu());
            game.getCurrentRound().setActivePlayer(next);
            events.add(new AskSkillEffectEvent("激將", next.getId(), List.of(), liuBeiPlayerId));
            events.add(game.getGameStatusEvent(shuPlayerId + " 拒絕激將，詢問下一位蜀勢力"));
            return events;
        }
        isOneRound = true;
        Player liuBei = game.getPlayer(liuBeiPlayerId);
        game.getCurrentRound().setActivePlayer(liuBei);
        events.add(new AskKillEvent(liuBeiPlayerId));
        events.add(game.getGameStatusEvent("激將全部拒絕，回到主公出殺"));
        return events;
    }
}
