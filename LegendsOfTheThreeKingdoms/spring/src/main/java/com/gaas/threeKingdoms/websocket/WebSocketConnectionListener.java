package com.gaas.threeKingdoms.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.outport.GameRepository;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.presenter.PlayerConnectionStatusPresenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 連線狀態廣播（issue #239）：監聽 STOMP 訂閱/斷線事件，變動時廣播該局全員
 * PlayerConnectionStatusEvent。守則：
 * <ul>
 *   <li>遊戲不存在或訂閱者不是該局玩家（觀戰/雜訊）→ 記錄於連線表但不廣播</li>
 *   <li>同玩家多分頁：首個 session 連上才算連線、最後一個斷線才算離線</li>
 *   <li>連線是傳輸層狀態，不進 domain / MongoDB</li>
 * </ul>
 */
@Component
public class WebSocketConnectionListener {

    private static final Pattern DESTINATION_PATTERN =
            Pattern.compile("^/websocket/legendsOfTheThreeKingdoms/([^/]+)/([^/]+)$");

    @Autowired
    private PlayerConnectionRegistry registry;
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    @EventListener
    public void onSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = accessor.getDestination();
        String sessionId = accessor.getSessionId();
        if (destination == null || sessionId == null) {
            return;
        }
        Matcher matcher = DESTINATION_PATTERN.matcher(destination);
        if (!matcher.matches()) {
            return;
        }
        String gameId = matcher.group(1);
        String playerId = matcher.group(2);
        boolean newlyConnected = registry.registerSubscription(sessionId, gameId, playerId);
        if (newlyConnected) {
            broadcastConnectionStatus(gameId, playerId, "已連線");
        }
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        Optional<String[]> fullyDisconnected = registry.unregisterSession(event.getSessionId());
        fullyDisconnected.ifPresent(identity ->
                broadcastConnectionStatus(identity[0], identity[1], "已離線"));
    }

    private void broadcastConnectionStatus(String gameId, String changedPlayerId, String changeText) {
        Optional<Game> gameOptional = gameRepository.findById(gameId);
        if (gameOptional.isEmpty()) {
            return; // 遊戲尚未建立（前端先開 socket）→ 連線表已記錄，之後的變動會補廣播
        }
        Game game = gameOptional.get();
        List<String> allPlayerIds = game.getPlayers().stream().map(Player::getId).toList();
        if (!allPlayerIds.contains(changedPlayerId)) {
            return; // 非該局玩家（觀戰/雜訊）不廣播
        }
        Set<String> connected = registry.getConnectedPlayerIds(gameId);
        // 以座位順序輸出，結果穩定
        List<String> connectedOrdered = allPlayerIds.stream().filter(connected::contains).toList();

        PlayerConnectionStatusPresenter.PlayerConnectionStatusViewModel viewModel =
                new PlayerConnectionStatusPresenter.PlayerConnectionStatusViewModel(
                        gameId,
                        new PlayerConnectionStatusPresenter.PlayerConnectionStatusDataViewModel(
                                connectedOrdered, connectedOrdered.size(), allPlayerIds.size()),
                        String.format("%s %s（%d/%d）", changedPlayerId, changeText,
                                connectedOrdered.size(), allPlayerIds.size()));
        try {
            String json = objectMapper.writeValueAsString(viewModel);
            for (String playerId : allPlayerIds) {
                messagingTemplate.convertAndSend(
                        String.format("/websocket/legendsOfTheThreeKingdoms/%s/%s", gameId, playerId), json);
            }
        } catch (Exception e) {
            System.err.println("****************** pushPlayerConnectionStatusEvent ");
            e.printStackTrace();
        }
    }
}
