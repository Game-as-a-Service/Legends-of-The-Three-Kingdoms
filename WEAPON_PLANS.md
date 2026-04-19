# 武器牌實作規劃

> 此檔案不納入 git 追蹤

## 總覽

| 牌名 | Card ID | 攻擊範圍 | 複雜度 | API 變動 | 觸發時機 |
|------|---------|----------|--------|----------|----------|
| 青釭劍 BlackPommel | ES6019 | 2 | 低 | 無 | 殺出牌時（跳過防具） |
| 青龍偃月刀 GreenDragonCrescentBlade | ES5005 | 3 | 中 | 複用 useEquipmentEffect | 殺被閃後 |
| 貫石斧 StonePiercingAxe | ED5083 | 3 | 中 | 新增 useStonePiercingAxeEffect | 殺被閃後 |
| 雌雄雙股劍 YinYangSwords | ES2002 | 2 | 中高 | 複用 playCard | 出殺時（異性觸發） |
| 丈八蛇矛 EighteenSpanViperSpear | ESQ025 | 3 | 高 | PlayCardRequest 加 cardIds | 出牌階段 |
| 方天畫戟 HeavenlyDoubleHalberd | EDQ103 | 4 | 高 | 新增 chooseHeavenlyDoubleHalberdTargets | 出殺時（最後一張手牌） |

---

## P2 — 青釭劍 (BlackPommel, ES6019)

### 效果
殺無視目標防具（如八卦陣）

### API
不需要新 API，被動效果

### Flow
殺出牌時 → 攻擊者有青釭劍？→ 是：跳過防具判定，直接 AskDodgeEvent → 否：正常流程

### 影響範圍
- 新增：`BlackPommelCard.java`（~15 行，繼承 WeaponCard，range=2）
- 修改：`PlayCard.java`（factory map 加 1 行）
- 修改：`NormalActiveKillBehavior.java`（playerAction() 加 1 個 if 判斷）

### 測試
- Domain: 5 個（裝備、無視八卦陣、對照組、借刀殺人、替換武器）
- E2E: 2 個

---

## P2 — 青龍偃月刀 (GreenDragonCrescentBlade, ES5005)

### 效果
殺被閃後，可立即再出一張殺（可重複，直到不出或命中）

### API
複用 `useEquipmentEffect`（詢問是否發動）+ `playCard`（出殺）

### Flow
```
A 出殺 → B 出閃 → A 有青龍偃月刀？
  → 是：AskPlayEquipmentEffectEvent → A 選擇：
    → ACTIVE：AskKillEvent → A 出殺 → B 再閃 → 循環
    → SKIP：殺被抵銷，結束
  → 否：殺被抵銷
```

### 影響範圍
- 新增：`GreenDragonCrescentBladeCard.java`
- 新增：`WaitingGreenDragonCrescentBladeKillBehavior.java`（處理循環出殺）
- 新增：`GreenDragonCrescentBladeEquipmentEffectHandler.java`
- 新增：`GreenDragonCrescentBladeEffectEvent.java`
- 修改：`NormalActiveKillBehavior.java`（dodge 分支加青龍偃月刀檢查）
- 修改：`PlayCard.java`（factory map）
- 修改：`Game.java`（handler chain 加入）
- 修改：`BehaviorData.java`（序列化）

### 關鍵設計
- 循環在 WaitingGreenDragonCrescentBladeKillBehavior 內自包含
- 追殺不計入每回合殺次數限制
- stack 深度固定為 2（NormalActiveKillBehavior + Waiting）

### 測試
- Domain: 9 個（裝備、追殺命中、追殺跳過、拒絕發動、多次循環、八卦陣交互、借刀殺人）
- E2E: 3 個

---

## P2 — 貫石斧 (StonePiercingAxe, ED5083)

### 效果
殺被閃後，可棄兩張牌（手牌或裝備）使殺強制命中

### API
- 詢問發動：複用 `useEquipmentEffect`
- 棄牌強制命中：新增 `POST /api/games/{gameId}/player:useStonePiercingAxeEffect`
  ```json
  { "playerId": "player-a", "cardIds": ["BS8008", "BH2028"] }
  ```

### Flow
```
A 出殺 → B 出閃 → A 有貫石斧 + ≥2 張可棄牌？
  → 是：AskPlayEquipmentEffectEvent → A 選擇：
    → ACTIVATE：棄 2 張牌 → 殺強制命中
    → SKIP：殺被抵銷
  → 否：殺被抵銷
```

### 影響範圍
- 新增：`StonePiercingAxeCard.java`
- 新增：`WaitingStonePiercingAxeResponseBehavior.java`
- 新增：`StonePiercingAxeEquipmentEffectHandler.java`
- 新增：`AskStonePiercingAxeEffectEvent.java`、`StonePiercingAxeEffectEvent.java`
- 新增：`UseStonePiercingAxeEffectUseCase.java` + Presenter + Request DTO
- 修改：`NormalActiveKillBehavior.java`（dodge 分支）
- 修改：`Game.java`、`GameController.java`、`PlayCard.java`
- 修改：`Player.java`（getDiscardableCardCount）、`Equipment.java`

### 關鍵設計
- 可棄手牌或裝備牌（含貫石斧本身）
- 攻擊者需有 ≥2 張可棄牌才會被詢問

### 測試
- Domain: 9 個（觸發、棄手牌、棄裝備、跳過、牌數不足、驗證錯誤）
- E2E: 3 個

---

## P3 — 雌雄雙股劍 (YinYangSwords, ES2002)

### 效果
對異性角色出殺時，目標選擇：棄一張手牌 或 讓攻擊者摸一張牌

### 前置工作
General enum 需新增 Gender 屬性（目前不存在）

### API
複用 playCard API：
- 棄牌：`playType=active`, `cardId=要棄的牌`
- 讓摸牌：`playType=skip`

### Flow
```
A(男) 對 B(女) 出殺 → A 有雌雄雙股劍 + 異性？
  → 是（出殺後、問閃前觸發）：
    → AskYinYangSwordsEffectEvent → B 選擇：
      → 棄一張手牌 → 繼續殺流程（AskDodge）
      → 讓 A 摸一張牌 → 繼續殺流程（AskDodge）
  → 否：直接 AskDodge
```

### 影響範圍
- 新增：`Gender.java` enum
- 新增：`YinYangSwordsCard.java`
- 新增：`WaitingYinYangSwordsResponseBehavior.java`
- 新增：`AskYinYangSwordsEffectEvent.java`、`YinYangSwordsEffectEvent.java`
- 修改：`General.java`（加 gender 欄位）
- 修改：`GeneralCard.java`、`Player.java`（暴露 gender）
- 修改：`NormalActiveKillBehavior.java`（playerAction 中 AskDodge 前插入）
- 修改：`PlayCard.java`、`BehaviorData.java`
- 修改：`GeneralCardData.java`（序列化）

### 測試
- Domain: 8 個（異性棄牌、異性摸牌、同性不觸發、無手牌、八卦陣交互、借刀殺人）
- E2E: 2 個

---

## P3 — 丈八蛇矛 (EighteenSpanViperSpear, ESQ025)

### 效果
棄兩張任意手牌當殺使用

### API
擴展 `PlayCardRequest` 新增 `List<String> cardIds` 欄位：
```json
{
  "playerId": "player-a",
  "targetPlayerId": "player-b",
  "cardId": "ESQ025",
  "cardIds": ["BD2080", "BH3029"],
  "playType": "active"
}
```

### Flow
1. 玩家選兩張手牌 + 目標 → playCard API（cardId=ESQ025, cardIds=[card1, card2]）
2. ViperSpearKillBehaviorHandler match → 建立 ViperSpearKillBehavior
3. 棄兩牌 → 視為出殺 → 正常殺流程（AskDodge）

### 影響範圍（最大）
- 新增：`EighteenSpanViperSpearCard.java`
- 新增：`ViperSpearKillBehavior.java`（繼承 NormalActiveKillBehavior）
- 新增：`ViperSpearKillBehaviorHandler.java`
- 修改：`PlayCardRequest.java`（加 cardIds）
- 修改：`PlayCardBehaviorHandler.java`（方法簽名加 cardIds，影響 18+ handler）
- 修改：`Game.java`（playerPlayCard 接受 cardIds）
- 修改：`PlayCardUseCase.java`、`PlayCard.java`、`Round.java`
- 修改：`BehaviorData.java`

### 關鍵設計
- 虛擬殺計入每回合殺次數限制
- ViperSpearKillBehavior 繼承 NormalActiveKillBehavior，只 override playerAction()
- handler chain 中要排在 NormalActiveKillBehaviorHandler 之前

### 測試
- Domain: 10 個（裝備、虛擬殺命中/被閃/致死、手牌不足、次數限制、任意牌型）
- E2E: 2 個

---

## P3 — 方天畫戟 (HeavenlyDoubleHalberd, EDQ103)

### 效果
最後一張手牌為殺時，可額外指定最多兩個目標（共三目標）

### API
新增 `POST /api/games/{gameId}/player:chooseHeavenlyDoubleHalberdTargets`
```json
{
  "playerId": "player-a",
  "targetPlayerIds": ["player-c", "player-d"]
}
```

### Flow
```
A 出殺（最後一張手牌）→ 系統偵測方天畫戟觸發
  → AskHeavenlyDoubleHalberdExtraTargetsEvent → A 選擇 0-2 個額外目標
  → 對每個目標獨立結算殺（順序：原目標 → 額外目標1 → 額外目標2）
  → 每個目標獨立 AskDodge
```

### 影響範圍
- 新增：`HeavenlyDoubleHalberdCard.java`
- 新增：`AskHeavenlyDoubleHalberdExtraTargetsEvent.java`
- 新增：`ChooseHeavenlyDoubleHalberdTargetsUseCase.java` + Presenter + Request DTO
- 修改：`NormalActiveKillBehavior.java`（playerAction 偵測最後一張手牌 + 方天畫戟）
- 修改：`GameController.java`、`Game.java`、`PlayCard.java`
- 修改：`BehaviorData.java`

### 關鍵設計
- 手牌數檢查在殺從手牌移除後（hand.size() == 0）
- 多目標順序結算（類似南蠻入侵的 per-player polling）
- 每個額外目標需在攻擊範圍 4 內
- stack 深度隨目標數增長

### 測試
- Domain: 10 個（裝備、0/1/2 額外目標、範圍驗證、部分閃部分命中、非最後手牌不觸發、八卦陣）
- E2E: 1 個

---

## 建議實作順序

1. **青釭劍** — 最簡單，1 個 if 判斷
2. **青龍偃月刀** — 中等，但模式清晰（參考麒麟弓）
3. **貫石斧** — 中等，觸發點與青龍偃月刀相同
4. **雌雄雙股劍** — 需先完成 Gender 前置工作
5. **方天畫戟** — 多目標結算較複雜
6. **丈八蛇矛** — 影響最廣（改 handler chain 簽名）
