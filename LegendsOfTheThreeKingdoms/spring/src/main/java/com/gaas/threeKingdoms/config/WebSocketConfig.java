package com.gaas.threeKingdoms.config;

import com.gaas.threeKingdoms.websocket.SubscriptionReadyInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;


@Configuration  // 表示這個類別是一個配置類別
@EnableWebSocketMessageBroker  // 啟用WebSocket並使用STOMP作為其訊息傳遞協議
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final SubscriptionReadyInterceptor subscriptionReadyInterceptor;

    public WebSocketConfig(SubscriptionReadyInterceptor subscriptionReadyInterceptor) {
        this.subscriptionReadyInterceptor = subscriptionReadyInterceptor;
    }

    /**
     * 訂閱完成後才廣播「已連線」，原因見 {@link SubscriptionReadyInterceptor} 的 javadoc。
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(subscriptionReadyInterceptor);
    }

    /**
     * 為什麼要 {@code setPreservePublishOrder(true)}。
     * <p>
     * 事件是照順序送出的 —— 例如 {@code chooseGeneralByOthers} 先推 GeneralSelectionStatusEvent
     * 再推 InitialEndEvent（同一個 request thread、同一個 destination）。但 Spring 預設的
     * {@code clientOutboundChannel} 是**多執行緒**的 {@code ExecutorSubscribableChannel}，
     * 同一個 client 的兩則訊息可能被兩條 thread 併發處理，抵達順序因此不保證。
     * <p>
     * 前端是照事件順序套用狀態的，所以這是產品端的正確性問題，不只是測試問題；
     * 測試端的症狀是整包訊息位移（e2e 讀到的第 n 則不是預期的那則），
     * CI 上出現過 {@code GameTest.happyPath} 期待 GeneralSelectionStatusEvent 卻拿到
     * InitialEndEvent。{@code GameTest#pollMessagesByEvent} 註解那句「STOMP 順序不保證」
     * 就是在描述這個現象 —— 但它是可以關掉的，不必只能容忍。
     * <p>
     * 開啟後 Spring 會用 {@code OrderedMessageChannelDecorator} 讓每個 session 的訊息依序處理
     * （跨 session 仍平行），代價很小。
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/websocket");  // 啟用一個簡單的message broker，並設定"/topic"前綴，該前綴的目的地會被視為可以被訂閱的目的地
        config.setApplicationDestinationPrefixes("/app");  // 設定"/api"前綴，該前綴的目的地會被視為需要服務器處理的消息
        // 同一個 client 的推播必須照送出順序抵達，理由見上方 javadoc
        config.setPreservePublishOrder(true);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/legendsOfTheThreeKingdoms").setAllowedOrigins("*");
    }

}
