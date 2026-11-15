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
 *
 * 三種情境（{@link Mode}）：
 * <ul>
 *   <li>{@code ASK_KILL}：南蠻 / 決鬥要求劉備打出殺（被動攔截）。</li>
 *   <li>{@code ACTIVE_KILL}：劉備出牌階段主動發動激將「使用」殺；ACCEPT → 視為劉備對
 *       {@link #targetPlayerId} 使用該殺（計入劉備本回合出殺次數）；全拒 → 回到劉備出牌階段。</li>
 *   <li>{@code BORROWED_SWORD}：劉備被借刀殺人要求對 {@link #targetPlayerId} 出殺；ACCEPT → 視為劉備出殺；
 *       全拒 → 回到劉備自己決定出殺 / 不出（無殺可出則直接交出武器）。</li>
 * </ul>
 */
@Getter
public class WaitingJiJiangResponseBehavior extends Behavior {

    public enum Mode { ASK_KILL, ACTIVE_KILL, BORROWED_SWORD }

    private final String liuBeiPlayerId;
    private final List<String> shuOrder;
    private int currentIndex;
    private final Mode mode;
    /** ACTIVE_KILL / BORROWED_SWORD：殺的目標；ASK_KILL 為 null。 */
    private final String targetPlayerId;

    public WaitingJiJiangResponseBehavior(Game game, Player liuBei, List<String> shuOrder, int currentIndex,
                                          Mode mode, String targetPlayerId) {
        super(game, liuBei, List.of(liuBei.getId()), liuBei, null,
                PlayType.SYSTEM_INTERNAL.getPlayType(), null, false, false, true);
        this.liuBeiPlayerId = liuBei.getId();
        this.shuOrder = List.copyOf(shuOrder);
        this.currentIndex = currentIndex;
        this.mode = mode == null ? Mode.ASK_KILL : mode;
        this.targetPlayerId = targetPlayerId;
    }

    public WaitingJiJiangResponseBehavior(Game game, Player liuBei, List<String> shuOrder, int currentIndex) {
        this(game, liuBei, shuOrder, currentIndex, Mode.ASK_KILL, null);
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
        // AskSkillEffectEvent 預設 options 為 ACCEPT/SKIP — SKIP 視同 DECLINE，前端照 options 畫按鈕才回得了
        if ("DECLINE".equals(choice) || "SKIP".equals(choice)) {
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
        if (mode == Mode.ACTIVE_KILL || mode == Mode.BORROWED_SWORD) {
            return resolveAcceptAsLiuBeiKill(shu, killCard);
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

    /**
     * 主動出殺 / 借刀殺人：蜀將的殺視為劉備使用 — 牌交到劉備手上後立即由劉備打出，
     * 沿用殺的既有流程（出殺次數、流離 / 鐵騎 / 八卦陣 / 閃 ...）。
     * 所有可能 throw 的驗證都在移動牌之前完成，避免 invalid 狀態下半途 mutate。
     * <p>
     * 主動出殺不走 playerPlayCard：目標已在發動激將時驗證過（攻擊範圍、空城），
     * 若代出者正是目標（如諸葛亮交出最後一張殺），重跑 handler 的空城檢查會在牌已移動後 throw。
     */
    private List<DomainEvent> resolveAcceptAsLiuBeiKill(Player shu, HandCard killCard) {
        Player liuBei = game.getPlayer(liuBeiPlayerId);
        Player target = game.getPlayer(targetPlayerId);
        if (mode == Mode.ACTIVE_KILL && !game.isInAttackRange(liuBei, target)) {
            throw new IllegalStateException(String.format("%s is not in attack range", targetPlayerId));
        }

        isOneRound = true;
        game.removeCompletedBehaviors();

        shu.playCard(killCard.getId());
        liuBei.getHand().addCardToHand(killCard);
        game.getCurrentRound().setActivePlayer(liuBei);

        List<DomainEvent> events = new ArrayList<>();
        events.add(new SkillEffectEvent("激將", shu.getId(), true, List.of(killCard.getId()), liuBeiPlayerId));
        if (mode == Mode.ACTIVE_KILL) {
            // 計入劉備本回合出殺次數（次數已於發動時驗證；連弩 / 咆哮不限次）
            game.getCurrentRound().isPlayedValidCard(killCard.getId());
            NormalActiveKillBehavior kill = new NormalActiveKillBehavior(game, liuBei,
                    new ArrayList<>(List.of(targetPlayerId)), liuBei, killCard.getId(),
                    PlayType.ACTIVE.getPlayType(), killCard);
            game.updateTopBehavior(kill);
            events.addAll(kill.playerAction());
            return events;
        }
        events.addAll(game.playerPlayCard(liuBeiPlayerId, killCard.getId(), targetPlayerId,
                PlayType.ACTIVE.getPlayType()));
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
        if (mode == Mode.ACTIVE_KILL) {
            // 無人代出 → 劉備未使用殺（不計次數），回到出牌階段
            events.add(game.getGameStatusEvent("激將全部拒絕，劉備繼續出牌"));
            return events;
        }
        if (mode == Mode.BORROWED_SWORD) {
            game.removeCompletedBehaviors();
            Behavior borrowedSword = game.peekTopBehavior();
            boolean canProduceKill = liuBei.getHand().getCards().stream().anyMatch(c -> c instanceof Kill)
                    || com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.EighteenSpanViperSpearCard.canUseAsKill(liuBei);
            if (!canProduceKill) {
                // 劉備自己也無殺 → 照借刀殺人規則交出武器
                events.addAll(borrowedSword.responseToPlayerAction(liuBeiPlayerId, borrowedSword.getBehaviorPlayer().getId(),
                        "", PlayType.SKIP.getPlayType()));
                return events;
            }
            events.add(new com.gaas.threeKingdoms.events.BorrowedSwordEvent(
                    borrowedSword.getCardId(), liuBeiPlayerId, targetPlayerId));
            events.add(game.getGameStatusEvent("激將全部拒絕，回到劉備自己出殺"));
            return events;
        }
        events.add(new AskKillEvent(liuBeiPlayerId));
        events.add(game.getGameStatusEvent("激將全部拒絕，回到主公出殺"));
        return events;
    }
}
