# 三國殺 API 文件

## 架構概述

```
Client (前端)
  │
  ├─ HTTP POST ──→ GameController ──→ UseCase ──→ Game (Domain)
  │                                                   │
  └─ WebSocket ←── WebSocketBroadCast ←── Presenter ←─┘
      (STOMP)        /websocket/legendsOfTheThreeKingdoms/{gameId}/{playerId}
```

- **架構模式**：MVP (Model-View-Presenter)
- **WebSocket**：STOMP over `/legendsOfTheThreeKingdoms`
- **Broker prefix**：`/websocket`
- **App prefix**：`/app`
- **推播目標**：`/websocket/legendsOfTheThreeKingdoms/{gameId}/{playerId}`（per-player 個人化訊息）

---

## 1. 建立遊戲

```
POST /api/games
```

| 欄位 | 型別 | 說明 |
|------|------|------|
| gameId | String | 遊戲 ID |
| players | List\<String\> | 玩家 ID 列表（4 人） |

**回傳**：HTTP 200 + WebSocket 推播遊戲初始化事件（角色分配、主公選將）

**流程**：建立遊戲 → 分配身份（主公/忠臣/反賊/內奸）→ 主公先選將

---

## 2. 查詢遊戲狀態

```
GET /api/games/{gameId}?playerId={playerId}
```

| 參數 | 型別 | 說明 |
|------|------|------|
| gameId | Path | 遊戲 ID |
| playerId | Query | 查詢的玩家 ID |

**回傳**：該玩家視角的遊戲狀態（只能看自己的手牌和身份）

---

## 3. 主公選將

```
POST /api/games/{gameId}/player:monarchChooseGeneral
```

| 欄位 | 型別 | 說明 |
|------|------|------|
| playerId | String | 主公玩家 ID |
| generalId | String | 選擇的武將 ID（如 SHU001 劉備） |

**流程**：主公選將 → 其他玩家收到可選將領列表

---

## 4. 其他玩家選將

```
POST /api/games/{gameId}/player:otherChooseGeneral
```

| 欄位 | 型別 | 說明 |
|------|------|------|
| playerId | String | 玩家 ID |
| generalId | String | 選擇的武將 ID |

**流程**：全部選完 → 發牌（每人 4 張）→ 主公回合開始

---

## 5. 出牌

```
POST /api/games/{gameId}/player:playCard
```

| 欄位 | 型別 | 說明 |
|------|------|------|
| playerId | String | 出牌玩家 ID |
| targetPlayerId | String | 目標玩家 ID（無目標傳空字串） |
| cardId | String | 卡牌 ID（如 BS8008） |
| playType | String | 出牌類型 |

### playType 說明

| playType | 說明 | 使用場景 |
|----------|------|----------|
| `active` | 主動出牌 | 自己回合出殺/錦囊/裝備 |
| `inactive` | 被動出牌 | 被殺時出閃、瀕死時出桃 |
| `skip` | 跳過 | 被殺不出閃、不出桃 |

### 對應的牌與流程

#### 基本牌

| 牌名 | cardId 範例 | targetPlayerId | 流程 |
|------|------------|----------------|------|
| 殺 | BS8008 | 目標玩家 ID | 出殺 → 目標出閃(playCard inactive) 或不出(skip) → 命中扣血或未命中 |
| 閃 | BH2028 | 空字串 | 被殺後被動出閃（playType=inactive） |
| 桃 | BH3029 | 空字串 | 主動回血 或 瀕死時被動出桃（playType=inactive） |

#### 錦囊牌（透過 playCard API 出牌）

| 牌名 | cardId 範例 | targetPlayerId | 流程 |
|------|------------|----------------|------|
| 決鬥 | SSA001 | 目標玩家 ID | 出決鬥 → (Ward 詢問) → 雙方交替出殺 |
| 閃電 | SSA014 | 空字串 | 放入自己判定區 → 每回合判定流轉 |
| 桃園結義 | SHA027 | 空字串 | 全體回 1 血（已滿血無效） |
| 萬箭齊發 | SHA040 | 空字串 | (Ward 詢問) → 全體需出閃，否則扣 1 血 |
| 南蠻入侵 | SS7007 | 空字串 | (Ward 詢問) → 全體需出殺，否則扣 1 血 |
| 無中生有 | SH7046 | 空字串 | (Ward 詢問) → 摸 2 張牌 |
| 樂不思蜀 | SS6006 | 目標玩家 ID | 放入目標判定區 → 目標回合判定（Ward 詢問 → 判定紅心跳過/其他跳過出牌） |
| 五穀豐登 | SH3042 | 空字串 | (Ward Phase1 詢問) → 翻牌 → 逐人選牌（Ward Phase2 每人詢問） |
| 過河拆橋 | SS3003 | 目標玩家 ID | (Ward 詢問) → 選擇棄目標一張牌（使用 useDismantleEffect API） |
| 順手牽羊 | SS3016 | 目標玩家 ID | (Ward 詢問) → 選擇拿目標一張牌（使用 useSnatchEffect API） |
| 借刀殺人 | SCQ064 | 目標玩家 ID | (Ward 詢問) → 目標需出殺或交出武器（使用 useBorrowedSwordEffect API） |

#### 裝備牌（透過 playCard API 裝備）

| 牌名 | cardId 範例 | targetPlayerId | 流程 |
|------|------------|----------------|------|
| 諸葛連弩 Chu Ko Nu | ECA066, EDA092 | 空字串 | 裝備武器（range 1） → 出殺無次數限制 |
| 麒麟弓 Qilin Bow | EH5031 | 空字串 | 裝備武器（range 5） → 殺命中後可棄對方一匹馬（useEquipmentEffect API） |
| 雌雄雙股劍 Yin-Yang Swords | ES2002 | 空字串 | 裝備武器（range 2） → 對異性出殺時目標選棄牌或讓攻擊者摸牌（AskYinYangSwordsEffectEvent） |
| 青釭劍 Black Pommel | ES6019 | 空字串 | 裝備武器（range 2） → 殺無視目標防具（觸發時發 BlackPommelEffectEvent） |
| 青龍偃月刀 Green Dragon Crescent Blade | ES5005 | 空字串 | 裝備武器（range 3） → 殺被閃抵銷時可再出一張殺對同一目標（AskGreenDragonCrescentBladeEffectEvent + GreenDragonCrescentBladeTriggerEvent） |
| 貫石斧 Stone Piercing Axe | ED5083 | 空字串 | 裝備武器（range 3） → 殺被閃抵銷時可棄兩張牌強制命中（AskStonePiercingAxeEffectEvent + StonePiercingAxeTriggerEvent） |
| 八卦陣 Eight Diagrams | ES2015, EC2067 | 空字串 | 裝備防具 → 需出閃時可判定（紅色=閃） |
| 絕影 ShadowHorse | ES5018 | 空字串 | 裝備+1馬 → 其他人對你距離+1 |
| 的盧 HexMark | EC5070 | 空字串 | 裝備+1馬 → 其他人對你距離+1 |
| 爪黃飛電 YellowFlash | EHK052 | 空字串 | 裝備+1馬 → 其他人對你距離+1 |
| 赤兔 RedRabbitHorse | EH5044 | 空字串 | 裝備-1馬 → 你對其他人距離-1 |
| 紫騂 VioletStallion | EDK104 | 空字串 | 裝備-1馬 → 你對其他人距離-1 |
| 黃爪飛電 FerghanaHorse | ESK026 | 空字串 | 裝備-1馬 → 你對其他人距離-1 |

---

## 6. 出無懈可擊

```
POST /api/games/{gameId}/player:playWardCard
```

| 欄位 | 型別 | 說明 |
|------|------|------|
| playerId | String | 出牌玩家 ID |
| cardId | String | 無懈可擊的 cardId（skip 時傳空字串） |
| playType | String | `active`（出牌）或 `skip`（跳過） |

**Ward cardId**：SSJ011、SCQ077、SCK078

**觸發時機**：收到 `WaitForWardEvent` / `AskPlayWardEvent` 後呼叫

**Ward 規則**：
- 奇數張 Ward → 效果取消
- 偶數張 Ward → 效果生效
- 所有有 Ward 的玩家都 skip → 效果生效

**適用的牌**：決鬥、南蠻入侵、萬箭齊發、過河拆橋、順手牽羊、借刀殺人、五穀豐登、無中生有、樂不思蜀（判定前）、閃電（判定前）

---

## 7. 結束出牌

```
POST /api/games/{gameId}/player:finishAction
```

| 欄位 | 型別 | 說明 |
|------|------|------|
| playerId | String | 玩家 ID |

**流程**：結束出牌階段 → 手牌超過體力值上限時進入棄牌階段 → 否則下一位玩家回合開始（判定階段 → 摸牌 → 出牌）

**注意**：`topBehavior` 必須為空才能結束，否則拋 `IllegalStateException`

---

## 8. 棄牌

```
POST /api/games/{gameId}/player:discardCards
```

| 欄位 | 型別 | 說明 |
|------|------|------|
| (body) | List\<String\> | 要棄掉的 cardId 列表 |

**流程**：棄牌至手牌數 = 體力值上限 → 下一位玩家回合開始

---

## 9. 使用裝備效果

```
POST /api/games/{gameId}/player:useEquipmentEffect
```

| 欄位 | 型別 | 說明 |
|------|------|------|
| playerId | String | 玩家 ID |
| targetPlayerId | String | 目標玩家 ID |
| cardId | String | 裝備 cardId |
| playType | String | 出牌類型 |

**適用**：麒麟弓（殺命中後選擇棄對方哪匹馬）

---

## 10. 選擇馬牌

```
POST /api/games/{gameId}/player:chooseHorseCard
```

| 欄位 | 型別 | 說明 |
|------|------|------|
| playerId | String | 玩家 ID |
| cardId | String | 馬的 cardId |

**適用**：麒麟弓命中後選擇棄對方的哪匹馬

---

## 11. 借刀殺人效果

```
POST /api/games/{gameId}/player:useBorrowedSwordEffect
```

| 欄位 | 型別 | 說明 |
|------|------|------|
| currentPlayerId | String | 出借刀殺人的玩家 ID |
| borrowedPlayerId | String | 被借刀的玩家 ID（擁有武器的人） |
| attackTargetPlayerId | String | 被借刀者需要殺的目標 |

**流程**：出借刀殺人 → (Ward 詢問) → 被借刀者出殺或交出武器

---

## 12. 過河拆橋效果

```
POST /api/games/{gameId}/player:useDismantleEffect
```

| 欄位 | 型別 | 說明 |
|------|------|------|
| currentPlayerId | String | 出過河拆橋的玩家 ID |
| targetPlayerId | String | 目標玩家 ID |
| cardId | String | 過河拆橋的 cardId |
| targetCardIndex | Integer | 目標玩家的牌 index（手牌隨機 index / 裝備區 index） |

**流程**：出過河拆橋 → (Ward 詢問) → 選擇棄目標一張牌

---

## 13. 順手牽羊效果

```
POST /api/games/{gameId}/player:useSnatchEffect
```

| 欄位 | 型別 | 說明 |
|------|------|------|
| currentPlayerId | String | 出順手牽羊的玩家 ID |
| targetPlayerId | String | 目標玩家 ID |
| cardId | String | 順手牽羊的 cardId |
| targetCardIndex | Integer | 目標玩家的牌 index |

**流程**：出順手牽羊 → (Ward 詢問) → 選擇拿目標一張牌加入手牌

---

## 14. 五穀豐登選牌

```
POST /api/games/{gameId}/player:chooseCardFromBountifulHarvest
```

| 欄位 | 型別 | 說明 |
|------|------|------|
| playerId | String | 選牌的玩家 ID |
| cardId | String | 從牌池中選擇的 cardId |

**流程**：出五穀豐登 → (Ward Phase1) → 翻 N 張牌 → 逐人選牌 → (Ward Phase2 每人詢問) → 選一張加入手牌

---

## 15. 雌雄雙股劍發動選擇（攻擊者）

```
POST /api/games/{gameId}/player:activateYinYangSwords
```

| 欄位 | 型別 | 說明 |
|------|------|------|
| playerId | String | 攻擊者玩家 ID（裝備雌雄雙股劍的 A） |
| choice | String | `ACTIVATE`（發動效果）或 `SKIP`（不發動，直接進入出閃流程） |

**觸發時機**：收到 `AskActivateYinYangSwordsEvent` 後呼叫

**流程**：
```
A(男) 對 B(女) 出殺 → A 裝備雌雄雙股劍 + 異性
  → 系統發出 AskActivateYinYangSwordsEvent（詢問 A 是否發動）
  → A 呼叫本 API：
    - ACTIVATE: 進入雌雄雙股劍效果 → 系統發出 AskYinYangSwordsEffectEvent（詢問 B）
    - SKIP: 跳過效果 → 直接進入 AskDodge / 八卦陣流程
```

**特殊情況**：
- A 選擇 ACTIVATE 但 B 沒有手牌 → 自動讓 A 摸牌，不詢問 B，直接進入 AskDodge
- 同性出殺時，不會觸發此詢問

---

## 15-2. 雌雄雙股劍效果選擇（目標）

```
POST /api/games/{gameId}/player:useYinYangSwordsEffect
```

| 欄位 | 型別 | 說明 |
|------|------|------|
| playerId | String | 做選擇的玩家 ID（被殺的目標 B） |
| choice | String | `TARGET_DISCARDS`（目標棄一張手牌）或 `ATTACKER_DRAWS`（讓攻擊者摸牌） |
| cardId | String | 要棄的手牌 cardId（choice=TARGET_DISCARDS 時必填；ATTACKER_DRAWS 時傳空字串） |

**觸發時機**：收到 `AskYinYangSwordsEffectEvent` 後呼叫（僅在攻擊者選擇 `ACTIVATE` 且目標有手牌時出現）

**流程**：
```
（承上，A 選擇 ACTIVATE）
  → 系統發出 AskYinYangSwordsEffectEvent（詢問 B 棄牌或讓 A 摸牌）
  → B 呼叫本 API 做選擇：
    - TARGET_DISCARDS: B 棄一張手牌到墓地 → 發出 YinYangSwordsEffectEvent → 繼續 AskDodge
    - ATTACKER_DRAWS: A 從牌堆摸 1 張 → 發出 YinYangSwordsEffectEvent → 繼續 AskDodge
```

**備註**：此 API 獨立於 `playCard`

---

## 16. 青龍偃月刀效果選擇

```
POST /api/games/{gameId}/player:useGreenDragonCrescentBladeEffect
```

| 欄位 | 型別 | 說明 |
|------|------|------|
| playerId | String | 攻擊者玩家 ID |
| choice | String | `KILL`（再出一張殺）或 `SKIP`（不發動） |
| killCardId | String | 要追加的殺 cardId（choice=KILL 時必填；SKIP 時傳空字串） |

**觸發時機**：收到 `AskGreenDragonCrescentBladeEffectEvent` 後呼叫

**流程**：
```
A (裝備青龍偃月刀) 對 B 出殺 → B 出閃抵銷
  → 系統發出 AskGreenDragonCrescentBladeEffectEvent（詢問 A）
  → A 呼叫本 API 做選擇：
    - SKIP: 殺被抵銷結束，流程正常結束
    - KILL: A 棄第二張殺到墓地 → 發出 GreenDragonCrescentBladeTriggerEvent
            → B 收到 AskDodgeEvent（若有防具先 AskPlayEquipmentEffectEvent）
            → B 再出閃 → 回到青龍偃月刀詢問（可多次循環）
            → B 不出閃 → 扣血，流程結束
```

**特殊情況**：
- A 沒有殺或提供非殺 cardId → 拋例外
- 青龍偃月刀追加的殺**不計入**每回合殺次數限制
- 可與八卦陣並存：B 出閃若是八卦陣判定，觸發青龍偃月刀後，A 再出殺，B 再判定八卦陣

**備註**：此 API 獨立於 `playCard`

---

## 17. 貫石斧效果選擇

```
POST /api/games/{gameId}/player:useStonePiercingAxeEffect
```

| 欄位 | 型別 | 說明 |
|------|------|------|
| playerId | String | 攻擊者玩家 ID |
| choice | String | `DISCARD_TWO`（棄兩張牌強制命中）或 `SKIP`（不發動） |
| discardCardIds | List\<String\> | 要棄的兩張 cardId（DISCARD_TWO 時必填，長度必須為 2；SKIP 時傳空陣列） |

**觸發時機**：收到 `AskStonePiercingAxeEffectEvent` 後呼叫

**流程**：
```
A (裝備貫石斧) 對 B 出殺 → B 出閃抵銷
  → 系統檢查 A 是否有 ≥2 張可棄牌（手牌 + 裝備區）
  → 有：發出 AskStonePiercingAxeEffectEvent（詢問 A）
  → A 呼叫本 API 做選擇：
    - SKIP: 殺被抵銷結束
    - DISCARD_TWO: A 棄兩張牌到墓地 → 發出 StonePiercingAxeTriggerEvent
                   → B 強制扣血（跳過 AskDodge）
                   → 若 B HP=0 進入瀕死流程
```

**特殊情況**：
- 可棄的牌可以是手牌或裝備區的牌（包括貫石斧本身）
- A 可棄牌總數 < 2 時，不會觸發貫石斧，殺直接被閃抵銷
- 棄牌不足 2 張、傳入非 A 擁有的 cardId → 拋例外

**備註**：此 API 獨立於 `playCard`

---

## 18. 丈八蛇矛出殺（棄兩張牌當殺）

```
POST /api/games/{gameId}/player:useViperSpearKill
```

| 欄位 | 型別 | 說明 |
|------|------|------|
| playerId | String | 攻擊者／回應者玩家 ID（裝備丈八蛇矛） |
| targetPlayerId | String | 攻擊／回應目標玩家 ID。Active 必填；Passive 時 BarbarianInvasion / Duel 不需要，BorrowedSword 必填 |
| discardCardIds | List\<String\> | 作為殺的兩張手牌 cardId（長度必須為 2） |

**觸發時機**：裝備丈八蛇矛的玩家以兩張手牌當殺**使用或打出**（不透過 `playCard` API）。分為：
- **Active（出牌階段主動出殺）**：自己回合的出牌階段，topBehavior 為空時呼叫
- **Passive（被動回應殺）**：被要求出殺時改用丈八蛇矛回應，支援以下 top behavior：
  - `BarbarianInvasionBehavior`：南蠻入侵詢問出殺
  - `DuelBehavior`：決鬥輪到自己出殺
  - `BorrowedSwordBehavior`：被借刀殺人要求出殺

**Active 流程**：
```
A (裝備丈八蛇矛) 呼叫本 API → 棄兩張手牌到墓地 → 視為出殺
  → 系統發出 ViperSpearKillTriggerEvent（通知前端丈八蛇矛發動）
  → 進入殺流程：B 收到 AskDodgeEvent
    - B 出閃 → 殺被抵銷
    - B 不出閃 → B 扣血（HP=0 進入瀕死流程）
```

**Passive 流程**（以南蠻入侵為例）：
```
南蠻入侵發動，B 收到 AskKillEvent → B (裝備丈八蛇矛) 呼叫本 API
  （targetPlayerId 可省略；BorrowedSword 場景需帶被攻擊者）
  → 棄兩張手牌到墓地 → ViperSpearKillTriggerEvent
  → 視為對來源玩家出一張殺，繼續原本 ask-Kill 結算
```

**特殊情況**：
- 攻擊者／回應者必須裝備丈八蛇矛
- Active：本回合若已出過殺（無諸葛連弩）仍受殺次數限制；目標必須在攻擊範圍
- discardCardIds 必須剛好為 2 張、不可重複，且必須是手牌中的 cardId
- topBehavior 若不是上述三種被動場景之一，呼叫會 throw（例如 AskDodge / AskPeach 不支援）

**備註**：
- 此 API 獨立於 `playCard`
- 內部使用虛擬殺（VirtualKill，cardId = `VIPER_SPEAR_VIRTUAL_KILL`），不在手牌也不在 PlayCard enum 中

---

## 19. 方天畫戟多目標殺

```
POST /api/games/{gameId}/player:useHeavenlyDoubleHalberdKill
```

| 欄位 | 型別 | 說明 |
|------|------|------|
| playerId | String | 攻擊者玩家 ID（裝備方天畫戟） |
| cardId | String | 要打出的殺 cardId（必須為攻擊者**最後一張手牌**） |
| targetPlayerIds | List\<String\> | 全部目標玩家 ID（size 1~3；不重複、不含自己、皆需在攻擊範圍內）。傳入順序不影響結算 — 系統會以攻擊者下家起的**座位順序**依次詢問各目標 |

**觸發時機**：攻擊者裝備方天畫戟、手上**只剩一張殺**時主動呼叫（不透過 `playCard` API）

**流程**：
```
A (裝備方天畫戟) 呼叫本 API → 棄最後一張殺到墓地
  → 系統發出 HeavenlyDoubleHalberdKillTriggerEvent（含所有目標 id）
  → 依序對每個目標：
    - 目標收 AskDodgeEvent（若有防具先 AskPlayEquipmentEffectEvent）
    - 目標出閃 → 該目標免傷，繼續下一目標
    - 目標不出閃 → 扣血（HP=0 進入瀕死流程；救回後繼續下一目標）
  → 所有目標處理完畢 → behavior 結束
```

**特殊情況**：
- 攻擊者必須裝備方天畫戟
- 該殺必須是攻擊者最後一張手牌（出牌後手牌為空才會觸發武器效果）
- 所有目標皆需在攻擊者攻擊範圍內、不重複、不可含攻擊者自己
- 單目標時（`targetPlayerIds.size() == 1`）短路為一般殺
- 中間目標進入瀕死：暫停 polling，dying flow 結束後恢復下一目標詢問
- 可與八卦陣 / 青釭劍 / 各防具並存：目標各自獨立判定

**備註**：此 API 獨立於 `playCard`

---

## 20. 奸雄（曹操武將技）效果選擇

```
POST /api/games/{gameId}/player:useJianXiongEffect
```

| 欄位 | 型別 | 說明 |
|------|------|------|
| playerId | String | 受傷玩家（曹操）ID |
| choice | String | `ACCEPT`（獲得造成傷害的牌）或 `SKIP`（放棄） |

**觸發時機**：曹操（`WEI001`）受到傷害且仍存活、top behavior 為 `NormalActiveKillBehavior`（含子類 ViperSpearKill / HeavenlyDoubleHalberdKill）時，系統廣播 `AskJianXiongEffectEvent` 後呼叫。

**啟動條件**（同時成立）：
1. 受傷玩家武將為曹操
2. `sourceCard != null`（武將技直接傷害如剛烈／離間／雷擊 sourceCard 為 null，不觸發）
3. 受傷後仍活著（HP > 0；瀕死流程不觸發）
4. Top behavior 屬於白名單之一：
   - `NormalActiveKillBehavior`（含子類 ViperSpearKill / HeavenlyDoubleHalberdKill）— 普通殺 / 武器觸發殺
   - `DuelBehavior` — 決鬥輸給對手（含 Path A 不出殺、Path B 互換後沒殺）
   - `LightningJudgementBehavior` — 閃電判定打中（Ward 路徑）
   - `BarbarianInvasionBehavior` / `ArrowBarrageBehavior` — AOE 受傷（曹操不出殺/不出閃）
   - 空 stack — 閃電判定無 Ward 路徑

**取牌規則**（依傷害來源）：
- **普通殺 / 火攻轉殺 / 武器觸發殺（青龍偃月刀、貫石斧）/ 決鬥 / 閃電 / 南蠻入侵 / 萬箭齊發**：獲得 sourceCard 本身（1 張），`sourceCardIds = [<sourceCard.id>]`
- **丈八蛇矛攻擊（FAQ 特例）**：獲得攻擊者打出的**兩張**手牌（VirtualKill 不算），`sourceCardIds = [<discard1>, <discard2>]`

**流程**：
```
B 對曹操出殺（或丈八蛇矛攻擊）→ 曹操不出閃、扣血未死
  → 系統 push WaitingJianXiongResponseBehavior 並 broadcast AskJianXiongEffectEvent
  → 曹操呼叫本 API：
    - ACCEPT：把 sourceCardIds 列出的牌全數從棄牌堆移到手牌 → JianXiongEffectEvent(taken=true)
    - SKIP：JianXiongEffectEvent(taken=false)，牌留在墓地
  → behavior 彈出，回到正常流程
```

**特殊情況 / 整合機制**：
- **致命傷被救回後仍可發動**：曹操受致命傷進入瀕死，被桃救回 → DyingAskPeachBehavior 在 revival 分支 replay `SkillEngine.onDamaged`，奸雄正常觸發
  - 一般傷害：pendingSourceCardId 帶 sourceCard.id，replay 時 PlayCard.findById 重建
  - 丈八蛇矛 (VirtualKill) 致命：pendingViperSpearDiscardCardIds 帶兩張棄牌 id，replay 時 JianXiongSkill 取得 DyingAskPeachBehavior 上的 pending discards → ACCEPT 拿兩張棄牌
- 一次 DamageEvent 只觸發 1 次（多點傷害不重複觸發）
- 若 ACCEPT 時 sourceCard 已不在棄牌堆（被其他效果先取走）→ throw `IllegalStateException`
- AOE polling 整合：BarbarianInvasion / ArrowBarrage 的 polling 流程偵測 WaitingJX 介入後，將 polling-advance 註冊為 callback 由 WaitingJX.resolveChoice 在 isOneRound=true 之前 resume，確保 stack 與 activePlayer 一致

**備註**：此 API 獨立於 `playCard`

---

## 21. 護駕（曹操主公技）效果選擇

```
POST /api/games/{gameId}/player:useHuJiaEffect
```

| 欄位 | 型別 | 說明 |
|------|------|------|
| playerId | String | 當前被詢問的魏勢力玩家 ID（不是曹操） |
| choice | String | `ACCEPT`（代替主公出閃）或 `DECLINE`（拒絕） |
| cardId | String? | `ACCEPT` 必填，為要打出的閃 cardId；`DECLINE` 時可為 null |

**觸發時機**：曹操（`WEI001`）為主公（`MONARCH`）且 stack 頂為 emit `AskDodgeEvent` 的 behavior 時，系統不發 `AskDodgeEvent`，改為依座位順序（曹操下家起）對每位存活魏勢力武將 broadcast `AskHuJiaEffectEvent`。任一人 ACCEPT → 視為曹操打出此閃；全部 DECLINE → fallback emit 原本的 `AskDodgeEvent(曹操)`。

**啟動條件**（同時成立）：
1. 受詢問玩家武將為曹操（WEI001）
2. 曹操為主公（`role == MONARCH`）
3. 至少有一個其他存活魏勢力武將（`faction == WEI` && 非曹操本人）
4. Top behavior 為以下 emitter 之一（實作 `HuJiaCompatibleAskDodgeBehavior`）：
   - `NormalActiveKillBehavior`（普通殺）
   - `ArrowBarrageBehavior`（萬箭齊發）
   - `HeavenlyDoubleHalberdKillBehavior`（方天畫戟）

**流程**：
```
A 對曹操 (B) 出殺（或萬箭齊發/方天畫戟瞄到曹操）
  → 系統 push WaitingHuJiaResponseBehavior 並 broadcast AskHuJiaEffectEvent(座位次序下一位 Wei)
  → activePlayer 切到該 Wei 武將
  → Wei 武將呼叫本 API：
    - ACCEPT: Wei 棄一張閃到墓地 → HuJiaEffectEvent(accepted=true)
              → pop WaitingHuJia → parent behavior 接手「視為曹操打出此閃」的後續（青龍/貫石斧/AOE polling 推進）
    - DECLINE: HuJiaEffectEvent(accepted=false)
              → 若還有下一位 Wei：activePlayer 換人、AskHuJiaEffectEvent(next)
              → 若已是最後一位：pop WaitingHuJia、AskDodgeEvent(曹操)、activePlayer 切回曹操
```

**特殊情況**：
- Wei 武將沒有閃：`AskHuJiaEffectEvent.dodgeCardIdsInHand` 為空，只能 DECLINE
- ACCEPT 但 cardId 不在 Wei 手中 → 400 `IllegalArgumentException`
- ACCEPT 但 cardId 不是閃 → 400 `IllegalArgumentException`
- 曹操自己無閃 + 所有 Wei 全 DECLINE → fallback AskDodge(曹操) 後 SKIP 受傷（不影響後續流程）
- 視為曹操出閃，因此攻擊者的青龍偃月刀 / 貫石斧鏈仍正常觸發
- v1 範圍：護駕僅在三處 AskDodge emitter（普通殺/萬箭齊發/方天畫戟）攔截；其他二次 AskDodge 場景（八卦陣失敗、青龍鏈、雌雄雙股劍、丈八蛇矛被動殺、瀕死 resume）為 follow-up

**備註**：此 API 獨立於 `playCard`。前端在收到 `AskHuJiaEffectEvent` 時應只開啟被詢問玩家的選擇 UI。

---

## 22. 被動 / 鎖定武將技一覽（無獨立 endpoint）

以下技能為鎖定技或自動觸發，後端自動套用，前端不需呼叫任何 API；
僅需注意對應的遊戲狀態變化（摸牌數 / 距離 / 目標合法性 / 出殺次數）。

| 武將 | 技能 | 效果（後端自動套用） | 前端可觀察的差異 |
|---|---|---|---|
| 馬超 SHU006 | 馬術 | 計算與其他角色距離 -1 | 攻擊/順手牽羊範圍變大；超距出牌不再 400 |
| 張飛 SHU003 | 咆哮 | 使用殺無次數限制 | 同回合第二張殺不再 400 |
| 周瑜 WU005 | 英姿 | 摸牌階段 +1（共 3）；手牌上限 = max(HP, 4) | DrawCardEvent 數量、NotifyDiscardEvent 棄牌數 |
| 許褚 WEI005 | 裸衣 | 摸牌階段 -1（共 1）；自己回合殺/決鬥傷害 +1 | DrawCardEvent 數量、PlayerDamagedEvent 扣 2 |
| 陸遜 WU007 | 謙遜 | 不能成為南蠻/萬箭/樂不思蜀/閃電目標 | AOE 詢問跳過陸遜；樂不思蜀指定陸遜 → 400 |
| 陸遜 WU007 | 連營 | 失去最後一張手牌 → 摸 1 | 出/棄最後一張牌後緊接 DrawCardEvent |
| 諸葛亮 SHU004 | 空城 | 手牌 0 時不能被殺/決鬥指定 | 指定空手牌諸葛亮 → 400 |
| 黃月英 SHU007 | 集智 | 使用錦囊 → 摸 1 | 出錦囊後緊接 DrawCardEvent |
| 黃月英 SHU007 | 奇才 | 使用錦囊無距離限制 | 超距順手牽羊不再 400 |

**v1 範圍備註**：
- 謙遜照 issue #192 文字實作（南蠻/萬箭/樂不思蜀/閃電）；與官方規則（順手牽羊+樂不思蜀）不同，調整時改 `QianXunSkill.isImmune` 即可
- 連營 v1 覆蓋「自己出牌 / response 出牌 / 棄牌階段」失去最後手牌；被拆/被順走最後一張為 follow-up
- 集智 v1 自動摸（不問）；無懈可擊（playWardCard 路徑）暫不觸發集智，為 follow-up

---

## 23. 通用武將技效果回應（useSkillEffect）

```
POST /api/games/{gameId}/player:useSkillEffect
```

| 欄位 | 型別 | 說明 |
|------|------|------|
| playerId | String | 回應的玩家 ID |
| skillName | String | 技能名（見下表） |
| choice | String | 選擇值，依技能而定 |
| cardIds | List\<String\>? | 選擇的牌（依技能） |
| targetPlayerId | String? | 選擇的目標（依技能） |

**觸發時機**：收到 `AskSkillEffectEvent`（內含 skillName / playerId / dataCardIds / dataPlayerId）後呼叫。
回應後廣播 `SkillEffectEvent`（accepted + data）。

### 各技能 payload

| 技能 | 觸發 | choice | cardIds | targetPlayerId |
|---|---|---|---|---|
| 反饋（司馬懿） | 受傷後 | `ACCEPT` / `SKIP` | 可選：指定來源裝備 id（不給 = 抽來源第一張手牌） | — |
| 遺計（郭嘉） | 受傷後 | `ACCEPT`（自摸 2）/ `GIVE`（令他人獲得 1）/ `SKIP` | — | GIVE 必填 |
| 剛烈（夏侯惇）第一段 | 受傷後 | `ACCEPT`（判定）/ `SKIP` | — | — |
| 剛烈 第二段（問傷害來源） | 判定非紅桃後 | `DISCARD` / `DAMAGE` | DISCARD 必填 2 張手牌 | — |

### 自動觸發技（無需呼叫本 API，僅廣播 `SkillEffectEvent`）

| 武將 | 技能 | 行為 |
|---|---|---|
| 郭嘉 | 天妒 | 自己判定牌生效後自動收入手牌（閃電 / 樂不思蜀 / 剛烈判定） |
| 甄姬 | 洛神 | 回合開始自動判定：黑色收入手牌續判、紅色停 |
| 馬超 | 鐵騎 | 出殺指定目標後自動判定：非紅桃 → 目標不能出閃直接結算 |
| 孫尚香 | 梟姬 | 失去裝備（被拆 / 被順 / 被反饋取走）自動摸 2 |

### 出牌階段主動技（topBehavior 為空時直接呼叫本 API 發動）

| 技能 | 限制 | choice | cardIds | targetPlayerId |
|---|---|---|---|---|
| 制衡（孫權） | — | `ACCEPT` | 要棄的牌（手牌+裝備 id，≥1）→ 摸等量 | — |
| 突襲（張遼） | 每回合 1 次 | `ACCEPT` | **目標玩家 id 列表**（1~2 名，借用此欄位） | — |
| 苦肉（黃蓋） | HP ≥ 2 | `ACCEPT` | — | — |
| 反間（周瑜）第一段 | 每回合 1 次 | `ACCEPT` | [要給的手牌 id] | 目標玩家 |
| 反間 第二段（目標回應） | — | `SPADE`/`HEART`/`DIAMOND`/`CLUB` | — | — |
| 結姻（孫尚香） | 每回合 1 次 | `ACCEPT` | 2 張手牌 id（傷最重者由系統判定） | — |
| 仁德（劉備） | 回血每回合 1 次 | `ACCEPT` | 要給的手牌 id（≥1；累積 2 張回 1 體力） | 接收者 |
| 觀星（諸葛亮）第一段 | 每回合 1 次 | `ACCEPT` | — | — |
| 觀星 第二段 | — | `ARRANGE` | 回堆頂的順序（未列出 → 置堆底） | — |

**v1 範圍備註**：
- 反饋 / 遺計 / 剛烈在 AOE polling（南蠻 / 萬箭）中不觸發（defer-resume 整合 follow-up）；瀕死不觸發
- 剛烈 DAMAGE 反傷不進瀕死流程整合（follow-up）
- 鐵騎 v1 自動判定（不問）；目標有八卦陣時走防具路徑不受鐵騎影響（follow-up）
- 梟姬不覆蓋「主動換裝蓋掉舊裝備」路徑（follow-up）
- 苦肉 v1 需 HP ≥ 2（瀕死整合 follow-up）；反間目標受傷不進瀕死流程（follow-up）
- 觀星 issue 時機為回合開始；v1 以出牌階段主動發動實作（時機整合 follow-up）
- 突襲 issue 時機為出牌階段開始；v1 出牌階段任意時點可發動（每回合一次）

---

## WebSocket 事件類型

前端透過 WebSocket 接收以下事件，根據事件類型決定 UI 行為：

### 遊戲流程事件

| 事件 | 說明 | 觸發 API |
|------|------|----------|
| `PlayCardEvent` | 有人出牌 | playCard |
| `PlayWardCardEvent` | 有人出無懈可擊 | playWardCard |
| `GameStatusEvent` | 遊戲狀態更新（每次操作都附帶） | 所有 API |
| `DrawCardEvent` | 玩家摸牌 | finishAction / playCard |
| `DiscardEvent` | 玩家棄牌 | discardCards |

### 需要玩家回應的事件

| 事件 | 說明 | 玩家需呼叫的 API |
|------|------|------------------|
| `AskPlayWardEvent` | 詢問是否出無懈可擊（有 Ward 的玩家收到） | playWardCard |
| `WaitForWardEvent` | 等待其他人出無懈可擊（沒 Ward 的玩家收到） | （等待） |
| `AskKillEvent` | 需要出殺（南蠻入侵/決鬥/借刀殺人） | playCard |
| `AskDodgeEvent` | 需要出閃（萬箭齊發/被殺） | playCard |
| `AskPeachEvent` | 瀕死需要出桃 | playCard |
| `BountifulHarvestEvent` | 五穀豐登輪到你選牌 | chooseCardFromBountifulHarvest |
| `AskActivateYinYangSwordsEvent` | 雌雄雙股劍：詢問攻擊者是否發動效果 | activateYinYangSwords |
| `AskYinYangSwordsEffectEvent` | 雌雄雙股劍效果：目標選擇棄牌或讓攻擊者摸牌（攻擊者選 ACTIVATE 後才出現） | useYinYangSwordsEffect |
| `AskGreenDragonCrescentBladeEffectEvent` | 青龍偃月刀效果：攻擊者選擇是否再出一張殺 | useGreenDragonCrescentBladeEffect |
| `AskStonePiercingAxeEffectEvent` | 貫石斧效果：攻擊者選擇是否棄兩張牌強制命中 | useStonePiercingAxeEffect |
| `ViperSpearKillTriggerEvent` | 丈八蛇矛發動：攻擊者棄兩張牌作為虛擬殺使用（通知事件，無需回應） | useViperSpearKill |
| `AskJianXiongEffectEvent` | 奸雄發動：曹操選擇是否獲得造成傷害的牌（含 `playerId`、`sourceCardIds : List<String>`） | useJianXiongEffect |

### 效果事件

| 事件 | 說明 |
|------|------|
| `WardEvent` | Ward 結算結果 |
| `BarbarianInvasionEvent` | 南蠻入侵效果 |
| `ArrowBarrageEvent` | 萬箭齊發效果 |
| `DuelEvent` | 決鬥效果 |
| `BountifulHarvestChooseCardEvent` | 玩家選了五穀豐登的牌 |
| `ContentmentEvent` | 樂不思蜀判定結果 |
| `LightningEvent` | 閃電判定結果 |
| `LightningTransferredEvent` | 閃電轉移到下家 |
| `SomethingForNothingEvent` | 無中生有效果 |
| `PeachGardenEvent` | 桃園結義效果 |
| `BorrowedSwordEvent` | 借刀殺人效果 |
| `EquipmentEvent` | 裝備效果 |
| `JudgementEvent` | 判定結果（八卦陣/樂不思蜀/閃電） |
| `YinYangSwordsEffectEvent` | 雌雄雙股劍效果結算：含 attackerPlayerId、targetPlayerId、choice（TARGET_DISCARDS/ATTACKER_DRAWS）、discardedCardId |
| `BlackPommelEffectEvent` | 青釭劍發動，殺無視目標防具（含 attackerPlayerId、targetPlayerId） |
| `GreenDragonCrescentBladeTriggerEvent` | 青龍偃月刀發動，追加一張殺（含 attackerPlayerId、targetPlayerId、killCardId） |
| `StonePiercingAxeTriggerEvent` | 貫石斧發動，棄兩張牌強制命中（含 attackerPlayerId、targetPlayerId、discardedCardIds） |
| `ViperSpearKillTriggerEvent` | 丈八蛇矛發動，棄兩張牌當殺使用（含 attackerPlayerId、targetPlayerId、discardedCardIds） |
| `HeavenlyDoubleHalberdKillTriggerEvent` | 方天畫戟發動，多目標殺（含 attackerPlayerId、cardId、targetPlayerIds） |
| `JianXiongEffectEvent` | 奸雄結算結果（含 `playerId`、`sourceCardIds : List<String>`、`taken`） |

---

## Debug API（測試用）

前端測試時可用以下 API 查看或調整牌堆順序。永遠可用，不需要特殊 profile。

### 19. 查看牌堆

```
GET /api/debug/games/{gameId}/deck
```

**回傳**：
```json
{
  "gameId": "my-id",
  "deckSize": 78,
  "cardIds": ["BS8008", "BH2028", "BH3029", "..."]
}
```

- `cardIds[0]` = 下一張被抽的牌
- `cardIds[1]` = 再下一張
- 以此類推

---

### 20. 設定牌堆

```
PUT /api/debug/games/{gameId}/deck
```

| 欄位 | 型別 | 說明 |
|------|------|------|
| cardIds | List\<String\> | 牌堆內容（index 0 = 下一張被抽的牌） |

**Request body**：
```json
{
  "cardIds": ["BS8008", "BH2028", "BH3029"]
}
```

**回傳**：同查看牌堆格式

**特殊情況**：
- 全量替換，原本牌堆會被完全覆蓋
- 無效 cardId → 400 `{"message": "Invalid card ID: XYZ"}`
- 允許重複 cardId（測試可能需要非正常牌堆）
- 任何遊戲階段都可以呼叫（debug 不設限）

**使用範例**：
```bash
# 確保接下來 A 摸到殺和閃
curl -X PUT http://localhost:8080/api/debug/games/my-id/deck \
  -H 'Content-Type: application/json' \
  -d '{"cardIds": ["BS8008", "BH2028"]}'

# 驗證牌堆已更新
curl http://localhost:8080/api/debug/games/my-id/deck
```
