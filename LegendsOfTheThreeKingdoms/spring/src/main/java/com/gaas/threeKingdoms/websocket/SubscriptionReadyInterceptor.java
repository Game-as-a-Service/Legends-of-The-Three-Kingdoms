package com.gaas.threeKingdoms.websocket;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.broker.AbstractBrokerMessageHandler;
import org.springframework.messaging.support.ExecutorChannelInterceptor;
import org.springframework.stereotype.Component;

/**
 * 在「broker 已經真的註冊好某條訂閱」之後才通知 {@link WebSocketConnectionListener}。
 *
 * <h4>為什麼不能用 SessionSubscribeEvent</h4>
 * 原本的連線廣播掛在 {@code @EventListener(SessionSubscribeEvent)} 上，但那個 event 是
 * {@code StompSubProtocolHandler} 在 {@code clientInboundChannel.send()} **回傳之後**才發佈的，
 * 而 {@code clientInboundChannel} 是 {@code ExecutorSubscribableChannel}（非同步派送）——
 * 也就是說 {@code SimpleBrokerMessageHandler} 註冊這條訂閱的工作此時只是被排進 executor、還沒做完。
 * <p>
 * 於是廣播可能早於註冊送出，而 SimpleBroker 對「沒有 subscriber 的 destination」是**直接丟棄、
 * 不排隊**，所以剛訂閱的玩家會永久錯過自己的「已連線」事件（不是晚到，是不見）。
 * 這條 race 以前被 issue #249 的 session 洩漏遮住了：destination 上累積了上千條訂閱，
 * 每次 {@code convertAndSend} 的 fan-out 慢到剛好把註冊的空窗蓋過去；洩漏一修好就露出來，
 * 表現為 {@code PlayerConnectionStatusE2ETest} 間歇失敗（B 收不到自己觸發的那則 2/4）。
 *
 * <h4>這個 hook 為什麼是對的</h4>
 * {@code ExecutorSubscribableChannel} 對每個 handler 的呼叫都包在 SendTask 裡，
 * 先 {@code beforeHandle}、呼叫 {@code handler.handleMessage(message)}、再
 * {@code afterMessageHandled}。而 {@code SimpleBrokerMessageHandler} 處理 SUBSCRIBE 時是
 * **同步**呼叫 {@code subscriptionRegistry.registerSubscription(message)}。
 * 因此當 broker handler 的 {@code afterMessageHandled} 被呼叫時，訂閱一定已經在註冊表裡，
 * 此時廣播才保證送得到 —— 這是排序保證，不是把 timeout 調長的機率遊戲。
 */
@Component
public class SubscriptionReadyInterceptor implements ExecutorChannelInterceptor {

    /**
     * 用 ObjectProvider 延後取得，避免循環相依：
     * {@link WebSocketConnectionListener} 需要 SimpMessagingTemplate，而該 template 依賴
     * clientInboundChannel，clientInboundChannel 又要先問過本 interceptor 所在的 configurer。
     */
    private final ObjectProvider<WebSocketConnectionListener> connectionListenerProvider;

    public SubscriptionReadyInterceptor(ObjectProvider<WebSocketConnectionListener> connectionListenerProvider) {
        this.connectionListenerProvider = connectionListenerProvider;
    }

    @Override
    public void afterMessageHandled(Message<?> message, MessageChannel channel,
                                    MessageHandler handler, Exception ex) {
        // 只認 broker 這個 handler：clientInboundChannel 上還有 annotation method / user destination
        // 等其他 handler，它們跑完並不代表訂閱已註冊。
        if (ex != null || !(handler instanceof AbstractBrokerMessageHandler)) {
            return;
        }
        if (SimpMessageHeaderAccessor.getMessageType(message.getHeaders()) != SimpMessageType.SUBSCRIBE) {
            return;
        }
        String sessionId = SimpMessageHeaderAccessor.getSessionId(message.getHeaders());
        String destination = SimpMessageHeaderAccessor.getDestination(message.getHeaders());
        connectionListenerProvider.getObject().onSubscriptionRegistered(sessionId, destination);
    }
}
