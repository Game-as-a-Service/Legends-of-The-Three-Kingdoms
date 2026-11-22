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

**流程**：主公選將 → 其他玩家收到可選將領列表；另廣播全員選將進度（見下方 GeneralSelectionStatusEvent）

---

## 4. 其他玩家選將

```
POST /api/games/{gameId}/player:otherChooseGeneral
```

| 欄位 | 型別 | 說明 |
|------|------|------|
| playerId | String | 玩家 ID |
| generalId | String | 選擇的武將 ID |

**流程**：每位玩家選完都廣播全員選將進度；全部選完 → 發牌（每人 4 張）→ 主公回合開始（`InitialEndEvent` + `RoundStartEvent` 即開局訊號，無另外的「開始遊戲」API）

### 選將進度廣播 GeneralSelectionStatusEvent（issue #237）

每次有人選完武將（含主公）廣播給**全部玩家**：

```json
{
  "gameId": "my-id",
  "playerIds": ["Scolley", "Happypola", "YangJun", "Tux"],
  "event": "GeneralSelectionStatusEvent",
  "data": {
    "selectedPlayerIds": ["Scolley", "Tux"],
    "pendingPlayerIds": ["Happypola", "YangJun"],
    "selectedCount": 2,
    "totalCount": 4,
    "allSelected": false
  },
  "message": "Tux 已選擇武將（2/4）"
}
```

- **不帶武將 id** — 身分局非主公暗選，武將由開局的 `InitialEndEvent` 揭曉（主公另有 `MonarchGeneralChosenEvent` 公開亮將）
- 最後一位選完會先收到 `allSelected: true` 的進度，接著才是 `InitialEndEvent`
- 前端判斷：`data.pendingPlayerIds.length === 0` = 全員已選；收到 `InitialEndEvent` = 進入牌局
- 主公回合開始若需先詢問技能（觀星 / 洛神 / 裸衣 / 突襲），開局的回合推播 `events` 只有 `RoundStartEvent` + `AskSkillEffectEvent`（尚無 `JudgementEvent` / `DrawCardEvent`），回應後才判定、摸牌
- **重整/重連補狀態**：選將階段呼叫 `GET /api/games/{gameId}?playerId=xxx`，推播的 `findGameEvent` 會在 `data.selectionStatus` 帶同一份進度物件（非選將階段為 `null`）

### 連線狀態廣播 PlayerConnectionStatusEvent（issue #239）

玩家 WebSocket 訂閱 `/websocket/legendsOfTheThreeKingdoms/{gameId}/{playerId}` 或斷線時，廣播該局全員：

```json
{
  "gameId": "my-id",
  "event": "PlayerConnectionStatusEvent",
  "data": {
    "connectedPlayerIds": ["Scolley", "Happypola", "Tux"],
    "connectedCount": 3,
    "totalCount": 4
  },
  "message": "Tux 已連線（3/4）"
}
```

- 連線是傳輸層狀態（不進遊戲存檔）：訂閱即連線、STOMP session 斷線即離線；重整會連發「已離線」+「已連線」兩則，前端照最後一則渲染
- 同玩家多分頁：首個分頁連上才算連線、最後一個關閉才算離線
- 遊戲尚未建立時的訂閱、或非該局玩家的訂閱（觀戰）→ 不廣播
- 前端判斷：`data.connectedCount === data.totalCount` = 全員在線；此事件在**整場遊戲**都會發（牌局中掉線也會廣播，可做斷線提示）

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

**流程**：出借刀殺人 → (Ward 詢問) → 出借刀者以本 API 指定被借刀者與攻擊目標 → 被借刀者收到 `BorrowedSwordEvent` 後回應（見下）

### 被借刀者收到的詢問事件

```json
{
  "event": "BorrowedSwordEvent",
  "data": {
    "cardId": "SCK065",
    "borrowedPlayerId": "<被借刀者>",
    "attackTargetPlayerId": "<攻擊目標>"
  },
  "message": "要求 <被借刀者> 對 <攻擊目標> 出殺"
}
```

⚠️ **被詢問者欄位是 `borrowedPlayerId`，不是 `playerId`**（與 `AskKillEvent` / `AskDodgeEvent`
等以 `data.playerId` 標示被詢問者的事件不同）。前端分派彈窗時若以 `data.playerId` 過濾，
被借刀者永遠不會進流程 — 需改用 `data.borrowedPlayerId`。
本事件**不帶出借刀者（A）的 id**；skip 交武器不需要它（見下方 `targetPlayerId` 說明）。

### 被借刀者的回應（走 playCard API，無獨立 endpoint）

**出殺** — 對指定目標出殺，之後進入正常的殺 → 閃流程：

```json
POST /api/games/{gameId}/player:playCard
{ "playerId": "<被借刀者>", "targetPlayerId": "<攻擊目標>", "cardId": "<殺的 cardId>", "playType": "active" }
```

**不出殺（交出武器）** — `playType: "skip"`、`cardId` 空字串；武器移入出借刀者手牌，廣播 `WeaponUsurpationEvent`：

```json
POST /api/games/{gameId}/player:playCard
{ "playerId": "<被借刀者>", "targetPlayerId": "", "cardId": "", "playType": "skip" }
```

**`targetPlayerId` 在 skip 路徑不影響結算**：收下武器的人由後端從 borrowed sword behavior
取（= 出借刀者），與請求帶什麼無關；該欄位只會原樣出現在廣播的 `PlayCardEvent.targetPlayerId`。
所以前端從 `BorrowedSwordEvent` 拿不到出借刀者 id 時，送空字串 `""` 即可（習慣寫法，同其他 skip 請求）。
出殺（`playType: "active"`）則相反 — `targetPlayerId` 必填且必須是 `attackTargetPlayerId`。

**注意**：被借刀者手上完全沒有殺（且不能以丈八蛇矛替代、也不能發動激將）時，系統不詢問、直接自動交出武器 —
前端不會收到 `BorrowedSwordEvent`，直接收 `WeaponUsurpationEvent`。有轉化技（龍膽/武聖）時亦可改用
`player:useSkillEffect` 以轉化牌當殺回應（見 §轉化技）。

**激將（被借刀者為主公劉備、且有其他存活蜀將）**：即使劉備沒有殺也會收到 `BorrowedSwordEvent`。
劉備可改送 `player:useSkillEffect`（`skillName: "激將"`, `choice: "ACCEPT"`, `cardIds`/`targetPlayerId` 不需帶，
目標固定為 `attackTargetPlayerId`）→ 依座位順序以 `AskSkillEffectEvent(激將)` 詢問蜀將（回應同 §23 激將）。
蜀將 ACCEPT → 該殺視為劉備對攻擊目標出殺（廣播 `PlayCardEvent`，playerId = 劉備），進入正常殺 → 閃流程，劉備保留武器；
全部拒絕 → 劉備有殺：重新廣播 `BorrowedSwordEvent`，劉備自己出殺或 skip；劉備無殺：直接交出武器（`WeaponUsurpationEvent`）。

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

**目標限制**：距離 1 以內（馬術/奇才可放寬）、目標需有手牌或裝備、
目標不得具備免疫技（陸遜 謙遜 → 400）。

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

**Ward 詢問名單**：Phase 1（取消整張五穀豐登）不含出牌者；Phase 2 為逐人效果結算，
詢問名單含**所有**持無懈者 — 包括出牌者與剛從牌池選走無懈的玩家（取得後立即可用）

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
    - 目標是大喬 → 先問流離（AskSkillEffectEvent）
    - 攻擊者是馬超 → 判鐵騎（SkillEffectEvent）；紅色 → 直接扣血，跳過下面的問閃
    - 目標收 AskDodgeEvent（若有防具先 AskPlayEquipmentEffectEvent）
    - 目標出閃 → 該目標免傷，繼續下一目標
    - 目標不出閃 → 扣血（HP=0 進入瀕死流程；救回後繼續下一目標）
    - 目標受傷觸發受傷後技能（奸雄／反饋／剛烈／遺計）→ 先問該技能，結算完才輪到下一目標（同南蠻／萬箭；瀕死救回後補問的亦同）
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
  - 轉化殺（武聖/龍膽，VirtualKill）致命：同一欄位 pendingViperSpearDiscardCardIds 帶被轉化的真實來源牌 id → ACCEPT 拿該來源牌
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

**觸發時機**（issue #217 起為主動觸發）：曹操（`WEI001`）為主公（`MONARCH`）且 stack 頂為 emit `AskDodgeEvent` 的 behavior 時，系統不發 `AskDodgeEvent`，改為**先對曹操本人** broadcast `AskSkillEffectEvent`（`skillName="護駕"`）詢問是否發動：

- 曹操 **ACCEPT** → 依座位順序（曹操下家起）對每位存活魏勢力武將 broadcast `AskHuJiaEffectEvent`。任一人 ACCEPT → 視為曹操打出此閃；全部 DECLINE → fallback emit 原本的 `AskDodgeEvent(曹操)`
- 曹操 **SKIP** → 直接 emit `AskDodgeEvent(曹操)` 自己出閃，不詢問魏將

曹操回應「是否發動」走通用 endpoint（見 §23 useSkillEffect）：

```
POST /api/games/{gameId}/player:useSkillEffect
{ "playerId": "<曹操>", "skillName": "護駕", "choice": "ACCEPT" | "SKIP" }
```

**啟動條件**（同時成立）：
1. 受詢問玩家武將為曹操（WEI001）
2. 曹操為主公（`role == MONARCH`）
3. 至少有一個其他存活魏勢力武將（`faction == WEI` && 非曹操本人）
4. 問閃的 host 為以下 behavior 之一（實作 `HuJiaCompatibleAskDodgeBehavior`）：
   - `NormalActiveKillBehavior`（普通殺；含轉化殺、丈八蛇矛的殺）
   - `ArrowBarrageBehavior`（萬箭齊發）
   - `HeavenlyDoubleHalberdKillBehavior`（方天畫戟）

**觸發的問閃時機**（以上 host 的所有問閃都會先問護駕）：
- 殺／萬箭／方天畫戟直接問閃（含流離、鐵騎黑色之後）
- 八卦陣判定失敗、或曹操放棄發動八卦陣（`useEquipmentEffect` 回 `SKIP`）之後
- 萬箭：前一位八卦陣成功，輪到曹操（與一般輪詢同序：第二階段無懈 → 曹操自己的八卦陣 → 護駕 → 閃；前一位是最後一位則萬箭結算完畢）
- 青龍偃月刀追殺（`useGreenDragonCrescentBladeEffect` 回 `KILL`；曹操有八卦陣則先問八卦陣）
- 雌雄雙股劍結算完接回殺的問閃
- 萬箭／方天畫戟：前一位瀕死結算完接回輪詢，輪到曹操

**流程**：
```
A 對曹操 (B) 出殺（或萬箭齊發/方天畫戟瞄到曹操）
  → 系統 broadcast AskSkillEffectEvent(skillName=護駕, playerId=曹操)、activePlayer = 曹操
  → 曹操呼叫 player:useSkillEffect：
    - SKIP:   AskDodgeEvent(曹操) 自己出閃（不詢問魏將）
    - ACCEPT: 系統 push WaitingHuJiaResponseBehavior 並 broadcast AskHuJiaEffectEvent(座位次序下一位 Wei)
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
- 二次問閃（八卦陣失敗／放棄、青龍追殺、雌雄雙股劍、萬箭八卦陣成功後輪到曹操、瀕死接回輪詢）同樣會先問護駕（見上方「觸發的問閃時機」）；代閃後由 host 接續原流程（青龍可再追殺、萬箭／方天畫戟推進到下一位）
- 不在範圍：決鬥／南蠻是問殺，與護駕無關

**備註**：此 API 獨立於 `playCard`。前端在收到 `AskHuJiaEffectEvent` 時應只開啟被詢問玩家的選擇 UI。

---

## 22. 被動 / 鎖定武將技一覽（無獨立 endpoint）

以下技能為鎖定技或自動觸發，後端自動套用，前端不需呼叫任何 API；
僅需注意對應的遊戲狀態變化（摸牌數 / 距離 / 目標合法性 / 出殺次數）。

| 武將 | 技能 | 效果（後端自動套用） | 前端可觀察的差異 |
|---|---|---|---|
| 馬超 SHU006 | 馬術 | 計算與其他角色距離 -1 | 攻擊/順手牽羊範圍變大；超距出牌不再 400 |
| 張飛 SHU003 | 咆哮 | 使用殺無次數限制 | 同回合第二張殺不再 400 |
| 周瑜 WU005 | 英姿 | 摸牌階段 +1（共 3）；手牌上限不變，仍等於當前 HP（身份標準版） | DrawCardEvent 數量 |
| 陸遜 WU007 | 謙遜 | 不能成為順手牽羊/樂不思蜀目標 | 順手牽羊或樂不思蜀（含國色轉化）指定陸遜 → 400；南蠻/萬箭/閃電對陸遜照常 |
| 陸遜 WU007 | 連營 | 失去最後一張手牌 → 摸 1 | 出/棄最後一張牌（含剛烈、雌雄雙股劍、貫石斧、丈八蛇矛、方天畫戟、青龍偃月刀、無懈可擊、主公殺忠臣棄牌）、或被拆/被拿走最後一張手牌後出現 DrawCardEvent |
| 諸葛亮 SHU004 | 空城 | 手牌 0 時不能被殺/決鬥指定 | 指定空手牌諸葛亮 → 400 |
| 黃月英 SHU007 | 集智 | 使用非延時錦囊 → 摸 1（含無懈可擊；樂不思蜀、閃電不觸發） | 出錦囊後緊接 DrawCardEvent；playWardCard（ACTIVE）回應中 PlayCardEvent 後緊接 DrawCardEvent |
| 黃月英 SHU007 | 奇才 | 使用錦囊無距離限制 | 超距順手牽羊不再 400 |
| 呂蒙 WU003 | 克己 | 本回合未使用過殺 → 略過棄牌階段 | finishAction 後 NotifyDiscardEvent 棄牌數為 0，直接進下一回合 |

**v1 範圍備註**：
- 謙遜依官方標準版實作（順手牽羊 + 樂不思蜀）；issue #192 原文的南蠻/萬箭/閃電已移除，調整時改 `QianXunSkill.isImmune` 即可
- 連營覆蓋「自己出牌 / response 出牌 / 棄牌階段」以及「被他人移走」失去最後手牌：過河拆橋在 `DismantleEvent` 後、反饋在 `SkillEffectEvent(反饋)` 後、突襲在 `SkillEffectEvent(突襲)` 後緊接陸遜的 `DrawCardEvent`。順手牽羊因謙遜無法指定陸遜。自己失去手牌的其他路徑：剛烈棄兩張在 `SkillEffectEvent(剛烈)` 後；雌雄雙股劍棄牌在 `YinYangSwordsEffectEvent` 後、問閃之前（摸到的閃可用來回應）；貫石斧在 `StonePiercingAxeTriggerEvent` 後、傷害事件之前（只棄裝備區的牌不算失去手牌；連營先於梟姬判斷）；青龍偃月刀追殺在 `GreenDragonCrescentBladeTriggerEvent` 後；無懈可擊在該無懈的 `PlayCardEvent` 後、無懈鏈結算之前；主公殺忠臣在 `DiscardEvent`／`DiscardEquipmentEvent` 後；丈八蛇矛（主動／被動）與方天畫戟多目標比照一般出殺，`DrawCardEvent` 排在該 API 回傳事件的最後。轉化技 response（龍膽、傾國）、流離、制衡等陸遜沒有的技能路徑不處理
- 集智 v1 自動摸（不問）；無懈可擊（playWardCard ACTIVE，含反制鏈中的每一張）也觸發：每張摸 1，於無懈詢問／結算之前摸，不改變 stage 與 activePlayer；SKIP 不摸
- 克己 v1 自動觸發（保留手牌嚴格有利，不詢問）；「使用過殺」含轉化殺/方天畫戟，不受諸葛連弩/咆哮影響

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
| targetPlayerIds | List\<String\>? | 選填，**目前只有仁德支援**：與 `cardIds` 逐張對應的接收者（第 i 張給第 i 位），一次發動分給多人。帶了就不看 `targetPlayerId`；其他技能帶此欄位會回 400 |

**觸發時機**：收到 `AskSkillEffectEvent`（內含 skillName / playerId / dataCardIds / dataPlayerId / **options**）後呼叫。
回應後廣播 `SkillEffectEvent`（accepted + data）。

⚠️ **請以 `data.options` 決定要畫哪些按鈕，不要固定畫「發動／放棄」**。多數詢問的 options 是
`["ACCEPT", "SKIP"]`，但同一個事件也用來問「二選一」型的問題（如剛烈第二段的
`["DISCARD", "DAMAGE"]`）—— 對那種詢問，`ACCEPT` / `SKIP` 不是題目的答案。
外層的 `message` 一律是該次詢問的真正問題（例如「剛烈判定生效：請 player-a 選擇棄兩張手牌或受 1 點傷害」），
直接顯示即可，不要自己組「是否發動武將技」。

### 各技能 payload

| 技能 | 觸發 | choice | cardIds | targetPlayerId |
|---|---|---|---|---|
| 反饋（司馬懿） | 受傷後（含南蠻/萬箭） | `ACCEPT` / `SKIP` | 可選 1 個值：來源**手牌 index**（數字字串，0-based，同順手牽羊 `targetCardIndex`；張數見 seats）或來源**裝備 id**（`dataCardIds` 列出）；不給 = 取手牌 index 0（無手牌則取第一件裝備） | — |
| 遺計（郭嘉） | 受傷後（含南蠻/萬箭） | `ACCEPT`（自摸 2）/ `GIVE`（令他人獲得 1）/ `SKIP` | — | GIVE 必填 |
| 剛烈（夏侯惇）第一段 | 受傷後（含南蠻/萬箭） | `ACCEPT`（判定）/ `SKIP` | — | — |
| 剛烈 第二段（問傷害來源） | 判定非紅桃後 | `DISCARD` / `DAMAGE` | DISCARD 必填 2 張手牌 | — |
| 激將（劉備主公技） | 主公劉備需要出殺時（南蠻/決鬥自動觸發；出牌階段主動出殺、借刀殺人由劉備發動，見 §11 與下方「出牌階段主動技」），依座位順序詢問蜀將（`dataPlayerId` = 劉備） | `ACCEPT` / `DECLINE`（`SKIP` 視同 `DECLINE`） | ACCEPT 必填 [殺 id]（蜀將手中） | — |
| 流離（大喬） | 大喬成為殺目標、被問閃之前 | `ACCEPT` / `SKIP` | ACCEPT 必填 [要棄的牌 id]（手牌或裝備區的牌皆可） | ACCEPT 必填：轉移目標（距離 1 內、非攻擊者；方天畫戟另排除已有目標；距離以棄牌前計算） |
| 鬼才（司馬懿） | 任意角色判定牌抽出後、生效前（閃電 / 樂不思蜀 / 八卦陣 / 鐵騎 / 剛烈 / 洛神；dataCardIds = [原判定牌]、dataPlayerId = 判定所屬玩家） | `ACCEPT` / `SKIP` | ACCEPT 必填 [替換用手牌 id] | — |
| 洛神（甄姬） | 甄姬回合開始、延遲錦囊（閃電/樂不思蜀）判定之前（issue #227） | `ACCEPT`（開始判定：黑色收入手牌自動續判、紅色停，不逐輪詢問）/ `SKIP` | — | — |
| 觀星（諸葛亮） | 諸葛亮回合開始（準備階段）、延遲錦囊判定之前；`dataCardIds` = 牌堆頂 X 張（X = 存活角色數，上限 5，第一張 = 堆頂；**只有諸葛亮本人收到 id，其他玩家為 `[]`**，張數見 `message`），`options` = `["ARRANGE","SKIP"]`；ARRANGE 結果 `SkillEffectEvent.dataCardIds` = 回堆頂順序，同樣只推給諸葛亮 | `ARRANGE` / `SKIP`（不發動，牌堆不變） | ARRANGE：回堆頂的順序（第一張 = 下一張被抽；未列出 → 依原順序置堆底；可為空 = 全置底） | — |
| 裸衣（許褚） | 許褚回合開始、判定階段之後、摸牌之前（原自動觸發改主動） | `ACCEPT`（少摸一張、本回合殺/決鬥傷害 +1）/ `SKIP`（正常摸牌無加成） | — | — |
| 護駕（曹操主公技）發動詢問 | 主公曹操被要求出閃、且有其他存活魏將時（issue #217） | `ACCEPT`（開始魏將輪詢，見 §21）/ `SKIP`（自己出閃） | — | — |

#### 剛烈（夏侯惇）兩段式詢問與回應

夏侯惇（`WEI003`）受到傷害後，**兩段各有一個 `AskSkillEffectEvent`，被問的人不同、options 也不同**，
都用同一支 `player:useSkillEffect` 回應（無獨立 endpoint）。前端請用 `data.playerId` 判斷該問誰、
用 `data.options` 決定按鈕。

**第一段：問夏侯惇要不要判定**

```json
{
  "event": "AskSkillEffectEvent",
  "data": {
    "skillName": "剛烈",
    "playerId": "<夏侯惇>",
    "dataCardIds": [],
    "dataPlayerId": "<傷害來源>",
    "options": ["ACCEPT", "SKIP"]
  },
  "message": "剛烈：詢問 <夏侯惇> 是否發動"
}
```

```json
POST /api/games/{gameId}/player:useSkillEffect
{ "playerId": "<夏侯惇>", "skillName": "剛烈", "choice": "ACCEPT" }
```

`SKIP` → 廣播 `SkillEffectEvent`（accepted=false）後結束。

**判定結果**（`ACCEPT` 後）：廣播 `SkillEffectEvent`，`message` 寫出是哪張判定牌造成生效／未生效，
`dataCardIds` = [判定牌 id]：

```json
{
  "event": "SkillEffectEvent",
  "data": { "skillName": "剛烈", "playerId": "<夏侯惇>", "accepted": true,
            "dataCardIds": ["<判定牌 id>"], "dataPlayerId": "<傷害來源>" },
  "message": "剛烈判定：黑桃9 殺 → 非紅心，生效"
}
```

`accepted` = 判定是否生效（非紅桃 true／紅桃 false，`message` 為「剛烈判定：紅心3 桃 → 紅心，未生效」）。
紅桃 → 流程結束，不會有第二段。

> 場上有司馬懿時，判定牌抽出後會先插入**鬼才**的詢問（`skillName: "鬼才"`、options `["ACCEPT","SKIP"]`、
> `message` 例：「鬼才：player-a 可替換 player-b 的剛烈判定牌 黑桃3 過河拆橋」、`dataCardIds` = [原判定牌]）。
> 司馬懿回應後才會出現上面的剛烈判定結果，且訊息裡的牌是**換上來的那張**。

**第二段：問傷害來源棄兩張手牌或受傷**（僅判定生效時）

```json
{
  "event": "AskSkillEffectEvent",
  "data": {
    "skillName": "剛烈",
    "playerId": "<傷害來源>",
    "dataCardIds": [],
    "dataPlayerId": "<夏侯惇>",
    "options": ["DISCARD", "DAMAGE"]
  },
  "message": "剛烈判定生效：請 <傷害來源> 選擇棄兩張手牌或受 1 點傷害"
}
```

⚠️ 這一段**被問的是傷害來源，不是夏侯惇**（`data.playerId` = 來源、`data.dataPlayerId` = 夏侯惇），
問的也不是「要不要發動剛烈」。這裡若沿用通用的發動／放棄彈窗，玩家按什麼都不是題目的答案。

棄兩張手牌（`cardIds` 必須剛好 2 張手牌）：

```json
POST /api/games/{gameId}/player:useSkillEffect
{ "playerId": "<傷害來源>", "skillName": "剛烈", "choice": "DISCARD", "cardIds": ["<手牌1>", "<手牌2>"] }
```

受 1 點傷害：

```json
POST /api/games/{gameId}/player:useSkillEffect
{ "playerId": "<傷害來源>", "skillName": "剛烈", "choice": "DAMAGE" }
```

**結算廣播**：棄牌 → `SkillEffectEvent`（`dataCardIds` = 棄掉的兩張、message「X 棄兩張手牌回應剛烈」）；
受傷 → `SkillEffectEvent`（message「X 受剛烈 1 點傷害（4→3）」）。兩者之後 `topBehavior` 皆清空，回合繼續。

**容錯**：剛烈是強制二選一，來源不能「不選」。後端只把「明確送 `DISCARD`」或「帶了剛好 2 張 `cardIds`」
視為棄牌，**其他任何 choice（含 `SKIP` / `ACCEPT` / 空值）一律結算為受 1 點傷害**，不會丟錯或卡住流程。
所以舊前端送 `SKIP` 也走得完，但按鈕仍請照 `options` 畫，玩家才選得到棄牌。
（來源手牌不足 2 張時只能受傷：送 `DISCARD` 但 `cardIds` 不是 2 張會得到 400
`DISCARD requires exactly 2 hand cards`。）

### 自動觸發技（無需呼叫本 API，僅廣播 `SkillEffectEvent`）

| 武將 | 技能 | 行為 |
|---|---|---|
| 郭嘉 | 天妒 | 自己判定牌生效後自動收入手牌（閃電 / 樂不思蜀 / 剛烈判定） |
| 馬超 | 鐵騎 | 出殺指定目標後自動判定（**先於八卦陣**）：**紅色**（紅心／方塊）→ 目標不能出閃、也不能發動八卦陣，直接結算；黑色 → 照常問八卦陣或閃 |
| 孫尚香 | 梟姬 | 失去裝備（被拆 / 被順 / **主動換裝蓋掉舊裝備** / 被反饋取走 / 被麒麟弓棄馬 / **被借刀殺人奪武器** / **貫石斧代價** / **制衡** / **主公殺忠臣棄光裝備**）自動摸 2；per-card，一次失去 N 張摸 2N |
| 孫權 | 救援 | 主公技。主公孫權瀕死時，其他吳勢力對其使用的桃回復效果 +1（自動加成，出桃仍走 playCard） |

### 出牌階段主動技（topBehavior 為空時直接呼叫本 API 發動）

| 技能 | 限制 | choice | cardIds | targetPlayerId |
|---|---|---|---|---|
| 制衡（孫權） | — | `ACCEPT` | 要棄的牌（手牌+裝備 id，≥1）→ 摸等量 | — |
| 苦肉（黃蓋） | 無次數限制；HP 1 也可（進瀕死，見下方「苦肉」） | `ACCEPT` | — | — |
| 反間（周瑜）第一段 | 每回合 1 次 | `ACCEPT` | [要給的手牌 id] | 目標玩家 |
| 反間 第二段（目標回應） | — | `SPADE`/`HEART`/`DIAMOND`/`CLUB` | — | — |
| 結姻（孫尚香） | 每回合 1 次 | `ACCEPT` | 2 張手牌 id（傷最重者由系統判定） | — |
| 仁德（劉備） | 回血每回合 1 次；可多次發動 | `ACCEPT` | 要給的手牌 id（≥1；本回合累積 2 張回 1 體力） | 接收者（全部給同一人）；或改帶 `targetPlayerIds` 逐張指定、一次分給多人。全部驗證通過才給牌；每位接收者一則 `SkillEffectEvent`（`dataPlayerId` = 接收者，`dataCardIds` = 給他的牌） |
| 激將（劉備主公技）主動出殺 | 須為主公、有其他存活蜀將；目標須在**劉備**攻擊範圍內、不能是自己；計入劉備本回合出殺次數（已出過殺且無連弩/咆哮 → 拒絕） | `ACCEPT` | — | 殺的目標 |

### 轉化牌技（choice = 轉化目標型別）

| 技能 | choice | cardIds | targetPlayerId | 使用情境 |
|---|---|---|---|---|
| 武聖（關羽） | `KILL` | [紅色手牌或裝備區紅色牌 id] | 主動殺必填 | 主動出殺（topBehavior 空）或回應南蠻/決鬥（KILL response） |
| 龍膽（趙雲） | `KILL` / `DODGE` | [閃 id] / [殺 id] | 主動殺必填 | 殺↔閃雙向；回應問閃用 DODGE、回應需殺用 KILL；問閃時亦可直接以 `player:playCard` 打出殺，後端自動視為發動龍膽（issue #229） |
| 傾國（甄姬） | `DODGE` | [黑色手牌 id] | — | 被問閃時（被殺/萬箭/方天畫戟）；亦可直接以 `player:playCard` 打出黑牌，後端自動視為發動傾國（issue #229） |
| 奇襲（甘寧） | `DISMANTLE` | [黑色手牌或裝備區黑色牌 id] | 必填 | 主動；後續走 useDismantleEffect |
| 國色（大喬） | `CONTENTMENT` | [方塊手牌或裝備區方塊牌 id] | 必填 | 主動；牌以樂不思蜀身份進判定區 |

轉化殺計入出殺次數限制（咆哮/諸葛連弩豁免照常）；轉化牌的目標免疫限制照常套用
（轉化殺 → 空城；國色轉化的樂不思蜀 → 謙遜）。

**裝備區來源**（武聖 / 奇襲 / 國色；傾國限手牌、龍膽只能轉殺/閃故不適用）：`cardIds` 可帶自己裝備區的牌 id。
該牌先離開裝備區，再驗證目標 — 攻擊距離、諸葛連弩出殺次數都以**失去該裝備後**計算
（例：把麒麟弓本身當殺，攻擊距離回到 1；已出過殺時把諸葛連弩本身當殺 → 400）；驗證失敗則裝備原樣留著。
成功時該牌進棄牌堆（國色則以樂不思蜀身份進目標判定區）並觸發失去裝備 hook（梟姬），
梟姬的事件排在轉化技 `SkillEffectEvent` 之後、`PlayCardEvent` 之前。

**問閃時的出牌驗證（issue #229）**：被問閃時以 `player:playCard` 打出非閃牌 —
可轉化（傾國黑牌 / 龍膽殺）→ 自動發動轉化技視為出閃；不可轉化 → 400 明確錯誤（先前會被默默吞掉導致卡住）。

**回應需殺（南蠻 / 決鬥 / 借刀殺人）的事件**：`SkillEffectEvent(武聖/龍膽)` 之後是來源牌的
`PlayCardEvent`（`playerId` = 轉化者、`cardId` = 來源牌、`targetPlayerId` = 南蠻使用者 / 決鬥對手 / 借刀殺人的攻擊目標、
`playType` = `active`），遊戲訊息如「趙雲 發動龍膽出殺」。不會發 `ViperSpearKillTriggerEvent`（只屬於丈八蛇矛）。

#### 傾國（甄姬）發動與使用

**條件**：甄姬（`WEI007`）**被問閃時**（被殺、萬箭齊發、方天畫戟 — 收到 `AskDodgeEvent` 或成為問閃對象），手上有黑色（黑桃/梅花）手牌。傾國沒有事前的 `AskSkillEffectEvent` 詢問 — 由前端在問閃 UI 直接提供黑牌作為出閃選項。

**方式一：直接出牌（推薦，前端不需特殊處理）** — 把黑色手牌當一般出牌打出：

```json
POST /api/games/{gameId}/player:playCard
{
  "playerId": "甄姬的playerId",
  "targetPlayerId": "攻擊者playerId",
  "cardId": "黑色手牌id",
  "playType": "active"
}
```

後端偵測「被問閃 + 非閃牌 + 有傾國」自動發動轉化。

**方式二：useSkillEffect**：

```json
POST /api/games/{gameId}/player:useSkillEffect
{
  "playerId": "甄姬的playerId",
  "skillName": "傾國",
  "choice": "DODGE",
  "cardIds": ["黑色手牌id"]
}
```

**結算（兩種方式相同）**：
- 廣播 `SkillEffectEvent`（skillName=傾國、accepted=true、dataCardIds=[該黑牌]）+ `PlayCardEvent`（視為出閃）
- 該黑牌進墓地，效果等同出閃：殺被擋、萬箭/方天畫戟輪詢推進到下一位
- `round.activePlayer` 回到應繼續行動者（普通殺 → 攻擊者；輪詢 → 下一位被詢問者）
- 攻擊者裝青龍偃月刀/貫石斧時照常觸發後續詢問（同真閃）

**錯誤情境（400）**：
- 出紅色牌 / 牌不在手 → `IllegalArgumentException`
- 不想發動：照原本流程出真閃或 `playType: "skip"`

#### 突襲（張遼）摸牌階段詢問

**規則**（身份標準版）：摸牌階段，你可以**放棄摸牌**，改為獲得最多兩名其他角色的各一張手牌。

**詢問**：張遼回合的判定階段結束、摸牌之前，若有任何其他存活角色有手牌，推 `AskSkillEffectEvent`：

```json
{ "event": "AskSkillEffectEvent",
  "data": { "skillName": "突襲", "playerId": "張遼的playerId", "options": ["ACCEPT", "SKIP"] },
  "message": "突襲：張遼 要發動突襲嗎？（放棄摸牌，改為獲得至多兩名其他角色的各一張手牌）" }
```

沒有任何其他角色有手牌時不能發動，不詢問、直接摸牌。

**回應**：

```json
POST /api/games/{gameId}/player:useSkillEffect
{ "playerId": "張遼的playerId", "skillName": "突襲", "choice": "ACCEPT", "cardIds": ["目標1", "目標2"] }
```

| choice | cardIds | 結果 |
|---|---|---|
| `ACCEPT` | **目標玩家 id**（1～2 名，借用此欄位） | 從每名目標拿一張手牌，**不摸牌**，進入出牌階段；`SkillEffectEvent` 的 `dataCardIds` 為拿到的牌、`message` 例「突襲：張遼 放棄摸牌，獲得 甘寧、孫權 各一張手牌」 |
| `SKIP` | — | 照常摸 2 張 |

`ACCEPT` 的目標不合法時回 400、詢問保留可重新選：不能選自己、不能同一人兩次、最多兩名、目標要存活且有手牌。
樂不思蜀生效時仍可發動，之後照常跳過出牌階段。

#### 國色（大喬）發動與使用

**條件**：大喬（`WU006`）**自己回合的出牌階段**（`round.activePlayer` 是大喬、`topBehavior` 為空），手上有方塊手牌。
效果是把那張方塊牌**當樂不思蜀**對一名其他角色使用。

```json
POST /api/games/{gameId}/player:useSkillEffect
{
  "playerId": "大喬的playerId",
  "skillName": "國色",
  "choice": "CONTENTMENT",
  "cardIds": ["方塊手牌id"],
  "targetPlayerId": "目標playerId"
}
```

| 欄位 | 說明 |
|---|---|
| `choice` | 固定 `CONTENTMENT`（＝樂不思蜀，程式中的卡牌英文名 `Contentment`；轉化技的 choice 都是「要轉化成的牌」：`KILL` 殺、`DODGE` 閃、`DISMANTLE` 過河拆橋） |
| `cardIds` | **剛好 1 張**方塊牌（手牌或裝備區），例：`BD2080`（方塊2 閃）、`EDK104`（裝備中的紫騂） |
| `targetPlayerId` | 必填，不可是自己 |

**結算**：
- 廣播 `SkillEffectEvent`（skillName=國色、accepted=true、dataCardIds=[該方塊牌]、targetPlayerId=目標）
  → `PlayCardEvent`（視為出樂不思蜀）→ `GameStatusEvent`（「發動樂不思蜀」）
- 該牌**以樂不思蜀身份**放進目標的判定區；之後與真的樂不思蜀完全相同：
  目標回合判定時才詢問無懈可擊（發動當下不問），判定非紅心 → 跳過出牌階段
  （判定訊息例：「樂不思蜀判定：黑桃7 南蠻入侵 → 非紅心，生效，跳過出牌階段」）
- 大喬仍在自己的出牌階段，可繼續出牌

**錯誤情境（400）**：

| 情境 | error | message |
|---|---|---|
| 不是大喬的行動時機 | `IllegalStateException` | `ActivePlayer is not <playerId> , now ActivePlayer is <id>` |
| `cardIds` 不是剛好 1 張 | `IllegalArgumentException` | `Conversion requires exactly 1 source card` |
| 牌不在手牌也不在裝備區 | `IllegalArgumentException` | `Source card not in hand or equipment: <cardId>` |
| 不是方塊 | `IllegalArgumentException` | `國色 不能把 <cardId> 當 CONTENTMENT` |
| 沒帶 `targetPlayerId` | `IllegalArgumentException` | `Conversion active play requires targetPlayerId` |
| 目標是自己 | `IllegalArgumentException` | `不可以對自己出樂不思蜀` |
| 目標判定區已有樂不思蜀 | `IllegalArgumentException` | `<targetPlayerId> 已經有樂不思蜀` |
| 目標是陸遜（謙遜） | `IllegalStateException` | `target cannot be targeted by Contentment (immunity skill)` |

#### 苦肉（黃蓋）發動與結算

**條件**：黃蓋（`WU004`）**自己回合的出牌階段**（`round.activePlayer` 是黃蓋、`topBehavior` 為空）。**無次數限制**，HP 1 也可以發動。

```json
POST /api/games/{gameId}/player:useSkillEffect
{
  "playerId": "黃蓋的playerId",
  "skillName": "苦肉",
  "choice": "ACCEPT"
}
```

`cardIds`、`targetPlayerId` 不需要帶。

**效果**：失去 1 點體力，然後摸 2 張牌。是「**失去體力**」不是「受到傷害」：沒有傷害來源，
**不會觸發任何受傷後技能**（奸雄 / 反饋 / 剛烈 / 遺計…）。

**情境一：HP ≥ 2** — 立即結算：
- 廣播 `SkillEffectEvent`（skillName=苦肉、accepted=true）→ 摸牌事件（2 張）→
  `GameStatusEvent`（「<playerId> 發動苦肉（-1 HP, +2 牌）」）
- 黃蓋仍在自己的出牌階段，可繼續出牌或再次發動

**情境二：HP 1** — 失去最後 1 點體力進入**瀕死求桃**，**先不摸牌**：
- 廣播 `SkillEffectEvent` → `PlayerDamagedEvent`（1→0，前端更新血量用）→ `PlayerDyingEvent` →
  `AskPeachEvent`（從黃蓋自己開始，依座位順序詢問）→
  `GameStatusEvent`（「<playerId> 發動苦肉，失去最後 1 點體力進入瀕死，救回後摸 2 張牌」）
- 之後走一般的瀕死求桃流程（`player:playCard` 出桃，或 `playType: "skip"`）：
  - **被救回** → 回到 1 血，**此時才摸 2 張**，回到黃蓋自己的出牌階段
  - **沒人救** → 黃蓋死亡、**不摸牌**；因為沒有傷害來源，**沒有人能領擊殺反賊的 3 張獎勵**，
    也沒有主公殺忠臣的懲罰；身分結算（主公死亡 → 遊戲結束等）照常，輪到下一位玩家

**錯誤情境（400）**：
- 不是黃蓋的行動時機 → `IllegalStateException`（`ActivePlayer is not <playerId> , now ActivePlayer is <id>`）
- 發動者不是黃蓋 → `IllegalStateException`（`<playerId> has no active skill named 苦肉`）

**v1 範圍備註**：
- 反饋 / 剛烈 / 遺計在 AOE polling（南蠻 / 萬箭）中可觸發（PR #221 / issue #165 / issue #250：
  受傷 → 詢問 → 詢問鏈收斂後 resume 輪詢，回到原本的 AOE 詢問輪；剛烈兩段式與鬼才巢狀介入亦支援）。
  遺計在輪詢中 `GIVE` 給尚未被詢問的角色也合法（該角色可能因此取得可回應的殺 / 閃）；
  瀕死求桃期間不詢問；被救回後才補問（`DyingAskPeachBehavior` revival 分支 replay `SkillEngine.onDamaged`），見下一點
- AOE polling 中瀕死被救回：先補觸發受傷後技能（反饋／剛烈／遺計），技能詢問結算完才繼續問下一位（不會同時發兩個詢問）
- AOE polling 中被扣死的玩家（含**最後一位**被詢問者）不會再收到 AskKill/AskDodge；
  瀕死結算結束後 `activePlayer` 回到回合玩家。死亡玩家的任何 action 一律 400
  （`IllegalStateException: ... is already dead and cannot act`）
- 剛烈 DAMAGE 反傷為夏侯惇造成的傷害：來源 HP 歸 0 進瀕死求桃、存活則觸發來源的受傷後技能（如司馬懿反饋）、擊殺獎勵／懲罰歸夏侯惇
- 鐵騎 v1 自動判定（不問）。與雌雄雙股劍同時觸發時先判鐵騎、再結算雙股劍（FAQ：先武將技後武器技）；鐵騎生效則雙股劍結算完直接受傷、不問閃
- 流離是「成為殺的目標時」，最先詢問：流離 → 鐵騎 → 雌雄雙股劍 → 八卦陣／閃（FAQ：大喬先決定流離，馬超再決定鐵騎）。大喬有八卦陣時也會先問流離；放棄流離則照上述順序繼續（不是直接問閃）
- 梟姬已覆蓋**所有**失去裝備的路徑：被過河拆橋拆裝備、被順手牽羊偷裝備、
  主動換裝（武器 / 防具 / ±1 馬）、被反饋取走裝備（取手牌不算）、
  被麒麟弓棄馬（只有一匹直接移除 / 兩匹由攻擊方選一匹）、
  被借刀殺人奪武器（B 選擇不出殺 / B 根本沒有殺兩條）、
  貫石斧代價棄到裝備區的牌（棄兩張裝備 → 摸 4 張）、制衡棄裝備、武聖／奇襲／國色以裝備區的牌轉化、
  主公殺死忠臣的罰則棄光裝備（一次失去 N 張 → 摸 2N）。
  麒麟弓與貫石斧的摸牌事件都排在**傷害事件之前**（官方順序是先失去裝備再造成傷害）。
  制衡那條在正常牌局不可達（制衡＝孫權、梟姬＝孫尚香，同一玩家不會兼有），
  hook 仍加上以保持所有棄裝備路徑一致。
  拆**判定區**（樂不思蜀 / 閃電）不觸發是正確的 — 判定區非裝備區。
  v1 自動觸發不詢問（官方為「可以」，摸牌恆有利故不問）
- 主動換裝被蓋掉的舊裝備會進棄牌堆；`PlayEquipmentCardEvent.deprecatedCardId`
  帶的是**同欄位**被換掉的那張（換武器 → 舊武器；欄位原本為空時為 `""`）
- 反間猜錯的傷害來源為周瑜：目標 HP 歸 0 進瀕死求桃，擊殺獎勵／懲罰歸周瑜；存活或被救回則觸發目標的受傷後技能（反饋／剛烈／遺計）
- 觀星改為回合開始（準備階段，延遲錦囊判定之前）詢問，不再能於出牌階段主動發動；ARRANGE / SKIP 後接續判定 → 摸牌（觀看的牌會先於判定與摸牌被使用）
- 觀星觀看的牌（`AskSkillEffectEvent` / ARRANGE 結果 `SkillEffectEvent` 的 `dataCardIds`）與接續摸到的牌只推給諸葛亮本人，其他玩家收到同一個 event 但 id 為 `[]`
- 激將覆蓋南蠻入侵/決鬥的 AskKill（自動攔截）、出牌階段主動出殺、借刀殺人代出殺（後兩者由劉備以 `useSkillEffect(激將, ACCEPT)` 發動）。
  主動出殺：發動時即驗證目標（劉備的攻擊範圍、空城）與出殺次數；蜀將 ACCEPT 才計入劉備本回合的殺，全部拒絕則不計次數、回到劉備出牌（可再自己出殺）。
  代出的殺是蜀將那張實體牌（花色點數照原牌，如仁王盾判黑殺），視為劉備使用
  南蠻/決鬥代出：`SkillEffectEvent(激將)` 之後是該殺的 `PlayCardEvent`（`playerId` = 劉備、`cardId` = 蜀將的殺、
  `targetPlayerId` = 南蠻使用者 / 決鬥對手），遊戲訊息「劉備 以激將打出 殺」；不會發 `ViperSpearKillTriggerEvent`
- 激將（含南蠻/決鬥）詢問蜀將的 `AskSkillEffectEvent.options` 為 `["ACCEPT","SKIP"]`；`SKIP` 與 `DECLINE` 等價
- 流離適用所有殺：普通殺、轉化殺（武聖/龍膽）、丈八蛇矛、方天畫戟。方天畫戟多目標時，輪到大喬才問流離；轉移目標不能是本殺已有的其他目標，新目標取代大喬在輪詢中的位置（順序不變）。AOE 不是殺，不能流離
- 流離可棄手牌或裝備區的牌（只有裝備、沒有手牌也會問流離）。棄裝備 → 移出裝備區進棄牌堆，視為失去裝備（觸發梟姬等）。轉移目標的距離以棄牌前計算：棄掉 -1 馬仍可轉給原本靠 -1 馬才在距離 1 內的角色
- 丈八蛇矛的殺與普通殺走同一流程：流離 → 鐵騎 → 八卦陣／閃（含護駕）
- 方天畫戟的殺也判鐵騎：輪到每個目標時各判一次（流離之後、八卦陣之前）；紅色 → 該目標直接扣血（不問閃、不問八卦陣），接著輪到下一位。判定牌同樣可被鬼才替換
- 救援採官方標準版語意（桃效果 +1）；issue 原文描述的「可出桃給其」即既有瀕死求桃流程
- 鬼才覆蓋全部判定路徑：閃電 / 樂不思蜀（皆含 Ward 路徑）/ 八卦陣 / 鐵騎 / 剛烈 / 洛神；
  洛神多張判定牌逐張詢問；同一次判定僅詢問一次（官方 FAQ）
- 洛神為發動詢問一次（ACCEPT 後黑收自動續判）；判定中每張判定牌仍逐張過鬼才暫停點
- 武聖/奇襲/國色可用裝備區的牌轉化（失去裝備後才算距離、觸發梟姬）；傾國限黑色手牌
- 轉化殺（武聖/龍膽）對曹操造成傷害 → 奸雄獲得被轉化的真實來源牌（`AskJianXiongEffectEvent.sourceCardIds` 為來源牌 id）；致命傷被救回後補問奸雄亦同
- 轉化殺的傷害結算以 VirtualKill 進行；事件中 cardId 為來源真實牌

---

## 錯誤回應格式

所有 API 的錯誤回應（400 / 404 / 500）統一為精簡 JSON（issue #200 起，不再回傳 stack trace）：

```json
{ "error": "DistanceErrorException", "message": "Players are not within range." }
```

| 欄位 | 說明 |
|------|------|
| error | 例外類型（exception simple name），前端可據此 switch 處理 |
| message | 人類可讀的錯誤原因 |

### 常見 error 類型

| error | HTTP | 常見情境 |
|-------|------|----------|
| `DistanceErrorException` | 400 | 殺 / 順手牽羊超出距離 |
| `IllegalStateException` | 400 | 非法遊戲狀態：不是你的回合、不是當前需回應的玩家、**已死亡玩家嘗試行動**、殺次數已用完、技能每回合限一次等 |
| `IllegalArgumentException` | 400 | 非法參數：卡牌不在手中、目標不合法、棄牌數不足等 |
| `ValidationError` | 400 | Request body 欄位驗證失敗 |
| `NotFoundException` | 404 | gameId 不存在 |

Stack trace 僅記錄於 server log。

---

## WebSocket 事件類型

前端透過 WebSocket 接收以下事件，根據事件類型決定 UI 行為：

### 遊戲流程事件

| 事件 | 說明 | 觸發 API |
|------|------|----------|
| `GeneralSelectionStatusEvent` | 選將進度（每次有人選完武將廣播全員；格式見「其他玩家選將」節） | monarchChooseGeneral / otherChooseGeneral |
| `PlayerConnectionStatusEvent` | 連線狀態（玩家訂閱/斷線時廣播全員；格式見「其他玩家選將」節） | WebSocket 訂閱/斷線（非 HTTP API） |
| `PlayCardEvent` | 有人出牌 | playCard |
| `PlayWardCardEvent` | 有人出無懈可擊 | playWardCard |
| `GameStatusEvent` | 遊戲狀態更新（每次操作都附帶） | 所有 API |
| `DrawCardEvent` | 玩家摸牌（`cards` 只有摸牌者本人看得到，其他玩家為 `[]`、看 `size`） | finishAction / playCard / useSkillEffect 等 |
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
| `ViperSpearKillTriggerEvent` | 丈八蛇矛發動：攻擊者棄兩張牌作為虛擬殺使用（通知事件，無需回應）；只在真的用丈八蛇矛時發，武聖/龍膽/激將回應需殺走 `PlayCardEvent` | useViperSpearKill |
| `AskJianXiongEffectEvent` | 奸雄發動：曹操選擇是否獲得造成傷害的牌（含 `playerId`、`sourceCardIds : List<String>`） | useJianXiongEffect |
| `AskHuJiaEffectEvent` | 護駕：詢問魏將是否代主公曹操出閃（含 `playerId`、`caoCaoPlayerId`、`dodgeCardIdsInHand`） | useHuJiaEffect |
| `AskSkillEffectEvent` | 通用武將技詢問（含 `skillName`、`playerId`、`dataCardIds`、`dataPlayerId`、`options`）— 反饋/遺計/剛烈/反間/觀星/激將/流離等。**`options` = 本次可回的 choice，按鈕照它畫**（多為 `["ACCEPT","SKIP"]`，剛烈第二段為 `["DISCARD","DAMAGE"]`）；`message` 是該次詢問的真正問題，直接顯示 | useSkillEffect |

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
| `HuJiaEffectEvent` | 護駕回應結果（含 `playerId`、`caoCaoPlayerId`、`accepted`、`dodgeCardId`） |
| `SkillEffectEvent` | 通用武將技結算結果（含 `skillName`、`playerId`、`accepted`、`dataCardIds`、`dataPlayerId`）— 含自動觸發技（天妒/鐵騎/梟姬/救援/馬術等鎖定技不發事件，僅結果可觀察）；洛神每張判定牌各發一則（accepted=是否黑色收牌）。涉及判定的技（剛烈/鐵騎/八卦陣/洛神/鬼才）`message` 會寫出判定牌（花色+點數+牌名，如「剛烈判定：黑桃9 殺 → 非紅心，生效」），戰報直接顯示本欄即可 |


### 判定區 `seats[].delayScrolls`

`GameStatusEvent` 等事件的 `data.seats[]` 中，每位玩家的判定區是**物件陣列**（不是卡牌 id 陣列）：

```json
"delayScrolls" : [ {
  "cardId" : "SD3081",
  "effect" : "CONTENTMENT",
  "effectName" : "樂不思蜀"
} ]
```

| 欄位 | 說明 |
|---|---|
| `cardId` | **實際的那張牌**（畫牌面用） |
| `effect` | 這張牌在判定區的效果：`CONTENTMENT`（樂不思蜀）/ `LIGHTNING`（閃電） |
| `effectName` | 效果的中文名稱，可直接顯示 |

`cardId` 與 `effect` 可能不一致：國色（大喬）會把方塊的任意牌當樂不思蜀放進判定區，
例如上面的 `SD3081` 是方塊3 順手牽羊，但在判定區的效果是樂不思蜀。**判斷判定區是什麼效果請看 `effect`，不要用 `cardId` 推牌名。**

一般情況：
- 樂不思蜀：`{ "cardId": "SH6045", "effect": "CONTENTMENT", "effectName": "樂不思蜀" }`
- 閃電：`{ "cardId": "SSA014", "effect": "LIGHTNING", "effectName": "閃電" }`

> 格式變更：舊版為卡牌 id 陣列（`"delayScrolls" : [ "SC6071" ]`），前端需改讀物件的 `cardId` / `effect`。

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

---

### 20.1 查看武將牌堆

```
GET /api/debug/games/{gameId}/generalCardDeck
```

**回傳**：
```json
{
  "gameId": "my-id",
  "deckSize": 25,
  "generalIds": ["SHU001", "WEI001", "WU001", "..."]
}
```

- `generalIds[0]` = 下一張被抽的武將
- 注意：主公的 5 張候選在**建立遊戲（POST /api/games）時已抽走**；建立後查到的是剩餘牌堆，供主公選將後其他玩家依座位順序各抽 3 張

---

### 20.2 設定武將牌堆

```
PUT /api/debug/games/{gameId}/generalCardDeck
```

| 欄位 | 型別 | 說明 |
|------|------|------|
| generalIds | List\<String\> | 武將牌堆內容（index 0 = 下一張被抽的武將；id 如 `SHU001` 劉備、`WEI002` 司馬懿） |

**回傳**：同查看武將牌堆格式

**特殊情況**：
- 全量替換，原本武將牌堆會被完全覆蓋
- 無效 generalId → 400 `{"message": "Invalid general ID: XYZ"}`
- 允許重複 generalId（測試可能需要）
- 任何遊戲階段都可以呼叫；實務時機 = 建立遊戲後、主公 `monarchChooseGeneral` **之前**（主公的 5 張候選已在建立時抽走，本 API 影響的是主公選完後其他三位玩家各抽的 3 張，座位順序：主公下家起）

**使用範例**：
```bash
# 主公選將後：下家 3 張候選 = 司馬懿/曹操/張遼，再下家 = 關羽/張飛/趙雲，最後 = 孫權/甘寧/呂蒙
curl -X PUT http://localhost:8080/api/debug/games/my-id/generalCardDeck \
  -H 'Content-Type: application/json' \
  -d '{"generalIds": ["WEI002", "WEI001", "WEI004", "SHU002", "SHU003", "SHU005", "WU001", "WU002", "WU003"]}'

# 驗證
curl http://localhost:8080/api/debug/games/my-id/generalCardDeck
```
