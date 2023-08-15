package com.waterball.LegendsOfTheThreeKingdoms.controller;

import com.waterball.LegendsOfTheThreeKingdoms.controller.dto.GameResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HelloWorldTest {

    @Value(value = "${local.server.port}")
    private int port;
    private WebSocketStompClient stompClient;
    private final WebSocketHttpHeaders headers = new WebSocketHttpHeaders();

    @BeforeEach
    public void setUp() {
        WebSocketClient webSocketClient = new StandardWebSocketClient();
        this.stompClient = new WebSocketStompClient(webSocketClient);
        this.stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }


    @Test
    public void getHelloWorld() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1); // 創建一個計數閥門，用於同步確保WebSocket的訊息處理完畢再繼續進行
        final AtomicReference<Throwable> failure = new AtomicReference<>(); // 創建一個原子型的引用變量，用於存放發生的異常

        StompSessionHandler handler = new TestSessionHandler(failure) {
            @Override
            public void afterConnected(final StompSession session, StompHeaders connectedHeaders) {
                session.subscribe("/websocket/greetings", new StompFrameHandler() {  // 訂閱伺服器的 "/topic/greetings" 路徑的訊息
                    @Override
                    public Type getPayloadType(StompHeaders headers) {  // 定義從伺服器收到的訊息內容的類型
                        return GameResponse.class;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        GameResponse greeting = (GameResponse) payload;
                        try {
                            assertEquals("Hello,world fromBackend", greeting.getGameId());
                        } catch (Throwable t) {
                            failure.set(t); // 儲存異常
                        } finally {
                            session.disconnect();
                            latch.countDown(); // 計數閥門的數值減1，如果數值為0，則讓等待的測試方法繼續執行
                        }
                    }
                });
                try {
                    session.send("/api/hello", "Hello From frontEnd");
                } catch (Throwable t) {
                    failure.set(t);
                    latch.countDown();
                }
            }
        };
        this.stompClient.connectAsync("ws://localhost:{port}/legendsOfTheThreeKingDom", this.headers, handler, this.port);

        if (latch.await(3, TimeUnit.SECONDS)) {
            if (failure.get() != null) {
                throw new AssertionError("", failure.get());
            }
        } else {
            fail("HelloWorld not received");
        }
    }

    public static class TestSessionHandler extends StompSessionHandlerAdapter {
        private final AtomicReference<Throwable> failure;

        public TestSessionHandler(AtomicReference failure) {
            this.failure = failure;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            this.failure.set(new Exception(headers.toString()));
        }

        @Override
        public void handleException(StompSession s, StompCommand c, StompHeaders h, byte[] p, Throwable ex) {
            this.failure.set(ex);
        }

        @Override
        public void handleTransportError(StompSession session, Throwable ex) {
            this.failure.set(ex);
        }
    }
}
