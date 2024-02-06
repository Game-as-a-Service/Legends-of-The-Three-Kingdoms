package com.gaas.threeKingdoms.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.repository.InMemoryGameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class DyingTest {

    private final InMemoryGameRepository repository = Mockito.mock(InMemoryGameRepository.class);

    private WebsocketUtil websocketUtil;

    @Autowired
    private MockMvc mockMvc;

    @Value(value = "${local.server.port}")
    private int port;

    @Autowired
    private ObjectMapper objectMapper;


    @BeforeEach
    public void setup() throws Exception {
        websocketUtil = new WebsocketUtil(port);
    }

    @Test
    public void testMockGame() throws Exception {

    }


    private void given_playerA_is_enter_dying_status(String gameId) {
        Game tempGame = Game.builder().gameId(gameId).build();
        Mockito.when(repository.findById(gameId)).thenReturn(tempGame);
    }


}
