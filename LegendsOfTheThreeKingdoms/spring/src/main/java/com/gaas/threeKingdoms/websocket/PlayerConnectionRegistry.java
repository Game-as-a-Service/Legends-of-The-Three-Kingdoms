package com.gaas.threeKingdoms.websocket;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * WebSocket 連線表（issue #239）— 傳輸層狀態，不進 domain / MongoDB。
 * <p>
 * sessionId ↔ (gameId, playerId)：同一玩家可能開多分頁（多 session），
 * 只有該玩家「最後一個 session 斷線」才視為離線。單機部署 in-memory 即可。
 */
@Component
public class PlayerConnectionRegistry {

    /** sessionId → [gameId, playerId] */
    private final Map<String, String[]> sessions = new HashMap<>();
    /** gameId → playerId → 該玩家仍連著的 sessionIds */
    private final Map<String, Map<String, Set<String>>> connections = new HashMap<>();

    /** @return true = 該玩家由離線轉為連線（首個 session） */
    public synchronized boolean registerSubscription(String sessionId, String gameId, String playerId) {
        sessions.put(sessionId, new String[]{gameId, playerId});
        Set<String> playerSessions = connections
                .computeIfAbsent(gameId, k -> new HashMap<>())
                .computeIfAbsent(playerId, k -> new HashSet<>());
        boolean newlyConnected = playerSessions.isEmpty();
        playerSessions.add(sessionId);
        return newlyConnected;
    }

    /** @return 該 session 斷線導致某玩家完全離線時，回傳 [gameId, playerId]；否則 empty */
    public synchronized Optional<String[]> unregisterSession(String sessionId) {
        String[] identity = sessions.remove(sessionId);
        if (identity == null) {
            return Optional.empty();
        }
        String gameId = identity[0];
        String playerId = identity[1];
        Map<String, Set<String>> gameConnections = connections.get(gameId);
        if (gameConnections == null) {
            return Optional.empty();
        }
        Set<String> playerSessions = gameConnections.get(playerId);
        if (playerSessions == null) {
            return Optional.empty();
        }
        playerSessions.remove(sessionId);
        if (!playerSessions.isEmpty()) {
            return Optional.empty(); // 還有其他分頁連著
        }
        gameConnections.remove(playerId);
        if (gameConnections.isEmpty()) {
            connections.remove(gameId); // 該局無人連線 → 清掉，避免累積
        }
        return Optional.of(identity);
    }

    public synchronized Set<String> getConnectedPlayerIds(String gameId) {
        Map<String, Set<String>> gameConnections = connections.get(gameId);
        return gameConnections == null ? Set.of() : Collections.unmodifiableSet(new HashSet<>(gameConnections.keySet()));
    }
}
