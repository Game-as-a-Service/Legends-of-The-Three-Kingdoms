package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.behavior.HuJiaCompatibleAskDodgeBehavior;
import com.gaas.threeKingdoms.events.AskDodgeEvent;
import com.gaas.threeKingdoms.events.AskHuJiaEffectEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.HuJiaEffectEvent;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.player.Player;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.gaas.threeKingdoms.handcard.PlayCard.isDodgeCard;

/**
 * 護駕（主公技）等待 Wei 武將回應 — 依序詢問每個存活 Wei 武將是否代替主公曹操出閃。
 *
 * behaviorPlayer = 曹操（被代替者，AskDodge 原本要問的人）
 * weiOrder       = 完整 Wei polling 順序（以曹操為起點，下家開始）
 * currentIndex   = 目前正在詢問的 Wei index
 *
 * ACCEPT：currentWei 打出指定 dodge cardId → 該閃進墓地、pop self、由 parent
 * {@link HuJiaCompatibleAskDodgeBehavior#acceptDodgeFromHuJia} 接手後續（GDCB / SPA / AOE polling）
 *
 * DECLINE：
 *   - 若還有下一個 Wei → 切 currentWei、emit 下一個 AskHuJiaEffectEvent、activePlayer 換人
 *   - 若已是最後一個 → pop self、emit 原本的 AskDodgeEvent(曹操)、activePlayer 切回曹操
 *
 * Stack 隱性假設（ACCEPT 路徑）：本 behavior 是由 {@link com.gaas.threeKingdoms.skill.wei.HuJiaSkill}
 * 在 {@link com.gaas.threeKingdoms.skill.registry.SkillEngine#beforeAskDodge} 內 push 在原 emit AskDodge 的
 * host 之上，因此 pop self 後 stack 第二格必定是該 {@link HuJiaCompatibleAskDodgeBehavior}。若未來有
 * 其他 behavior 在 HuJia push 之後又插入額外行為（堆出 [host, X, WaitingHuJia]），ACCEPT 路徑 peek
 * 到的 parent 會是 X 而非 host → instanceof 檢查會丟例外（已加守門）。新增此類介入時須一併處理。
 */
@Getter
public class WaitingHuJiaResponseBehavior extends Behavior {

    private final String caoCaoPlayerId;
    private final List<String> weiOrder;
    private int currentIndex;

    public WaitingHuJiaResponseBehavior(Game game, Player caoCao, List<String> weiOrder, int currentIndex) {
        super(game,
                caoCao,
                List.of(caoCao.getId()),
                caoCao,
                null,
                PlayType.SYSTEM_INTERNAL.getPlayType(),
                null,
                false,
                false,
                true);
        this.caoCaoPlayerId = caoCao.getId();
        this.weiOrder = List.copyOf(weiOrder);
        this.currentIndex = currentIndex;
    }

    public WaitingHuJiaResponseBehavior(Game game, Player caoCao, List<String> weiOrder) {
        this(game, caoCao, weiOrder, 0);
    }

    public String getCurrentWei() {
        return weiOrder.get(currentIndex);
    }

    public List<DomainEvent> resolveChoice(String respondingPlayerId, AskHuJiaEffectEvent.Choice choice, String dodgeCardId) {
        String expected = getCurrentWei();
        if (!expected.equals(respondingPlayerId)) {
            throw new IllegalStateException(
                    String.format("player %s is not the current Wei reactor (%s)", respondingPlayerId, expected));
        }

        if (choice == AskHuJiaEffectEvent.Choice.ACCEPT) {
            return resolveAccept(respondingPlayerId, dodgeCardId);
        } else if (choice == AskHuJiaEffectEvent.Choice.DECLINE) {
            return resolveDecline(respondingPlayerId);
        } else {
            throw new IllegalArgumentException("Invalid HuJia choice: " + choice);
        }
    }

    private List<DomainEvent> resolveAccept(String weiPlayerId, String dodgeCardId) {
        if (dodgeCardId == null || dodgeCardId.isEmpty()) {
            throw new IllegalArgumentException("ACCEPT requires a dodge cardId");
        }
        Player wei = game.getPlayer(weiPlayerId);
        HandCard handCard = wei.getHand().getCard(dodgeCardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not in Wei's hand: " + dodgeCardId));
        if (!isDodgeCard(handCard.getId())) {
            throw new IllegalArgumentException("Card is not a Dodge: " + dodgeCardId);
        }

        // Wei 失去這張閃，移到墓地
        wei.playCard(dodgeCardId);
        game.getGraveyard().add(handCard);

        // Pop self（必須先 pop，下面 dispatch 才會把 parent 接到 stack 頂）
        isOneRound = true;
        game.removeCompletedBehaviors();

        Behavior parent = game.peekTopBehavior();
        if (!(parent instanceof HuJiaCompatibleAskDodgeBehavior compatibleParent)) {
            throw new IllegalStateException(
                    "HuJia parent behavior does not implement HuJiaCompatibleAskDodgeBehavior: "
                            + parent.getClass().getSimpleName());
        }

        List<DomainEvent> events = new ArrayList<>();
        events.add(new HuJiaEffectEvent(weiPlayerId, caoCaoPlayerId, true, dodgeCardId));
        events.addAll(compatibleParent.acceptDodgeFromHuJia(caoCaoPlayerId, weiPlayerId, dodgeCardId));
        return events;
    }

    private List<DomainEvent> resolveDecline(String weiPlayerId) {
        List<DomainEvent> events = new ArrayList<>();
        events.add(new HuJiaEffectEvent(weiPlayerId, caoCaoPlayerId, false, null));

        if (currentIndex + 1 < weiOrder.size()) {
            currentIndex++;
            String nextWei = getCurrentWei();
            Player nextWeiPlayer = game.getPlayer(nextWei);
            game.getCurrentRound().setActivePlayer(nextWeiPlayer);
            events.add(new AskHuJiaEffectEvent(nextWei, caoCaoPlayerId, dodgeCardIdsInHand(nextWeiPlayer)));
            events.add(game.getGameStatusEvent(weiPlayerId + " 拒絕護駕，詢問下一位魏勢力"));
            return events;
        }

        // 所有 Wei 都拒絕 → pop self、fallback 到原本的 AskDodge(曹操)
        isOneRound = true;
        Player caoCao = game.getPlayer(caoCaoPlayerId);
        game.getCurrentRound().setActivePlayer(caoCao);
        events.add(new AskDodgeEvent(caoCaoPlayerId));
        events.add(game.getGameStatusEvent("護駕全部拒絕，回到主公出閃"));
        return events;
    }

    public static List<String> dodgeCardIdsInHand(Player player) {
        return player.getHand().getCards().stream()
                .filter(c -> isDodgeCard(c.getId()))
                .map(HandCard::getId)
                .collect(Collectors.toList());
    }
}
