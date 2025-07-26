package com.gaas.threeKingdoms.e2e.scrollcard;

import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class SnatchTest extends AbstractBaseIntegrationTest {

    public SnatchTest() {
        this.gameId = "my-id";
    }

    private String snatchCardId = "SS3016";

    @DisplayName("""
        Given
        玩家ABCD
        B有一張手牌
        A 有順手牽羊
        
        When
        A 出順手牽羊，指定 B
        
        Then
        回傳 PlayCardEvent
                """)
    @Test
    public void given(){

    }
}
