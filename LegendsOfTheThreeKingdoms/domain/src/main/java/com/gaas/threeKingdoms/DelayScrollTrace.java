package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.events.DelayScrollEvent;
import com.gaas.threeKingdoms.handcard.scrollcard.ScrollCard;
import com.gaas.threeKingdoms.player.Player;

import java.util.Collection;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * 判定區（延時錦囊）的追蹤 log：每張牌進出判定區、每次回合開始判定都記一筆。
 * <p>
 * 起因：使用者回報國色貼的樂不思蜀「有時候沒有判定就不見了」，事後從資料庫與 log 都無法還原經過。
 * 有了這些紀錄，下次可直接從 {@code docker logs} 看出牌是在哪一步、誰的回合離開判定區。
 * 用 JDK 內建的 java.util.logging（domain 不引入 log 套件），Spring Boot 會轉接到 Logback。
 */
public final class DelayScrollTrace {

    private static final Logger LOG = Logger.getLogger(DelayScrollTrace.class.getName());

    private DelayScrollTrace() {
    }

    public static void added(Player player, ScrollCard card) {
        LOG.info(() -> String.format("[判定區] + %s 放入 %s → %s",
                player.getId(), describe(card), describe(player.getDelayScrollCards())));
    }

    public static void removed(Player player, String cardId, String reason) {
        LOG.info(() -> String.format("[判定區] - %s 移除 %s（%s）→ %s",
                player.getId(), cardId, reason, describe(player.getDelayScrollCards())));
    }

    public static void judgementStart(String gameId, Player player) {
        LOG.info(() -> String.format("[判定區] game=%s %s 回合開始判定，判定區 %s",
                gameId, player.getId(), describe(player.getDelayScrollCards())));
    }

    public static void judging(String gameId, Player player, ScrollCard card) {
        LOG.info(() -> String.format("[判定區] game=%s %s 取出 %s 進行判定 → 剩 %s",
                gameId, player.getId(), describe(card), describe(player.getDelayScrollCards())));
    }

    private static String describe(Collection<? extends ScrollCard> cards) {
        return cards.stream().map(DelayScrollTrace::describe).collect(Collectors.joining(", ", "[", "]"));
    }

    private static String describe(ScrollCard card) {
        try {
            return card.getId() + "(" + DelayScrollEvent.of(card).getEffect() + ")";
        } catch (IllegalStateException e) {
            return card.getId() + "(" + card.getClass().getSimpleName() + ")";
        }
    }
}
