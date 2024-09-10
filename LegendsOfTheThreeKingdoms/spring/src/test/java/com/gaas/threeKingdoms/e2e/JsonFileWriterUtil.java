package com.gaas.threeKingdoms.e2e;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

public class JsonFileWriterUtil {

    /**
     * 寫入 JSON 到指定的檔案路徑，如果目錄不存在則自動建立
     *
     * @param playerId    玩家 ID
     * @param filePathTemplate 檔案路徑模板，包含 %s 佔位符對應 playerId
     * @throws IOException 當寫入檔案發生錯誤時
     */
    public static String writeJsonToFile(WebsocketUtil websocketUtil, String playerId, String filePathTemplate) throws IOException {
        // 檢查輸入是否為 null
        String jsonContent = websocketUtil.getValue(playerId);
        Objects.requireNonNull(jsonContent, "JSON 內容不能為 null");
        Objects.requireNonNull(playerId, "玩家 ID 不能為 null");
        Objects.requireNonNull(filePathTemplate, "檔案路徑模板不能為 null");

        // 使用 playerId 替換模板中的 %s 來動態生成檔案路徑
        playerId = playerId.replace("-", "_");
        String filePath = String.format(filePathTemplate, playerId);

        // 獲取 Path 物件
        Path path = Paths.get(filePath);

        // 如果目錄不存在，則建立所有必需的目錄
        if (!Files.exists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }

        // 將 JSON 字符串寫入檔案，若檔案已存在則覆寫
        Files.writeString(path, jsonContent, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        return jsonContent;
    }
}
