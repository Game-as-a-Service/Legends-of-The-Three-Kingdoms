package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.AskKillEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.PlayCardEvent;
import com.gaas.threeKingdoms.events.ViperSpearKillTriggerEvent;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.round.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.gaas.threeKingdoms.behavior.behavior.WardBehavior.WARD_TARGET_PLAYER_IDS;
import static com.gaas.threeKingdoms.behavior.behavior.WardBehavior.WARD_TRIGGER_PLAYER_ID;
import static com.gaas.threeKingdoms.handcard.PlayCard.isKillCard;
import static com.gaas.threeKingdoms.handcard.PlayCard.isSkip;

public class BarbarianInvasionBehavior extends Behavior implements com.gaas.threeKingdoms.behavior.JianXiongCompatibleTopBehavior {

    @Override
    public boolean isPollingCaller() {
        return true;
    }

    private boolean pollingStarted = false;

    public BarbarianInvasionBehavior(Game game, Player behaviorPlayer, List<String> reactionPlayers, Player currentReactionPlayer, String cardId, String playType, HandCard card) {
        super(game, behaviorPlayer, reactionPlayers, currentReactionPlayer, cardId, playType, card, true, false, false);
    }

    public boolean isPollingStarted() {
        return pollingStarted;
    }

    public void setPollingStarted(boolean pollingStarted) {
        this.pollingStarted = pollingStarted;
    }

    @Override
    public List<DomainEvent> playerAction() {
        List<DomainEvent> events = new ArrayList<>();
        playerPlayCard(behaviorPlayer, currentReactionPlayer, cardId);

        events.add(new PlayCardEvent("出牌", behaviorPlayer.getId(), "", cardId, playType));

        if (game.doesAnyPlayerHaveWard(behaviorPlayer.getId())) {
            // Phase 1: Ward 可取消整個南蠻入侵
            game.getCurrentRound().setStage(Stage.Wait_Accept_Ward_Effect);
            setIsOneRound(false);

            Behavior wardBehavior = new WardBehavior(
                    game, null,
                    game.whichPlayersHaveWard(behaviorPlayer.getId()).stream()
                            .map(Player::getId).collect(Collectors.toList()),
                    null, cardId, PlayType.INACTIVE.getPlayType(), card, true
            );
            wardBehavior.putParam(WARD_TRIGGER_PLAYER_ID, behaviorPlayer.getId());
            wardBehavior.putParam(WARD_TARGET_PLAYER_IDS, new ArrayList<>(reactionPlayers));
            game.updateTopBehavior(wardBehavior);

            events.addAll(wardBehavior.playerAction());
        } else {
            // 沒人有 Ward → 直接開始輪詢
            pollingStarted = true;
            events.addAll(askNextPlayerOrWard());
        }

        events.add(game.getGameStatusEvent("發動南蠻入侵"));
        return events;
    }

    @Override
    public List<DomainEvent> doBehaviorAction() {
        List<DomainEvent> events = new ArrayList<>();
        if (!pollingStarted) {
            // Phase 1 Ward even → 開始輪詢第一個玩家
            pollingStarted = true;
            events.addAll(askNextPlayerOrWard());
        } else {
            // Phase 2 Ward even → 直接問當前玩家出殺
            com.gaas.threeKingdoms.skill.registry.SkillEngine.beforeAskKill(game, game.getPlayer(currentReactionPlayer.getId()), this).ifPresentOrElse(events::addAll, () -> events.add(new AskKillEvent(currentReactionPlayer.getId())));
            game.getCurrentRound().setActivePlayer(currentReactionPlayer);
        }
        return events;
    }

    @Override
    public List<DomainEvent> doWardCancelledAction() {
        if (!pollingStarted) {
            return null; // Phase 1: 標準移除
        }
        // Phase 2: 跳過這個玩家，繼續輪詢
        List<DomainEvent> events = new ArrayList<>();
        Player next = nextAliveReactorAfter(currentReactionPlayer.getId());

        if (next == null) {
            isOneRound = true;
            game.getCurrentRound().setStage(Stage.Normal);
            game.getCurrentRound().setActivePlayer(game.getCurrentRoundPlayer());
            return events;
        }

        currentReactionPlayer = next;
        events.addAll(askNextPlayerOrWard());
        return events;
    }

    private List<DomainEvent> askNextPlayerOrWard() {
        List<DomainEvent> events = new ArrayList<>();
        if (game.doesAnyPlayerHaveWard(behaviorPlayer.getId())) {
            game.getCurrentRound().setStage(Stage.Wait_Accept_Ward_Effect);
            setIsOneRound(false);

            Behavior wardBehavior = new WardBehavior(
                    game, null,
                    game.whichPlayersHaveWard(behaviorPlayer.getId()).stream()
                            .map(Player::getId).collect(Collectors.toList()),
                    null, cardId, PlayType.INACTIVE.getPlayType(), card, true
            );
            wardBehavior.putParam(WARD_TRIGGER_PLAYER_ID, behaviorPlayer.getId());
            wardBehavior.putParam(WARD_TARGET_PLAYER_IDS, List.of(currentReactionPlayer.getId()));
            game.updateTopBehavior(wardBehavior);

            events.addAll(wardBehavior.playerAction());
        } else {
            game.getCurrentRound().setStage(Stage.Normal);
            com.gaas.threeKingdoms.skill.registry.SkillEngine.beforeAskKill(game, game.getPlayer(currentReactionPlayer.getId()), this).ifPresentOrElse(events::addAll, () -> events.add(new AskKillEvent(currentReactionPlayer.getId())));
            game.getCurrentRound().setActivePlayer(currentReactionPlayer);
        }
        return events;
    }

    @Override
    protected List<DomainEvent> doResponseToPlayerAction(String playerId, String targetPlayerId, String cardId, String playType) {

        if (isSkip(playType)) {
            int originalHp = currentReactionPlayer.getHP();
            List<DomainEvent> damagedEvent = game.getDamagedEvent(playerId, targetPlayerId, cardId, card, playType, originalHp, currentReactionPlayer, game.getCurrentRound(), Optional.of(this));

            // 偵測 JianXiong 介入：若 WaitingJX 在 stack 頂，把 polling-advance 註冊為
            // callback，等 JianXiong 解決後再執行（避免覆蓋 activePlayer 與打亂 stack）
            if (!game.isTopBehaviorEmpty()
                    && game.peekTopBehavior() instanceof WaitingJianXiongResponseBehavior wjx) {
                wjx.setOnResolved(() -> advanceAfterDamage(playerId));
                // 立即補上 GameStatusEvent — PlayCardPresenter 會 require；polling-advance 才 defer
                List<DomainEvent> events = new ArrayList<>(damagedEvent);
                String message = game.getGamePhase().getPhaseName().equals("GeneralDying")
                        ? "扣血已瀕臨死亡" : "扣血但還活著";
                events.add(game.getGameStatusEvent(message));
                return events;
            }

            List<DomainEvent> events = new ArrayList<>(damagedEvent);
            events.addAll(advanceAfterDamage(playerId));
            return events;
        } else if (isKillCard(cardId)) {
            playerPlayCardNotUpdateActivePlayer(game.getPlayer(playerId), cardId);
            return advanceAfterKillResponse(playerId,
                    playerId + "出殺",
                    List.of(new PlayCardEvent("出牌", playerId, targetPlayerId, cardId, playType)));
        } else {
            //TODO:怕有其他效果或殺的其他case
        }
        return null;
    }

    @Override
    public List<DomainEvent> acceptVirtualKillResponse(String playerId, String targetPlayerId, HandCard virtualKill, List<String> discardedCardIds) {
        // 棄牌已在 Game.playerUseViperSpearKill() 處理；此處僅推進 reaction 狀態
        // BarbarianInvasion 不使用 targetPlayerId — 回應對象固定為入侵者 (behaviorPlayer)
        return advanceAfterKillResponse(playerId,
                playerId + "用丈八蛇矛出殺",
                List.of(new ViperSpearKillTriggerEvent(playerId, behaviorPlayer.getId(), discardedCardIds)));
    }

    private List<DomainEvent> advanceAfterKillResponse(String playerId, String statusMessage, List<DomainEvent> insertEvents) {
        List<DomainEvent> events = new ArrayList<>();
        Player next = nextAliveReactorAfter(playerId);
        boolean isLastPlayer = next == null;
        if (isLastPlayer) {
            isOneRound = true;
            game.getCurrentRound().setActivePlayer(game.getCurrentRoundPlayer());
        } else {
            currentReactionPlayer = next;
            game.getCurrentRound().setActivePlayer(currentReactionPlayer);
        }
        events.add(game.getGameStatusEvent(statusMessage));
        events.addAll(insertEvents);
        if (!isLastPlayer) {
            events.addAll(askNextPlayerOrWard());
        }
        return events;
    }

    public boolean isInReactionPlayers(String playerId) {
        return reactionPlayers.contains(playerId);
    }

    /**
     * 受傷後的 polling-advance 邏輯（推進到下個 reactor / 結束 polling）。
     * 抽出以便 JianXiong 介入時 defer 為 callback；正常路徑直接在 doResponseToPlayerAction
     * 內呼叫。
     */
    @Override
    public List<DomainEvent> resumeJianXiongPolling(String damagedPlayerId) {
        // issue #209：reload 後 onResolved 遺失時的 resume 路徑
        return advanceAfterDamage(damagedPlayerId);
    }

    /**
     * 依 reactionPlayers 列表順序找 playerId 之後第一個存活 reactor；null = 沒有了。
     * 不可用座位 getNextPlayer：謙遜等免疫技會把玩家從列表排除（座位上仍存在），
     * 座位推進會誤問免疫者、且免疫者在列表尾時 isLast 判斷失效（issue：南蠻跳人/亂序）。
     */
    private Player nextAliveReactorAfter(String playerId) {
        int idx = reactionPlayers.indexOf(playerId);
        for (int i = idx + 1; i < reactionPlayers.size(); i++) {
            Player candidate = game.getPlayer(reactionPlayers.get(i));
            if (!candidate.isAlreadyDeath()) {
                return candidate;
            }
        }
        return null;
    }

    private List<DomainEvent> advanceAfterDamage(String playerId) {
        Player next = nextAliveReactorAfter(playerId);

        List<DomainEvent> events = new ArrayList<>();
        if (!game.getGamePhase().getPhaseName().equals("GeneralDying")) {
            if (next == null) {
                isOneRound = true;
                game.getCurrentRound().setActivePlayer(game.getCurrentRoundPlayer());
            } else {
                isOneRound = false;
                currentReactionPlayer = next;
                game.getCurrentRound().setActivePlayer(currentReactionPlayer);
            }
            events.add(game.getGameStatusEvent("扣血但還活著"));
            if (next != null) {
                events.addAll(askNextPlayerOrWard());
            }
        } else {
            events.add(game.getGameStatusEvent("扣血已瀕臨死亡"));
            if (next == null) {
                isOneRound = true;
            } else {
                // 瀕死 resume hook 讀取 currentReactionPlayer；先推進到下一位
                currentReactionPlayer = next;
            }
        }
        return events;
    }

}
