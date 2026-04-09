package com.gaas.threeKingdoms.e2e.testcontainer.test;

import com.gaas.threeKingdoms.LegendsOfTheThreeKingdomsApplication;
import com.gaas.threeKingdoms.e2e.JsonFileValidateHelper;
import com.gaas.threeKingdoms.e2e.JsonFileWriterUtil;
import com.gaas.threeKingdoms.e2e.MockMvcUtil;
import com.gaas.threeKingdoms.e2e.WebsocketUtil;
import com.gaas.threeKingdoms.outport.GameRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@SpringBootTest(
        classes = LegendsOfTheThreeKingdomsApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public abstract class AbstractBaseIntegrationTest {

    public static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest").withExposedPorts(27017);

    /**
     * 4 人場標準玩家 ID 列表，e2e test 絕大多數都是用 player-a/b/c/d 的固定命名。
     */
    protected static final List<String> DEFAULT_PLAYER_IDS = List.of("player-a", "player-b", "player-c", "player-d");

    @DynamicPropertySource
    static void containersProperties(DynamicPropertyRegistry registry) {
        mongoDBContainer.start();
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("spring.data.mongodb.host", mongoDBContainer::getHost);
        registry.add("spring.data.mongodb.port", mongoDBContainer::getFirstMappedPort);
    }

    @Autowired
    protected GameRepository repository;

    @Autowired
    protected MockMvc mockMvc;

    protected MockMvcUtil mockMvcUtil;

    protected JsonFileValidateHelper helper;

    protected WebsocketUtil websocketUtil;

    @Value(value = "${local.server.port}")
    protected Integer port;

    protected String gameId = "my-id";

    @BeforeEach
    public void setup() throws Exception {
        mockMvcUtil = new MockMvcUtil(mockMvc);
        websocketUtil = new WebsocketUtil(port, gameId);
        helper = new JsonFileValidateHelper(websocketUtil);
        Thread.sleep(1000);
    }

    @AfterEach
    public void deleteMockGame() {
        repository.deleteById(gameId);
    }

    /**
     * 由 subclass override 成 true 來切到「寫入模式」：{@link #assertAllPlayerJson} 會
     * 覆蓋 JSON fixture 而不是比對，方便大幅更動事件時重新產生 golden files。
     *
     * 預設為 false，避免不小心把整個 e2e suite 都切到寫入模式覆蓋其他無關的 fixture。
     * 只在本地暫時需要重產 fixture 時，於該 test class override 成 true，commit 前務必 revert。
     */
    protected boolean shouldRegenerateFixtures() {
        return false;
    }

    /**
     * 對 {@link #DEFAULT_PLAYER_IDS} 的每個玩家比對其 websocket 收到的 JSON 與 fixture 檔案。
     * filePathTemplate 中的 %s 會被替換為 playerId（連字符轉底線，如 player-a → player_a）。
     *
     * 若 subclass 把 {@link #shouldRegenerateFixtures()} override 成 true，會改為寫入模式，
     * 覆蓋該 test class 的 fixture 而不影響其他 test class。
     */
    protected void assertAllPlayerJson(String filePathTemplate) throws Exception {
        assertAllPlayerJson(filePathTemplate, DEFAULT_PLAYER_IDS);
    }

    /**
     * 與 {@link #assertAllPlayerJson(String)} 相同，但可以指定自訂玩家列表
     * （例如 3 人場或使用不同 id 命名的測試）。
     */
    protected void assertAllPlayerJson(String filePathTemplate, List<String> playerIds) throws Exception {
        boolean regenerate = shouldRegenerateFixtures();
        for (String playerId : playerIds) {
            String actualJson = regenerate
                    ? JsonFileWriterUtil.writeJsonToFile(websocketUtil, playerId, filePathTemplate)
                    : websocketUtil.getValue(playerId);
            String filePlayerId = playerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, filePlayerId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, actualJson);
        }
    }
}
