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
import org.springframework.messaging.simp.SimpMessagingTemplate;
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

    /** 只用來送就緒哨兵，見 {@link #setup()}。 */
    @Autowired
    protected SimpMessagingTemplate messagingTemplate;

    protected MockMvcUtil mockMvcUtil;

    protected JsonFileValidateHelper helper;

    protected WebsocketUtil websocketUtil;

    @Value(value = "${local.server.port}")
    protected Integer port;

    protected String gameId = "my-id";

    /**
     * 這裡原本是 {@code Thread.sleep(1000)}：等 7 條 STOMP 訂閱建立完成，否則測試接著建局送出的
     * 第一批推播會因為「destination 還沒有 subscriber」被 SimpleBroker 直接丟棄。
     * 現在改成等實際的就緒證據（見 {@link WebsocketUtil#awaitSubscriptionsReady}）——
     * 既是保證而不是「1 秒應該夠了吧」的猜測，也把 259 支測試各付 1 秒的固定稅拿掉。
     */
    @BeforeEach
    public void setup() throws Exception {
        mockMvcUtil = new MockMvcUtil(mockMvc);
        websocketUtil = new WebsocketUtil(port, gameId);
        helper = new JsonFileValidateHelper(websocketUtil);
        websocketUtil.awaitSubscriptionsReady(messagingTemplate::convertAndSend);
    }

    /**
     * 順序不能反：**先刪 game，再關 websocket**。
     * <p>
     * {@code WebSocketConnectionListener#onDisconnect} 在某玩家最後一條 session 斷線時會廣播
     * 「已離線」的 PlayerConnectionStatusEvent 給該局全員（issue #239）。DISCONNECT frame 是
     * 非同步處理的，若此時 game 還在 MongoDB 裡，這則廣播會多送一則訊息、打進下一支測試的佇列，
     * 造成新的訊息位移。先刪 game 就讓 {@code broadcastConnectionStatus} 查不到 game 提早 return。
     * <p>
     * （在洩漏被修掉之前這條路徑一直是死的：session 從不 unregister，
     * {@code PlayerConnectionRegistry} 永遠判斷不到「最後一條」，洩漏意外遮住了它。）
     */
    @AfterEach
    public void tearDown() {
        repository.deleteById(gameId);
        if (websocketUtil != null) {
            websocketUtil.close();
        }
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
