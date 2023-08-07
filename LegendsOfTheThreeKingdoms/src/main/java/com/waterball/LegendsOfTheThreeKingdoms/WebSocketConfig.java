package com.waterball.LegendsOfTheThreeKingdoms;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;


@Configuration  // 表示這個類別是一個配置類別
@EnableWebSocketMessageBroker  // 啟用WebSocket並使用STOMP作為其訊息傳遞協議
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");  // 啟用一個簡單的message broker，並設定"/topic"前綴，該前綴的目的地會被視為可以被訂閱的目的地
        config.setApplicationDestinationPrefixes("/api");  // 設定"/api"前綴，該前綴的目的地會被視為需要服務器處理的消息
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/legendsOfTheThreeKingDom");  // 設定STOMP協議的WebSocket端點，客戶端將使用這個端點來進行連接
    }

}