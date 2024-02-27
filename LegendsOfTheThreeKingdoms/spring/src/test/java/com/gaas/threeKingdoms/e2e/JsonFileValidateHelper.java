package com.gaas.threeKingdoms.e2e;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonFileValidateHelper {

    private WebsocketUtil websocketUtil;
    private String jsonContent;

    public JsonFileValidateHelper(WebsocketUtil websocketUtil) {
        this.websocketUtil = websocketUtil;
    }

    public JsonFileValidateHelper assertThat(String player) {
        jsonContent = websocketUtil.getValue(player);
        return this;
    }

    public void getJsonIsEqualToFile(String filePathString) {
        Path path = Paths.get("src/test/resources/TestJsonFile/" + filePathString);
        try {
            String fileContent = Files.readString(path);
            assertEquals(fileContent, jsonContent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
